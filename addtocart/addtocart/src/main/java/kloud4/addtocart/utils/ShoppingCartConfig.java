package kloud4.addtocart.utils;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
@Configuration
//@Configuration(proxyBeanMethods = false)
//@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 1000,collectionName="cartDetails")
public class ShoppingCartConfig {
	@Value("${spring.data.mongodb.uri}")
	private String dbUrl;
	@Value("${spring.data.mongodb.database}")
	private String databaseName;
	
	//@Bean
    public MongoOperations mongoOperations() throws UnknownHostException {
		MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(dbUrl)).applyToSocketSettings(builder ->builder.connectTimeout(5, TimeUnit.SECONDS)).build());
        return new MongoTemplate(mongoClient, databaseName);
    }
}
