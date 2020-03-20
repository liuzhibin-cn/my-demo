package my.demo.service.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.alibaba.dubbo.config.annotation.Service;

import my.demo.dao.user.UserDao;
import my.demo.entity.User;
import my.demo.entity.UserAccount;
import my.demo.service.ServiceResult;
import my.demo.service.UserService;
import my.demo.utils.MyDemoUtils;

@Service
public class UserServiceImpl implements UserService {
	static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	public static final String INVALID_ACCOUNT = "Invalid account";
	public static final String EMPTY_ACCOUNT = "Empty account";
	public static final String INVALID_PASSWORD = "Invalid password";
	public static final String EMPTY_PASSWORD = "Empty password";
	
	static Date baseLine = null;
	
	@Autowired
	UserDao dao;
	
	static {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			baseLine = sdf.parse("2019-01-01");
		} catch (ParseException e) {
			log.info("Create BaseLine date error", e);
		}
	}
	
	public void checkAccountAndPassword(ServiceResult<User> result, String mobile, String password) {
		result.setSuccess(true);
		mobile = mobile==null ? "" : mobile.trim();
		password = password==null ? "" : password.trim();
		if(mobile==null || mobile.trim().isEmpty()) {
			result.fail(EMPTY_ACCOUNT);
			return;
		}
		if(password==null || password.trim().isEmpty()) {
			result.fail(EMPTY_PASSWORD);
			return;
		}
		if(!mobile.matches("1[0-9]{10,10}")) {
			result.fail(INVALID_ACCOUNT);
			return;
		}
		if(password.length()<6 || !password.matches(".*[0-9]+.*") || !password.matches(".*[a-zA-Z]+.*")) {
			result.fail(INVALID_PASSWORD);
		}
	}
	@Override
	@Transactional
	public ServiceResult<User> registerByMobile(String mobile, String password) {
		if(MyDemoUtils.isSeataPresent()) {
			log.info("[register] XID: {}", MyDemoUtils.getXID());
		}
		MyDemoUtils.tag("account", mobile);
		ServiceResult<User> result = new ServiceResult<>();
		//简单校验
		this.checkAccountAndPassword(result, mobile, password);
		if(!result.isSuccess()) return result;
		register(result, mobile, password);
		return result;
	}
	private void register(ServiceResult<User> result, String mobile, String password) {
		//1. 账号是否已经注册过
		if(this.isRegistered(mobile)) {
			log.info("[register] Already registered, mobile: {}", mobile);
			result.fail("Account already registered");
			return;
		}
		//2. 创建用户资料，主键和分片字段均为 user_id
		User user = new User();
		user.setNickname(mobile.substring(0, 3) + "****" + mobile.substring(7, 11));
		user.setMobile(mobile);
		user.setEmail("");
		user.setCreatedAt(new Date());
		if(dao.createUser(user)<=0) {
			log.warn("[register] Failed to create user, account: {}", mobile);
			result.fail("Failed to create user");
			return;
		}
		if(user.getUserId()<=0) {
			log.warn("[register] Failed to create user, invalid user-id, account: {}", mobile);
			result.fail("Failed to create user");
			return;
		}
		log.info("[register] User created, user-id: {}, account: {}", user.getUserId(), mobile);
		//3. 创建用户登录账号，分片字段account_hash，主键account
		UserAccount userAccount = new UserAccount();
		userAccount.setAccount(mobile);
		userAccount.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
		userAccount.setAccountHash(this.getAccountHashcode(mobile));
		userAccount.setUserId(user.getUserId());
		if(dao.createUserAccount(userAccount)<=0) {
			log.warn("[register] Failed to create user account, user-id: {}, account: {}", user.getUserId(), mobile);
			result.fail("Failed to create user account");
			return;
		}
		MyDemoUtils.tag("userId", user.getUserId());
		log.info("[register] User account created, user-id: {}, account: {}", user.getUserId(), mobile);
		result.success(user);
	}

	@Override
	public ServiceResult<User> login(String account, String password) {
		MyDemoUtils.tag("account", account);
		ServiceResult<User> result = new ServiceResult<>();
		this.checkAccountAndPassword(result, account, password);
		if(!result.isSuccess()) return result;
		try {
			//1. 通过account获取用户登录账号，分片键 account_hash
			UserAccount userAccount = this.getUserAccount(account);
			if(userAccount==null) {
				log.debug("[login] Account not found, account: {}", account);
				return result.fail("Account not found");
			}
			//   校验密码
			if(!DigestUtils.md5DigestAsHex(password.getBytes()).equals(userAccount.getPassword())) {
				log.debug("[login] Incorrect password, account: {}", account);
				return result.fail("Incorrect password");
			}
			//2. 通过user_id获取user对象，分片键 user_id
			User user = dao.getUser(userAccount.getUserId());
			if(user==null) {
				log.warn("[login] User not found, account: {}", account);
				return result.fail("Account error: User not found");
			}
			log.info("[login] Success, user-id: {}, account: {}", user.getUserId(), account);
			MyDemoUtils.tag("userId", user.getUserId());
			return result.success(user);
		} catch(Exception ex) {
			log.error("[login] System error, msg: " + ex.getMessage(), ex);
			return result.fail("System error: " + ex.getMessage());
		}
	}
	
	@Override
	public ServiceResult<User> getUser(long userId) {
		return new ServiceResult<>(dao.getUser(userId));
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