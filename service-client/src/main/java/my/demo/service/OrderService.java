package my.demo.service;

import java.util.ArrayList;

import my.demo.entity.Cart;
import my.demo.entity.Order;
import my.demo.entity.OrderItem;

public interface OrderService {
	ServiceResult<Order> createOrder(Cart cart);
	ServiceResult<ArrayList<Order>> findUserOrders(long userId, int offset, int count);
	ServiceResult<ArrayList<OrderItem>> getOrderItems(long orderId);
}