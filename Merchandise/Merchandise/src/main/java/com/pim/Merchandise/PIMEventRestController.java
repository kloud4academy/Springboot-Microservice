package com.pim.Merchandise;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.pim.Merchandise.PINServiceUtils.PIMEventService;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@Controller
public class PIMEventRestController {
	Logger logger = LoggerFactory.getLogger(PIMEventRestController.class);
    @Autowired
    private Gson gson;
   // @Autowired
    //private KafkaTemplate<String, String> kafkaTemplate;
      
    @Autowired
    private PIMEventService pimeventService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping("/cart-ms/pimupdateevent")
    public ModelAndView pimUpdateEvent(HttpServletRequest request) {
    	ModelAndView mv = new ModelAndView();
        try {
			byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
			String responseString = new String(requestBody, StandardCharsets.UTF_8);
			logger.info("------Received updated event" + responseString);
			pimeventService.processPIMUpdateEvent(responseString);
		} catch (Exception e) {
			e.printStackTrace();
		}
        mv.setViewName("catalogsynch");
        return mv;
    }
    
}
