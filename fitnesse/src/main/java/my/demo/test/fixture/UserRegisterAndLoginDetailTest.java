package my.demo.test.fixture;

import fit.Fixture;
import my.demo.entity.User;
import my.demo.service.ServiceResult;
import my.demo.service.UserService;
import my.demo.test.Manager;

/**
 * ActionFixture example
 */
public class UserRegisterAndLoginDetailTest extends Fixture {
	private String mobile;
	private String password;
	private ServiceResult<User> result = null;

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void register() {
		UserService service = Manager.getUserService();
		try {
			result = service.registerByMobile(mobile, password);
		} catch(Exception e) {
			result = new ServiceResult<>();
			result.fail("SysError: " + e.getMessage());
		}
	}
	public void login() {
		UserService service = Manager.getUserService();
		try {
			result = service.login(mobile, password);
		} catch(Exception e) {
			result = new ServiceResult<>();
			result.fail("SysError: " + e.getMessage());
		}
	}
	
	public boolean success() {
		return result.isSuccess();
	}
	public long getUserId() {
		return result.getResult() == null ? 0 : result.getResult().getUserId();
	}
	public String getUserMobile() {
		return result.getResult() == null ? "" : result.getResult().getMobile();
	}
	public String getUserNickname() {
		return result.getResult() == null ? "" : result.getResult().getNickname();
	}
}