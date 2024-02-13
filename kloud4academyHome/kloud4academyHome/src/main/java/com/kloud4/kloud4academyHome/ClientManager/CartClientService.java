package com.kloud4.kloud4academyHome.ClientManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.controller.CartData;

import bo.ProductInfo;
import bo.SendCartRequest;
import bo.WishListItemBean;
import bo.WishlistRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class CartClientService extends BaseClientService {
	
	Logger logger = LoggerFactory.getLogger(CartClientService.class);
	
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate redisCacheTemplate;
	@Autowired
	private Gson gson;
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackCreateWishlistAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> wishListAPICall(WishlistRequest wishListBean,HttpServletResponse response, HttpServletRequest request,String endpointUrl) throws Exception {
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<WishlistRequest> entity = new  HttpEntity<WishlistRequest>(wishListBean,headers);
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("wishlistmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8084" + endpointUrl;
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> wishlistResponse = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object"+response);
					return wishlistResponse;
				}
			}
		}
		
		ResponseEntity<String> tempresponse = restTemplate().exchange("https://localhost:8084"+endpointUrl, HttpMethod.POST, entity, String.class);
		return tempresponse;
	}
	
	private ResponseEntity<String> fallbackCreateWishlistAPIError(WishlistRequest wishListBean,HttpServletResponse response, HttpServletRequest request,String endpointUrl,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	
	public WishListItemBean populateWishListItem(WishlistRequest wishlistResponse) {
		String responseString = "";
		WishListItemBean wishListItemBean = new WishListItemBean();
		List<ProductInfo> productList = new ArrayList<ProductInfo>();
		for (String productId : wishlistResponse.getProductIdList()) {
			responseString = (String) redisCacheTemplate.opsForValue().get(productId);
			ProductInfo productInfo = gson.fromJson(responseString, ProductInfo.class);
			productList.add(productInfo);
		}
		
		wishListItemBean.setProductInfoList(productList);
		return wishListItemBean;
	}
}
