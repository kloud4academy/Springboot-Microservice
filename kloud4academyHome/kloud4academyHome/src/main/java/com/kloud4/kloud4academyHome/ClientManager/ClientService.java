package com.kloud4.kloud4academyHome.ClientManager;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.controller.CartData;
import com.kloud4.kloud4academyHome.model.Item;
import com.kloud4.kloud4academyHome.model.ShoppingCart;

import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.ProductSearchRequest;
import bo.SendCartRequest;
import bo.ShoppingCartCount;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class ClientService extends BaseClientService{
	Logger logger = LoggerFactory.getLogger(ClientService.class);
	@Autowired
	private DiscoveryClient discoveryClient;
	@Value("${server.port}")
	private String serverPort;
	@Autowired
	Gson gson;
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate redisCacheTemplate;
	
	private static HttpEntity<?> getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(headers);
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> fetchProductsUsingCategory(String category) throws Exception {
		List servicesList  = discoveryClient.getServices();
		if(servicesList != null && servicesList.contains("product-service")) {
			String productApiUrl = "http://product-service:8081" + "/nt-ms/fetchProductsUsingCategory" + "/"+category;
			logger.info("---------Final Url " +productApiUrl );
			
			ResponseEntity<String> response = restTemplate().exchange(productApiUrl,HttpMethod.GET, getHeaders(),String.class);
			return response;
		}
		return null;
		//ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8081/nt-ms/fetchProductsUsingCategory" + "/"+category, String.class);
	//	return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> fetchProductUsingProductId(String productId) throws Exception {
		List servicesList  = discoveryClient.getServices();
		if(servicesList != null && servicesList.contains("product-service")) {
			String productApiUrl = "http://product-service:8081" + "/nt-ms/fetchProduct" + "/"+productId;
			ResponseEntity<String> response = restTemplate().exchange(productApiUrl,HttpMethod.GET, getHeaders(),String.class);
			return response;
		}
		return null;
//		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8081/nt-ms/fetchProduct" + "/"+productId, String.class);
//		return response;
	}
	
	public String sendProductReview(ProductReviewRequest productReviewReq) {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<ProductReviewRequest> entity = new  HttpEntity<ProductReviewRequest>(productReviewReq,headers);
		if(servicesList != null && servicesList.contains("productmicroservice")) {
			for (Object service : servicesList) {
				if("productmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8081" + "/nt-ms/sendproductReview";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object"+response);
					return "Reivew saved successfully";
				}
			}
		}
			
		ResponseEntity<String> response = restTemplate().exchange("https://localhost:8081/nt-ms/sendproductReview", HttpMethod.POST, entity, String.class);
		
		return response.getBody();
	}
	
	private ResponseEntity<String> fallbackAPIError(String category,Exception e) {
		logger.info("--------Fallback called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	}
	
	@CircuitBreaker(name="kloud4breaker")
	public ResponseEntity<String> fetchProductReviews(String productId) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("productmicroservice")) {
			for (Object service : servicesList) {
				if("productmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8081" + "/nt-ms/fetchProductReviews";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().getForEntity(productApiUrl + "/"+productId, String.class);
					logger.info("----Print Response Object for fetchProductReviews "+response);
					return response;
				}
			}
		}
		logger.info("-------ProductId parameter----"+productId);
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8081/nt-ms/fetchProductReviews" + "/"+productId, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackCreateCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> addToCart(SendCartRequest sendCartRequest) throws Exception {
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<SendCartRequest> entity = new  HttpEntity<SendCartRequest>(sendCartRequest,headers);
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("cartmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8082" + "/cart-ms/shopping/addToCart";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object"+response);
					return response;
				}
			}
		}
		
		ResponseEntity<String> response = restTemplate().exchange("https://localhost:8082/cart-ms/shopping/addToCart", HttpMethod.POST, entity, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackViewCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> viewCart(String cartId) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("cartmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8082" + "/cart-ms/shopping/viewCart";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().getForEntity(productApiUrl + "/"+cartId, String.class);
					logger.info("----Print Response Object for fetchProductReviews "+response);
					return response;
				}
			}
		}
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8082/cart-ms/shopping/viewCart" + "/"+cartId, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackViewCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> loadCartByShopperId(String shopperId) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("cartmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8082" + "/cart-ms/shopping/loadcartbyshopperid";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().getForEntity(productApiUrl + "/"+shopperId, String.class);
					logger.info("----Print Response Object for fetchProductReviews "+response);
					return response;
				}
			}
		}
		logger.info("-------ProductId parameter-in load Cart---"+shopperId);
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8082/cart-ms/shopping/loadcartbyshopperid" + "/"+shopperId, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackUpdateCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> updateCart(CartData cartData,HttpSession session, HttpServletResponse response, HttpServletRequest request) throws Exception {
		SendCartRequest sendCartRequest = populateSendCartRequest(cartData,session,response,request);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<SendCartRequest> entity = new  HttpEntity<SendCartRequest>(sendCartRequest,headers);
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("cartmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8082" + "/cart-ms/shopping/updateCart";
					logger.info("-------productApiUrl----"+productApiUrl);
				//	ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object");
					//return response;
				}
			}
		}
		
		ResponseEntity<String> cartResponse = restTemplate().exchange("https://localhost:8082/cart-ms/shopping/updateCart", HttpMethod.POST, entity, String.class);
		return cartResponse;
	}
	
	public ResponseEntity<String> addToCart(CartData cartData,HttpSession session, HttpServletResponse response, HttpServletRequest request) throws Exception {
		SendCartRequest sendCartRequest = populateSendCartRequest(cartData,session,response,request);
		ResponseEntity<String> responseEntity = addToCart(sendCartRequest);
		
		return responseEntity;
	}
	
	private SendCartRequest populateSendCartRequest(CartData cartData, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		SendCartRequest sendCartRequest = new SendCartRequest();
		sendCartRequest.setShopperProfileId(createOrGetShopperProfile(response,request));
		
		sendCartRequest.setColor(cartData.getColor());
		sendCartRequest.setPrice(cartData.getPrice());
		sendCartRequest.setQuantity(cartData.getQuantity());
		sendCartRequest.setSize(cartData.getSize());
		sendCartRequest.setProductId(cartData.getProductId());
		sendCartRequest.setCartId(cartData.getCartId());
		
		return sendCartRequest;
	}
	
	public ShoppingCart populateCartProdunctInfo(ShoppingCart shoppingCart) {
		Item item = null;
		
		
		for(Object itemObject : shoppingCart.getItems()) {
			item = (Item) itemObject;
			String productString = "";
			ResponseEntity<String> responseEntity = null;
			try {
				if(redisCacheTemplate.hasKey(item.getProductId())) {
					productString = (String) redisCacheTemplate.opsForValue().get(item.getProductId());
				} else {
					responseEntity = fetchProductUsingProductId(item.getProductId());
					productString = responseEntity.getBody();
				}
				ProductInfo productInfo = gson.fromJson(productString,ProductInfo.class);
				item.setProductInfo(productInfo);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return shoppingCart;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackDeleteCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> deleteCart(CartData cartData,HttpSession session, HttpServletResponse response, HttpServletRequest request) throws Exception {
		SendCartRequest sendCartRequest = populateSendCartRequest(cartData,session,response,request);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<SendCartRequest> entity = new  HttpEntity<SendCartRequest>(sendCartRequest,headers);
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("cartmicroservice")) {
			for (Object service : servicesList) {
				if("cartmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8082" + "/cart-ms/shopping/deleteCart";
					logger.info("-------productApiUrl----"+productApiUrl);
				//	ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					//logger.info("----Print Response Object"+response);
					//return response;
				}
			}
		}
		
		ResponseEntity<String> cartresponse = restTemplate().exchange("https://localhost:8082/cart-ms/shopping/deleteCart", HttpMethod.POST, entity, String.class);
		return cartresponse;
	}
	
	public void checkandReloadCartCount(HttpSession session,String cartUrl,HttpServletResponse response, HttpServletRequest request) {
		String shopperProfileId = "";
		if(StringUtils.isEmpty(cartUrl) && WebUtils.getCookie(request, "cartcookie") != null) {
			shopperProfileId = WebUtils.getCookie(request, "cartcookie").getValue();
		}
		if(!StringUtils.isEmpty(shopperProfileId)) {
			try {
				ResponseEntity<String> responseEntity = loadCartByShopperId(shopperProfileId);
				ShoppingCartCount shoppingCartCount = gson.fromJson(responseEntity.getBody(), ShoppingCartCount.class);
				if(shoppingCartCount != null && shoppingCartCount.getCartId() != null) {
					session.setAttribute("cartUrl", "/cartdetail/cart/"+shoppingCartCount.getCartId());
					session.setAttribute("cartId", shoppingCartCount.getCartId());
					session.setAttribute("cartSize", String.valueOf(shoppingCartCount.getCartSize()));
				}
			} catch (Exception e) {
				logger.error("Error occurred during loadcart by shoppperid ");
			}
		}
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbacksearchAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> searchProduct(String searchString) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<String> entity = new  HttpEntity<String>(searchString,headers);
		if(servicesList != null && servicesList.contains("searchmicroservice")) {
			for (Object service : servicesList) {
				if("searchmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8083" + "/search-ms/shopping/searchproduct";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object for searchproduct "+response);
					return response;
				}
			}
		}
		ResponseEntity<String> response = restTemplate().exchange("https://localhost:8083/search-ms/shopping/searchproduct", HttpMethod.POST, entity, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbacksearchFilterAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> filterProducts(ProductSearchRequest productSearchRequest,String endpointURL) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<ProductSearchRequest> entity = new  HttpEntity<ProductSearchRequest>(productSearchRequest,headers);
		if(servicesList != null && servicesList.contains("searchmicroservice")) {
			for (Object service : servicesList) {
				if("searchmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8083" + endpointURL;
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().exchange(productApiUrl, HttpMethod.POST, entity, String.class);
					logger.info("----Print Response Object for searchproduct "+response);
					return response;
				}
			}
		}
		ResponseEntity<String> response = restTemplate().exchange(endpointURL, HttpMethod.POST, entity, String.class);
		return response;
	}
	
	private ResponseEntity<String> fallbacksearchFilterAPIError(ProductSearchRequest productSearchRequest,String endpointURL,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	
	private ResponseEntity<String> fallbacksearchAPIError(String searchString,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	
	private ResponseEntity<String> fallbacksearchAPIError(SendCartRequest sendCartRequest,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	
	private ResponseEntity<String> fallbackViewCartAPIError(String cartId,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	private ResponseEntity<String> fallbackUpdateCartAPIError(CartData shoppingCart,HttpSession session, HttpServletResponse response, HttpServletRequest request,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	private ResponseEntity<String> fallbackDeleteCartAPIError(CartData cartData,HttpSession session, HttpServletResponse response, HttpServletRequest request,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
}