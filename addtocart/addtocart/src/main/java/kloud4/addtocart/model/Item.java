package kloud4.addtocart.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("item")
public class Item implements Serializable {

	//private static final long serialVersionUID = 1000033222L;
	
	private String productId;
	private double price;
	private double discountPrice;
	private String itemType;
	private int toalQuantity;
	private ProductInfo productInfo;
	
}
