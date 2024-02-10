package com.kloud4.kloud4academyHome.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.ClientManager.ClientService;

import bo.ProductInfo;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomePageController extends BaseRestController {
	Logger logger = LoggerFactory.getLogger(HomePageController.class);
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	Gson gson;
	
	@GetMapping({"/home","/"})
	public ModelAndView showHomePage(HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		ResponseEntity<String> responseEntity;
		ResponseEntity<String> recentResponseEntity;
		ModelAndView mv = new ModelAndView();
		String cartSize = "0";
		String cartUrl = "";
   		cartUrl = (String) session.getAttribute("cartUrl");
   		if(StringUtils.isBlank(cartUrl)) {
   			clientService.checkandReloadCartCount(session, cartUrl, response, request);
   			cartUrl = (String) session.getAttribute("cartUrl");
   			cartSize = (String) session.getAttribute("cartSize");
   		} else {
   			cartSize = (String) session.getAttribute("cartSize");
   		}
   		mv.addObject("cartUrl", cartUrl);
   		mv.addObject("cartSize", cartSize);
		try {
			responseEntity = clientService.fetchProductsUsingCategory("Featured");
			if(super.checkCircuitBreaker(responseEntity)) {
	    		mv.addObject("apiError", "true");
	    		mv.addObject("error", "true");
	    		mv.setViewName("homepage");
				return mv;
	    	}
			recentResponseEntity = clientService.fetchProductsUsingCategory("RecentProducts");
		    if(responseEntity != null) {
		    	ProductInfo[] productInfoList = gson.fromJson(responseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("featureProducts", productInfoList);
		    	mv.addObject("error", "");
			}
		    if(recentResponseEntity != null) {
		    	ProductInfo[] recentProducts = gson.fromJson(recentResponseEntity.getBody(), ProductInfo[].class);
		    	mv.addObject("recentProducts", recentProducts);
		    	mv.addObject("error", "");
		    }
		    mv.setViewName("homepage");
		    
		} catch(Exception e) {
			logger.info("-------Products fetching error ----"+e.getMessage());
			mv.addObject("error", "true");
			mv.setViewName("homepage");
		}
		return mv;
	}
}
