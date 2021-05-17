package com.goodcookie.goodcookiebackend.exception;


/**
 * Exception class to handle application errors
 */
public class GoodCookieBackendException extends RuntimeException {
    public GoodCookieBackendException(String message, Exception exception){
        super(message,exception);
    }
    public GoodCookieBackendException(String message){
        super(message);
    }
}
