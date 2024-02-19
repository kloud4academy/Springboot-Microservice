package com.pim.Merchandise.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("PIMProductTracker")
public class PIMProductTracker {

	private String fileName;
	private String status;
}
