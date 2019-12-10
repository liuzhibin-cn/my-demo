package my.demo.service.order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import my.demo.dao.order.OrderDao;
import my.demo.domain.Cart;
import my.demo.domain.Item;
import my.demo.domain.Order;
import my.demo.domain.OrderDetail;
import my.demo.domain.Stock;
import my.demo.service.ItemService;
import my.demo.service.OrderService;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;

@Service
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
		
		List<OrderDetail> lockList = new ArrayList<>(cart.getItems().size());
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
				OrderDetail detail = new OrderDetail();
				detail.setOrderId(order.getOrderId());
				detail.setItemId(cartItem.getItemId());
				//获取产品名称
				Item item = itemService.getItem(cartItem.getItemId()).getResult();
				detail.setTitle(item.getTitle());
				detail.setQuantity(cartItem.getQuantity());
				detail.setPrice(cartItem.getPrice());
				detail.setSubtotal(cartItem.getSubtotal());
				detail.setDiscount(cartItem.getDiscount());
				detail.setCreatedAt(new Date());
				order.setTotal( order.getTotal() + cartItem.getSubtotal() );
				order.setDiscount( order.getDiscount() + cartItem.getDiscount() );
				order.addDetail(detail);
			} );
			
			//3. 锁定库存（简单起见，不处理锁定失败后释放问题）
			for(OrderDetail detail : order.getDetails()) {
				//检查可用库存s
				ServiceResult<Stock> stockResult = stockService.getStock(detail.getItemId());
				if(!stockResult.isSuccess()) {
					log.info("> Get stock error, item: " + detail.getItemId() + ", message: " + stockResult.getMessage());
					break;
				}
				if(stockResult.getResult().getAvailableQty()<detail.getQuantity()) {
					log.info("> Stock not enough, item: " + detail.getItemId() 
						+ ", available: " + stockResult.getResult().getAvailableQty()
						+ ", request: " + detail.getQuantity());
					break;
				}
				//锁定库存
				ServiceResult<Boolean> lockResult = stockService.lock(detail.getItemId(), detail.getQuantity());
				if(lockResult.isSuccess() && lockResult.getResult()) lockList.add(detail);
				else {
					log.info("> Lock stock error, item: " + detail.getItemId() + ", message: " + lockResult.getMessage());
					break;
				}
			}
			if(lockList.size() != order.getDetails().size()) {
				return result.fail("Understock");
			}
			
			//4. 插入订单、订单明细数据（简单起见，不处理创建失败后库存释放问题）
			order.getDetails().forEach(detail -> {
				orderDao.createOrderDetail(detail);
			});
			orderDao.createOrder(order);
			log.info("> Order created: " + order.getOrderId());
			
			//5. 维护用户ID、订单ID索引表
			orderDao.createUserOrder(order.getUserId(), order.getOrderId());
			log.info("> User Order created: user: " + order.getUserId() + ", order: " + order.getOrderId());
			
			return result.success(order);
		} catch(Exception ex) {
			log.error("> Create order error: " + ex.getMessage(), ex);
			return result.fail("System error: " + ex.getMessage());
		} finally {
			if(!lockList.isEmpty()) this.unlockStock(lockList);
		}
	}
	private void unlockStock(List<OrderDetail> list) {
		//未实现
	}
	private long newId() {
		//高43位毫秒数 + 低21位随机数
		return ((System.currentTimeMillis() - BASE_LINE.getTime()) << 21) | (Math.round(Math.random() * 2097150) & 0x1FFFFF);
	}
	
	@Override
	public ServiceResult<List<Order>> findUserOrders(int userId, int offset, int count) {
		try {
			List<Long> orderIds = orderDao.findUserOrderIds(userId, offset, count);
			if(orderIds==null || orderIds.isEmpty()) {
				log.info("> User order not found, user: " + userId);
				return new ServiceResult<List<Order>>(null);
			}
			return new ServiceResult<>(orderDao.findOrders(orderIds));
		}catch (Exception ex) {
			log.error("> Find user order error: " + ex.getMessage(), ex);
			return new ServiceResult<List<Order>>().fail("System error: " + ex.getMessage());
		}
	}

	@Override
	public ServiceResult<List<OrderDetail>> getOrderDetails(long orderId) {
		try {
			return new ServiceResult<List<OrderDetail>>(orderDao.getOrderDetails(orderId));
		} catch(Exception ex) {
			log.error("> Get order detail error: " + ex.getMessage(), ex);
			return new ServiceResult<List<OrderDetail>>().fail("System error: " + ex.getMessage());
		}
	}
}
