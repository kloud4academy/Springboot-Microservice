package client;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pim.Merchandise.model.PIMEvent;

import jakarta.annotation.PostConstruct;

@Service
@EnableScheduling
public class PIMSubscriber {
	Logger logger = LoggerFactory.getLogger(PIMSubscriber.class);
    @Value("${kafka.rest.proxy}")
    private String kafkaRestProxyUrl;
    @Value("${listener.consumer.id}")
    private String consumerId;
    @Value("${listener.consumer.group.id}")
    private String consumerGroupId;
    @Value("${listener.auto.offset.reset}")
    private String autoOffsetReset;

    @Value("${topic.name}")
    private String topicName;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    final private MediaType kafkaV2Json = new MediaType("application", "vnd.kafka.v2+json");
    final private MediaType kafkaJsonV2Json = new MediaType("application", "vnd.kafka.json.v2+json");
    
    private String consumerUrl() {
        return kafkaRestProxyUrl + "/consumers/" + consumerGroupId;
    }

    private String consumerInstanceUrl() {
        return consumerUrl() + "/instances/" + consumerId;
    }
    
    private void subscribe() {
        logger.info("Subscribing to " + topicName);
        final Map<String, Object> subscribeParams = Map.<String, Object>of("topics", StringUtils.split(topicName,",")
        );
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.ALL));
        headers.setContentType(kafkaV2Json);
        headers.add("clientId", "10_47xlyeqtmlgkgck0scswkskwwogsw8k04kkkcgs0so0wkcgcoc");
        headers.add("secret", "3sog2bhf7k000wgc008w4gk804s8osw4g40ogk40g4ok4s8cc8");
        headers.add("username", "mongodb1_7701");
        headers.add("password", "e5d762c40");
        final HttpEntity request = new HttpEntity(subscribeParams,headers);
        ResponseEntity response = restTemplate.exchange(consumerInstanceUrl() + "/subscription", HttpMethod.POST, request, Void.class);
        assert response.getStatusCode().equals(HttpStatus.NO_CONTENT) || response.getStatusCode().equals(HttpStatus.OK);
    }
    
    /**
     * Listen on the topic.
     * @throws JsonProcessingException
     */
    @Scheduled(fixedDelayString = "${listener.consumer.idle-between-poll.ms}")
    public void listener() throws JsonProcessingException {
        if((((System.currentTimeMillis() / 1000L) / 60L) % 5) == 0L) {
            logger.info("Listening for events from " + topicName);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(kafkaJsonV2Json));
        
        headers.add("clientId", "10_47xlyeqtmlgkgck0scswkskwwogsw8k04kkkcgs0so0wkcgcoc");
        headers.add("secret", "49u3uf6jgqqsw8g0848k0gwcgkgk84kkosok4sos44w4wg4888");
        headers.add("username", "mongodb1_7701");
        headers.add("password", "e5d762c40");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(headers);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(consumerInstanceUrl() + "/records", HttpMethod.GET, request, new ParameterizedTypeReference<List<Map<String, Object>>>() {
        });
        List<Map<String, Object>> events = response.getBody();
        if (events.size() == 0) {
            // We're doing this because of: https://github.com/confluentinc/kafka-rest/issues/432
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            response = restTemplate.exchange(consumerInstanceUrl() + "/records", HttpMethod.GET, request, new ParameterizedTypeReference<List<Map<String, Object>>>() {
            });
            events = response.getBody();
        }
        // ..now dispatch the events to the actual handler.
        events.forEach(e -> {
            // consume(objectMapper.convertValue(e.get("value"), Event.class));
            try {
                logger.info(e.get("topic") + "\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.get("value")));
            } catch (JsonProcessingException ex) {
                logger.error("Problems reading " + e.get("value").toString(), ex);
            }
        });
    }
    @PostConstruct
    private void initialize() {
        subscribe();
    }
    /**
     * This is the main driver.
     * @param event
     */
    public void consume(PIMEvent event) {
        logger.info("Got event: " + event.toString());
    }

}
