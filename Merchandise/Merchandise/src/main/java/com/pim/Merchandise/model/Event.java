package com.pim.Merchandise.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

	private String action;
	private String event_id;
	private String event_datetime;
	private String author;
	private String author_type;
	private String product_identifier;
	private String product_uuid;
}
