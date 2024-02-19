package client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

//@CrossOrigin
@Controller
public class PIMProducerRestController {

//	@Value("${kafka.topicName}")
//    private String topicName;
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @RequestMapping(path="/cart-ms/kloud4event", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(value = HttpStatus.NO_CONTENT)
//    @ResponseBody
//    public ResponseEntity sendEvent(@RequestBody String cartRequest) {
//    	//@RequestBody String cartRequest
//        ListenableFuture<org.springframework.kafka.support.SendResult<String, String>> future;
//		try {
//			future = (ListenableFuture<org.springframework.kafka.support.SendResult<String, String>>) kafkaTemplate.send(topicName, objectMapper.writeValueAsString(cartRequest));
//			org.springframework.kafka.support.SendResult<String, String> result = future.get();
//	        return new ResponseEntity(Map.of("result", result.toString()), HttpStatus.OK);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return null;
//    }
    
    @RequestMapping("/cart-ms/kloudevent")
	@ResponseBody
	public String fetchProduct() {
		return "Workind da";
	}
}
