package klou4search.search.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName="search-product",createIndex=false)
public class Product {
	@Id
	private String Id;
	@ReadOnlyProperty
	@Field(name="productId")
	private String productId;
	@ReadOnlyProperty
	@Field(name="productName")
	private String productName;
	@ReadOnlyProperty
	@Field(name="productDesc")
	private String productDesc;
	@ReadOnlyProperty
	@Field(name="price")
	private String price;
	@ReadOnlyProperty
	@Field(name="imageURL")
	private String imageURL;
}
