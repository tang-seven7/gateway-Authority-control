package com.seven.springcloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class RestTemplateConfig extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());
        if (statusCode == null) {
            throw new UnknownHttpStatusCodeException(response.getRawStatusCode(), response.getStatusText(),
                    response.getHeaders(), getResponseBody(response), getCharset(response));
        }
        handleError(response, statusCode);
    }

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        switch (statusCode.series()) {
            case CLIENT_ERROR:
                HttpClientErrorException exp1 = new HttpClientErrorException(statusCode, response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
                log.error("客户端调用异常",exp1);
                throw  exp1;
            case SERVER_ERROR:
                HttpServerErrorException exp2 = new HttpServerErrorException(statusCode, response.getStatusText(),
                        response.getHeaders(), getResponseBody(response), getCharset(response));
                log.error("服务端调用异常",exp2);
                throw exp2;
            default:
                UnknownHttpStatusCodeException exp3 = new UnknownHttpStatusCodeException(statusCode.value(), response.getStatusText(),
                        response.getHeaders(), getResponseBody(response), getCharset(response));
                log.error("网络调用未知异常");
                throw exp3;
        }
    }

    @Bean
    public RestTemplate restTemplate(@Qualifier("simpleClientHttpRequestFactory") ClientHttpRequestFactory factory){
        RestTemplate restTemplate = new RestTemplate(factory);
        //配置中文utf-8
        restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(5000);
        return factory;
    }
}
