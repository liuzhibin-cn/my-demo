package my.demo.service;

import java.util.List;

import my.demo.domain.Cart;
import my.demo.domain.Order;
import my.demo.domain.OrderItem;

public interface OrderService {
	ServiceResult<Order> createOrder(Cart cart);
	ServiceResult<List<Order>> findUserOrders(long userId, int offset, int count);
	ServiceResult<List<OrderItem>> getOrderItems(long orderId);
}