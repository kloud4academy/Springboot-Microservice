package com.kloud4.product.productmicroservice;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.kloud4.product.productmicroservice.exceptions.MongoDbException;
import com.kloud4.product.productmicroservice.exceptions.ProductNotFoundException;
import com.kloud4.product.productmicroservice.manager.ServiceManager;

import bo.ProductReviewRequest;
import model.Product;
import model.ProductReviews;

@Controller
public class ProductRestController{
	Logger logger = LoggerFactory.getLogger(ProductRestController.class);
	@Autowired
	Gson gson;
	@Autowired
	private ServiceManager apiServiceManager;

	@RequestMapping(path="/nt-ms/fetchProductsUsingCategory/{categoryId}",method = RequestMethod.GET)
	@ResponseBody
	public String fetchProductsUsingCategory(@PathVariable String categoryId) {
		List<Product> productsData = apiServiceManager.fetchProductsByCategory(categoryId);
		return gson.toJson(productsData);
	}
	
	@RequestMapping(path="/nt-ms/fetchProduct/{productId}")
	@ResponseBody
	public String fetchProduct(@PathVariable String productId) throws MongoDbException, ProductNotFoundException {
		Product productsData = apiServiceManager.fetchProduct(productId);
		return gson.toJson(productsData);
	}
	
	@RequestMapping(path="/nt-ms/sendproductReview",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.POST)
	@ResponseBody
	public String sendProductReviews(@RequestBody ProductReviewRequest productReview) throws MongoDbException {
		String response = apiServiceManager.saveProductReview(productReview);
		return response;
	}
	
	@RequestMapping(path="/nt-ms/fetchProductReviews/{productId}")
	@ResponseBody
	public String fetchProductReviews(@PathVariable String productId) {
		ProductReviews productReviews = apiServiceManager.fetchProductReviews(productId);
		if(productReviews != null)
			return gson.toJson(productReviews);
		return "No Reviews Found";
	}
}
