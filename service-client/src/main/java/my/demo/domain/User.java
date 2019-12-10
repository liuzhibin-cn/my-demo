package my.demo.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 */
public class User implements Serializable {
	private static final long serialVersionUID = -5284692770015499256L;
	private int userId;
	private String nickname;
	private String mobile;
	private String email;
	private Date createdAt;
	private Date lastUpdate;
	
	/**
	 * 用户ID
	 */
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * 用户昵称
	 */
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname==null ? "" : nickname;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile==null ? "" : mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email==null ? "" : email;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt==null ? new Date() : createdAt;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
}