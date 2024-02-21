package com.kloud4.product.productmicroservice.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.kloud4.product.productmicroservice.exceptions.MongoDbException;
import com.kloud4.product.productmicroservice.exceptions.ProductNotFoundException;
import com.mongodb.client.result.UpdateResult;

import bo.ProductReviewRequest;
import model.Product;
import model.ProductReviews;
import model.Review;
import repository.ProductRepository;
import repository.ProductReviewRepository;

@Service
public class ServiceManager {
	Logger logger = LoggerFactory.getLogger(ServiceManager.class);
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private MongoTemplate mongoDbTemplate;
	@Autowired
	private ProductReviewRepository productReviewRepository;
	
	public List<Product> fetchProductsByCategory(String category) {
		List<Product> productList = null;
		try {
			productList = productRepository.findAll(category);
		} catch (Exception e) {
			logger.error("Exception occurred during product fetching in mongodb in fetchProductsByCategory");
		}
		
		if(productList == null || productList.isEmpty()) {
			productList = productRepository.findAll("default");
		}
		return productList;
	}
	
	public Product fetchProduct(String productId) throws MongoDbException,ProductNotFoundException {
		Product product = null;
		try {
			product = productRepository.findAllById(productId);
			if(product == null) throw new ProductNotFoundException(productId);
		} catch (Exception e) {
			throw new MongoDbException(e,productId);
		}
		
		return product;
	}
	
	public String saveProductReview(ProductReviewRequest productReviewReq) {
		ProductReviews productReview = new ProductReviews();
		productReview.setNoOfReviewsToShow(productReviewReq.getNoOfReviewsToShow());
		productReview.setProductId(productReviewReq.getProductId());
		Review review = new Review();
		review.setEmail(productReviewReq.getReviews().get(0).getEmail());
		review.setName(productReviewReq.getReviews().get(0).getName());
		review.setRating(Integer.parseInt("5"));
		review.setReviewComment(productReviewReq.getReviews().get(0).getReviewComment());
		review.setShowReview(productReviewReq.getReviews().get(0).getShowReview());
		
		Query query = new Query().addCriteria(Criteria.where("productId").is(productReviewReq.getProductId()));
		ProductReviews productReviewEntity = mongoDbTemplate.findOne(query, ProductReviews.class);
		if(productReviewEntity != null) {
			List<Review> existingReviews = productReviewEntity.getReviews();
			if(existingReviews != null && existingReviews.size() > 0) {
				existingReviews.add(review);
			} else {
				existingReviews = new ArrayList<Review>();
				existingReviews.add(review);
			}
			
			Update updateDefinition = new Update().set("reviews", existingReviews);
			UpdateResult updateResult = mongoDbTemplate.upsert(query, updateDefinition, ProductReviews.class);
		} else {
			List<Review> newReviews = new ArrayList<Review>();
			newReviews.add(review);
			productReview.setReviews(newReviews);
			mongoDbTemplate.insert(productReview);
		}
		return "Review submitted successfully";
	}
	
	public ProductReviews fetchProductReviews(String productId) {
		try {
			return productReviewRepository.findAllById(productId);
		} catch (Exception e) {
			logger.error("MongoDB error occurred during fetching reviews");
		}
		return null;
	}
}
