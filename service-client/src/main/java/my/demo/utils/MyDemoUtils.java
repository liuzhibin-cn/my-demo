package my.demo.utils;

import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.springframework.util.ClassUtils;

import io.seata.core.context.RootContext;

public class MyDemoUtils {
	private static boolean skywalkingPresent = ClassUtils.isPresent("org.apache.skywalking.apm.toolkit.trace.ActiveSpan", ClassUtils.getDefaultClassLoader());
	private static boolean seataPresent = ClassUtils.isPresent("io.seata.core.context.RootContext", ClassUtils.getDefaultClassLoader());
	
	/**
	 * 启用SkyWalking时，在当前Span中添加一个Tag。未启用SkyWalking不做任何操作
	 * @param name
	 * @param value
	 */
	public static void tag(String name, Object value) {
		if(skywalkingPresent) {
			if(value==null) ActiveSpan.tag(name, "null");
			else ActiveSpan.tag(name, value.toString());
		}
	}

	/**
	 * 是否启用了Seata
	 * @return
	 */
	public static boolean isSeataPresent() {
		return seataPresent;
	}
	/**
	 * 获取Seata全局事务XID
	 * @return
	 */
	public static String getXID() {
		return RootContext.getXID();
	}
}