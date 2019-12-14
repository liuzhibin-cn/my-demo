package my.demo.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.sampler.Sampler;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
public class TracingExtensionFactory {
	@Autowired
	ZipkinProperties properties;
	
	@Bean
	public Tracing tracing(){
		Sender sender = OkHttpSender.create(properties.getHttpUrl());
		AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
			.closeTimeout(properties.getHttpConnectTimeout(), TimeUnit.MILLISECONDS)
			.messageTimeout(properties.getHttpReadTimeout(), TimeUnit.MILLISECONDS)
			.build();
		Tracing tracing = Tracing.newBuilder()
			.localServiceName(properties.getServiceName())
			.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "shiliew"))
			.sampler(Sampler.ALWAYS_SAMPLE)
			.spanReporter(reporter)
			.build();
		return tracing;
	}
}