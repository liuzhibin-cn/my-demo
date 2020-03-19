package my.demo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import my.demo.service.HelloService;
import my.demo.utils.MyDemoUtils;

@Controller
public class HelloController {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Reference
	HelloService helloService;
	
	@Value("${mydemo.version}")
	String version;
	@Value("${mydemo.hostname}")
	String hostName;
	
	@GetMapping(value="/hello/{name}")
	public @ResponseBody String hello(@PathVariable(name="name") String name) {
		return helloService.hello(name) + ", WEB: " + version + " - " + MyDemoUtils.getIpByHostName(hostName);
	}
}