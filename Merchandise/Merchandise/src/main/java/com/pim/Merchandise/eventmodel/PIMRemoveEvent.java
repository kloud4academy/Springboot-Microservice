package com.pim.Merchandise.eventmodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PIMRemoveEvent {
	
	private String event_subscription_url;
	private int status_code;
	
	
}
