package com.kloud4.kloud4academyHome.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.ClientManager.CartClientService;
import com.kloud4.kloud4academyHome.ClientManager.ClientService;

import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.Review;
import bo.WishListItemBean;
import bo.WishlistRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class BaseRestController {
	@Autowired
	private ClientService clientService;
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate redisCacheTemplate;
	@Autowired
	private CartClientService cartClientService;
	
	@Autowired
	Gson gson;

	Logger logger = LoggerFactory.getLogger(BaseRestController.class);
	
	public boolean checkCircuitBreaker(ResponseEntity<String> responseEntity) {
		if(responseEntity.getStatusCode() != null && responseEntity.getStatusCode().toString().equalsIgnoreCase("404 NOT_FOUND")) {
			logger.info("-------Circuitbreaker started----:"+responseEntity.getBody());
			return true;
		}
		return false;
	}
	
	public boolean checkCircuitBreaker(ResponseEntity<String> responseEntity, String responseString) {
		if(("statuscode".equalsIgnoreCase(responseString)) ||  (responseEntity != null && responseEntity.getStatusCode() != null && responseEntity.getStatusCode().toString().equalsIgnoreCase("404 NOT_FOUND"))) {
			logger.info("-------Circuitbreaker started----:");
			return true;
		}
		return false;
	}
	
	public void loadProductReviews(ModelAndView mv,String productId) {
		try {
			String productReviewKey = productId+"review";
			String responseString = "";
			try {
				if(redisCacheTemplate.hasKey(productReviewKey)) {
					responseString = (String) redisCacheTemplate.opsForValue().get(productReviewKey);
					logger.info("----------Wow coole redis cache is working for reviews: "+ responseString);
				}
			} catch(Exception e) {
				logger.error("Redis cache error while fetching reviews");
			}
			ProductReviewRequest productReviews = null;
			if(responseString != null) {
				productReviews = gson.fromJson(responseString, ProductReviewRequest.class);
			}
			logger.info("----------after Commnog int productReviews "+ productReviews);
			ResponseEntity<String> recentResponseEntity = clientService.fetchProductsUsingCategory("Featured");
		    if(recentResponseEntity != null) {
		    	ProductInfo[] productInfoList = gson.fromJson(recentResponseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("relatedProducts", productInfoList);
		    	mv.addObject("error", "");
			}
		    if(productReviews == null) {
		    	Review review = new Review();
		    	List<Review> reviewList = new ArrayList();
		    	reviewList.add(review);
		    	productReviews = new ProductReviewRequest();
		    	productReviews.setReviews(reviewList);
		    }
			mv.addObject("productReviews", productReviews);
		} catch (Exception e) {
			logger.error("Product review error");
		}
	}
	
	public void loadProductReviewsSubmit(ModelAndView mv,String productId) {
		try {
			String productReviewKey = productId+"review";
			String responseString = "";
			ProductReviewRequest productReviews = null;
			if(redisCacheTemplate.hasKey(productReviewKey)) {
				redisCacheTemplate.delete(productReviewKey);
				ResponseEntity<String> reviewResponseEntity = clientService.fetchProductReviews(productId);
				responseString = reviewResponseEntity.getBody();
				if(!"statuscode".equalsIgnoreCase(responseString) || !"No Reviews Found".equalsIgnoreCase(responseString)) {
					try {
						redisCacheTemplate.opsForValue().set(productReviewKey, responseString);
						productReviews = gson.fromJson(responseString, ProductReviewRequest.class);
					}catch (Exception e) {
						logger.error("Redis Cache Error for product reviews"+e.getMessage());
					}
				}
			} else {
				ResponseEntity<String> reviewResponseEntity = clientService.fetchProductReviews(productId);
				responseString = reviewResponseEntity.getBody();
				if(!"statuscode".equalsIgnoreCase(responseString) || !"No Reviews Found".equalsIgnoreCase(responseString)) {
					try {
						redisCacheTemplate.opsForValue().set(productReviewKey, responseString);
						productReviews = gson.fromJson(responseString, ProductReviewRequest.class);
					}catch (Exception e) {
						logger.error("Redis Cache Error for product reviews"+e.getMessage());
					}
				}
			}
			
			if(productReviews == null) {
				Review review = new Review();
		    	List<Review> reviewList = new ArrayList();
		    	reviewList.add(review);
		    	productReviews = new ProductReviewRequest();
		    	productReviews.setReviews(reviewList);
			}
			mv.addObject("productReviews", productReviews);
			logger.info("----------Review count is :::::: ");
			ResponseEntity<String> recentResponseEntity = clientService.fetchProductsUsingCategory("Featured");
		    if(recentResponseEntity != null) {
		    	ProductInfo[] productInfoList = gson.fromJson(recentResponseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("relatedProducts", productInfoList);
		    	mv.addObject("error", "");
			}
		} catch (Exception e) {
			logger.error("Product review error");
		}
	}
	
	public String getWishlistCountForCartPage(HttpSession session, HttpServletResponse response,HttpServletRequest request) throws Exception {
		WishlistRequest wishlistRequestView = new WishlistRequest();
		wishlistRequestView.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
		WishlistRequest wishlistResponse = null;
		ResponseEntity<String> responseEntity = cartClientService.wishListAPICall(wishlistRequestView, response, request,"/cart-ms/wishlist/viewwishlist");
		if(responseEntity != null && responseEntity.getBody() != null) {
			wishlistResponse = gson.fromJson(responseEntity.getBody(), WishlistRequest.class);
		}
		String itemSize = "";
		if(wishlistResponse != null && wishlistResponse.getProductIdList() != null && wishlistResponse.getProductIdList().size() > 0) {
			itemSize = String.valueOf(wishlistResponse.getProductIdList().size());
			session.setAttribute("wishlistSize", itemSize);
		} else {
			session.setAttribute("wishlistSize", "");
		}
		
		return itemSize;
	}
	public String getWishlistCountForOtherPage(HttpSession session, HttpServletResponse response,HttpServletRequest request) throws Exception {
		String wishlistSize = (String) session.getAttribute("wishlistSize");
		if(StringUtils.isBlank(wishlistSize)) {
			WishlistRequest wishlistRequestView = new WishlistRequest();
			wishlistRequestView.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
			WishlistRequest wishlistResponse = null;
			ResponseEntity<String> responseEntity = cartClientService.wishListAPICall(wishlistRequestView, response, request,"/cart-ms/wishlist/viewwishlist");
			if(responseEntity != null && responseEntity.getBody() != null) {
				wishlistResponse = gson.fromJson(responseEntity.getBody(), WishlistRequest.class);
			}
			int itemSize = 0;
			if(wishlistResponse != null && wishlistResponse.getProductIdList() != null && wishlistResponse.getProductIdList().size() > 0) {
				itemSize = wishlistResponse.getProductIdList().size();
				session.setAttribute("wishlistSize", String.valueOf(itemSize));
			}
			else {
				itemSize = 0;
				session.setAttribute("wishlistSize","0");
			}
			
			wishlistSize = String.valueOf(itemSize);
		}
		
		return wishlistSize;
	}
	
	public void setCartAndWishListCount(HttpSession session,HttpServletResponse response,HttpServletRequest request,ModelAndView mv) {
		String cartSize = "0";
	    String cartUrl = (String) session.getAttribute("cartUrl");
	    String cartId = (String) session.getAttribute("cartId");
	    String wishlistSize = (String) session.getAttribute("wishlistSize");
	    if(StringUtils.isBlank(wishlistSize)) {
	    	try {
				wishlistSize = getWishlistCountForCartPage(session, response, request);
			} catch (Exception e) {
				logger.error("wishlist count call error: "+e.getMessage());
			}
		}
   		if(StringUtils.isBlank(cartUrl)) {
   			clientService.checkandReloadCartCount(session, cartUrl, response, request);
   			cartUrl = (String) session.getAttribute("cartUrl");
   			cartSize = (String) session.getAttribute("cartSize");
   			cartId = (String) session.getAttribute("cartId");
   		} else {
   			cartSize = (String) session.getAttribute("cartSize");
   		}
   		
   		mv.addObject("cartUrl", cartUrl);
   		mv.addObject("cartSize", cartSize);
   		mv.addObject("cartId", cartId);
   		mv.addObject("wishlistSize",wishlistSize);
	}
}
