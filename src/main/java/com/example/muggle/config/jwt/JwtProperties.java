package com.example.muggle.config.jwt;

public interface JwtProperties {

	String SECRET = "Muggle";
    int EXPIRATION_TIME =  864000000;
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
}
