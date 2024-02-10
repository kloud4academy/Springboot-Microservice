package model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
	
	public Product(String productId,String productName,String productDesc,Double price,List<String> sizes,List<String> colors, String productType,String category, String imageURL) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.productDesc = productDesc;
		this.price = price;
		this.sizes = sizes;
		this.colors = colors;
		this.productType = productType;
		this.category = category;
		this.imageURL = imageURL;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public List<String> getSizes() {
		return sizes;
	}

	public void setSizes(List<String> sizes) {
		this.sizes = sizes;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(List<String> colors) {
		this.colors = colors;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
}
