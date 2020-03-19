package my.demo.test.fixture;

import my.demo.entity.User;
import my.demo.service.ServiceResult;
import my.demo.service.UserService;
import my.demo.test.Manager;

public class UserRegisterAndLoginSummaryTest {
	private String mobile;
	private String password;
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String register() {
		UserService service = Manager.getUserService();
		ServiceResult<User> result = null;
		try {
			result = service.registerByMobile(mobile, password);
		} catch(Exception e) {
			return "SysError: " + e.getMessage();
		}
		return result.isSuccess() ? "OK" : result.getMessage();
	}
	public String login() {
		UserService service = Manager.getUserService();
		ServiceResult<User> result = null;
		try {
			result = service.login(mobile, password);
		} catch(Exception e) {
			return "SysError: " + e.getMessage();
		}
		return result.isSuccess() ? "OK" : result.getMessage();
	}
}