package my.demo.dao.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.mapping.StatementType;

import my.demo.domain.User;
import my.demo.domain.UserAccount;

@Mapper
public interface UserDao {
	@Select("select * from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	@ResultMap("userAccount")
	UserAccount getUserAccount(@Param("account") String account, @Param("accountHash") int accountHash);
	
	@Insert("insert into usr_user_account(account, password, user_id, account_hash) values (#{account}, #{password}, #{userId}, #{accountHash})")
	int createUserAccount(UserAccount userAccount);
	
	@Select("select * from usr_user where user_id=#{userId}")
	@ResultMap("user")
	User getUser(long userId);
	
	@Insert("insert into usr_user (nickname, mobile, email, created_at) values (#{nickname}, #{mobile}, #{email}, #{createdAt})")
	@SelectKey(before=false, keyColumn="user_id", keyProperty="userId", resultType=Long.class, statementType=StatementType.STATEMENT, statement="select last_insert_id() as user_id")
	int createUser(User user);
}