package com.kloud4.product.productmicroservice.exceptions;

public class ProductNotFoundException extends Exception{

	public ProductNotFoundException(String Id) {
		super(String.format("Not able to fetch Product details: "+Id));
	}
	public ProductNotFoundException(String Id,String err) {
		super(String.format("Not able to fetch Product details due to database exception: "+Id+ "Exception Msg:" +err));
	}
}
