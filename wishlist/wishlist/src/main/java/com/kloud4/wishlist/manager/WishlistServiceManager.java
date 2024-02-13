package com.kloud4.wishlist.manager;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kloud4.wishlist.model.CartRequest;
import com.kloud4.wishlist.model.WishListBean;
import com.mongodb.client.result.UpdateResult;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class WishlistServiceManager {

	Logger logger = LoggerFactory.getLogger(WishlistServiceManager.class);
	
	@Autowired
	private MongoOperations mongoOperations;
	@Autowired
	private DiscoveryClient discoveryClient;
	
	public String creatOrUpdateWishlist(WishListBean wishListBean) throws Exception {
		Query query = new Query().addCriteria(Criteria.where("wishListId").is(wishListBean.getWishListId()));
		WishListBean wishList = mongoOperations.findOne(query, WishListBean.class);
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		if(wishList != null) {
			Set<String> wishlistProducts = wishList.getProductIdList();
			if(wishlistProducts != null && wishlistProducts.size() > 0) {
				logger.info("------wishListBean.getProductIdList().stream().findFirst().get()-:"+wishListBean.getProductIdList().stream().findFirst().get());
				wishlistProducts.add(wishListBean.getProductIdList().stream().findFirst().get());
			} else {
				wishlistProducts = new HashSet<String>();
				logger.info("------wishListBean.getProductIdList().stream().findFirst().get()-:"+wishListBean.getProductIdList().stream().findFirst().get());
				wishlistProducts.add(wishListBean.getProductIdList().stream().findFirst().get());
			}
			
			Update updateDefinition = new Update().set("productIdList", wishlistProducts);
			UpdateResult updateResult = mongoOperations.upsert(query, updateDefinition, WishListBean.class);
		} else {
			WishListBean wishListDoc = new WishListBean();
			wishListDoc.setWishListId(wishListBean.getWishListId());
			
			Set<String> wishlistProducts = new HashSet<String>();
			wishlistProducts.add(wishListBean.getProductIdList().stream().findFirst().get());
			wishListDoc.setProductIdList(wishlistProducts);
			wishListDoc.setUpdatedDate(now);
			mongoOperations.insert(wishListDoc);
		}
		deleteCart(wishListBean.getProductIdList().stream().findFirst().get(), wishListBean.getCartId());
		
		return "Item moved success";
	}
	
	public String deleteWishlist(WishListBean wishListBean) {
		Query query = new Query().addCriteria(Criteria.where("wishListId").is(wishListBean.getWishListId()));
		WishListBean wishList = mongoOperations.findOne(query, WishListBean.class);
		if(wishList != null) {
			Set<String> wishlistProducts = wishList.getProductIdList();
			if(wishlistProducts != null && wishlistProducts.size() == 1) {
				mongoOperations.remove(query, WishListBean.class);
				return "Deleted and no items in wishlist";
			}
			if(wishlistProducts != null && wishlistProducts.size() > 0) {
				wishlistProducts.remove(wishListBean.getProductIdList().stream().findFirst().get());
			} else {
				return "No wishlist item to delete";
			}
			
			Update updateDefinition = new Update().set("productIdList", wishlistProducts);
			UpdateResult updateResult = mongoOperations.upsert(query, updateDefinition, WishListBean.class);
		} else {
			return "No wishlist Id to delete";
		}
		return "Item delete success";
	}
	
	public WishListBean viewWishlist(WishListBean wishListBean) {
		Query query = new Query().addCriteria(Criteria.where("wishListId").is(wishListBean.getWishListId()));
		WishListBean wishList = mongoOperations.findOne(query, WishListBean.class);
		return wishList;
	}
	
	@CircuitBreaker(name="kloud4breaker",fallbackMethod="fallbackDeleteCartAPIError")
	@Retry(name="kloud4breaker")
	public ResponseEntity<String> deleteCart(String productId, String cartId) throws Exception {
		CartRequest sendCartRequest = new CartRequest();
		sendCartRequest.setProductId(productId);
		sendCartRequest.setCartId(cartId);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    HttpEntity<CartRequest> entity = new  HttpEntity<CartRequest>(sendCartRequest,headers);
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
}
