package my.demo.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
		app.runTestCases(100);
		long end = System.currentTimeMillis();
		log.info("耗时" + (end - start) + "ms.");
		context.stop();
		context.close();
	}
	private void runTestCases(int loops) {
		//预热
		this.runTestCaseWioutTrace();
		//执行测试
		while(loops-->0) {
			this.runTestCaseWithTrace();
		}
	}
	@Trace //强制SkyWalking跟踪该方法，产生全局trace-id，这样test-app的日志输出中即带有有效的trace-id了
	private void runTestCaseWithTrace() {
		this.runTestCaseDo();
	}
	private void runTestCaseWioutTrace() {
		this.runTestCaseDo();
	}
	private void runTestCaseDo() {
		log.debug("Start a test case");
		//随机注册一个用户
		String prefix = MOBILE_PREFIXS[(int)Math.round(Math.random()*1000) % MOBILE_PREFIXS.length];
		String mobile = prefix + String.format("%08d", Math.round(Math.random()*100000000));
		String password = Math.round(Math.random() * 10000000) + "";
		ServiceResult<User> result = userService.registerByMobile(mobile, password);
		if(!result.isSuccess()) {
			log.info("[register] failed, account:" + mobile + ", msg:" + result.getMessage());
			return;
		}
		log.info("[register] success, account:" + mobile + ", user-id:" + result.getResult().getUserId());
		
		//登录
		result = userService.login(mobile, password);
		if(!result.isSuccess()) {
			log.info("[login] failed, account:" + mobile + ", msg:" + result.getMessage());
			return;
		}
		log.info("[login] success, account:" + mobile + " , user-id:" + result.getResult().getUserId());
		User user = result.getResult();
		
		//浏览商品
		List<Item> items = itemService.findItem().getResult();
		
		//随机选择1-2个添加购物车
		Cart cart = this.addCart(user, items);
		
		//下单
		ServiceResult<Order> orderResult = orderService.createOrder(cart);
		if(!orderResult.isSuccess()) {
			log.warn("[create-order] failed, msg:" + result.getMessage());
			return;
		}
		log.info("[create-order] success, order:" + orderResult.getResult().getOrderId());
		
		//查用户订单列表
		ServiceResult<List<Order>> userOrderResult = orderService.findUserOrders(user.getUserId(), 0, 10);
		if(!userOrderResult.isSuccess()) {
			log.info("[find-order] failed, msg:" + userOrderResult.getMessage());
			return;
		}
		if(userOrderResult.getResult().isEmpty()) {
			log.info("[find-order] no orders found");
			return;
		}
		userOrderResult.getResult().forEach(order -> {
			log.debug("[show-order] order:" + order.getOrderId() + ", details:" + order.getDetails().size());
			orderService.getOrderDetails(order.getOrderId()).getResult().forEach(detail -> {
				log.debug("[show-order]     item:" + detail.getItemId() + ", qty:" + detail.getQuantity() 
					+ ", amt:" + String.format("%.2f", detail.getSubtotal()) + ", discount:" + String.format("%.2f", detail.getDiscount()));
			});
		});
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