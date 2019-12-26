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

/**
 * <p>如果启用了Seata，在这里配置Seata数据源代理，拦截JDBC数据库操作实现AT事务管理，
 * 主要是使用Seata的{@link DataSourceProxy}创建Spring {@link DataSourceTransactionManager}和
 * MyBatis {@link SqlSessionFactory}。
 * 
 * <p>没有启用Seata时，该配置类不会生效，一切交给Spring、MyBatis自动配置即可。
 */
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
	//为Spring的DataSourceTransactionManager使用dataSourceProxy
    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(DataSourceProxy dataSourceProxy) {
        return new DataSourceTransactionManager(dataSourceProxy);
    }
    // 项目使用了mybatis，使用dataSourceProxy创建SqlSessionFactory bean
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