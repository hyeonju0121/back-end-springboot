package com.mycompany.webapp.security;

import java.io.IOException;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Autowired
	private JwtProvider jwtProvider;
	
	// 클라이언트에서 넘어온 access token이 유효한지 안한지 검사를 해야 함
	// 유효하다면, 안에 있는 사용자 아이디를 받아내야 함
	// 그 아이디에 해당하는 정보를 db에서 갖고 와서
	// autentication 에 세팅 해줘야 한다.
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String accessToken = null;

		// 클라이언트에서 넘어온 access token 얻어오기
		// jwt 토큰은 요청 헤더에 실려서 넘어옴
		String headerValue = request.getHeader("Authorization"); // 요청 헤더 이름

		if (headerValue != null && headerValue.startsWith("Bearer")) {
			accessToken = headerValue.substring(7); // "Bearer "
			log.info("accessToken={}", accessToken);
		}

		// AccessToken 유효성 검사 (토큰 유효성 검증)
		Jws<Claims> jws = jwtProvider.validateToken(accessToken);
		if (jws != null) {
			// 유효한 경우
			log.info("AccessToken이 유효함");
			String userId = jwtProvider.getUserId(jws);
			log.info("userId={}", userId);
		} else {
			// 유효하지 않은 경우
			log.info("AccessToken이 유효하지 않음 ");
		}

		// 다음 필터를 실행
		filterChain.doFilter(request, response);

	}
}
