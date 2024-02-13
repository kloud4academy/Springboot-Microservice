package com.kloud4.wishlist.model;

import java.util.Date;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("wishList")
public class WishListBean {
	private String wishListId;
	private Set<String> productIdList;
	private String cartId;
	private Date updatedDate;
}
