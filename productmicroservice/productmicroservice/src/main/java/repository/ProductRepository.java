package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import model.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product,String>{
	
	@Query(value="{category:'?0'}")
	List<Product> findAll(String category);
	@Query(value="{productId:'?0'}")
	Product findAllById(String productId);
	public long count();
}



