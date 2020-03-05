package my.demo.utils;

import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.springframework.util.ClassUtils;

import io.seata.core.context.RootContext;

public class MyDemoUtils {
	private static boolean skywalkingPresent = ClassUtils.isPresent("org.apache.skywalking.apm.toolkit.trace.ActiveSpan", ClassUtils.getDefaultClassLoader());
	private static boolean seataPresent = ClassUtils.isPresent("io.seata.core.context.RootContext", ClassUtils.getDefaultClassLoader());
	
	/**
	 * Add a user defined tag in current span if SkyWalking is enabled
	 * @param name
	 * @param value
	 */
	public static void tag(String name, Object value) {
		if(skywalkingPresent) {
			if(value==null) ActiveSpan.tag(name, "null");
			else ActiveSpan.tag(name, value.toString());
		}
	}

	public static boolean isSeataPresent() {
		return seataPresent;
	}
	/**
	 * XID: Seata global transaction ID
	 * @return
	 */
	public static String getXID() {
		return RootContext.getXID();
	}
}