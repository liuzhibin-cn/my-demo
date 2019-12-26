package my.demo.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class OrderItem implements Serializable {
	private static final long serialVersionUID = 7609913567717327072L;
	private long orderItemId;
	private long orderId;
	private int itemId;
	private String title;
	private int quantity;
	private double price;
	private double subtotal;
	private double discount;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date createdAt;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date lastUpdate;
	
	public long getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(long detailId) {
		this.orderItemId = detailId;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
}