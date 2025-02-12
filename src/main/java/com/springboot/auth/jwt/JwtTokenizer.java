package com.springboot.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

//JWT 생성, 검증, 발급하는 클래스
@Component
public class JwtTokenizer {
    //JWT 생성 및 검증에 사용되는 Secret Key 정보
    @Getter
    @Value("${jwt.key}")
    private String secretKey;

    //Access Token 만료 시간 정보
    @Getter
    @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    //Refresh Token 만료 시간 정보
    @Getter
    @Value("${jwt.refresh-token-expiration-minutes}")
    private int refreshTokenExpirationMinutes;

    public String encodedBase64SecretKey(String secretKey){
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //JWT의 서명에 사용할 Secret Key를 생성하는 메서드
    private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
        //인코딩된 Secrey Key를 디코딩하여 byte 배열을 반환
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);

        //key객체를 생성한다.
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return key;
    }

    //액세스 토큰 생성 메서드
    public String generateAccessToken(Map<String, Object> claims,
                                      String subject,
                                      Date expiration,
                                      String base64EncodedSecretKey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                //JWT에 포함시킬 Custom Claims를 추가 ( 인증된 사용자와 관련된 정보가 저장되어있다.)
                .setClaims(claims)
                //JWT 제목, 발행일자, 만료일시
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                //서명을 위한 key 객체를 설정
                .signWith(key)
                .compact();
    }

    //리플래쉬 토큰 생성 메서드
    public String generateRefreshToken(String subject,
                                       Date expiration,
                                       String base64EncodedSecretKey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    //JWT를 검증하여 파싱된 Claims를 반환하는 메서드
    public Jws<Claims> getClaims(String jws, String base64EncodedSecretkey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretkey);

        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
        return claims;
    }

    //JWT 검증 메서드
    //JWT에 포함되어있는 Signature를 검증하여 JWT의 위/변조 여부를 확인
    public void verifySignature(String jws, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        Jwts.parserBuilder()
                //서명에 사용된 key를 이용해 내부적으로 시그니처를 검증
                //검증에 성공하면 JWT를 파싱하여 claims를 얻어온다.
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
    }

    //JWT의 만료 일시를 지정하기 위한 메서드, JWT 생성시 사용된다.
    public Date getTokenExpiration(int expirationMinutes){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        Date expiration = calendar.getTime();

        return expiration;
    }
}
