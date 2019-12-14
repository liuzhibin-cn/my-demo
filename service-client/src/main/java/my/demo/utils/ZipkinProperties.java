package my.demo.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="zipkin")
public class ZipkinProperties {
	private String serviceName;
	private String httpUrl;
	private int httpConnectTimeout;
	private int httpReadTimeout;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getHttpUrl() {
		return httpUrl;
	}
	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}
	public int getHttpConnectTimeout() {
		return httpConnectTimeout;
	}
	public void setHttpConnectTimeout(int httpConnectTimeout) {
		this.httpConnectTimeout = httpConnectTimeout;
	}
	public int getHttpReadTimeout() {
		return httpReadTimeout;
	}
	public void setHttpReadTimeout(int httpReadTimeout) {
		this.httpReadTimeout = httpReadTimeout;
	}
}