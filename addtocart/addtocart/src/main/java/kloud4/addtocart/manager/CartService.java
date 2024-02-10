package kloud4.addtocart.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;
import kloud4.addtocart.model.CartRequest;
import kloud4.addtocart.model.Item;
import kloud4.addtocart.model.ShoppingCart;
import kloud4.addtocart.model.ShoppingCartCount;
import kloud4.addtocart.repository.CartRepository;
import kloud4.addtocart.utils.Klou4RandomUtils;

@Service
public class CartService {
	Logger logger = LoggerFactory.getLogger(CartService.class);
	@Autowired
	private MongoOperations mongoOperations;
	@Autowired
	CartRepository cartRepository;
	
	public ShoppingCart createCart(CartRequest cartRequest) {
		ShoppingCart cart = populateCreateCart(cartRequest);;
		return cart;
	}
	
	private ShoppingCart populateCreateCart(CartRequest cartRequest) {
		String cartId = Klou4RandomUtils.createCartId();
		logger.debug("----------Klou4RandomUtils.createCartId()");
		ShoppingCart cart = new ShoppingCart();
		cart.setCartId(cartId);
		Item item = new Item();
		item.setDiscountPrice(0.0);
		item.setToalQuantity(Integer.parseInt(cartRequest.getQuantity()));
		item.setPrice(Double.parseDouble(cartRequest.getPrice()) * item.getToalQuantity());
		item.setProductId(cartRequest.getProductId());
		List<Item> items =new ArrayList<Item>();
		items.add(item);
		cart.setItems(items);
		cart.setOrderDiscount(0.0);
		if(StringUtils.isBlank(cartRequest.getShopperProfileId())) {
			cart.setShopperProfileId(Klou4RandomUtils.createShopperId());
		} else {
			cart.setShopperProfileId(cartRequest.getShopperProfileId());
		}
		repricingOrder(cart);
		cart.setOrderTotal(cart.getOrderTotal());
		
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		cart.setUpdateDate(now);
		mongoOperations.save(cart);
		return cart;
	}
	
	private void repricingOrder(ShoppingCart cart) {
		double itemsTotal = 0.0;
		double itemDiscountTotal = 0.0;
		double shippingTotal = 0.0;
		double orderDiscount = 0.0;
		double discountTotal = 0.0;
		
		for (Object itemObj : cart.getItems()) {
			Item item = (Item) itemObj;
			itemsTotal+=item.getPrice();
			itemDiscountTotal+=item.getDiscountPrice();
		}
		shippingTotal = calculateShippingPrice();
		orderDiscount = cart.getOrderDiscount();
		discountTotal = itemDiscountTotal + orderDiscount;
		double orderTotal = itemsTotal + shippingTotal -  discountTotal;
		logger.info("-------Final order total :" +orderTotal);
		cart.setOrderTotal(orderTotal);
	}
	
	private double calculateShippingPrice() {
		return 0.0;
	}
	
	public void populateCart(ShoppingCart cart, CartRequest cartRequest) {
		logger.info("----------populateCart method ");
		List<Item> cartItems = cart.getItems();
		if(cartItems != null) {
			double itemTotalPrice = 0.0;
			boolean itemmatch = false;
			for (Object item : cartItems) {
				Item itemObej = (Item) item;
				if(itemObej.getProductId().equalsIgnoreCase(cartRequest.getProductId())) {
					itemTotalPrice = Double.parseDouble(cartRequest.getPrice()) * Integer.parseInt(cartRequest.getQuantity());
					itemObej.setPrice(itemTotalPrice);
					itemObej.setToalQuantity(Integer.parseInt(cartRequest.getQuantity()));
					itemmatch = true;
					logger.info("-----Final item price" + itemObej.getPrice());
					break;
				}
			}
			if(!itemmatch) {
				logger.info("---------Added as new item into cart ");
				Item newItem = new Item();
				newItem.setDiscountPrice(0.0);
				double totalItemPrice = Double.parseDouble(cartRequest.getPrice()) * Integer.parseInt(cartRequest.getQuantity());
				newItem.setPrice(totalItemPrice);
				newItem.setProductId(cartRequest.getProductId());
				newItem.setToalQuantity(Integer.parseInt(cartRequest.getQuantity()));
				if(cartItems != null) {
					cartItems.add(newItem);
				}
			}
			
		}
		
		
		repricingOrder(cart);
		
	}
	
