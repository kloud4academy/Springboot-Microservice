package kloud4.addtocart.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("inventory")
public class InventoryItem {

	private String productId;
	private String inventoryId;
	private int inStock;
	private int inventoryThreshold;
	private boolean outStock;
	
}
