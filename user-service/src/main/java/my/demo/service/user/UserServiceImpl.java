package my.demo.service.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import my.demo.dao.user.UserDao;
import my.demo.domain.User;
import my.demo.domain.UserAccount;
import my.demo.service.ServiceResult;
import my.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	static Date BASE_LINE = null;
	
	@Autowired
	UserDao dao;
	
	static {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			BASE_LINE = sdf.parse("2019-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ServiceResult<User> registerByMobile(String mobile, String password) {
		ServiceResult<User> result = new ServiceResult<User>();
		//简单校验
		if(mobile==null || mobile.isEmpty() || mobile.trim().length()!=11) {
			return result.fail("Invalid mobile: " + mobile);
		}
		if(password==null || password.trim().length()<6) {
			return result.fail("Invalid password: " + password);
		}
		try { //注册
			//1. 账号是否已经注册过
			if(this.isRegistered(mobile)) {
				return result.fail("Account " + mobile + " already registered");
			}
			//2. 创建用户登录账号，分片字段account_hash，主键account，插入过程利用mycat全局序列生成了user_id
			//   用全局序列生成ID比较简单，能有效确保ID全局唯一，但高并发插入会产生瓶颈；
			//   另外，只能通过其它唯一字段查询记录，获取当前插入产生的mycat全局序列值；
			UserAccount userAccount = new UserAccount();
			userAccount.setAccount(mobile);
			userAccount.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
			userAccount.setAccountHash(this.getAccountHashcode(mobile));
			if(dao.createUserAccount(userAccount)<=0) {
				return result.fail("Failed to create user account");
			}
			//3. 创建用户资料，主键和分片字段均为 user_id
			User user = new User();
			user.setUserId(userAccount.getUserId());
			user.setNickname(mobile.substring(0, 3) + "****" + mobile.substring(7, 11));
			user.setMobile(mobile);
			user.setEmail("");
			user.setCreatedAt(new Date());
			if(dao.createUser(user)<=0) {
				return result.fail("Failed to create user");
			}
			return result.success(user);
		} catch (Exception ex) {
			return result.fail("System error: " + ex.getMessage());
		}
	}

	@Override
	public ServiceResult<User> login(String account, String password) {
		ServiceResult<User> result = new ServiceResult<User>();
		if(account==null || account.trim().isEmpty()) {
			return result.fail("Empty account");
		}
		if(password==null || password.trim().isEmpty()) {
			return result.fail("Empty password");
		}
		try {
			//1. 通过account获取用户登录账号，分片键 account_hash
			UserAccount userAccount = this.getUserAccount(account);
			if(userAccount==null) {
				return result.fail("Account " + account + " not found");
			}
			//   校验密码
			if(!DigestUtils.md5DigestAsHex(password.getBytes()).equals(userAccount.getPassword())) {
				return result.fail("Incorrect password");
			}
			//2. 通过user_id获取user对象，分片键 user_id
			User user = dao.getUser(userAccount.getUserId());
			if(user==null) {
				return result.fail("Account error");
			}
			return result.success(user);
		} catch(Exception ex) {
			return result.fail("System error: " + ex.getMessage());
		}
	}
	
	private UserAccount getUserAccount(String account) {
		return dao.getUserAccount(account, this.getAccountHashcode(account));
	}
	private boolean isRegistered(String account) {
		return this.getUserAccount(account)!=null;
	}
	private int getAccountHashcode(String account) {
		int hashcode = account.hashCode();
		return hashcode<0 ? -hashcode : hashcode;
	}

}