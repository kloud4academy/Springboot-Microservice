package com.kloud4.kloud4academyHome.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.ClientManager.CartClientService;
import com.kloud4.kloud4academyHome.ClientManager.ClientService;
import com.kloud4.kloud4academyHome.model.CartUpdate;
import com.kloud4.kloud4academyHome.model.ShoppingCart;

import bo.WishListItemBean;
import bo.WishlistRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class CartRestController extends BaseRestController{
	Logger logger = LoggerFactory.getLogger(CartRestController.class);
	
	@Autowired
	Gson gson;
	@Autowired
	private ClientService clientService;
	@Autowired
	private CartClientService cartClientService;
	
	@RequestMapping(value="/productdetail/cart", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> submitCartDetails(@Validated @RequestBody CartData cartData, Errors errors,Model model, HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		logger.info("Getting called CartRestController---Test-"+cartData.getQuantity() + cartData.getPrice());
		AjaxResponseBody result = new AjaxResponseBody();
		if (errors.hasErrors()) {
			logger.error("Error occurred in CartRestController----"+cartData.getQuantity() + cartData.getPrice());
			result.setMsg(errors.getAllErrors().get(0).getDefaultMessage());
			return ResponseEntity.badRequest().body("Cart validation error: " + errors.getAllErrors().get(0).getDefaultMessage());
	    } else if ("0".equalsIgnoreCase(cartData.getQuantity())){
	    	return ResponseEntity.badRequest().body("Cart validation error: please select the quantity!");
	    }
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = clientService.addToCart(cartData,session,response,request);
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
			result.setMsg("There is addTocart error");
			return ResponseEntity.badRequest().body("Cart validation backend error please wait for sometime!");
		}
		
		result.setMsg(responseEntity.getBody());
        return ResponseEntity.ok(ResponseEntity.ok(result));
	}
	
	
	@RequestMapping(path="/cartdetail/cart/{cartId}",method = RequestMethod.GET)
	public ModelAndView cartdetail(@PathVariable String cartId,Model model,HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		CartUpdate cartUpdate = new CartUpdate();
		ResponseEntity<String> responseEntity = null;
		ShoppingCart shoppingCart = null;
		ModelAndView mv = new ModelAndView();
		try {
			responseEntity = clientService.viewCart(cartId);
			shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
			shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
			session.setAttribute("cartUrl", "/cartdetail/cart/"+cartId);
			session.setAttribute("cartId",cartId);
			try {
				mv.addObject("wishlistSize", getWishlistCountForCartPage(session, response, request));
			} catch(Exception e) {
				logger.error("Error occurred during getwishlistcount" + e.getMessage());
			}
			if(shoppingCart != null && shoppingCart.getItems() != null) 
				session.setAttribute("cartSize", String.valueOf(shoppingCart.getItems().size()));
			else
				session.setAttribute("cartSize","0");
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
			return new ModelAndView("redirect:/productlist/Women");
		}
		
		if((shoppingCart == null) ||(shoppingCart != null && shoppingCart.getItems() != null && shoppingCart.getItems().size() == 0)) 
			return new ModelAndView("redirect:/productlist/Women");
		
		mv.addObject("shoppingCart", shoppingCart);
		model.addAttribute("cartUpdate", cartUpdate);
		mv.setViewName("cart");
		return mv;
	}
	
	@RequestMapping(path="/cartdetail/cart/{cartId}",method = RequestMethod.POST)
	public ModelAndView cartUpdate(@PathVariable String cartId,CartUpdate cartUpdate, BindingResult result, Model model,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		ShoppingCart shoppingCart = null;
		ModelAndView mv = new ModelAndView();
		try {
			CartData cartData = new CartData();
			cartData.setQuantity(cartUpdate.getQuantity());
			cartData.setProductId(cartUpdate.getProductId());
			cartData.setPrice(cartUpdate.getPrice());
			cartData.setCartId(cartId);
			responseEntity = clientService.updateCart(cartData, session, response, request);
			shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
			shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
			int itemSize = 0;
			if(shoppingCart != null && shoppingCart.getItems() != null && shoppingCart.getItems().size() > 0) {
				itemSize = shoppingCart.getItems().size();
				session.setAttribute("cartSize", String.valueOf(itemSize));
			}
			else {
				itemSize = 0;
				session.setAttribute("cartSize","0");
			}
			
			try {
				mv.addObject("wishlistSize", getWishlistCountForCartPage(session, response, request));
			} catch(Exception e) {
				logger.error("Error occurred during getwishlistcount" + e.getMessage());
			}
			mv.addObject("cartSize", String.valueOf(itemSize));
			mv.addObject("shoppingCart", shoppingCart);
			mv.setViewName("cart");
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
			ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
			return  redirectmv;
		}
		
		return mv;
	}
	
	@RequestMapping(path="/cartdetail/{productId}/cartdelete/{cartId}",method = RequestMethod.GET)
	public Object cartDelete(@PathVariable String productId,@PathVariable String cartId,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		ModelAndView mv = new ModelAndView();
		try {
			if(StringUtils.isBlank(productId)) {
				ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
				return redirectmv;
				//viewProductList("Women", redirectmv);
			}
			CartData cartData = new CartData();
			cartData.setProductId(productId);
			cartData.setCartId(cartId);
			responseEntity = clientService.deleteCart(cartData, session, response, request);
			ShoppingCart shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
			
			if((shoppingCart == null) || (shoppingCart.getItems() == null || shoppingCart.getItems().size() == 0)) {
				logger.info("There are not items in the cart");
				return new ModelAndView("redirect:/productlist/Women");
			}
			shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
			int itemSize = 0;
			if(shoppingCart != null && shoppingCart.getItems() != null && shoppingCart.getItems().size() > 0) {
				itemSize = shoppingCart.getItems().size();
				session.setAttribute("cartSize", String.valueOf(itemSize));
			}
			else {
				itemSize = 0;
				session.setAttribute("cartSize","0");
			}
			String wishlistSize = (String) session.getAttribute("wishlistSize");
			mv.addObject("wishlistSize", wishlistSize);
			mv.addObject("cartSize", String.valueOf(itemSize));
			mv.addObject("shoppingCart", shoppingCart);
			mv.setViewName("cart");
		} catch(Exception e) {
			logger.error("create cart error in delete cart...."+e.getMessage());
			ModelAndView redirectmv = new ModelAndView("redirect:/productlist/Women");
			return  redirectmv;
			//viewProductList("Women", mv);
		}
	    
		return mv;
	}
	
	@RequestMapping(path="/cart/movetowishlist/{productId}/{cartId}",method = RequestMethod.GET)
	public ModelAndView wishlistCart(@PathVariable String productId,@PathVariable String cartId,HttpSession session,HttpServletResponse response, HttpServletRequest request) {
		logger.info("--------Called wishlistCart controller");
		WishlistRequest wishlistRequest = new WishlistRequest();
		ModelAndView mv = new ModelAndView();
		List<String> productList = new ArrayList<String>();
		productList.add(productId);
		wishlistRequest.setProductIdList(productList);
		wishlistRequest.setCartId(cartId);
		wishlistRequest.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
		try {
			cartClientService.wishListAPICall(wishlistRequest, response, request,"/cart-ms/wishlist/movetowishlist");
		} catch(Exception e) {
			logger.error("Error occured during whishlist update"+e.getMessage());
		}
		mv.setViewName("cart");
	//	return mv;
		return new ModelAndView("redirect:/cartdetail/cart/"+cartId);
	}
	
	@RequestMapping(path="/cart/viewwishlist/{cartId}",method = RequestMethod.GET)
	public ModelAndView viewwishlistCart(@PathVariable String cartId,HttpSession session,HttpServletResponse response, HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		WishlistRequest wishlistResponse = null;
		ModelAndView mv = new ModelAndView();
		WishlistRequest wishlistRequest = new WishlistRequest();
		WishListItemBean wishListItemBean = null;
		wishlistRequest.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
		try {
			responseEntity = cartClientService.wishListAPICall(wishlistRequest, response, request,"/cart-ms/wishlist/viewwishlist");
			if(responseEntity.getBody() == null || responseEntity.getBody() == "") {
				return new ModelAndView("redirect:/productlist/Women");
			}
			wishlistResponse = gson.fromJson(responseEntity.getBody(), WishlistRequest.class);
			mv.addObject("wishlistSize",getWishlistCountForCartPage(session, response, request));
			if(wishlistResponse != null) {
				wishListItemBean = cartClientService.populateWishListItem(wishlistResponse);
			} else {
				return new ModelAndView("redirect:/productlist/Women");
			}
		} catch(Exception e) {
			logger.error("Error occured during whishlist update"+e.getMessage());
			return new ModelAndView("redirect:/productlist/Women");
		}
		
		mv.addObject("cartSize", (String)session.getAttribute("cartSize"));
		mv.addObject("wishlistItems", wishListItemBean);
		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setCartId(cartId);
		mv.addObject("shoppingCart", shoppingCart);
		mv.setViewName("wishlist");
		return mv;
	}
	
	@RequestMapping(path="/cart/viewwishlist/{cartId}",method = RequestMethod.POST)
	public ModelAndView addwishlistToCart(@PathVariable String cartId,CartUpdate cartUpdate, BindingResult result, Model model,HttpSession session,HttpServletResponse response, HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		WishlistRequest wishlistResponse = null;
		ModelAndView mv = new ModelAndView();
		String selectedQty = cartUpdate.getQuantity();
		if("".equalsIgnoreCase(cartUpdate.getQuantity()) || "0".equalsIgnoreCase(cartUpdate.getQuantity()))
			selectedQty = "1";
			
		CartData cartData = new CartData();
		cartData.setQuantity(selectedQty.trim());
		cartData.setProductId(cartUpdate.getProductId());
		cartData.setPrice(cartUpdate.getPrice());
		cartData.setCartId(cartId);
		try {
			responseEntity = clientService.updateCart(cartData, session, response, request);
			if(responseEntity != null && responseEntity.getBody() != null) {
				ShoppingCart shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
				if(shoppingCart != null && shoppingCart.getItems() != null) {
					mv.addObject("cartSize", String.valueOf(shoppingCart.getItems().size()));
				    session.setAttribute("cartSize", String.valueOf(shoppingCart.getItems().size()));
				}
				else {
					mv.addObject("cartSize", (String)session.getAttribute("cartSize"));
				}
				WishlistRequest wishlistRequest = new WishlistRequest();
				List<String> productList = new ArrayList<String>();
				productList.add(cartUpdate.getProductId());
				wishlistRequest.setProductIdList(productList);
				wishlistRequest.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
				ResponseEntity<String> deleteresponseEntity = cartClientService.wishListAPICall(wishlistRequest, response, request,"/cart-ms/wishlist/deletewishlist");
			}
		} catch (Exception e) {
			logger.error("Error occured during whishlist to cart"+e.getMessage());
		}
		WishlistRequest wishlistRequest = new WishlistRequest();
		WishListItemBean wishListItemBean = null;
		wishlistRequest.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
		try {
			responseEntity = cartClientService.wishListAPICall(wishlistRequest, response, request,"/cart-ms/wishlist/viewwishlist");
			if(responseEntity.getBody() == null || responseEntity.getBody() == "") {
				return new ModelAndView("redirect:/cart/viewwishlist/"+cartId);
			}
			wishlistResponse = gson.fromJson(responseEntity.getBody(), WishlistRequest.class);
			mv.addObject("wishlistSize",getWishlistCountForCartPage(session, response, request));
			if(wishlistResponse != null) {
				wishListItemBean = cartClientService.populateWishListItem(wishlistResponse);
			} else {
				return new ModelAndView("redirect:/cart/viewwishlist/"+cartId);
			}
		} catch(Exception e) {
			logger.error("Error occured during whishlist update"+e.getMessage());
			return new ModelAndView("redirect:/productlist/Women");
		}
		
		mv.addObject("wishlistItems", wishListItemBean);
		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setCartId(cartId);
		mv.addObject("shoppingCart", shoppingCart);
		mv.setViewName("wishlist");
		return mv;
	}
	
	@RequestMapping(path="/cart/deletewishlist/{productId}/{cartId}",method = RequestMethod.GET)
	public Object deleteWishlist(@PathVariable String productId,@PathVariable String cartId,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		WishlistRequest wishlistResponse = null;
		ModelAndView mv = new ModelAndView();
		WishlistRequest wishlistRequest = new WishlistRequest();
		List<String> productList = new ArrayList<String>();
		productList.add(productId);
		wishlistRequest.setProductIdList(productList);
		
		WishListItemBean wishListItemBean = null;
		wishlistRequest.setWishListId(cartClientService.createOrGetShopperProfile(response, request));
		try {
			responseEntity = cartClientService.wishListAPICall(wishlistRequest, response, request,"/cart-ms/wishlist/deletewishlist");
			if(responseEntity.getBody() == null || responseEntity.getBody() == "") {
				return new ModelAndView("redirect:/productlist/Women");
			}
			wishlistResponse = gson.fromJson(responseEntity.getBody(), WishlistRequest.class);
			mv.addObject("wishlistSize",getWishlistCountForCartPage(session, response, request));
			if(wishlistResponse != null) {
				wishListItemBean = cartClientService.populateWishListItem(wishlistResponse);
			} else {
				return new ModelAndView("redirect:/productlist/Women");
			}
			
		} catch(Exception e) {
			logger.error("Error occured during whishlist delete"+e.getMessage());
			return new ModelAndView("redirect:/productlist/Women");
		}
		
		mv.addObject("cartSize", (String)session.getAttribute("cartSize"));
		mv.addObject("wishlistItems", wishListItemBean);
		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setCartId(cartId);
		mv.addObject("shoppingCart", shoppingCart);
		mv.setViewName("wishlist");
		return mv;
	}
	
	@RequestMapping(value="/productdetail/directcart", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> addToCart(@Validated @RequestBody CartData cartData, Errors errors,Model model, HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		logger.info("Getting called CartRestController---Test-"+cartData.getQuantity() + cartData.getPrice());
		AjaxResponseBody result = new AjaxResponseBody();
		if ("0".equalsIgnoreCase(cartData.getQuantity())){
	    	return ResponseEntity.badRequest().body("Cart validation error: please select the quantity!");
		}
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = clientService.addToCart(cartData,session,response,request);
		} catch(Exception e) {
			logger.error("addToCart cart error...."+e.getMessage());
			result.setMsg("There is addTocart error");
			return ResponseEntity.badRequest().body("Cart validation backend error please wait for sometime!");
		}
		
		result.setMsg(responseEntity.getBody());
        return ResponseEntity.ok(ResponseEntity.ok(result));
	}
}
