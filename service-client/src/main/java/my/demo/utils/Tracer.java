package my.demo.utils;

import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;

public class Tracer {
	private static boolean checked = false;
	private static boolean skywalkingLoaded = false;
	
	/**
	 * 启用SkyWalking时在调用链当前Span中添加tag项，未启用时不做任何处理（PinPoint、ZipKin都不支持）。
	 * @param name tag名称
	 * @param value tag值
	 */
	public static void traceTag(String name, Object value) {
		//检查是否加载了SkyWalking依赖项
		if(!checked) {
			synchronized (Tracer.class) {
				if(!checked) {
					try {
						@SuppressWarnings("rawtypes")
						Class clz = Class.forName("org.apache.skywalking.apm.toolkit.trace.ActiveSpan");
						skywalkingLoaded = clz!=null;
					} catch (ClassNotFoundException e) {
					}
					checked = true;
				}
			}
		}
		if(skywalkingLoaded) {
			if(value==null) ActiveSpan.tag(name, "null");
			else ActiveSpan.tag(name, value.toString());
		}
	}
}