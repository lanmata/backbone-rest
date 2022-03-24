/*
 * @(#)$file.className.java.
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

package com.prx.backoffice.v1.report.api.client;


import com.prx.commons.util.DateUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DocumentRestClient.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 27-12-2021
 * @since 11
 */
public class DocumentRestClient {
    private static final String PLACEHOLDER_ABSOLUTE_PATH = "\\ambients\\tempo\\templates\\example_placeholders.txt";
    private static final String DOCUMENT_ABSOLUTE_PATH = "\\ambients\\tempo\\templates\\model_resignation.docx";
    private static final String JKS = "\\projects\\prx\\backbone-rest\\prx_backbone_rest.jks";
    private static final String SERVICE_URL = "https://localhost:8084/v1/report/template";
    private static final String DOCUMENT_BACKUP_PATH = "\\ambients\\tempo\\templates\\backup\\";
    private static final String EXTENSION = ".docx";
    private static final String TOKEN_COLON = ":";
    private static final String JKS_PASSWORD = "prx-backbone-rest";

    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        final var documentRestClient = new DocumentRestClient();
        documentRestClient.invokeDocumentApi();
    }

    private void invokeDocumentApi() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        var httpClient = getRestTemplate();
        var placeholders =  getPlaceholderMap();

        if(!placeholders.isEmpty()) {
            var templateFile = new File(DOCUMENT_ABSOLUTE_PATH);
            var multipart = MultipartEntityBuilder.create();
            var requestBuilder = RequestBuilder.get(SERVICE_URL);
            var dateFormatValue = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtil.PATTERN_DATETIME_YYMMDDHHMMSS));
            multipart.addBinaryBody("documentTemplate", templateFile);
            placeholders.forEach(multipart::addTextBody);
            requestBuilder.setEntity(multipart.build());
            var httpResponse = httpClient.execute(requestBuilder.build());

            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()){
                try(var bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(DOCUMENT_BACKUP_PATH + dateFormatValue + EXTENSION))){
                    bufferedOutputStream.write(httpResponse.getEntity().getContent().readAllBytes());
                }
            }
        }
    }

    private Map<String, String> getPlaceholderMap(){
        var placeholders = new HashMap<String,String>();
        try(var inputStream = new BufferedReader(new FileReader(PLACEHOLDER_ABSOLUTE_PATH))) {
            while(inputStream.ready()) {
                var value = inputStream.readLine();
                if(null != value && value.contains(TOKEN_COLON)) {
                    var values = value.split(TOKEN_COLON);
                    placeholders.put(values[0], values[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return placeholders;
    }

    private HttpClient getRestTemplate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        var mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        var messageConverters = new ArrayList<HttpMessageConverter<?>>();
        var supportedMediaTypes = new ArrayList<MediaType>();
        var httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        var sslContext = SSLContexts.custom().loadTrustMaterial(new File(JKS), JKS_PASSWORD.toCharArray()).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslConnectionSocketFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();
        var basicHttpClientConnectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        var httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(basicHttpClientConnectionManager).build();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        supportedMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
        messageConverters.add(mappingJackson2HttpMessageConverter);
        var restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
        restTemplate.setMessageConverters(messageConverters);

        return httpClient;
    }
}
