package my.demo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
	private static final long serialVersionUID = -1531734075915913410L;
	private long userId;
	private String contact;
	private String phone;
	private String address;
	private List<CartItem> items;
	
	public Cart(long userId) {
		this.userId = userId;
		this.items = new ArrayList<>();
	}
	public Cart saveAddress(String contact, String phone, String address) {
		this.contact = contact;
		this.phone = phone;
		this.address = address;
		return this;
	}
	public Cart addItem(int itemId, int quantity, double price, double subtotal, double discount) {
		CartItem ci = new CartItem();
		ci.setItemId(itemId);
		ci.setQuantity(quantity);
		ci.setPrice(price);
		ci.setSubtotal(subtotal);
		ci.setDiscount(discount);
		this.items.add(ci);
		return this;
	}

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public List<CartItem> getItems() {
		return items;
	}
	public void setItems(List<CartItem> items) {
		this.items = items;
	}
}