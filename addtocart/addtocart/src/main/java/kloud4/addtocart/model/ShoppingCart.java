package kloud4.addtocart.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("cartDetails")
public class ShoppingCart implements Serializable {

	//private static final long serialVersionUID = 1000000222L;
	private String cartId;
	private List<Item> items;
	private double orderDiscount;
	private double orderTotal;
	private String orderType;
	private String shopperProfileId;
	private Date updateDate;
	
}
