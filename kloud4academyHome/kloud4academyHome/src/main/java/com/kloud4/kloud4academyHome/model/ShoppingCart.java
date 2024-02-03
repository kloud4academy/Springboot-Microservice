package com.kloud4.kloud4academyHome.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart implements Serializable {

	//private static final long serialVersionUID = 1000000222L;
	private String cartId;
	private List<Item> items;
	private double orderDiscount;
	private double orderTotal;
	private String orderType;
	private String shopperProfileId;
	
}
