package my.demo.dao.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import my.demo.entity.User;
import my.demo.entity.UserAccount;

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
	
	/**
	 * 注意：
	 * <ol>
	 * <li>user_id值由底层数据库自动生成，不使用MyCat、Sharding-Proxy时，将user_id设为MySQL的自增字段；</li>
	 * <li>使用MyCat全局序列时，可以通过MySQL的last_insert_id()、GENERATED_KEY返回user_id值，参考docs/Sharding-Mycat-Overview-Quickstart.md；</li>
	 * <li>使用Sharding-Proxy时，只能通过MySQL的GENERATED_KEY返回user_id值，参考Sharding-Sharding-Proxy-Overview-Quickstart.md；</li>
	 * </ol> 
	 * @param user
	 * @return
	 */
	@Insert("insert into usr_user (nickname, mobile, email, created_at) values (#{nickname}, #{mobile}, #{email}, #{createdAt})")
	@Options(useGeneratedKeys=true, keyProperty="userId", keyColumn="user_id")
	int createUser(User user);
}