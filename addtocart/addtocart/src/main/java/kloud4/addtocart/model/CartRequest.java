package kloud4.addtocart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {

	private String productId;
	private String price;
	private String quantity;
	private String size;
	private String color;
	private String cartId;
	private String shopperProfileId;
}
