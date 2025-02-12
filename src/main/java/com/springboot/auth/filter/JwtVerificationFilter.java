package com.springboot.auth.filter;

import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.AuthorityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//클라이언트의 요청으로부터 받은 JWT를 검증하는 클래스
public class JwtVerificationFilter extends OncePerRequestFilter {
    //JWT를 검증하고 Claims(토큰에 포함된 정보)를 얻는데 사용하기 위해 DI
    private final JwtTokenizer jwtTokenizer;
    //JWT 검증을 성공하면 Authenticaiton 객체에 채울 사용자의 권한을 생성하기 위해 DI
    private final AuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, AuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    //실제 검증을 진행하는 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //서명 검증에서 발생할 수 있는 Exception 예외 처리
        //예외가 발생하면 SecurityContext에 클라이언트 인증정보(Authentication 객체)가 저장되지 않는다.
        //저장되지 않으면, Security Filter 내부에서 AuthenticationException이 발생 -> AuthenticationEntryPoint가 처리
        //발생한 예외를 HttpServletRequset의 애트리뷰트로 추가한다.
        try{
            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        }catch (SignatureException se){
            request.setAttribute("exception",se);
        }catch (ExpiredJwtException ee){
            request.setAttribute("exception",ee);
        }catch (Exception e){
            request.setAttribute("exception",e);
        }
    }

    private Map<String, Object> verifyJws(HttpServletRequest request){
        //로그인 인증이 성공되면 서버 측에서 Authorization Header에 JWT를 추가했다.
        //클라이언트가 response header로 전달받은 JWT를 request header에 추가해서 서버 측에 전송
        //request의 header에서 JWT를 얻어오며, replace()메서드를 통해 Bearer부분을 제거
        String jws = request.getHeader("Authorization").replace("Bearer ", "");

        //JWT 서명을 검증하기 위한 Secret Ket를 얻어온다.
        String base64EncodedSecretKey = jwtTokenizer.encodedBase64SecretKey(jwtTokenizer.getSecretKey());
        //JWT에서 Claims를 파싱한다. Claims가 정상적으로 파싱되면 서명 검증이 성공된 것
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
        return claims;
    }

    //Authentication 객체를 SecurityContext에 저장하기 위한 메서드
    private void setAuthenticationToContext(Map<String, Object> claims) {
        //JWT에서 파싱한 Claims에서 username을 얻어온다.
        String username = (String) claims.get("username");

        //JWT의 Claims에서 얻은 권한 정보를 기반으로 List<GrantedAuthority>생성
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List) claims.get("roles"));

        //username과 List<GrantedAuthority>를 포함한 Authentication 객체를 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

        //SecurityContext에 Authentication 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //특정 조건에 부합(true)하면 해당 filter의 동작이 수행하지 않고 다음 filter로 건너뛰게 해준다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //request header에서 Authorization의 값을 얻어온다.
        String authorization = request.getHeader("Authorization");

        //만약 null 또는 Bearer로 시작하지 않는다면 해당 Filter의 동작은 수행하지 않도록 한다.
        //즉, JWT가 Authorization header에 포함되지 않았다면
        //JWT 자격증명이 필요없는 리소스에 대한 요청이라고 판단하여 건너뛰는 것
        return authorization == null || !authorization.startsWith("Bearer");
    }
}
