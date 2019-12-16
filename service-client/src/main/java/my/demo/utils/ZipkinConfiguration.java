package my.demo.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.dubbo.rpc.TracingFilter;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
@EnableConfigurationProperties({ZipkinProperties.class})
@ConditionalOnClass(TracingFilter.class)
public class ZipkinConfiguration {
	@Autowired
	ZipkinProperties properties;
	
	@Bean
	public Tracing tracing() {
		Sender sender = OkHttpSender.create(properties.getServer());
		AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
			.closeTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
			.messageTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
			.build();
		Tracing tracing = Tracing.newBuilder()
			.localServiceName(properties.getServiceName())
			.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "brave-trace"))
			.sampler(Sampler.ALWAYS_SAMPLE)
			.spanReporter(reporter)
			.currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().addScopeDecorator(MDCScopeDecorator.create()).build())
			.build();
		return tracing;
	}
}