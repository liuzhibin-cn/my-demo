package my.demo.domain;

import java.io.Serializable;

public class Stock implements Serializable {
	private static final long serialVersionUID = -4603287294441831684L;
	private int itemId;
	private int totalQty;
	private int lockQty;
	
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getTotalQty() {
		return totalQty;
	}
	public void setTotalQty(int totalQty) {
		this.totalQty = totalQty;
	}
	public int getLockQty() {
		return lockQty;
	}
	public void setLockQty(int lockQty) {
		this.lockQty = lockQty;
	}
	public int getAvailableQty() {
		return this.totalQty - this.lockQty;
	}
}