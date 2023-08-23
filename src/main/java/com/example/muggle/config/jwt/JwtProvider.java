package com.example.muggle.config.jwt;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.muggle.user.dto.TokenDTO;
import com.example.muggle.user.entity.UserEntity;
import com.example.muggle.user.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
	
	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_TYPE = "bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;
	
	private UserService userService;
	
	@Value("${jwt.secretKey}")
    private String secretKey;
	
    /*public JwtProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	} */
	
	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}
    
    //JW 토큰 생성
    public String createToken(String userPk, String roles) {
    	
    	Claims claims = Jwts.claims().setSubject(userPk);
    	claims.put("roles", roles);
    	Date now = new Date();
    	
    	return Jwts.builder()
    			.setClaims(claims)
    			.setIssuedAt(now)
    			.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
    			.signWith(SignatureAlgorithm.HS256, secretKey)
    			.compact();
    }
   
//    public TokenDTO generateTokenDto(Authentication authentication) {
//    	//권한들 가져오기
//    	String authorities = authentication.getAuthorities().stream()
//    			.map(GrantedAuthority::getAuthority)
//    			.collect(Collectors.joining(","));
//    	
//    	long now = (new Date()).getTime();
//    	
//    	//Access Token 생성
//    	Date tokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
//    	
//    	System.out.println(tokenExpiresIn);
//    	
//    	String accessToken = Jwts.builder()
//    			.setSubject(authentication.getName())
//    			.claim(AUTHORITIES_KEY, authorities)
//    			.setExpiration(tokenExpiresIn)
//    			.signWith(key, SignatureAlgorithm.HS512)
//    			.compact();
//    	
//    	return TokenDTO.builder()
//    			.grantType(accessToken)
//    			.tokenExpiresIn(tokenExpiresIn.getTime())
//    			.build();
//    }
    
    public Authentication getAuthentication(String accessToken) {

    	UserEntity userDetails = userService.loadUserByUsername(this.getUserPk(accessToken));
    	
    	return new UsernamePasswordAuthenticationToken(userDetails.getEmail(),"",userDetails.getAuthorities());
    }
    
    public String getUserPk(String token) {
    	
    	return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }
    
 // Request의 Header에서 token 값을 가져옵니다. "X-AUTH-TOKEN" : "TOKEN값'
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("token");
    }
    
    public boolean validateToken(String token) {
        try {
            //Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        	Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
    
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
