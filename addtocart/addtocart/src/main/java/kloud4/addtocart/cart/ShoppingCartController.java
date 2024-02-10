package kloud4.addtocart.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpSession;
import kloud4.addtocart.model.CartRequest;
import kloud4.addtocart.model.ShoppingCart;
import kloud4.addtocart.model.ShoppingCartCount;

@Controller
public class ShoppingCartController {
	Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);
	
	@Autowired
	Gson gson;
	@Autowired
	private kloud4.addtocart.manager.CartService cartService;
	
	@RequestMapping(path="/cart-ms/shopping/addToCart",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.POST)
	@ResponseBody
    public String createCart(@RequestBody CartRequest cartRequest) {
		logger.info("-------Shoppingcart controller");
		ShoppingCart shoppingCart = cartService.addtoCart(cartRequest);
        return shoppingCart.getCartId();
    }
	
	@RequestMapping("/cart-ms/shopping/updateCart")
	@ResponseBody
    public String updateCart(@RequestBody CartRequest cartRequest) {
		ShoppingCart shoppingCart = cartService.loadCartByCartId(cartRequest.getCartId());
		cartService.updateCart(shoppingCart, cartRequest);
		return gson.toJson(cartService.loadCartByCartId(cartRequest.getCartId()));
    }
	
	@RequestMapping("/cart-ms/shopping/deleteCart")
	@ResponseBody
    public String deleteCart(@RequestBody CartRequest cartRequest) {
		ShoppingCart shoppingCart = cartService.loadCartByCartId(cartRequest.getCartId());
		cartService.deleteCart(shoppingCart, cartRequest);
		ShoppingCart updateshoppingCart = cartService.loadCartByCartId(cartRequest.getCartId());
		if(updateshoppingCart != null && updateshoppingCart.getItems() != null && updateshoppingCart.getItems().size() > 0) {
			return gson.toJson(updateshoppingCart);
		}
        
		return "cart has been deleted";
    }
	
	@RequestMapping(path="/cart-ms/shopping/viewCart/{cartId}",method = RequestMethod.GET)
	@ResponseBody
	public String viewCart(@PathVariable String cartId) {
		ShoppingCart shoppingCart = cartService.viewCart(cartId);
		logger.info("-----------VIEW shopping cart");
        return gson.toJson(shoppingCart);
    }
	
	@RequestMapping(path="/cart-ms/shopping/loadcartbyshopperid/{shopperId}",method = RequestMethod.GET)
	@ResponseBody
	public String loadcartbyshopperid(@PathVariable String shopperId) {
		ShoppingCart shoppingCart = cartService.loadCartByProfileId(shopperId);
		ShoppingCartCount shoppingCartCount = cartService.populateShoppingCartCount(shoppingCart);
        return gson.toJson(shoppingCartCount);
    }
}
