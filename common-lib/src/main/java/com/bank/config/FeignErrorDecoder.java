package com.bank.config;

import com.bank.exception.ServiceException;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import com.bank.exception.ServiceException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String message = "External Service Error";

        try {

            if (response.body() != null) {

                message = Util.toString(response.body().asReader());

            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return new ServiceException(response.status(), message);
    }
}