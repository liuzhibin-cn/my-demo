package my.demo.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simply 
 */
@Component
public class DbUtils {
	@Value("${mydemo.mysql.host}")
	String host;
	@Value("${mydemo.mysql.port}")
	String port;
	@Value("${mydemo.mysql.user}")
	String user;
	@Value("${mydemo.mysql.password}")
	String password;
	@Value("${mydemo.userDb}")
	String userDb;
	
	public Connection getUserDbConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String url = String.format("jdbc:mysql://%s:%s/%s?connectTimeout=2000&socketTimeout=2000&characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&useSSL=false", host, port, userDb);
		return DriverManager.getConnection(url, user, password); 
	}
	public int executeUpdate(Connection connection, String sql) throws SQLException {
		Statement stmt = null;
		int result = 0;
		try {
			stmt = connection.createStatement();
			result = stmt.executeUpdate(sql);
		} catch(Exception e) {
			e.printStackTrace();
			if(stmt!=null) stmt.close();
		} finally {
			if(stmt!=null) stmt.close();
		}
		return result;
	}
	public ResultSet execQuery(Connection connection, String sql) throws SQLException {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			return stmt.executeQuery(sql);
		} catch(Exception e) {
			e.printStackTrace();
			if(stmt!=null) stmt.close();
		}
		return null;
	}
}