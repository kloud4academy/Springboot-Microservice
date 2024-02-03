package com.kloud4.kloud4academyHome.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjaxResponseBody {
	String msg;
    List<CartData> result;
}
