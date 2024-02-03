package com.kloud4.kloud4academyHome.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartData {
	private String productId;
	private String price;
	private String quantity;
	@NotBlank(message = "size can't empty!")
	private String size;
	private String color;
	private String cartId;
}
