package com.kloud4.kloud4academyHome.controller;

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
import com.kloud4.kloud4academyHome.ClientManager.ClientService;
import com.kloud4.kloud4academyHome.model.CartUpdate;
import com.kloud4.kloud4academyHome.model.ShoppingCart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class CartRestController extends BaseRestController{
	@Autowired
	Gson gson;
	@Autowired
	private ClientService clientService;
	Logger logger = LoggerFactory.getLogger(CartRestController.class);
	
	@RequestMapping(value="/productdetail/cart", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> submitCartDetails(@Validated @RequestBody CartData cartData, Errors errors,Model model, HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		logger.info("Getting called CartRestController---Test-"+cartData.getQuantity() + cartData.getPrice());
		AjaxResponseBody result = new AjaxResponseBody();
		if (errors.hasErrors()) {
			logger.error("Error occurred in CartRestController----"+cartData.getQuantity() + cartData.getPrice());
//            result.setMsg(errors.getAllErrors()
//                        .stream().map(x -> x.getDefaultMessage())
//                        .collect(Collectors.joining(",")));
			result.setMsg("errror");
			return ResponseEntity.badRequest().body(result);
            //return errors.getAllErrors().get(0).toString();
	    }
		ResponseEntity<String> responseEntity = null;
		try {
			
			responseEntity = clientService.addToCart(cartData,session,response,request);
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
			e.printStackTrace();
		}
		
//		List<CartData> cartDetails = new ArrayList<CartData>();
//        if (cartDetails != null && cartDetails.get(0).getQuantity().isEmpty()){
//            result.setMsg("Backend error while calculating quantity");
//        } else {
//        	result.setMsg("success");
//        }
//        result.setMsg("success");
//        result.setResult(cartDetails);
       // return ResponseEntity.ok(result);
		logger.info("---------Print cartID::::" +responseEntity.getBody());
		result.setMsg(responseEntity.getBody());
        return ResponseEntity.ok(ResponseEntity.ok(result));
	}
	
	
	@RequestMapping(path="/cartdetail/cart/{cartId}",method = RequestMethod.GET)
	public ModelAndView cartdetail(@PathVariable String cartId,Model model) {
		CartUpdate cartUpdate = new CartUpdate();
		logger.info("--------Called Cart controller");
		ResponseEntity<String> responseEntity = null;
		ModelAndView mv = new ModelAndView();
		try {
			responseEntity = clientService.viewCart(cartId);
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
		}
		ShoppingCart shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
		shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
		mv.addObject("shoppingCart", shoppingCart);
		model.addAttribute("cartUpdate", cartUpdate);
		mv.setViewName("cart");
	    
		return mv;
	}
	
	@RequestMapping(path="/cartdetail/cart/{cartId}",method = RequestMethod.POST)
	public ModelAndView cartUpdate(@PathVariable String cartId,CartUpdate cartUpdate, BindingResult result, Model model,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		ModelAndView mv = new ModelAndView();
		try {
			CartData cartData = new CartData();
			cartData.setQuantity(cartUpdate.getQuantity());
			cartData.setProductId(cartUpdate.getProductId());
			cartData.setPrice(cartUpdate.getPrice());
			cartData.setCartId(cartId);
			responseEntity = clientService.updateCart(cartData, session, response, request);
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
		}
		ShoppingCart shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
		shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
		mv.addObject("shoppingCart", shoppingCart);
		mv.setViewName("cart");
	    
		return mv;
	}
	
	@RequestMapping(path="/cartdetail/{productId}/cartdelete/{cartId}",method = RequestMethod.GET)
	public ModelAndView cartDelete(@PathVariable String cartId,@PathVariable String productId,HttpSession session, HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity = null;
		ModelAndView mv = new ModelAndView();
		try {
			CartData cartData = new CartData();
			cartData.setQuantity(cartData.getQuantity());
			cartData.setProductId(cartData.getProductId());
			cartData.setPrice(cartData.getPrice());
			cartData.setCartId(cartId);
			responseEntity = clientService.deleteCart(cartData, session, response, request);
		} catch(Exception e) {
			logger.error("create cart error...."+e.getMessage());
		}
		logger.info("delete cart response body : "+responseEntity.getBody());
		logger.info("delete cart response status : "+responseEntity.getStatusCode());
		ShoppingCart shoppingCart = gson.fromJson(responseEntity.getBody(), ShoppingCart.class);
		shoppingCart = clientService.populateCartProdunctInfo(shoppingCart);
		mv.addObject("shoppingCart", shoppingCart);
		mv.setViewName("cart");
	    
		return mv;
	}
}
