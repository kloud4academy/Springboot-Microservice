package com.pim.Merchandise.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("product")
public class Product {

	private String uuId;
	private String skuId;
	private String enabled;
	private String category;
	private String pattern;
	private String colors;
	private String specificDetails;
	private String StyleName;
	private String description;
	private String erpName;
	private String genderType;
	private String onlineDate;
	private String imageUrl;
	private String image;
	private String longDescription;
	private String price;
	private String engERP;
	private String productName;
	private String vendorItemNo;
	private String size;
	private String madein;
	private String material;
	private String modelNo;
	private String features;
	private String curvedScreen;
	private String brand;
	private String builtinSpeakers;
	private String bluetoothEnabled;
	private String inch;
	}
