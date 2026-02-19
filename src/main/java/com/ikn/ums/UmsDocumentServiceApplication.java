package com.ikn.ums;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.tenant.core.context.interceptor.RestTemplateInterceptor;
import com.ikn.ums.tenant.core.datasource.DataSourceConfig;

@Import({ DataSourceConfig.class })
@SpringBootApplication(scanBasePackages = { "com.ikn.ums", "com.ikn.ums.tenant.core",
		"com.ikn.ums.tenant.client" }, exclude = { DataSourceAutoConfiguration.class })
@EnableFeignClients(basePackages = "com.ikn.ums.tenant.client.feign")
public class UmsDocumentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UmsDocumentServiceApplication.class, args);
	}

	@Bean
	public ModelMapper createModelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper;
	}
	

    @Bean
    public RestTemplateInterceptor restTemplateInterceptor() {
        return new RestTemplateInterceptor();
    }

	@Bean(name = "internalRestTemplate")
	@LoadBalanced
	public RestTemplate internalRestTemplate(RestTemplateInterceptor interceptor) {
	    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
	    factory.setConnectTimeout(5000);
	    factory.setReadTimeout(5000);

	    RestTemplate rt = new RestTemplate(factory);
	    rt.setInterceptors(List.of(interceptor));
	    return rt;
	}

	@Bean(name = "externalRestTemplate")
	@Primary
	public RestTemplate externalRestTemplate() {
		return new RestTemplate();
	}
}
