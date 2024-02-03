package com.kloud4.kloud4academyHome.ClientManager;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import com.google.gson.Gson;
import com.kloud4.kloud4academyHome.controller.CartData;
import com.kloud4.kloud4academyHome.model.Item;
import com.kloud4.kloud4academyHome.model.ShoppingCart;

import bo.ProductInfo;
import bo.ProductReviewRequest;
import bo.SendCartRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class ClientService {
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
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> fetchProductsUsingCategory(String category) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("productmicroservice")) {
			for (Object service : servicesList) {
				if("productmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + serverPort + "/nt-ms/fetchProductsUsingCategory";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().getForEntity(productApiUrl + "/"+category, String.class);
					logger.info("----Print Response Object"+response);
					return response;
				}
			}
		}
		
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8081/nt-ms/fetchProductsUsingCategory" + "/"+category, String.class);
		return response;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> fetchProductUsingProductId(String productId) throws Exception {
		List servicesList  = discoveryClient.getServices();
		logger.info("-------servicesList----"+servicesList);
		
		if(servicesList != null && servicesList.contains("productmicroservice")) {
			for (Object service : servicesList) {
				if("productmicroservice".equalsIgnoreCase(service.toString())) {
					String productApiUrl = "https://" +service+ ":" + "8081" + "/nt-ms/fetchProduct";
					logger.info("-------productApiUrl----"+productApiUrl);
					ResponseEntity<String> response = restTemplate().getForEntity(productApiUrl + "/"+productId, String.class);
					logger.info("----Print Response Object"+response);
					return response;
				}
			}
		}
		logger.info("-------ProductId parameter----"+productId);
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8081/nt-ms/fetchProduct" + "/"+productId, String.class);
		return response;
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
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = null;
		try {
			org.apache.hc.core5.ssl.TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		    org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory csf = new org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory(sslContext);
		    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("https", csf).build();
		    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		    CloseableHttpClient httpclient = HttpClients.custom()
		            .setConnectionManager(cm).build();
		   requestFactory.setHttpClient(httpclient);
			
		   restTemplate = new RestTemplate(requestFactory);
		} catch(Exception e) {
			
		}
		return restTemplate;
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
	
	private ResponseEntity<String> fallbackCreateCartAPIError(SendCartRequest sendCartRequest,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	
	private ResponseEntity<String> fallbackViewCartAPIError(String cartId,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	private ResponseEntity<String> fallbackUpdateCartAPIError(CartData shoppingCart,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
	} 
	private ResponseEntity<String> fallbackDeleteCartAPIError(CartData cartData,Exception e) {
		logger.info("--------fallbackCartAPIError called circuitbreaker started....");
		return new ResponseEntity<String>("statuscode", HttpStatusCode.valueOf(404));
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
		logger.info("-------ProductId parameter----"+cartId);
		ResponseEntity<String> response = restTemplate().getForEntity("https://localhost:8082/cart-ms/shopping/viewCart" + "/"+cartId, String.class);
		return response;
	}
	
	public String createShopperProfile(HttpServletResponse response, HttpServletRequest request) {
		String shopperProfileId = "";
		try {
			logger.info("-------Create cookie called");
			if(WebUtils.getCookie(request, "cartcookie") != null) {
				shopperProfileId = WebUtils.getCookie(request, "cartcookie").getValue();
			}
			logger.info("-------Create cookie called : shopperProfileId :"+shopperProfileId);
			if(StringUtils.isEmpty(shopperProfileId)) {
				logger.info("-------Create cookie");
				shopperProfileId = Klou4RandomUtils.createShopperId();
				jakarta.servlet.http.Cookie shopperCookie = new jakarta.servlet.http.Cookie("cartcookie", shopperProfileId);
				//7 days expiry
				shopperCookie.setMaxAge(7 * 24 * 60 * 60);
				shopperCookie.setSecure(true);
				shopperCookie.setPath("/");
				shopperCookie.setDomain("localhost");
				response.addCookie(shopperCookie);
				logger.info("-------after set create cookie");
			} 
		} catch (Exception e) {
			logger.error("Cookie error occurred"+e.getMessage());
		}
		
		return shopperProfileId;
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
		logger.info("-------CartIdentifier:"+sendCartRequest.getShopperProfileId());
		logger.info("-------CartIdentifier profile:"+sendCartRequest.getCartId());
		ResponseEntity<String> responseEntity = addToCart(sendCartRequest);
		logger.info("-------CartIdentifier responseEntity:"+responseEntity.getBody());
		
		return responseEntity;
	}
	
	private SendCartRequest populateSendCartRequest(CartData cartData, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		SendCartRequest sendCartRequest = new SendCartRequest();
		sendCartRequest.setShopperProfileId(createShopperProfile(response,request));
		
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
			String productString = (String) redisCacheTemplate.opsForValue().get(item.getProductId());
			ProductInfo productInfo = gson.fromJson(productString,ProductInfo.class);
			item.setProductInfo(productInfo);
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
}