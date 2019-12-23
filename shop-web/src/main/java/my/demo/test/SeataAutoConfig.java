package my.demo.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.seata.spring.annotation.GlobalTransactionScanner;

/**
 * @Author: heshouyou
 * @Description  seata global configuration
 * @Date Created in 2019/1/24 10:28
 */
@Configuration
public class SeataAutoConfig {

    /**
     * init global transaction scanner
     *
     * @Return: GlobalTransactionScanner
     */
    @Bean
    public GlobalTransactionScanner globalTransactionScanner(){
        return new GlobalTransactionScanner("shop-web", "my_demo_gtx");
    }
}
