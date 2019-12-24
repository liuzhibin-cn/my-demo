package my.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return druidDataSource;
    }
}