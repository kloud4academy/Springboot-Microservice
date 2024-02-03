package model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("review")
public class Review {

	private String name;
	private String email;
	private String reviewComment;
	private int rating;
	private String showReview;
}
