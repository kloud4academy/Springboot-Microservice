package com.kloud4.kloud4academyHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.ClientManager.ClientService;
import com.kloud4.kloud4academyHome.controller.BaseRestController;

import bo.FilterBean;
import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.ProductSearchRequest;
import bo.Review;
import bo.SearchRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
	@Value("#{${priceRangeMap}}")
	private Map<String,String> priceRangeMap;
	@Value("${colorList}")
	private List<String> colorList;
	
	@RequestMapping(value={"/productlist/{categoryId}","/productfilter/{categoryId}","/productdetail/productlist/{categoryId}","/cartdetail/cart/productlist/{categoryId}","/cartdetail/{productId}/cartdelete/productlist/{categoryId}","/cart/viewwishlist/productlist/{categoryId}","/cart/deletewishlist/{productId}/productlist/{categoryId}"},method = RequestMethod.GET)
	public ModelAndView productList(@PathVariable String categoryId,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		FilterBean filterBean = new FilterBean();
	    ResponseEntity<String> responseEntity;
	    setCartAndWishListCount(session, response, request, mv);
		try {
			responseEntity = clientService.fetchProductsUsingCategory(categoryId);
		    if(responseEntity != null) {
		    	if(checkCircuitBreaker(responseEntity)) {
		    		mv.addObject("apiError", "true");
		    		mv.addObject("error", "true");
		    		mv.addObject("filterBean", filterBean);
					mv.setViewName("productlist");
					return mv;
		    	}
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("productResponse", productInfoList);
		    	mv.addObject("error", "");
		    	mv.addObject("priceRangeMap", priceRangeMap);
		    	mv.addObject("colorList", colorList);
		    	mv.addObject("filterBean", filterBean);
		    	mv.setViewName("productlist");
			}
		} catch (Exception e) {
			logger.info("-------Products fetching error in productlist ----");
			mv.addObject("productResponse", "No Products Found");
			mv.addObject("error", "true");
			mv.addObject("filterBean", filterBean);
			mv.setViewName("productlist");
		}
	    
		return mv;
	}
	
	@RequestMapping(path="/productdetail/{productId}",method = RequestMethod.GET)
	public ModelAndView fetchProductUsingProductId(@PathVariable String productId, Model model,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		Review review = new Review();
		review.setProductId(productId);
		ModelAndView mv = new ModelAndView();
	    ResponseEntity<String> responseEntity = null;
	    String responseString = "";
	    String viewName = "productdetail";
	    setCartAndWishListCount(session, response, request, mv);
		try {
			try {
				if(redisCacheTemplate.hasKey(productId)) {
					responseString = (String) redisCacheTemplate.opsForValue().get(productId);
				}
			} catch (Exception e) {
				logger.error("Redis Cache error occurred"+e.getMessage());
			}
			if("".equalsIgnoreCase(responseString) || responseString == null) {
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
					mv.setViewName(viewName);
					return mv;
		    	}
		    	ProductInfo productInfo = gson.fromJson(responseString, ProductInfo.class);
		    	if("bags".equalsIgnoreCase(productInfo.getCategory())) {
		    		viewName = "otherproductdetail";
		    	} else {
		    		mv.setViewName(viewName);
		    	}
		    	mv.addObject("productInfo", productInfo);
		    	mv.addObject("error", "");
				mv.setViewName(viewName);
				model.addAttribute("review", review);
				loadProductReviews(mv, productId);
			}
		} catch (Exception e) {
			logger.info("-------ProductInfo fetching error in productlist ----"+e.getMessage());
			mv.addObject("productInfo", "No Product Found");
			mv.addObject("error", "true");
			mv.addObject("productInfo", "");
			mv.setViewName(viewName);
		}
	    
		return mv;
	}
	
	
	
	@RequestMapping(value="/productdetail/{productId}",method=RequestMethod.POST)
	public ModelAndView submitreview(@PathVariable String productId,@Valid Review review, BindingResult result, Model model,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		 ModelAndView mv = new ModelAndView();
		 String cartSize = "0";
		 String cartId = (String) session.getAttribute("cartId");
    	 if(session.getAttribute("cartUrl") != null) {
    		mv.addObject("cartUrl", session.getAttribute("cartUrl"));
    		cartSize = (String) session.getAttribute("cartSize");
    		cartId = (String) session.getAttribute("cartId");
    		mv.addObject("cartSize", cartSize);
    		mv.addObject("cartId", cartId);
    	 }
	     mv.addObject("error", "");
	     ProductInfo productInfo = null;
	     try {
	    	 mv.addObject("wishlistSize",getWishlistCountForOtherPage(session, response, request));
	    	 if(redisCacheTemplate.hasKey(productId)) {
					String productString = (String) redisCacheTemplate.opsForValue().get(productId);
				     productInfo = gson.fromJson(productString, ProductInfo.class);
				}
	    	 
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
	
	@RequestMapping(value="/productlist/searchproduct",method = RequestMethod.POST)
	public ModelAndView searchproduct(SearchRequest searchRequest,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		FilterBean filterBean = new FilterBean();
		 
	    ResponseEntity<String> responseEntity = null;;
		try {
			setCartAndWishListCount(session, response, request, mv);
			if(searchRequest != null && !StringUtils.isBlank(searchRequest.getSearchString())) {
				responseEntity = clientService.searchProduct(searchRequest.getSearchString());
			} else {
				ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
				return redirectmv;
			}
		    if(responseEntity != null) {
		    	if(checkCircuitBreaker(responseEntity)) {
		    		//TODO No need to enable now only for demo
		    		mv.addObject("apiError", "false");
		    		mv.addObject("error", "true");
					mv.setViewName("productlist");
					return mv;
		    	}
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("productResponse", productInfoList);
		    	mv.addObject("error", "");
		    	mv.addObject("filterBean", filterBean);
				mv.setViewName("productlist");
			}
		} catch (Exception e) {
			logger.info("-------Products search error in productlist ----");
			mv.addObject("productResponse", "No Products Found");
			mv.addObject("error", "true");
			mv.setViewName("productlist");
		}
	    
		return mv;
	}
	
	@RequestMapping(path="/productfilter/colorfilter",method=RequestMethod.POST)
	public ModelAndView colorFilter(FilterBean filterBean,BindingResult result, Model model,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
	    ResponseEntity<String> responseEntity = null;
		try {
			setCartAndWishListCount(session, response, request, mv);
			if(filterBean != null && !StringUtils.isBlank(filterBean.getSelectedColor())) {
				ProductSearchRequest productSearchRequest = new ProductSearchRequest();
				productSearchRequest.setSortType("colors");
				productSearchRequest.setSortByString(filterBean.getSelectedColor());
				responseEntity = clientService.filterProducts(productSearchRequest,"https://localhost:8083/search-ms/shopping/searchproductsort");
			} else {
				ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
				return redirectmv;
			}
		    if(responseEntity != null) {
		    	if(checkCircuitBreaker(responseEntity)) {
		    		//TODO No need to enable now only for demo
		    		mv.addObject("apiError", "false");
		    		mv.addObject("error", "true");
					mv.setViewName("productlist");
					return mv;
		    	}
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("productResponse", productInfoList);
		    	mv.addObject("error", "");
				mv.setViewName("productlist");
				mv.addObject("priceRangeMap", priceRangeMap);
		    	mv.addObject("colorList", colorList);
			}
		} catch (Exception e) {
			logger.info("-------Products search error in productlist ----");
			mv.addObject("productResponse", "No Products Found");
			mv.addObject("error", "true");
			mv.setViewName("productlist");
		}
	    
		return mv;
	}
	
	@RequestMapping(path="/productfilter/pricefilter",method=RequestMethod.POST)
	public ModelAndView productPriceFilter(FilterBean filterBean,BindingResult result, Model model,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
	    ResponseEntity<String> responseEntity = null;;
		try {
			setCartAndWishListCount(session, response, request, mv);
			if(filterBean != null && !StringUtils.isBlank(filterBean.getSelectedPrice())) {
				String[] pricearry = filterBean.getSelectedPrice().split("-");
				ProductSearchRequest productSearchRequest = new ProductSearchRequest();
				productSearchRequest.setPriceFrom(Double.parseDouble(pricearry[0]));
				productSearchRequest.setPriceTo(Double.parseDouble(pricearry[1]));
				responseEntity = clientService.filterProducts(productSearchRequest,"https://localhost:8083/search-ms/shopping/searchproductranges");
			} else {
				ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
				return redirectmv;
			}
		    if(responseEntity != null) {
		    	if(checkCircuitBreaker(responseEntity)) {
		    		//TODO No need to enable now only for demo
		    		mv.addObject("apiError", "false");
		    		mv.addObject("error", "true");
					mv.setViewName("productlist");
					return mv;
		    	}
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	logger.info("-----first productInfoList : "+productInfoList);
		    	mv.addObject("productResponse", productInfoList);
		    	mv.addObject("error", "");
				mv.setViewName("productlist");
				mv.addObject("priceRangeMap", priceRangeMap);
		    	mv.addObject("colorList", colorList);
			}
		} catch (Exception e) {
			logger.info("-------Products search filter error in productlist ----");
			mv.addObject("productResponse", "No Products Found");
			mv.addObject("error", "true");
			mv.setViewName("productlist");
		}
	    
		return mv;
	}
	
	
	@RequestMapping(path="/removecache",method = RequestMethod.POST)
	@ResponseBody
	public String removecacheByProductId(@RequestBody String productId) {
		logger.info("---------Removed Redisccache item"+productId);
		try {
			if(redisCacheTemplate.hasKey(productId)) {
				redisCacheTemplate.delete(productId);
			}
		} catch (Exception e) {
			logger.error("Redis Cache error occurred"+e.getMessage());
		}
	    
		return "Item has been delete success";
	}
}
