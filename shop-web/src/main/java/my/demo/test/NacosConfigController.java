package my.demo.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.nacos.api.config.annotation.NacosValue;

@Controller
@RequestMapping(value = "/nacos")
public class NacosConfigController {
	@NacosValue(value = "${store.mode:}", autoRefreshed = true)
	String host;
	@NacosValue(value = "${store.db.user:}", autoRefreshed = true)
	String port;
	
	@GetMapping(path = "/get-config")
	public @ResponseBody String getConfig() {
		return host + ":" + port;
	}
}