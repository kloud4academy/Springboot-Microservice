package model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("product")
public class Product {
	private String productId;
	private String productName;
	private String productDesc;
	private Double price;
	private String productType;
	private List<String> sizes;
	private List<String> colors;
	private String category;
	private String imageURL;
	private String pattern;
	
}
