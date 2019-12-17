package my.demo.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class User implements Serializable {
	private static final long serialVersionUID = -5284692770015499256L;
	private long userId;
	private String nickname;
	private String mobile;
	private String email;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date createdAt;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="Asia/Shanghai")
	private Date lastUpdate;
	
	/**
	 * 用户ID
	 */
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
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