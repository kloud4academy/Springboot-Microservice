package com.kloud4.wishlist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.kloud4.wishlist.manager.WishlistServiceManager;
import com.kloud4.wishlist.model.WishListBean;

@Controller
public class WishlistRestController {

	Logger logger = LoggerFactory.getLogger(WishlistRestController.class);
	
	@Autowired
	Gson gson;
	@Autowired
	WishlistServiceManager wishlistServiceManager;
	
	@RequestMapping("/cart-ms/wishlist/movetowishlist")
	@ResponseBody
    public String createOrUpdateWishList(@RequestBody WishListBean wishListBean) throws Exception {
		return wishlistServiceManager.creatOrUpdateWishlist(wishListBean);
    }
	
	@RequestMapping("/cart-ms/wishlist/deletewishlist")
	@ResponseBody
    public String deleteWishList(@RequestBody WishListBean wishListBean) {
		wishlistServiceManager.deleteWishlist(wishListBean);
		WishListBean wishList = wishlistServiceManager.viewWishlist(wishListBean);
		return gson.toJson(wishList);
    }
	
	@RequestMapping("/cart-ms/wishlist/viewwishlist")
	@ResponseBody
    public String viewWishList(@RequestBody WishListBean wishListBean) {
		WishListBean wishList = wishlistServiceManager.viewWishlist(wishListBean);
		return gson.toJson(wishList);
    }
}
