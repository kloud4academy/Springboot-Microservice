package com.pim.Merchandise.config;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

//@Configuration
public class RestClientConfig {
	
	@Value("${restclient.connect.timeout.ms}")
    private int connectTimeout;
    @Value("${restclient.connection-request.timeout.ms}")
    private int connectionRequestTimeout;
    @Value("${restclient.read.timeout.ms}")
    private int connectionReadTimeout;

    
  //  @Bean
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
