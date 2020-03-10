package my.demo.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
	Logger log = LoggerFactory.getLogger(getClass());
	@Value("${mydemo.version}")
	String version;
	@Value("${mydemo.hostname}")
	String hostName;
	
	@GetMapping(value="/hello/{name}")
	public @ResponseBody String hello(@PathVariable(name="name") String name) {
		return "Hello " + name + ". " + version + " - " + getIpByHostName(hostName);
	}

	private String getIpByHostName(String hostName) {
		InetAddress addr = null;
		if(hostName!=null && !hostName.isEmpty() && !"localhost".equals(hostName.trim().toLowerCase())) {
			try {
				addr = InetAddress.getByName(hostName);
				return addr.getHostAddress();
			} catch (UnknownHostException e) {
			}
		}
		try {
			addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			return "";
		}
	}
}