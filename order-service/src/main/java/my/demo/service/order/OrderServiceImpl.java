package my.demo.service.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import my.demo.dao.order.OrderDao;
import my.demo.entity.Cart;
import my.demo.entity.Item;
import my.demo.entity.Order;
import my.demo.entity.OrderItem;
import my.demo.entity.Stock;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.utils.MyDemoUtils;

@Service
public class OrderServiceImpl implements OrderService {
	Logger log = LoggerFactory.getLogger(this.getClass());

	static Date BASE_LINE = null;
	static Date DEFAULT_TIME = null;
	
	@Reference
	ItemService itemService;
	@Reference
	StockService stockService;
	@Autowired
	OrderDao orderDao;

	static {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			BASE_LINE = sdf.parse("2018-01-01");
			DEFAULT_TIME = sdf.parse("1900-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ServiceResult<Order> createOrder(Cart cart) {
		if(MyDemoUtils.isSeataPresent()) {
			log.info("[create] XID: " + MyDemoUtils.getXID());
		}
		ServiceResult<Order> result = new ServiceResult<Order>();
		//1. Verification
		if(cart==null) {
			return result.fail("Null cart");
		}
		if(cart.getItems()==null || cart.getItems().isEmpty()) {
			return result.fail("Empty cart");
		}
		if(cart.getUserId()<=0) {
			return result.fail("Invalid cart, empty member id");
		}
		MyDemoUtils.tag("userId", cart.getUserId());
		
		List<OrderItem> lockList = new ArrayList<>(cart.getItems().size());
		try {
			this.createOrder(result, lockList, cart);
			return result;
		} catch(Exception ex) {
			log.error("[create] System error, user-id: " + cart.getUserId() + ", msg: " + ex.getMessage(), ex);
			return result.fail("System error: " + ex.getMessage());
		} finally {
			if(!lockList.isEmpty()) this.unlockStock(lockList);
		}
	}
	@Transactional
	private void createOrder(ServiceResult<Order> result, List<OrderItem> lockList, Cart cart) {
		//2. Create Order, OrderItem
		Order order = new Order();
		order.setOrderId(this.newId());
		order.setStatus("New");
		order.setUserId(cart.getUserId());
		order.setPayStatus("New");
		order.setPayTime(DEFAULT_TIME);
		order.setContact(cart.getContact());
		order.setPhone(cart.getPhone());
		order.setAddress(cart.getAddress());
		order.setCreatedAt(new Date());		
		cart.getItems().forEach( cartItem -> {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrderId(order.getOrderId());
			orderItem.setItemId(cartItem.getItemId());
			//Get item title
			Item item = itemService.getItem(cartItem.getItemId()).getResult();
			orderItem.setTitle(item.getTitle());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setPrice(cartItem.getPrice());
			orderItem.setSubtotal(cartItem.getSubtotal());
			orderItem.setDiscount(cartItem.getDiscount());
			orderItem.setCreatedAt(new Date());
			order.setTotal( order.getTotal() + cartItem.getSubtotal() );
			order.setDiscount( order.getDiscount() + cartItem.getDiscount() );
			order.addOrderItem(orderItem);
		} );
		MyDemoUtils.tag("orderId", order.getOrderId());
		
		//3. Lock stock
		for(OrderItem orderItem : order.getOrderItems()) {
			//Check stock availablility
			ServiceResult<Stock> stockResult = stockService.getStock(orderItem.getItemId());
			if(!stockResult.isSuccess()) {
				log.info("[create] Get stock error, item-id: " + orderItem.getItemId() + ", msg: " + stockResult.getMessage());
				break;
			}
			if(stockResult.getResult().getAvailableQty()<orderItem.getQuantity()) {
				log.info("[create] Stock not enough, item-id: " + orderItem.getItemId() 
					+ ", available-qty: " + stockResult.getResult().getAvailableQty()
					+ ", request-qty: " + orderItem.getQuantity());
				break;
			}
			//Lock stock
			ServiceResult<Boolean> lockResult = stockService.lock(orderItem.getItemId(), orderItem.getQuantity());
			if(lockResult.isSuccess() && lockResult.getResult()) lockList.add(orderItem);
			else {
				log.info("[create] Lock stock error, item-id: " + orderItem.getItemId() + ", msg: " + lockResult.getMessage());
				break;
			}
		}
		if(lockList.size() != order.getOrderItems().size()) {
			result.fail("Failed to lock stock");
			return;
		}
		
		//4. Insert Order, OrderItem to MySQL
		order.getOrderItems().forEach(orderItem -> {
			orderDao.createOrderItem(orderItem);
			log.info("[create] OrderItem created, item-id: " + orderItem.getItemId());
		});
		orderDao.createOrder(order);
		log.debug("[create] Order created, order-id: " + order.getOrderId());
		
		//5. Manage user defined index for (user-id, order_id) 
		orderDao.createUserOrder(order.getUserId(), order.getOrderId());
		log.info("[create] User Order created: user-id: " + order.getUserId() + ", order-id: " + order.getOrderId());
		
		orderDao.testUpdateOrderItem(order.getOrderId());
		
		//6. Get order from MySQL
		Order persisted = orderDao.getOrder(order.getOrderId());
		persisted.setOrderItems(orderDao.getOrderItems(persisted.getOrderId()));
					
		result.success(persisted);
	}
	private void unlockStock(List<OrderItem> list) {
	}
	private long newId() {
		return ((System.currentTimeMillis() - BASE_LINE.getTime()) & 274877906943L << 10) | new Random(System.currentTimeMillis()).nextInt(1023);
	}
	
	@Override
	public ServiceResult<List<Order>> findUserOrders(long userId, int offset, int count) {
		try {
			MyDemoUtils.tag("userId", userId);
			MyDemoUtils.tag("offset", offset);
			MyDemoUtils.tag("count", count);
			List<Long> orderIds = orderDao.findUserOrderIds(userId, offset, count);
			if(orderIds==null || orderIds.isEmpty()) {
				if(log.isDebugEnabled()) {
					log.debug("[find] user-id: " + userId + ", orders: 0, offset: " + offset + ", count: " + count);
				}
				return new ServiceResult<List<Order>>(null);
			}
			ServiceResult<List<Order>> result = new ServiceResult<>(orderDao.findOrders(orderIds));
			if(log.isDebugEnabled()) {
				log.debug("[find] user-id: " + userId + ", orders: " + result.getResult().size() + ", offset: " + offset + ", count: " + count);
			}
			return result;
		}catch (Exception ex) {
			log.error("[find] System error, user-id: " + userId + ", msg: " + ex.getMessage(), ex);
			return new ServiceResult<List<Order>>().fail("System error: " + ex.getMessage());
		}
	}

	@Override
	public ServiceResult<List<OrderItem>> getOrderItems(long orderId) {
		MyDemoUtils.tag("orderId", orderId);
		try {
			List<OrderItem> orderItems = orderDao.getOrderItems(orderId);
			if(log.isDebugEnabled()) {
				log.debug("[get-item] order-id: " + orderId + ", order-items: " + orderItems.size());
			}
			return new ServiceResult<List<OrderItem>>(orderItems);
		} catch(Exception ex) {
			log.error("[get-item] System error: " + ex.getMessage(), ex);
			return new ServiceResult<List<OrderItem>>().fail("System error: " + ex.getMessage());
		}
	}
} 