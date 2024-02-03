package com.kloud4.kloud4academyHome.model;

import java.io.Serializable;

import bo.ProductInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item implements Serializable {

	//private static final long serialVersionUID = 1000033222L;
	
	private String productId;
	private double price;
	private double discountPrice;
	private String itemType;
	private int toalQuantity;
	private ProductInfo productInfo;
	
}
