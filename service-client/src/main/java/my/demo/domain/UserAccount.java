package my.demo.domain;

import java.io.Serializable;

/**
 * 用户账号、用户ID对应关系
 */
public class UserAccount implements Serializable {
	private static final long serialVersionUID = 8639953921479963556L;
	private String account;
	private String password;
	private int userId;
	private int accountHash;
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getAccountHash() {
		return accountHash;
	}
	public void setAccountHash(int accountHash) {
		this.accountHash = accountHash;
	}
}