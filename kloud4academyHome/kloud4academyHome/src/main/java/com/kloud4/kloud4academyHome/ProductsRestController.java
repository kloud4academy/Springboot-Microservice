package com.kloud4.kloud4academyHome;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.ClientManager.ClientService;
import com.kloud4.kloud4academyHome.controller.BaseRestController;

import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.Review;
import jakarta.validation.Valid;

@Controller
public class ProductsRestController extends BaseRestController {
	Logger logger = LoggerFactory.getLogger(ProductsRestController.class);
	
	@Autowired
	private ClientService clientService;
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate redisCacheTemplate;
	
	@Autowired
	Gson gson;
	
	@RequestMapping(value={"/productlist/{categoryId}", "/productdetail/productlist/{categoryId}","/cartdetail/cart/productlist/{categoryId}"},method = RequestMethod.GET)
	public ModelAndView productList(@PathVariable String categoryId) {
		logger.info("-------Getting called controller----"+categoryId);
		ModelAndView mv = new ModelAndView();
	    ResponseEntity<String> responseEntity;
		try {
			responseEntity = clientService.fetchProductsUsingCategory(categoryId);
		    if(responseEntity != null) {
		    	if(checkCircuitBreaker(responseEntity)) {
		    		mv.addObject("apiError", "true");
		    		mv.addObject("error", "true");
					mv.setViewName("productlist");
					return mv;
		    	}
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("productResponse", productInfoList);
		    	mv.addObject("error", "");
				mv.setViewName("productlist");
			}
		} catch (Exception e) {
			logger.info("-------Products fetching error in productlist ----");
			mv.addObject("productResponse", "No Products Found");
			mv.addObject("error", "true");
			mv.setViewName("productlist");
		}
	    
		return mv;
	}
	
	@RequestMapping(path="/productdetail/{productId}",method = RequestMethod.GET)
	public ModelAndView fetchProductUsingProductId(@PathVariable String productId, Model model) {
		Review review = new Review();
		review.setProductId(productId);
		ModelAndView mv = new ModelAndView();
	    ResponseEntity<String> responseEntity = null;
	    String responseString = "";
		try {
			try {
				if(redisCacheTemplate.hasKey(productId)) {
					responseString = (String) redisCacheTemplate.opsForValue().get(productId);
					logger.info("----------Wow coole redis cache is working: "+ responseString);
				}
			} catch (Exception e) {
				logger.error("Redis Cache error occurred"+e.getMessage());
			}
			if("".equalsIgnoreCase(responseString) || responseString == null) {
				logger.info("----------calling service and Not Fetch from Cache "+ responseString);
				responseEntity = clientService.fetchProductUsingProductId(productId);
				responseString = responseEntity.getBody();
				if(!"statuscode".equalsIgnoreCase(responseString)) {
					try {
						redisCacheTemplate.opsForValue().set(productId, responseString);
					}catch (Exception e) {
						logger.error("Redis Cache Error"+e.getMessage());
					}
				}
			} 
		    if(responseEntity != null || responseString != null) {
		    	if(checkCircuitBreaker(responseEntity, responseString)) {
		    		mv.addObject("apiError", "true");
		    		mv.addObject("error", "true");
					mv.setViewName("productdetail");
					return mv;
		    	}
		    	ProductInfo productInfo = gson.fromJson(responseString, ProductInfo.class);
		    	mv.addObject("productInfo", productInfo);
		    	mv.addObject("error", "");
				mv.setViewName("productdetail");
				model.addAttribute("review", review);
				loadProductReviews(mv, productId);
			}
		} catch (Exception e) {
			logger.info("-------ProductInfo fetching error in productlist ----"+e.getMessage());
			mv.addObject("productInfo", "No Product Found");
			mv.addObject("error", "true");
			mv.addObject("productInfo", "");
			mv.setViewName("productdetail");
		}
	    
		return mv;
	}
	
	
	
	@RequestMapping(value="/productdetail/{productId}",method=RequestMethod.POST)
	public ModelAndView submitreview(@PathVariable String productId,@Valid Review review, BindingResult result, Model model) {
		 logger.info("---------Display review "+productId);
		 ModelAndView mv = new ModelAndView();
	     mv.addObject("error", "");
	     ProductInfo productInfo = null;
	     try {
	    	 String productString = (String) redisCacheTemplate.opsForValue().get(productId);
		     productInfo = gson.fromJson(productString, ProductInfo.class);
	     } catch(Exception e) {
	    	 logger.error("--------Redis Cache error in SubmitReview method"+e.getMessage());
	     }
	     mv.addObject("productInfo", productInfo);
		 mv.setViewName("productdetail");
		 if (result.hasErrors()) {
			 mv.addObject("reviewError", "true");
			 loadProductReviews(mv, productId);
			 return mv;
		 }
		 review.setProductId(productId);
		 sendProductReviews(review);
		 loadProductReviewsSubmit(mv, productId);
		 mv.addObject("reviewstatus", "Review submitted successfully");
		model.addAttribute("review", new Review());
	  return mv;
	}
	
	private String sendProductReviews(Review review) {
		ProductReviewRequest productReviewRequest = new ProductReviewRequest();
		productReviewRequest.setNoOfReviewsToShow("3");
		productReviewRequest.setProductId(review.getProductId());
		review.setShowReview("true");
		List<Review> reviewList = new ArrayList<Review>();
		reviewList.add(review);
		productReviewRequest.setReviews(reviewList);
		return clientService.sendProductReview(productReviewRequest);
	}
}
