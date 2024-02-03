package bo;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
	private String name;
	private String email;
	@NotEmpty(message = "Review Comment can not be empty!!")
	private String reviewComment;
	private int rating;
	private String productId;
	private String showReview;
}
