package com.kloud4.product.productmicroservice.exceptions;

public class MongoDbException extends Exception {

	public MongoDbException(Exception e, String id) {
		super(String.format("MongoDB connection error: "+id + ": " +e.getMessage()));
	}
}
