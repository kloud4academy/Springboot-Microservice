package model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("productReviews")
public class ProductReviews {

	private String productId;
	private List<Review> reviews;
	private String noOfReviewsToShow;
}
