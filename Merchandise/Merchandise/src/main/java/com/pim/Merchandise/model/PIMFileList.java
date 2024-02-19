package com.pim.Merchandise.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PIMFileList {

	private String fileName;
	private Date lastModifiedDate;
	private String status;
}
