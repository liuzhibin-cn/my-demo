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
	// 用户登录账号表user_account: 主键account，分片字段account_hash
	// user_id通过mycat全局序列USER生成
	// ================================================================
	@Select("select * from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	@ResultMap("userAccount")
	UserAccount getUserAccount(@Param("account") String account, @Param("accountHash") int accountHash);
	
	@Insert("insert into usr_user_account(account, password, user_id, account_hash) values (#{account}, #{password}, next value for MYCATSEQ_USER, #{accountHash})")
	@SelectKey(before=false, keyColumn="user_id", keyProperty="userId", resultType=Integer.class
		, statementType=StatementType.PREPARED
    	, statement="select user_id from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	int createUserAccount(UserAccount userAccount);
	
	// ================================================================
	// 用户表: 主键和分片字段均为user_id
	// ================================================================
	@Select("select * from usr_user where user_id=#{userId}")
	@ResultMap("user")
	User getUser(long userId);
	
	@Insert("insert into usr_user (user_id, nickname, mobile, email, created_at) values (#{userId}, #{nickname}, #{mobile}, #{email}, #{createdAt})")
	int createUser(User user);
}