package com.pim.Merchandise.PINServiceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pim.Merchandise.model.Product;

@Service
public class PIMEventService {
	Logger logger = LoggerFactory.getLogger(PIMEventService.class);
	
	@Autowired
	private MongoTemplate mongoDbTemplate;
	@Autowired
    private ObjectMapper objectMapper;
	
	public void processPIMUpdateEvent(String updatedEventString) throws Exception {
		JsonNode jsonNode = objectMapper.readTree(updatedEventString);
		ArrayNode eventArrayNode = jsonNode.withArray("events");
		JsonNode eventNode = eventArrayNode.numberNode(0);
		for (int i = 0; i < eventArrayNode.size(); i++) {
			String category = eventArrayNode.get(i).get("data").get("resource").get("family").toString().replaceAll("\"", "");
			Product product = new Product();
			product.setCategory(category);
			product = populateCommonEventAttributes(i, eventArrayNode, product);
			if("clothing".equalsIgnoreCase(category)) {
				product = processClothCategoryUpdateEvent(i, eventArrayNode, product);
			} else if("Bags".equalsIgnoreCase(category)) {
				product = processBagsCategoryUpdateEvent(i, eventArrayNode, product);
				
	    	}  else if("LED TVs".equalsIgnoreCase(category)) {
	    		product = processTvsCategoryUpdateEvent(i, eventArrayNode, product);
	    	}
			updateToDB(product);
		}
		
	}
	
	private void updateToDB(Product product) {
		Query query = new Query().addCriteria(Criteria.where("productId").is(product.getProductId()));
		//Update updateDefinition = new Update().set("product", product);
		//mongoDbTemplate.upsert(query, updateDefinition, Product.class);
		mongoDbTemplate.findAndReplace(query, product);
		logger.info("--------Product Event Has been Updated success");
	}
	
