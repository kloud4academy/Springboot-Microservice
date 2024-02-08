package klou4search.search.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import klou4search.search.manager.ElasticSearchService;

@Controller
public class SearchController {
	Logger logger = LoggerFactory.getLogger(SearchController.class);
	@Autowired
	private ElasticSearchService elasticSearchService;
	@Autowired
	private Gson gson;
	
	@RequestMapping(path="/search-ms/shopping/searchproduct",method = RequestMethod.POST)
	@ResponseBody
    public String searchProduct(@RequestBody String searchString) {
		String productListResponse = gson.toJson(elasticSearchService.searchProducts(searchString));
		if(productListResponse != null) {
			return productListResponse;
		}
		
		return "There are no products found";
    }
}
