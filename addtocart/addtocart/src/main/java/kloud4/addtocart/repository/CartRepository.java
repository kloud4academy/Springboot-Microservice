package kloud4.addtocart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import kloud4.addtocart.model.ShoppingCart;

@Repository
public interface CartRepository  extends MongoRepository<ShoppingCart,String> {

	public long count();
}
