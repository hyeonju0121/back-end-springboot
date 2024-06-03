package com.mycompany.webapp.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {
	// the key would be read from your application configuration instead.
	// https://github.com/jwtk/jjwt?tab=readme-ov-file#quickstart

	// 필드
	// 서명 및 암호화를 위한 SecretKey
	private SecretKey secretKey;
	// AccessToken 의 유효 기간 (단위: 밀리세컨)
	private long accessTokenDuration = 24 * 60 * 60 * 1000;
	// 30분 뒤 -> 30 * 60 * 1000

	// 생성자 --------------------------------------------------
	public JwtProvider(@Value("${jwt.security.key}") String jwtSecurityKey) {
		log.info("jwtSecurityKey={}", jwtSecurityKey);
		try {
			// Application.property에서 문자열 키를 읽고, secretKey를 생성
			secretKey = Keys.hmacShaKeyFor(jwtSecurityKey.getBytes("UTF-8"));
		} catch (Exception e) {
			log.info(e.toString());
		}
	}

	// AccessToken 생성
	// email 도 저장하고 싶다면, 매개변수에 이메일 정보 추가 
	// .claim("email", email)
	public String createAccessToken(String userId, String authority) {
		String token = null;
		try {
			JwtBuilder builder = Jwts.builder();
			// header 설정
			// 자동으로 설정

			// payload 설정
			builder.subject(userId);
			builder.claim("authority", authority);
			builder.expiration(new Date(new Date().getTime() + accessTokenDuration));

			// signature 설정
			builder.signWith(secretKey);
			token = builder.compact();
		} catch (Exception e) {
			log.info(e.toString());
		}
		return token;
	}

	// Access Token 유효성 검증
	public Jws<Claims> validateToken(String token) {
		Jws<Claims> jws = null;
		try {
			// JWT 파서 빌더 생성
			JwtParserBuilder builder = Jwts.parser();
			// JWT 파서 빌더에 비밀키 설정
			builder.verifyWith(secretKey);
			// JWT 파서 생성
			JwtParser parser = builder.build();
			// AccessToken으로부터 payload 얻기
			jws = parser.parseSignedClaims(token);
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.info("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {  
			log.info("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.info("JWT 토큰이 잘못되었습니다.");
		}
		return jws;
	}

	public String getUserId(Jws<Claims> jws) {
		// Payload 객체 (claim) 얻기
		Claims claims = jws.getPayload();
		// 사용자 아이디 얻기
		String userId = claims.getSubject();
		return userId;
	}

	public String getAuthority(Jws<Claims> jws) {
		// Payload 객체 (claim) 얻기
		Claims claims = jws.getPayload();
		// 사용자 권한 얻기
		String autority = claims.get("authority").toString();
		return autority;
	}

	
	/*
	 * public static final void main(String[] args) { JwtProvider jwtProvider = new
	 * JwtProvider("com.mycompany.jsonwebtoken.kosacourse");
	 * 
	 * String accessToken = jwtProvider.createAccessToken("user", "ROLE_USER");
	 * log.info("AccessToken: " + accessToken);
	 * 
	 * Jws<Claims> jws = jwtProvider.validateToken(accessToken);
	 * log.info("validate: " + ((jws != null) ? true : false));
	 * 
	 * if(jws != null) { String userId = jwtProvider.getUserId(jws);
	 * log.info("userId: " + userId);
	 * 
	 * String autority = jwtProvider.getAuthority(jws); log.info("autority: " +
	 * autority); } }
	 */
	
}