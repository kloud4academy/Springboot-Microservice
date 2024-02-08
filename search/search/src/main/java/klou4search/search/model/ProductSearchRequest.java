package klou4search.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

	private String productId;
    private String size;
    private String color;
    private String productDesc;
    private String category;
	private String imageURL;
	private String productName;
	private String searchString;
}
