package com.springboot.config;

import com.springboot.auth.jwt.JwtTokenizer;
import org.springframework.context.annotation.Configuration;

//Spring Security를 설정하기 위한 클래스 (접근권한 등)
@Configuration
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;

}
