package my.demo.domain;

import java.io.Serializable;

public class Item implements Serializable {
	private static final long serialVersionUID = -7845145044315043115L;
	private int id;
	private String title;
	private double price;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(this==obj) return true;
		if(this.getClass()!=obj.getClass()) return false;
		return this.id == ((Item)obj).id;
	}
	@Override
	public int hashCode() {
		return this.id;
	}
	@Override
	public String toString() {
		return this.id + "-" + this.title;
	}
}