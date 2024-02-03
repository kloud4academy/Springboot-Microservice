package repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import model.ProductReviews;

public interface ProductReviewRepository extends MongoRepository<ProductReviews,String> {

	@Query(value="{productId:'?0'}")
	ProductReviews findAllById(String productId);
	public long count();
}
