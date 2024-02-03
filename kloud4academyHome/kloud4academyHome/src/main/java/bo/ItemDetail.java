package bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetail {
	
	private String productId;
	private double price;
	private double discountPrice;
	private String itemType;
}