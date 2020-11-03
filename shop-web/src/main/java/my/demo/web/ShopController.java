package my.demo.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import io.seata.spring.annotation.GlobalTransactional;
import my.demo.entity.Cart;
import my.demo.entity.CartItem;
import my.demo.entity.Item;
import my.demo.entity.Order;
import my.demo.entity.User;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.service.UserService;
import my.demo.utils.MyDemoUtils;

@Controller
@RequestMapping(value="/shop")
public class ShopController {
	Logger log = LoggerFactory.getLogger(getClass());
	
	private static final String[] MOBILE_PREFIXS = { "135", "136", "137", "138", "139", "186", "180", "187", "158" };
	private static final String COOKIE_NAME = "user-id";
	
	private Random random = new Random();
	@Reference(check=false)
	ItemService itemService;
	@Reference(check=false)
	StockService stockService;
	@Reference(check=false)
	UserService userService;
	@Reference(check=false)
	OrderService orderService;
	
	@GetMapping(value = { "" })
	public String home() {
		return "home";
	}
	
	@PostMapping(value="/register")
	public @ResponseBody Object register(HttpServletResponse response, @RequestParam(name="mobile") String mobile, @RequestParam(name="password") String password) {
		ServiceResult<User> r = userService.registerByMobile(mobile, password);
		if(!r.isSuccess()) return "Register failed: " + r.getMessage();
		Cookie cookie = new Cookie(COOKIE_NAME, String.valueOf(r.getResult().getUserId()));
		cookie.setPath("/");
		response.addCookie(cookie); 
		return r.getResult();
	}
	@PostMapping(value="/login")
	public @ResponseBody Object login(HttpServletResponse response, @RequestParam(name="mobile") String mobile, @RequestParam(name="password") String password) {
		ServiceResult<User> r = userService.login(mobile, password);
		if(!r.isSuccess()) return "Login failed: " + r.getMessage();
		Cookie cookie = new Cookie(COOKIE_NAME, String.valueOf(r.getResult().getUserId()));
		cookie.setPath("/");
		response.addCookie(cookie);
		return r.getResult();
	}
	@PostMapping(value="/order/create")
	public @ResponseBody Object createOrder(@CookieValue(name="user-id", required=false, defaultValue="0") long userId) {
		if(userId<=0) return "Please login first";
		ServiceResult<User> userResult = userService.getUser(userId);
		if(!userResult.isSuccess()) return "Get user " + userId + " error: " + userResult.getMessage();
		if(userResult.getResult()==null) return "User not found: " + userId;
		Cart cart = this.addCart(userResult.getResult(), itemService.findItem().getResult());
		return this.createOrder(cart);
	}
	@GetMapping(value="/full-test")
	@GlobalTransactional(timeoutMills = 10000, name = "full-test-case")
	public @ResponseBody TestResult fullTestCase(@RequestParam(name="count", required=false, defaultValue="1") int count) {
		return this.runFullTestCase(count);
	}
	@GetMapping(value="/item/list")
	public @ResponseBody Object findItems() {
		return itemService.findItem().getResult();
	}
	
	private TestResult runFullTestCase(int count) {
		TestResult result = new TestResult();
		while(count-->0) {
			try {
				if(this.runFullTestCase()) {
					result.success();
				} else {
					result.fail();
				}
			} catch(Exception ex) {
				result.fail();
				log.error("TestCase " + count + " failed: " + ex.getMessage(), ex);
			}
		}
		result.stop();
		return result;
	}
	private String randomPassword() {
		char[] chars = { 'a', 'b', 'x', 'y', 'T', 'U' };
		char[] digits = { '1', '2', '3', '7', '8', '9' };
		char[] specialChars = {'@', '!', '#', '$', '%', '&'};
		int ix1 = random.nextInt(chars.length);
		int ix2 = random.nextInt(chars.length);
		return "" + chars[ix1] + digits[ix1] + specialChars[ix1] + chars[ix2] + digits[ix2] + specialChars[ix2];
	}
	private boolean runFullTestCase() {
		if(MyDemoUtils.isSeataPresent()) {
			log.debug("Start a test case, XID: {}", MyDemoUtils.getXID());
		}
		//Register a user
		String prefix = MOBILE_PREFIXS[random.nextInt(MOBILE_PREFIXS.length)];
		String mobile = prefix + String.format("%08d", random.nextInt(99999999));
		String password = this.randomPassword();
		ServiceResult<User> result = userService.registerByMobile(mobile, password);
		if(!result.isSuccess()) {
			log.info("[register] failed, account:{}, msg:{}", mobile, result.getMessage());
			return false;
		}
		log.info("[register] success, account:{}, user-id:{}", mobile, result.getResult().getUserId());
		
		//Login
		result = userService.login(mobile, password);
		if(!result.isSuccess()) {
			log.info("[login] failed, account:{}, msg:{}", mobile, result.getMessage());
			return false;
		}
		log.info("[login] success, account:{} , user-id:{}", mobile, result.getResult().getUserId());
		User user = result.getResult();
		
		//View items
		List<Item> items = itemService.findItem().getResult();
		
		//Add 1 or 2 items to cart
		Cart cart = this.addCart(user, items);
		
		//Create order
		Order order = this.createOrder(cart);
		if(order==null) return false;
		
		//Print order info
		log.debug("[show-order] order:{}, items:{}", order.getOrderId(), order.getOrderItems().size());
		order.getOrderItems().forEach(orderItem -> 
			log.debug("[show-order]     item:{}, qty:{}, amt:{}, discount:{}"
				, orderItem.getItemId(), orderItem.getQuantity(), String.format("%.2f", orderItem.getSubtotal()), String.format("%.2f", orderItem.getDiscount()))
		);
		return true;
	}
	private Order createOrder(Cart cart) {
		ServiceResult<Order> orderResult = orderService.createOrder(cart);
		if(!orderResult.isSuccess()) {
			log.warn("[create-order] failed, msg:{}", orderResult.getMessage());
			return null;
		}
		log.info("[create-order] success, order:{}", orderResult.getResult().getOrderId());
		return orderResult.getResult();
	}
	private Cart addCart(User user, List<Item> items) {
		Cart cart = new Cart(user.getUserId()).saveAddress(user.getNickname(), user.getMobile(), "No. 18, Danleng Street, Haidian District, Beijing City, China");
		List<Item> pickedItems = this.pickupItems(items);
		pickedItems.forEach(item -> {
			cart.addItem(item.getId(), 1, item.getPrice(), Math.random()/10 * item.getPrice());
			CartItem ci = cart.getItems().get(cart.getItems().size()-1);
			log.info("[add-cart] item:{}, qty:{}, price:{}, amt:{}, discount:{}"
				,ci.getItemId(), ci.getQuantity(), String.format("%.2f", ci.getPrice()), String.format("%.2f", ci.getSubtotal()),  String.format("%.2f", ci.getDiscount()));
		});
		return cart;
	}
	private List<Item> pickupItems(List<Item> items) {
		int count = random.nextInt(2) + 1;
		List<Item> result = new ArrayList<>(count);
		Set<Integer> pickedList = new HashSet<>(count); 
		while(count-->0) {
			int index = random.nextInt(items.size()); 
			if(pickedList.contains(index)) continue; 
			result.add(items.get(index));
			pickedList.add(index);
		}
		return result;
	}
}