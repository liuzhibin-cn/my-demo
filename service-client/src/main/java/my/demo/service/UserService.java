package my.demo.service;

import my.demo.entity.User;

public interface UserService {
	ServiceResult<User> registerByMobile(String mobile, String password);
	ServiceResult<User> login(String account, String password);
	ServiceResult<User> getUser(long userId);
}