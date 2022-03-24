/*
 * @(#)MockLoaderBase.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */
package com.prx.backoffice;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

/**
 * MockLoaderBase.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 19-02-2021
 */
@SpringBootTest
@ActiveProfiles("local")
@RunWith(MockitoJUnitRunner.class)
@MockServerSettings(perTestSuite = true)
@ExtendWith(value = {MockServerExtension.class})
public class MockLoaderBase {

	@Autowired
	WebApplicationContext applicationContext;

	/** URL de prueba */
	public static final String TEST_URL = "https://localhost:";
	/** clientAndServer */
	private ClientAndServer clientAndServer;

	@BeforeEach
	void init(){
		RestAssuredMockMvc.webAppContextSetup(applicationContext);
	}

	/**
	 * Carga un archivo de Respuesta.
	 *
	 * @param fileName Nombre Archivo
	 * @return Archivo Respuesta
	 */
	public static byte[] loadResponse(final String fileName) {
		try {
			final var in = MockLoaderBase.class.getClassLoader().getResourceAsStream(fileName);
			return IOUtils.toByteArray(in);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Response doesn't exist", e);
		}
	}

	/**
	 * Carga un archivo de Respuesta del directorio soap.
	 *
	 * @param responseName Nombre Archivo {@link String}
	 * @return Archivo Respuesta {@link Byte}
	 */
	public static byte[] loadSoapResponse(final String responseName) {
		return MockLoaderBase.loadResponse("soap/" + responseName);
	}

	/**
	 * Carga un archivo de Respuesta del directorio soap en formato String.
	 *
	 * @param responseName Nombre Archivo {@link String}
	 * @return Archivo Respuesta {@link String}
	 */
	public static String loadSoapStringResponse(final String responseName) {
		return new String(MockLoaderBase.loadSoapResponse(responseName), StandardCharsets.UTF_8);
	}

	/**
	 * Carga un archivo de Respuesta del directorio json.
	 *
	 * @param responseName Nombre Archivo {@link String}
	 * @return Archivo Respuesta {@link Byte}
	 */
	public static byte[] loadJsonResponse(final String responseName) {
		return MockLoaderBase.loadResponse("json/" + responseName);
	}

	/**
	 * Carga un archivo de Respuesta del directorio json en formato String.
	 *
	 * @param responseName Nombre Archivo {@link String}
	 * @return Archivo Respuesta {@link String}
	 */
	public static String loadJsonStringResponse(final String responseName) {
		return new String(MockLoaderBase.loadJsonResponse(responseName), StandardCharsets.UTF_8);
	}

	@BeforeEach
	public void init(final ClientAndServer clientAndServer) {
		MockitoAnnotations.openMocks(this);
		this.clientAndServer = clientAndServer;
		this.clientAndServer.reset();
		final var mockHttpServletRequest = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
	}

	/**
	 * Realiza mock simple sólo con Endpoint y Body.
	 *
	 * @param endpoint Endpoint {@link String}
	 * @param body Cuerpo Respuesta {@link String}
	 */
	public void simpleMock(final String endpoint, final String body) {
		final var contentType = MediaType.TEXT_PLAIN.toString();
		mock(null, endpoint, HttpStatusCode.OK_200.code(), contentType, body);
	}

	/**
	 * Realiza mock para archivos dentro del folder soap.
	 *
	 * @param endpoint Endpoint {@link String}
	 * @param file Archivo {@link String}
	 */
	public void soapMock(final String endpoint, final String file) {
		final var contentType = MediaType.TEXT_XML_UTF_8.toString();
		final var body = MockLoaderBase.loadSoapStringResponse(file);
		mock(HttpMethod.POST.name(), endpoint, HttpStatusCode.OK_200.code(), contentType, body);
	}

	/**
	 * Realiza mock para archivos dentro del folder json.
	 *
	 * @param endpoint Endpoint {@link String}
	 * @param response Respuesta {@link String}
	 */
	public void jsonMock(final String endpoint, final String response) {
		jsonMock(HttpMethod.POST.name(), endpoint, HttpStatusCode.OK_200.code(), response);
	}

	/**
	 * Realiza mock para archivos dentro del folder json.
	 *
	 * @param endpoint Endpoint {@link String}
	 * @param status Estado Http {@link Integer}
	 * @param response Respuesta {@link String}
	 */
	public void jsonMock(final String endpoint, final int status, final String response) {
		jsonMock(HttpMethod.POST.name(), endpoint, status, response);
	}

	/**
	 * Realiza mock para archivos dentro del folder json.
	 *
	 * @param method Metodo Http {@link String}
	 * @param endpoint Endpoint {@link String}
	 * @param status Estado Http {@link Integer}
	 * @param response Respuesta {@link String}
	 */
	public void jsonMock(final String method, final String endpoint, final int status, final String response) {
		final var contentType = MediaType.APPLICATION_JSON_UTF_8.toString();
		final var body = MockLoaderBase.loadJsonStringResponse(response);
		mock(method, endpoint, status, contentType, body);
	}

	/**
	 * setupMockServer.
	 *
	 * @param method Metodo {@link String}
	 * @param endpoint Endpoint {@link String}
	 * @param status Estado {@link Integer}
	 * @param contentType ContentType {@link String}
	 * @param body Cuerpo Respuesta {@link String}
	 */
	public void mock(final String method, final String endpoint, final int status, final String contentType,
			final String body) {
		var rq = HttpRequest.request();
		if (StringUtils.isNoneBlank(method)) {
			rq = rq.withMethod(method);
		}
		this.clientAndServer.when(rq.withPath(endpoint)).respond(HttpResponse.response().withStatusCode(status)
				.withHeader(new Header(HttpHeaders.CONTENT_TYPE, contentType)).withBody(body));
	}

	/**
	 * Obtiene puerto mock server.
	 *
	 * @return {@link Integer}
	 */
	public int getPort() {
		return this.clientAndServer.getPort();
	}

}