	public ShoppingCart updateCart(ShoppingCart shoppingCart, CartRequest cartRequest) {
		logger.info("-------update cart is getting called-----");
		populateCart(shoppingCart, cartRequest);
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		shoppingCart.setUpdateDate(now);
		mongoOperations.findAndReplace(Query.query(Criteria.where("cartId").is(shoppingCart.getCartId())), shoppingCart);
		return shoppingCart;
	}
	
	public String deleteCart(ShoppingCart shoppingCart, CartRequest cartRequest) {
		populateDeleteCart(shoppingCart, cartRequest);
		List<Item> itemsList = shoppingCart.getItems();
		for(Object item : itemsList) {
			Item itemobj = (Item) item;
		}
		mongoOperations.findAndReplace(Query.query(Criteria.where("cartId").is(shoppingCart.getCartId())), shoppingCart);
		return "deleted cart item";
	}
	
	public void populateDeleteCart(ShoppingCart cart, CartRequest cartRequest) {
		logger.info("----------populateDeleteCart method ");
		List<Item> cartItems = cart.getItems();
		boolean itemFound = false;
		if(cartItems != null) {
			Item itemObej = null;
			for (Object item : cartItems) {
				itemObej = (Item) item;
				if(itemObej.getProductId().equalsIgnoreCase(cartRequest.getProductId())) {
					itemFound = true;
					break;
				}
			}
			if(itemFound) {
				cart.getItems().remove(itemObej);
			}
		}
		
		repricingOrder(cart);
	}
	
	public ShoppingCart viewCart(String cartId) {
		return loadCartByCartId(cartId);
	}
	
	public ShoppingCart loadCart(CartRequest cartRequest) {
		Query query = new Query();
		List<Criteria> criteria = new ArrayList<>();
		criteria.add(Criteria.where("shopperProfileId").is(cartRequest.getShopperProfileId()));
		criteria.add(Criteria.where("cartId").is(cartRequest.getCartId()));

		query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
		
		ShoppingCart shoppingCart = mongoOperations.findOne(query, ShoppingCart.class,"cartDetails");
		
		logger.info("---------cart has been loadded...." + shoppingCart);
		return shoppingCart;
	}
	
	public ShoppingCart loadCartByProfileId(String profileId) {
		Query query = new Query().addCriteria(Criteria.where("shopperProfileId").is(profileId));
		ShoppingCart shoppingCart = mongoOperations.findOne(query, ShoppingCart.class,"cartDetails");
		
		logger.info("---------cart has been loadded...." + shoppingCart);
		return shoppingCart;
	}
	
	public ShoppingCart loadCartByCartId(String cartId) {
		Query query = new Query().addCriteria(Criteria.where("cartId").is(cartId));
		ShoppingCart shoppingCart = mongoOperations.findOne(query, ShoppingCart.class,"cartDetails");
		logger.info("---------shopper cart has been loadded by shopperId...." + shoppingCart);
		return shoppingCart;
	}
	
	public ShoppingCart addtoCart(CartRequest cartRequest) {
		ShoppingCart shoppingCart = null;
		logger.info("----addtocart service called-----"+cartRequest.getShopperProfileId());
		if(cartRequest != null && StringUtils.isNotBlank(cartRequest.getShopperProfileId()) && StringUtils.isNotBlank(cartRequest.getCartId()))  {
			logger.info("----addtocart ProfileId and cartid is there-----");
			shoppingCart = loadCartByCartId(cartRequest.getCartId());
			updateCart(shoppingCart, cartRequest);
		} else if(StringUtils.isNotBlank(cartRequest.getShopperProfileId()) && StringUtils.isBlank(cartRequest.getCartId())) {
			logger.info("----addtocart ProfileId only is there-----");
			logger.info("----create cart and keep in session first time....");
			shoppingCart = loadCartByProfileId(cartRequest.getShopperProfileId());
			if(shoppingCart != null) {
				updateCart(shoppingCart, cartRequest);
			} else {
				shoppingCart = createCart(cartRequest);
			}
			logger.info("------------Cart Jsessionid cookie: ");
		}
		return shoppingCart;
	}
	
	public ShoppingCartCount populateShoppingCartCount(ShoppingCart shoppingCart) {
		ShoppingCartCount shoppingCartCount = new ShoppingCartCount();
		shoppingCartCount.setCartId(shoppingCart.getCartId());
		if(shoppingCart != null && shoppingCart.getItems() != null) {
			shoppingCartCount.setCartSize(String.valueOf(shoppingCart.getItems().size()));
		}
		
		return shoppingCartCount;
	}
}
