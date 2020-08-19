package com.prx.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@FeignClient
@EnableEurekaClient
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.prx.persistence"})
@EntityScan(basePackages = {"com.prx.persistence"})
@SpringBootApplication(scanBasePackages = {"com.prx.backoffice","com.prx.commons.config"})
public class PrxBackofficeRestApplication  extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(PrxBackofficeRestApplication.class, args);
	}

}
