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
import com.kloud4.kloud4academyHome.ClientManager.ClientService;

import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.Review;

public class BaseRestController {
	@Autowired
	private ClientService clientService;
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate redisCacheTemplate;
	
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
}
