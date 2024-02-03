package com.kloud4.kloud4academyHome.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDetails {
	private String price;
	private String quantity;
	private String size;
	private String color;

}
