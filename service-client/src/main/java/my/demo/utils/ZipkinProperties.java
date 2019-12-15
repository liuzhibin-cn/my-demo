package my.demo.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="zipkin")
public class ZipkinProperties {
	@Value("${dubbo.application.name}")
	private String serviceName;
	private String server;
	private int connectTimeout;
	private int readTimeout;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String httpUrl) {
		this.server = httpUrl;
	}
	public int getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(int httpConnectTimeout) {
		this.connectTimeout = httpConnectTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int httpReadTimeout) {
		this.readTimeout = httpReadTimeout;
	}
}