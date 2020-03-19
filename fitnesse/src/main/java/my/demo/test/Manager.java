package my.demo.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;

import my.demo.service.ItemService;
import my.demo.service.UserService;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages={"my.demo.test"})
@EnableDubbo(scanBasePackages = { "my.demo.test" })
public class Manager {
	static ConfigurableApplicationContext context = null;
	@Reference
	UserService userService;
	@Reference
	ItemService itemService;
	private static void startSpringBoot() {
		context = new SpringApplicationBuilder(Manager.class).web(WebApplicationType.NONE).run();
	}
	public static UserService getUserService() {
		if(context==null) {
			synchronized (Manager.class) {
				if(context==null) {
					startSpringBoot();
				}
			}
		}
		return context.getBean(Manager.class).userService;
	}
	public static ItemService getItemService() {
		if(context==null) {
			synchronized (Manager.class) {
				if(context==null) {
					startSpringBoot();
				}
			}
		}
		return context.getBean(Manager.class).itemService;
	}
	
	@Autowired
	DbUtils db;
	public static DbUtils getDbUtils() {
		if(context==null) {
			synchronized (Manager.class) {
				if(context==null) {
					startSpringBoot();
				}
			}
		}
		return context.getBean(Manager.class).db;
	}
}