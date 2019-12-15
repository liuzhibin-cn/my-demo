package my.demo.service.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages={"my.demo.service.order", "my.demo.utils"})
@MapperScan(basePackages = { "my.demo.dao.order" })
@DubboComponentScan(basePackages = { "my.demo.service.order" })
public class Application {
	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
	}
} 