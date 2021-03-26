/*
 *
 *  * @(#)PrxBackofficeRestApplication.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */

package com.prx.backoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * MessageUtilTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 26-10-2020
 */
@FeignClient
@SpringBootApplication
@EnableEurekaClient
@RequiredArgsConstructor
@EntityScan(basePackages = {"com.prx.persistence"})
@EnableJpaRepositories(basePackages = {"com.prx.persistence"})
@ComponentScan(basePackages = {"com.prx.backoffice", "com.prx.commons.config", "com.prx.commons.properties"})
public class PrxBackofficeRestApplication {
	public static void main(String[] args) {
		SpringApplication.run(PrxBackofficeRestApplication.class, args);
	}
}
