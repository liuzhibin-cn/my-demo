package my.demo.service;

import my.demo.domain.User;

public interface UserService {
	/**
	 * 用手机号注册用户
	 * @param mobile 手机号
	 * @param password 密码
	 * @return 注册成功返回User对象
	 */
	ServiceResult<User> registerByMobile(String mobile, String password);
	/**
	 * 用户通过账号+密码登录
	 * @param account 账号
	 * @param password 密码
	 * @return 登录成功返回User对象
	 */
	ServiceResult<User> login(String account, String password);
}