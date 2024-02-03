package bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
	private String name;
	private String email;
	private String reviewComment;
	private String rating;
	private String productId;
	private String showReview;
}
