package my.demo.utils;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.alibaba.druid.pool.DruidDataSource;

import io.seata.rm.datasource.DataSourceProxy;

@Configuration
@ConditionalOnClass({ DataSourceProxy.class, SqlSessionFactory.class })
@AutoConfigureBefore(MybatisAutoConfiguration.class)
@AutoConfigureAfter(DruidConfiguration.class)
@EnableTransactionManagement
public class SeataConfiguration implements TransactionManagementConfigurer {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Value("${mybatis.mapperLocations}")
	String mapperLocations;
	@Autowired
	DruidDataSource druidDataSource;
	
	@Bean
    public DataSourceProxy dataSourceProxy(DruidDataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }
    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(DataSourceProxy dataSourceProxy) {
        return new DataSourceTransactionManager(dataSourceProxy);
    }
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {
    	log.info("[seata-configuration] Mybatis sqlSessionFactory created, mapperLocations: " + mapperLocations);
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSourceProxy);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        factoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return factoryBean.getObject();
    }
    
	@Override
	public TransactionManager annotationDrivenTransactionManager() {
		return transactionManager(dataSourceProxy(druidDataSource));
	}
}