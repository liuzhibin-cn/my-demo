package my.demo.service.user.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import my.demo.entity.User;
import my.demo.service.ServiceResult;
import my.demo.service.user.UserServiceImpl;

public class UserServiceImplTest {
	@Test
	public void testCheckAccountAndPassword() {
		UserServiceImpl service = new UserServiceImpl();
		ServiceResult<User> result = new ServiceResult<>();
		service.checkAccountAndPassword(result, "13966668888", "qwe123");
		Assert.assertTrue(result.isSuccess());

		service.checkAccountAndPassword(result, "", "qwe123");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.EMPTY_ACCOUNT);
		service.checkAccountAndPassword(result, "139666688a8", "qwe123");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.INVALID_ACCOUNT);
		service.checkAccountAndPassword(result, "1396668", "qwe123");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.INVALID_ACCOUNT);
		
		service.checkAccountAndPassword(result, "13966668888", "");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.EMPTY_PASSWORD);
		service.checkAccountAndPassword(result, "13966668888", "qwe23");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.INVALID_PASSWORD);
		service.checkAccountAndPassword(result, "13966668888", "QQQQQQ");
		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(result.getMessage(), UserServiceImpl.INVALID_PASSWORD);
	}
}