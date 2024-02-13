package bo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishListItemBean {

	private List<ProductInfo> productInfoList;
	private String totalQuantity;
}
