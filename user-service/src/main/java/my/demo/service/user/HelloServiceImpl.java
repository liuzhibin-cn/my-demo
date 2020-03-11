package my.demo.service.user;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;

import my.demo.service.HelloService;
import my.demo.utils.MyDemoUtils;

@Service
public class HelloServiceImpl implements HelloService {
	@Value("${mydemo.hostname}")
	String hostName;
	SimpleDateFormat sdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss SSS] ");
	
	@Override
	public String hello(String name) {
		return sdf.format(new Date()) + "Hello " + name + " from SVC: " + MyDemoUtils.getIpByHostName(hostName);
	}

}