package com.kloud4.kloud4academyHome.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor	
public class CartUpdate {
	private String quantity;
	private String cartaction;
	private String productId;
	private String price;
}
