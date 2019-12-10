package my.demo.domain;

import java.io.Serializable;

public class CartItem implements Serializable {
	private static final long serialVersionUID = -3350171720912880246L;
	private int itemId;
	private int quantity;
	private double price;
	private double subtotal;
	private double discount;
	
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int productId) {
		this.itemId = productId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	public double getDiscount() {
		return discount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
}