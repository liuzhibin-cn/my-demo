package my.demo.dao.order;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import my.demo.domain.Order;
import my.demo.domain.OrderItem;

@Mapper
public interface OrderDao {
	// ================================================================
	// 订单表ord_order: 主键和分片字段均为order_id，其值在服务代码中生成；
	// 订单明细表ord_order_item: 主键order_item_id，其值通过mycat全局序列ORDERDETAIL生成；分片字段order_id（定义为mycat父子关系表）；
	// ================================================================
	@Insert("insert into ord_order (order_id, user_id, status, total, discount, payment, pay_time, pay_status, contact, phone, address, created_at) " 
			+ "values(#{orderId}, #{userId}, #{status}, #{total}, #{discount}, #{payment}, #{payTime}, #{payStatus}, #{contact}, #{phone}, #{address}, #{createdAt})")
	int createOrder(Order order);
	@Insert("insert into ord_order_item (order_item_id, order_id, item_id, title, quantity, price, subtotal, discount, created_at) " 
			+ "values(next value for MYCATSEQ_ORDERDETAIL, #{orderId}, #{itemId}, #{title}, #{quantity}, #{price}, #{subtotal}, #{discount}, #{createdAt})")
	int createOrderItem(OrderItem orderItem);
	
	List<Order> findOrders(@Param("orderIds") List<Long> orderIds);
	
	@Select("select * from ord_order where order_id = #{orderId}")
	@ResultMap("order")
	Order getOrder(long orderId);
	
	@Select("select * from ord_order_item where order_id = #{orderId}")
	@ResultMap("orderDetail")
	List<OrderItem> getOrderItems(long orderId);
	
	// ================================================================
	// 用户ID-订单ID 索引表 ord_user_order: 分片字段user_id
	// ================================================================
	@Insert("insert into ord_user_order (user_id, order_id) values(#{userId}, #{orderId})")
	int createUserOrder(@Param("userId") long userId, @Param("orderId") long orderId);
	/**
	 * 查找某个用户的订单ID列表
	 * @param userId
	 * @param offset
	 * @param count
	 * @return
	 */
	@Select("select order_id from ord_user_order where user_id = #{userId} limit #{offset}, #{count}")
	@ResultType(Long.class)
	List<Long> findUserOrderIds(@Param("userId") long userId, @Param("offset") int offset, @Param("count") int count);
}