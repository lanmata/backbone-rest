package com.prx.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

//@EnableSwagger2
@FeignClient
@EnableEurekaClient
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.prx.persistence"})
@EntityScan(basePackages = {"com.prx.persistence"})
@ComponentScan(basePackages = {"com.prx.backoffice","com.prx.commons.config"})
@SpringBootApplication
public class PrxBackofficeRestApplication  extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(PrxBackofficeRestApplication.class, args);
	}

	@Bean
	public SecurityConfiguration securityConfiguration() {
		return SecurityConfigurationBuilder.builder()
				.enableCsrfSupport(true)
				.build();
	}

}
