package com.kloud4.kloud4academyHome.ClientManager;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisSSLConfiguration {

	 // @Value("${spring.data.redis.host}")
	  private String host;

	 // @Value("${spring.data.redis.port}")
	  private int port;

	  //@Value("${spring.data.redis.password}")
	  private String password;

	  //@Value("${spring.redis.ssl:false}")
	  private boolean sslEnabled;

	  private final ResourceLoader resourceLoader;
	  
	  @Bean
	  RedisConnectionFactory redisConnectionFactory() throws IOException {
	    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
	    redisStandaloneConfiguration.setHostName("redis-10683.c212.ap-south-1-1.ec2.cloud.redislabs.com");
	    redisStandaloneConfiguration.setPort(10683);
	    redisStandaloneConfiguration.setPassword("vjwr2v7EJkm4VH8ZY87659tCJyHVGbjp");
	    redisStandaloneConfiguration.setUsername("default");
	    //redisStandaloneConfiguration.setDatabase(Arivalagan-free-db);

	    LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder =
	        LettuceClientConfiguration.builder();

	    if (sslEnabled){
	      SslOptions sslOptions = SslOptions.builder()
	          .trustManager(resourceLoader.getResource("classpath:redis.pem").getFile())
	          .build();

	      ClientOptions clientOptions = ClientOptions
	          .builder()
	          .sslOptions(sslOptions)
	          .protocolVersion(ProtocolVersion.RESP3)
	          .build();

	      lettuceClientConfigurationBuilder
	          .clientOptions(clientOptions)
	          .useSsl();
	    }

	    LettuceClientConfiguration lettuceClientConfiguration = lettuceClientConfigurationBuilder.build();

	    return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
	  }
}
