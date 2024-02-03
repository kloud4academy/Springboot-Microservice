package bo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

	private String cartId;
	private List<ItemDetail> items;
	private double orderDiscount;
	private double orderTotal;
	private String orderType;
}