package my.demo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class Order implements Serializable {
	private static final long serialVersionUID = -60380267375585244L;
	private long orderId = 0;
	private long userId = 0;
	private String status = "";
	private double total = 0;
	private double discount = 0;
	private double payment = 0;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date payTime = null;
	private String payStatus = "";
	private String contact = "";
	private String phone = "";
	private String address = "";
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date createdAt = null;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date lastUpdate;
	private List<OrderItem> items;
	
	public void addOrderItem(OrderItem orderItem) {
		if(this.items==null) this.items = new ArrayList<>();
		this.items.add(orderItem);
	}
	public List<OrderItem> getOrderItems() {
		return this.items==null ? new ArrayList<>(0) : this.items;
	}
	public void setOrderItems(List<OrderItem> orderItems) {
		this.items = orderItems;
	}
	
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long memberId) {
		this.userId = memberId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public double getDiscount() {
		return discount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public double getPayment() {
		return payment;
	}
	public void setPayment(double payment) {
		this.payment = payment;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public String getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
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