package my.demo.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.nacos.api.config.annotation.NacosValue;

@Controller
@RequestMapping(value = "/nacos")
public class NacosConfigController {
	@NacosValue(value = "${host:127.0.0.1}", autoRefreshed = true)
	String host;
	@NacosValue(value = "${port:9099}", autoRefreshed = true)
	int port;
	
	@GetMapping(path = "/get-config")
	public @ResponseBody String getConfig() {
		return host + ":" + port;
	}
}