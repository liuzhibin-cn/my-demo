package my.demo.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;

@Configuration
@EnableConfigurationProperties({DruidProperties.class, DataSourceProperties.class})
@ConditionalOnClass( DruidDataSource.class )
@ConditionalOnProperty({ "spring.datasource.url" })
public class DruidConfiguration {
	@Autowired
	DruidProperties druidProperties;
    @Autowired
    DataSourceProperties dataSourceProperties;
	
    @Bean
    public Slf4jLogFilter logFilter() {
    	Slf4jLogFilter filter = new Slf4jLogFilter();
    	//Log operations on ResultSet
    	filter.setResultSetLogEnabled(false);
    	filter.setDataSourceLogEnabled(false);
    	//Log operations on Connection
    	filter.setConnectionLogEnabled(false); 				//false: Disable all logs on connection events
//    	filter.setConnectionConnectBeforeLogEnabled(false);	
//    	filter.setConnectionConnectAfterLogEnabled(true); 	
//    	filter.setConnectionCommitAfterLogEnabled(true); 	
//    	filter.setConnectionRollbackAfterLogEnabled(true); 	
//    	filter.setConnectionCloseAfterLogEnabled(false); 	
    	//Log operations on Statement
    	filter.setStatementLogEnabled(true);
    	filter.setStatementCreateAfterLogEnabled(false);	
    	filter.setStatementParameterSetLogEnabled(false);	
    	filter.setStatementParameterClearLogEnable(false);	
    	filter.setStatementCloseAfterLogEnabled(false);		
    	filter.setStatementPrepareAfterLogEnabled(false);	
    	return filter;
    }
    
    @Bean
    @Primary
    public DruidDataSource druidDataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        druidDataSource.setInitialSize(druidProperties.getInitialSize());
        druidDataSource.setMaxActive(druidProperties.getMaxActive());
        druidDataSource.setMaxWait(druidProperties.getMaxWait());
        druidDataSource.setMinIdle(druidProperties.getMinIdle());
        druidDataSource.setValidationQuery(druidProperties.getValidationQuery());
        druidDataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        druidDataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        druidDataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        druidDataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        druidDataSource.setRemoveAbandoned(druidProperties.isRemoveAbandoned());
        druidDataSource.setRemoveAbandonedTimeout(druidProperties.getRemoveAbandonedTimeout());
        druidDataSource.setLogAbandoned(druidProperties.isLogAbandoned());
        
        druidDataSource.setDefaultAutoCommit(druidProperties.isDefaultAutoCommit());
        druidDataSource.setDefaultTransactionIsolation(druidProperties.getDefaultTransactionIsolation());
        
        try {
			druidDataSource.setFilters("stat");
			List<Filter> filters = new ArrayList<>();
			filters.add(logFilter());
			druidDataSource.setProxyFilters(filters);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return druidDataSource;
    }
}