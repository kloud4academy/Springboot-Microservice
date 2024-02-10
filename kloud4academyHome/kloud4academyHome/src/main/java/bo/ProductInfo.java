package bo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {

	private String productId;
	private String productName;
	private String productDesc;
	private Double price;
	private String productType;
	private List<String> sizes;
	private List<String> colors;
	private String category;
	private String imageURL;
}
