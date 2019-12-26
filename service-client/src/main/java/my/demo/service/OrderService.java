package my.demo.service;

import java.util.List;

import my.demo.entity.Cart;
import my.demo.entity.Order;
import my.demo.entity.OrderItem;

public interface OrderService {
	ServiceResult<Order> createOrder(Cart cart);
	ServiceResult<List<Order>> findUserOrders(long userId, int offset, int count);
	ServiceResult<List<OrderItem>> getOrderItems(long orderId);
}