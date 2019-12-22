package my.demo.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import my.demo.domain.Cart;
import my.demo.domain.CartItem;
import my.demo.domain.Item;
import my.demo.domain.Order;
import my.demo.domain.User;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.service.UserService;

@Controller
@RequestMapping(value="/shop")
public class ShopController {
	Logger log = LoggerFactory.getLogger(getClass());
	
	private static String[] MOBILE_PREFIXS = { "135", "136", "137", "138", "139", "186", "180", "187", "158" };
	private static String COOKIE_NAME = "user-id";
	
	@Reference
	ItemService itemService;
	@Reference
	StockService stockService;
	@Reference
	UserService userService;
	@Reference
	OrderService orderService;
	
	@GetMapping(value = { "" })
	public String home() {
		return "home";
	}
	
	@PostMapping(value="/register") //简单起见使用GET请求
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
	private boolean runFullTestCase() {
		log.debug("Start a test case");
		//随机注册一个用户
		String prefix = MOBILE_PREFIXS[(int)Math.round(Math.random()*1000) % MOBILE_PREFIXS.length];
		String mobile = prefix + String.format("%08d", Math.round(Math.random()*100000000));
		String password = Math.round(Math.random() * 10000000) + "";
		ServiceResult<User> result = userService.registerByMobile(mobile, password);
		if(!result.isSuccess()) {
			log.info("[register] failed, account:" + mobile + ", msg:" + result.getMessage());
			return false;
		}
		log.info("[register] success, account:" + mobile + ", user-id:" + result.getResult().getUserId());
		
		//登录
		result = userService.login(mobile, password);
		if(!result.isSuccess()) {
			log.info("[login] failed, account:" + mobile + ", msg:" + result.getMessage());
			return false;
		}
		log.info("[login] success, account:" + mobile + " , user-id:" + result.getResult().getUserId());
		User user = result.getResult();
		
		//浏览商品
		List<Item> items = itemService.findItem().getResult();
		
		//随机选择1-2个添加购物车
		Cart cart = this.addCart(user, items);
		
		//下单
		Order order = this.createOrder(cart);
		
		//打印
		log.debug("[show-order] order:" + order.getOrderId() + ", items:" + order.getOrderItems().size());
		order.getOrderItems().forEach(orderItem -> {
			log.debug("[show-order]     item:" + orderItem.getItemId() + ", qty:" + orderItem.getQuantity() 
				+ ", amt:" + String.format("%.2f", orderItem.getSubtotal()) + ", discount:" + String.format("%.2f", orderItem.getDiscount()));
		});
		return true;
	}
	private Order createOrder(Cart cart) {
		ServiceResult<Order> orderResult = orderService.createOrder(cart);
		if(!orderResult.isSuccess()) {
			log.warn("[create-order] failed, msg:" + orderResult.getMessage());
			return null;
		}
		log.info("[create-order] success, order:" + orderResult.getResult().getOrderId());
		return orderResult.getResult();
	}
	private Cart addCart(User user, List<Item> items) {
		Cart cart = new Cart(user.getUserId()).saveAddress(user.getNickname(), user.getMobile(), "北京市海淀区翠微路17号院");
		List<Item> pickedItems = this.pickupItems(items);
		pickedItems.forEach(item -> {
			cart.addItem(item.getId(), 1, item.getPrice(), item.getPrice(), (1-Math.random()/10) * item.getPrice());
			CartItem ci = cart.getItems().get(cart.getItems().size()-1);
			log.info("[add-cart] item:" + ci.getItemId() + ", qty:" + ci.getQuantity() + ", price:" + String.format("%.2f", ci.getPrice()) 
				+ ", amt:" + String.format("%.2f", ci.getSubtotal()) + ", discount:" + String.format("%.2f", ci.getDiscount()));
		});
		return cart;
	}
	private List<Item> pickupItems(List<Item> items) {
		//随机挑选1-2个商品
		int count = ((int)Math.round(Math.random() * 10) % 2) + 1;
		List<Item> result = new ArrayList<>(count);
		Set<Integer> pickedList = new HashSet<>(count); //已经被挑选出来的商品索引（items数组的索引）
		while(count-->0) {
			int index = (int)Math.round(Math.random()*100) % items.size(); //随机挑选一个
			if(pickedList.contains(index)) continue; //已被挑选，放弃
			result.add(items.get(index));
			pickedList.add(index);
		}
		return result;
	}
}