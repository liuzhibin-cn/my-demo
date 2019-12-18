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
	// ================================================================
	// table: usr_user_account:
	// primary key: account
	// sharding fied: account_hash Math.abs(account.hashCode())
	// user_id: The value is auto-generated using Mycat's global sequence or Sharding-Proxy's id-generator when inserting into usr_user_account
	// ================================================================
	@Select("select * from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	@ResultMap("userAccount")
	UserAccount getUserAccount(@Param("account") String account, @Param("accountHash") int accountHash);
	
	//SQL for Mycat
	@Insert("insert into usr_user_account(account, password, user_id, account_hash) values (#{account}, #{password}, next value for MYCATSEQ_USER, #{accountHash})")
//	//SQL for Sharding-Proxy
//	@Insert("insert into usr_user_account(account, password, account_hash) values (#{account}, #{password}, #{accountHash})")
	@SelectKey(before=false, keyColumn="user_id", keyProperty="userId", resultType=Long.class
		, statementType=StatementType.PREPARED
    	, statement="select user_id from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	int createUserAccount(UserAccount userAccount);
	
	// ================================================================
	// table: usr_user
	// primary key: user_id
	// sharding key: user_id
	// ================================================================
	@Select("select * from usr_user where user_id=#{userId}")
	@ResultMap("user")
	User getUser(long userId);
	
	@Insert("insert into usr_user (user_id, nickname, mobile, email, created_at) values (#{userId}, #{nickname}, #{mobile}, #{email}, #{createdAt})")
	int createUser(User user);
}