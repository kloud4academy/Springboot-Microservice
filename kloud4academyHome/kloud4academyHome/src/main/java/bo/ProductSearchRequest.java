package bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

	private double priceFrom;
    private double priceTo;
	private String sortByString;
	private String sortType;
}
