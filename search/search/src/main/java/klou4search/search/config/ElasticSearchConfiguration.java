package klou4search.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticSearchConfiguration extends ElasticsearchConfigurationSupport {	
	@Value("${spring.elasticsearch.url}")
	private String elasticsearchUrl;
	
    @Bean
    public RestClient getRestClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "kzDNZQCbvT0Pazi2MaH6LdJU"));
        
        return RestClient.builder(new HttpHost("d3c2ba999788403b96c86839090802cf.us-central1.gcp.cloud.es.io", 443,"https"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }).build();
    }

    @Bean
    public ElasticsearchTransport getElasticsearchTransport() {
    	RestClientTransport restClientTransport = new RestClientTransport(getRestClient(), new JacksonJsonpMapper(mapper));
    	return restClientTransport;
    }

    @Bean
    public ElasticsearchClient getElasticsearchClient() {
    	ElasticsearchClient elasticsearchClient = new ElasticsearchClient(getElasticsearchTransport());
    	return elasticsearchClient;
    }
    SimpleSerializers serializers = new SimpleSerializers();
    SimpleDeserializers deserializers = new SimpleDeserializers();
    
	public final static ObjectMapper mapper = new ObjectMapper()
	        .registerModule(new JavaTimeModule())
	        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
	        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
	        .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
	        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
	        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	        
}
