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

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import my.demo.dao.order.OrderDao;
import my.demo.domain.Cart;
import my.demo.domain.Item;
import my.demo.domain.Order;
import my.demo.domain.OrderItem;
import my.demo.domain.Stock;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.utils.Tracer;

@Service(cluster="failfast", retries=0, loadbalance="roundrobin", timeout=2000)
public class OrderServiceImpl implements OrderService {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 简单演示在应用中创建订单ID
	 */
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
		ServiceResult<Order> result = new ServiceResult<Order>();
		//1. 数据校验
		if(cart==null) {
			return result.fail("Null cart");
		}
		if(cart.getItems()==null || cart.getItems().isEmpty()) {
			return result.fail("Empty cart");
		}
		if(cart.getUserId()<=0) {
			return result.fail("Invalid cart, empty member id");
		}
		Tracer.traceTag("userId", cart.getUserId());
		
		List<OrderItem> lockList = new ArrayList<>(cart.getItems().size());
		try {
			//2. 创建订单、订单明细对象
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
				//获取产品名称
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
			Tracer.traceTag("orderId", order.getOrderId());
			
			//3. 锁定库存（简单起见，不处理锁定失败后释放问题）
			for(OrderItem orderItem : order.getOrderItems()) {
				//检查可用库存
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
				//锁定库存
				ServiceResult<Boolean> lockResult = stockService.lock(orderItem.getItemId(), orderItem.getQuantity());
				if(lockResult.isSuccess() && lockResult.getResult()) lockList.add(orderItem);
				else {
					log.info("[create] Lock stock error, item-id: " + orderItem.getItemId() + ", msg: " + lockResult.getMessage());
					break;
				}
			}
			if(lockList.size() != order.getOrderItems().size()) {
				return result.fail("Failed to lock stock");
			}
			
			//4. 插入订单、订单明细数据（简单起见，不处理创建失败后库存释放问题）
			order.getOrderItems().forEach(orderItem -> {
				orderDao.createOrderItem(orderItem);
				log.info("[create] OrderItem created, item-id: " + orderItem.getItemId());
			});
			orderDao.createOrder(order);
			log.debug("[create] Order created, order-id: " + order.getOrderId());
			
			//5. 维护用户ID、订单ID索引表
			orderDao.createUserOrder(order.getUserId(), order.getOrderId());
			log.info("[create] User Order created: user-id: " + order.getUserId() + ", order-id: " + order.getOrderId());
			
			//6. 从数据库读取订单返回
			Order persisted = orderDao.getOrder(order.getOrderId());
			persisted.setOrderItems(orderDao.getOrderItems(persisted.getOrderId()));
						
			return result.success(persisted);
		} catch(Exception ex) {
			log.error("[create] System error, user-id: " + cart.getUserId() + ", msg: " + ex.getMessage(), ex);
			return result.fail("System error: " + ex.getMessage());
		} finally {
			if(!lockList.isEmpty()) this.unlockStock(lockList);
		}
	}
	private void unlockStock(List<OrderItem> list) {
		//未实现
	}
	private long newId() {
		//高43位毫秒数 + 低21位随机数
		return ((System.currentTimeMillis() - BASE_LINE.getTime()) & 274877906943L << 10) | new Random(System.currentTimeMillis()).nextInt(1023);
	}
	
	@Override
	public ServiceResult<List<Order>> findUserOrders(long userId, int offset, int count) {
		try {
			Tracer.traceTag("userId", userId);
			Tracer.traceTag("offset", offset);
			Tracer.traceTag("count", count);
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
		Tracer.traceTag("orderId", orderId);
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