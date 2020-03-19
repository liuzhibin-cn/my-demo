package my.demo.test.fixture;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import my.demo.entity.User;
import my.demo.service.ServiceResult;
import my.demo.service.UserService;
import my.demo.test.Manager;

public class UserRegisterAndLoginDetailTest {
	private ServiceResult<User> result = null;
	public void registerWithMobileAndPassword(String mobile, String password) {
		UserService service = Manager.getUserService();
		try {
			result = service.registerByMobile(mobile, password);
		} catch (Exception e) {
			result = new ServiceResult<>();
			result.fail("SysError: " + e.getMessage());
		}
	}
	public void loginWithMobileAndPassword(String mobile, String password) {
		UserService service = Manager.getUserService();
		try {
			result = service.login(mobile, password);
		} catch (Exception e) {
			result = new ServiceResult<>();
			result.fail("SysError: " + e.getMessage());
		}
	}
	public boolean success() {
		return result.isSuccess();
	}
	public long userIdIs() {
		return result.getResult() == null ? 0 : result.getResult().getUserId();
	}
	public String userMobileIs() {
		return result.getResult() == null ? "" : result.getResult().getMobile();
	}
	public String userNicknameIs() {
		return result.getResult() == null ? "" : result.getResult().getNickname();
	}
	public String returnedResult() {
		if(result==null) return "null";
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			return mapper.writeValueAsString(result);
		} catch (JsonProcessingException e) {
			return "SysError: " + e.getMessage();
		}
	}
}