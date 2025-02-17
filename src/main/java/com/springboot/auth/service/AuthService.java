package com.springboot.auth.service;

import com.springboot.auth.jwt.JwtTokenizer;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtTokenizer jwtTokenizer;

    public AuthService(JwtTokenizer jwtTokenizer) {
        this.jwtTokenizer = jwtTokenizer;
    }

    public void logout(String username){
        boolean isDeleted = jwtTokenizer.deleteRegisterToken(username);

        if(!isDeleted) throw new RuntimeException();
    }

}
