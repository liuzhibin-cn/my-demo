package my.demo.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import my.demo.domain.Cart;
import my.demo.domain.Item;
import my.demo.domain.Order;
import my.demo.domain.User;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.service.UserService;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages={"my.demo.test"})
public class Application {
	static Logger log = LoggerFactory.getLogger(Application.class);
	
	@Reference
	ItemService itemService;
	@Reference
	StockService stockService;
	@Reference
	UserService userService;
	@Reference
	OrderService orderService;
	
	private static String[] MOBILE_PREFIXS = { "135", "136", "137", "138", "139", "186", "180", "187", "158" };
	
	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
		Application app = context.getBean(Application.class);
		long start = System.currentTimeMillis();
		app.runTest(100);
		long end = System.currentTimeMillis();
		log.info("Elapsed: " + (end - start) + " ms.");
		context.stop();
		context.close();
	}
	private void runTest(int loops) {
		while(loops-->0) this.runTest();
	}
	private void runTest() {
		//随机注册一个用户
		String prefix = MOBILE_PREFIXS[(int)Math.round(Math.random()*1000) % MOBILE_PREFIXS.length];
		String mobile = prefix + String.format("%08d", Math.round(Math.random()*100000000));
		String password = Math.round(Math.random() * 10000000) + "";
		ServiceResult<User> result = userService.registerByMobile(mobile, password);
		if(!result.isSuccess()) {
			log.warn("> Failed to register: " + result.getMessage());
			return;
		}
		log.info("> [register] user-id: " + result.getResult().getUserId() + ", mobile: " + mobile + ", password: " + password);
		
		//登录
		result = userService.login(mobile, password);
		if(!result.isSuccess()) {
			log.warn("> Failed to login: " + result.getMessage());
			return;
		}
		log.info("> [login] user-id: " + result.getResult().getUserId());
		User user = result.getResult();
		
		//浏览商品
		List<Item> items = itemService.findItem().getResult();
		
		//随机选择1-2个添加购物车
		Cart cart = this.addCart(user, items);
		
		//下单
		ServiceResult<Order> orderResult = orderService.createOrder(cart);
		if(!orderResult.isSuccess()) {
			log.warn("> Failed to create order: " + result.getMessage());
			return;
		}
		
		//查用户订单列表
		orderService.findUserOrders(user.getUserId(), 0, 10).getResult().forEach(order -> {
			log.info("> [order] id: " + order.getOrderId() + ", contact: " + order.getContact() + ", phone: " + order.getPhone() + ", amt: " + order.getTotal());
			//查订单明细
			orderService.getOrderDetails(order.getOrderId()).getResult().forEach(detail -> {
				log.info("> [detail] item: " + detail.getItemId() + ", title: " + detail.getTitle() 
					+ ", qty: " + detail.getQuantity() + ", amt: " + detail.getSubtotal() + ", discount: " + detail.getDiscount());
			});
		});
	}
	private Cart addCart(User user, List<Item> items) {
		Cart cart = new Cart(user.getUserId()).saveAddress(user.getNickname(), user.getMobile(), "北京市海淀区翠微路17号院");
		List<Item> pickedItems = this.pickupItems(items);
		pickedItems.forEach(item -> {
			cart.addItem(item.getId(), 1, item.getPrice(), item.getPrice(), (1-Math.random()/10) * item.getPrice());
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