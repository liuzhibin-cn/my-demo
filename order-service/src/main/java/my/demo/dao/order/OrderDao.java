package my.demo.dao.order;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import my.demo.entity.Order;
import my.demo.entity.OrderItem;

@Mapper
public interface OrderDao {
	@Insert("insert into ord_order (order_id, user_id, status, total, discount, payment, pay_time, pay_status, contact, phone, address, created_at) " 
			+ "values(#{orderId}, #{userId}, #{status}, #{total}, #{discount}, #{payment}, #{payTime}, #{payStatus}, #{contact}, #{phone}, #{address}, #{createdAt})")
	int createOrder(Order order);

	@Insert("insert into ord_order_item (order_id, item_id, title, quantity, price, subtotal, discount, created_at) " 
			+ "values(#{orderId}, #{itemId}, #{title}, #{quantity}, #{price}, #{subtotal}, #{discount}, #{createdAt})")
	int createOrderItem(OrderItem orderItem);
	
	@Update("update ord_order_item set subtotal=subtotal-2, discount=discount+2 where order_id=#{orderId}")
	int testUpdateOrderItem(@Param("orderId") long orderId);
	
	ArrayList<Order> findOrders(@Param("orderIds") List<Long> orderIds);
	
	@Select("select * from ord_order where order_id = #{orderId}")
	@ResultMap("order")
	Order getOrder(long orderId);
	
	@Select("select * from ord_order_item where order_id = #{orderId}")
	@ResultMap("orderItem")
	ArrayList<OrderItem> getOrderItems(long orderId);
	
	@Insert("insert into ord_user_order (user_id, order_id) values(#{userId}, #{orderId})")
	int createUserOrder(@Param("userId") long userId, @Param("orderId") long orderId);
	@Select("select order_id from ord_user_order where user_id = #{userId} limit #{offset}, #{count}")
	@ResultType(Long.class)
	List<Long> findUserOrderIds(@Param("userId") long userId, @Param("offset") int offset, @Param("count") int count);
}