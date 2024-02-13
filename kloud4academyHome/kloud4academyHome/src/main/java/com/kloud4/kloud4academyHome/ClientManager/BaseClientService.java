package com.kloud4.kloud4academyHome.ClientManager;

import java.security.cert.X509Certificate;

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
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BaseClientService {

	Logger logger = LoggerFactory.getLogger(BaseClientService.class);
	
	public String createOrGetShopperProfile(HttpServletResponse response, HttpServletRequest request) {
		String shopperProfileId = "";
		try {
			if(WebUtils.getCookie(request, "cartcookie") != null) {
				shopperProfileId = WebUtils.getCookie(request, "cartcookie").getValue();
			}
			if(StringUtils.isEmpty(shopperProfileId)) {
				shopperProfileId = Klou4RandomUtils.createShopperId();
				jakarta.servlet.http.Cookie shopperCookie = new jakarta.servlet.http.Cookie("cartcookie", shopperProfileId);
				//7 days expiry
				shopperCookie.setMaxAge(7 * 24 * 60 * 60);
				shopperCookie.setSecure(true);
				shopperCookie.setPath("/");
				shopperCookie.setDomain("localhost");
				response.addCookie(shopperCookie);
			} 
		} catch (Exception e) {
			logger.error("Cookie error occurred"+e.getMessage());
		}
		
		return shopperProfileId;
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
