package bo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewRequest {

	private String productId;
	private List<Review> reviews;
	private String noOfReviewsToShow;
}
