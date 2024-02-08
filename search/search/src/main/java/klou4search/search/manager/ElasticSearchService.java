package klou4search.search.manager;

import java.util.ArrayList;
import java.util.List;

//import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import klou4search.search.model.Product;
import klou4search.search.model.ProductSearchRequest;

@Service
public class ElasticSearchService {
	Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);
	@Autowired
	ElasticsearchOperations elasticSearchOperations;
	@Autowired
	ElasticsearchClient elasticSearchClient;
	public List<Product> searchProducts(String searchString) {
		
		List<org.springframework.data.elasticsearch.core.SearchHit<Product>> productList = null;
		if(searchString != null) {
			List<String >fields = List.of("productName", "productDesc","sizes","colors","category"); 
			Builder queryBuilder = new MultiMatchQuery.Builder();
			queryBuilder.fields(fields);
			queryBuilder.operator(Operator.And);
			queryBuilder.type(TextQueryType.CrossFields);
			queryBuilder.query(searchString);
			NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder().withQuery(queryBuilder.build()._toQuery());
			NativeQuery nativeQuery = new NativeQuery(nativeQueryBuilder);
			try {
				SearchHits<Product> searchHits = elasticSearchOperations.search(nativeQuery, Product.class);
				if(searchHits != null && !searchHits.isEmpty()) {
					List<Product> productMatchsList = new ArrayList<Product>();
					searchHits.forEach(productSearchHit->{
						productMatchsList.add(productSearchHit.getContent());
					});
					return productMatchsList;
				}
			} catch (Exception e) {
				logger.error("super errror"+e.getMessage());
			}
		}
		return null;
	}
}