	private Product populateCommonEventAttributes(int i, ArrayNode eventArrayNode, Product product) {
		String action = eventArrayNode.get(i).get("action").toString();
		product.setEventAction(action);
		String uuId = eventArrayNode.get(i).get("data").get("resource").get("uuid").toString().replaceAll("\"", "");
		product.setUuId(uuId);
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("color")) {
			String color = eventArrayNode.get(i).get("data").get("resource").get("values").get("color").get(i).get("data").toString().replaceAll("\"", "");
			product.setColors(color);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("price")) {
			String price = eventArrayNode.get(i).get("data").get("resource").get("values").get("price").get(i).get("data").get(i).get("amount").toString().replaceAll("\"", "");
			product.setPrice(price);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("image_1")) {
			String imageUrl = eventArrayNode.get(i).get("data").get("resource").get("values").get("image_1").get(i).get("data").toString().replaceAll("\"", "");
			product.setImageUrl(imageUrl);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("image_2")) {
			String image2 = eventArrayNode.get(i).get("data").get("resource").get("values").get("image_2").get(i).get("data").toString().replaceAll("\"", "");
			product.setImage(image2);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("sku")) {
			String productId = eventArrayNode.get(i).get("data").get("resource").get("values").get("sku").get(i).get("data").toString().replaceAll("\"", "");
			product.setProductId(productId);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("name")) {
			String productName = eventArrayNode.get(i).get("data").get("resource").get("values").get("name").get(i).get("data").toString().replaceAll("\"", "");
			product.setProductName(productName);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("erp_name")) {
			String erpName = eventArrayNode.get(i).get("data").get("resource").get("values").get("erp_name").get(i).get("data").toString().replaceAll("\"", "");
			product.setErpName(erpName);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("description")) {
			String description = eventArrayNode.get(i).get("data").get("resource").get("values").get("description").get(i).get("data").toString().replaceAll("\"", "");
			product.setLongDescription(description);
		}
		return product;
	}
	
	private Product processClothCategoryUpdateEvent(int i, ArrayNode eventArrayNode, Product product) {
		logger.info("------------called processClothCategoryUpdateEvent:::");
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("made_in")) {
			String madein = eventArrayNode.get(i).get("data").get("resource").get("values").get("made_in").get(i).get("data").toString().replaceAll("\"", "");
			product.setMadein(madein);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("material")) {
			String material = eventArrayNode.get(i).get("data").get("resource").get("values").get("material").get(i).get("data").toString().replaceAll("\"", "");
			product.setMaterial(material);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("gender_type")) {
			String gender_type = eventArrayNode.get(i).get("data").get("resource").get("values").get("gender_type").get(i).get("data").toString().replaceAll("\"", "");
			product.setGenderType(gender_type);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("clothing_size")) {
			String size = eventArrayNode.get(i).get("data").get("resource").get("values").get("clothing_size").get(i).get("data").toString().replaceAll("\"", "");
			logger.info("------------Here is the Size" + size);
			product.setSize(size);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("color_pattern")) {
			String pattern = eventArrayNode.get(i).get("data").get("resource").get("values").get("color_pattern").get(i).get("data").toString().replaceAll("\"", "");
			product.setPattern(pattern);
		}	
		
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("style_name")) {
			String styleName = eventArrayNode.get(i).get("data").get("resource").get("values").get("style_name").get(i).get("data").toString().replaceAll("\"", "");
			product.setStyleName(styleName);
		}
		return product;
	}
	
	private Product processBagsCategoryUpdateEvent(int i, ArrayNode eventArrayNode, Product product) {
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("gender_type")) {
			String gender_type = eventArrayNode.get(i).get("data").get("resource").get("values").get("gender_type").get(i).get("data").toString().replaceAll("\"", "");
			product.setGenderType(gender_type);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("clothing_size")) {
			String size = eventArrayNode.get(i).get("data").get("resource").get("values").get("clothing_size").get(i).get("data").toString().replaceAll("\"", "");
			product.setSize(size);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("color_pattern")) {
			String pattern = eventArrayNode.get(i).get("data").get("resource").get("values").get("color_pattern").get(i).get("data").toString().replaceAll("\"", "");
			product.setPattern(pattern);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("style_name")) {
			String styleName = eventArrayNode.get(i).get("data").get("resource").get("values").get("style_name").get(i).get("data").toString().replaceAll("\"", "");
			product.setStyleName(styleName);
		}
		return product;
	}
	
	private Product processTvsCategoryUpdateEvent(int i, ArrayNode eventArrayNode, Product product) {
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("made_in")) {
			String madein = eventArrayNode.get(i).get("data").get("resource").get("values").get("made_in").get(i).get("data").toString().replaceAll("\"", "");
			product.setMadein(madein);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("material")) {
			String material = eventArrayNode.get(i).get("data").get("resource").get("values").get("material").get(i).get("data").toString().replaceAll("\"", "");
			product.setMaterial(material);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("gender_type")) {
			String gender_type = eventArrayNode.get(i).get("data").get("resource").get("values").get("gender_type").get(i).get("data").toString().replaceAll("\"", "");
			product.setGenderType(gender_type);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("clothing_size")) {
			String size = eventArrayNode.get(i).get("data").get("resource").get("values").get("clothing_size").get(i).get("data").toString().replaceAll("\"", "");
			product.setSize(size);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("color_pattern")) {
			String pattern = eventArrayNode.get(i).get("data").get("resource").get("values").get("color_pattern").get(i).get("data").toString().replaceAll("\"", "");
			product.setPattern(pattern);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("features")) {
			String features = eventArrayNode.get(i).get("data").get("resource").get("values").get("features").get(i).get("data").toString().replaceAll("\"", "");
			product.setFeatures(features);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("brand")) {
			String brand = eventArrayNode.get(i).get("data").get("resource").get("values").get("brand").get(i).get("data").toString().replaceAll("\"", "");
			product.setBrand(brand);
		}	
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("built_in_speakers")) {
			String buildinspeakers = eventArrayNode.get(i).get("data").get("resource").get("values").get("built_in_speakers").get(i).get("data").toString().replaceAll("\"", "");
			product.setBuiltinSpeakers(buildinspeakers);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("bluetooth_enabled")) {
			String bluetooth_enabled = eventArrayNode.get(i).get("data").get("resource").get("values").get("bluetooth_enabled").get(i).get("data").toString().replaceAll("\"", "");
			product.setBluetoothEnabled(bluetooth_enabled);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("model_number")) {
			String model_number = eventArrayNode.get(i).get("data").get("resource").get("values").get("model_number").get(i).get("data").toString().replaceAll("\"", "");
			product.setModelNo(model_number);
		}
		if(eventArrayNode.get(i).get("data").get("resource").get("values").has("model_number")) {
			String model_number = eventArrayNode.get(i).get("data").get("resource").get("values").get("model_number").get(i).get("data").toString().replaceAll("\"", "");
			product.setModelNo(model_number);
		}
		//productData.setInch(productArry[140]);
		return product;
	}
}
