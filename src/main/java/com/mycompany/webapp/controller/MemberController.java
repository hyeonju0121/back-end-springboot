package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.AppUserDetails;
import com.mycompany.webapp.security.AppUserDetailsService;
import com.mycompany.webapp.security.JwtProvider;
import com.mycompany.webapp.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/member")
public class MemberController {
	// 토큰 생성을 위한 JwtProvider 의존성 주입
	@Autowired
	private JwtProvider jwtProvider;

	// 사용자의 상세정보를 얻기 위해 AppUserDetailsService 의존성 주입
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	@Autowired
	private MemberService memberService;

	@PostMapping("/login")
	public Map<String, String> userLogin(String mid, String mpassword) {

		// DB에서 사용자 상세 정보 얻기
		AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(mid);

		// 비밀번호 검사
		// 스프링은 기본적으로 DelegatingPasswordEncoder 암호화 방식을 사용함
		// {암호화 알고리즘} 암호화된 패스워드 -> 형태로 저장되는게 DelegatingPasswordEncoder 방식
		// DelegatingPasswordEncoder는 암호화 할때도 사용하지만, 실제 비밀번호와 암호화된 비밀번호를 비교하는데에도 사용함
		PasswordEncoder passwordEncoder = PasswordEncoderFactories
				.createDelegatingPasswordEncoder(); // 요청값으로 들어온 사용자
		// 패스워드와 DB에 저장되어 있는 패스워드 비교
		// appUserDetails.getMember().getMpassword() -> 실제 db에 저장된 암호화된 패스워드 반환
		boolean checkResult = passwordEncoder.matches(
				mpassword, userDetails.getMember().getMpassword());

		// 스프링 시큐리티 인증 처리
		if (checkResult) {
			Authentication authentication = 
					new UsernamePasswordAuthenticationToken(userDetails, null,
										userDetails.getAuthorities()); // 스프링 시큐리티에 인증 객체 설정
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		// 응답 생성
		Map<String, String> map = new HashMap<>();
		if (checkResult) { // 패스워드가 일치한 경우
			// 로그인 성공시 --------------------------------------
			String accessToken = jwtProvider.createAccessToken(
					mid, userDetails.getMember().getMrole());
			// JSON 응답 구성
			map.put("message", "success");
			map.put("mid", mid);
			map.put("accessToken", accessToken);
		} else {
			// 로그인 실패시 -------------------------------------
			map.put("result", "fail");
		}

		return map;
	}
	
	@PostMapping("/join")
	public Member join(@RequestBody Member member) {
		// 비밀번호 암호화 
		PasswordEncoder passwordEncoder = PasswordEncoderFactories
				.createDelegatingPasswordEncoder();
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		
		
		// 아이디 활성화 설정
		member.setMenabled(true);
		
		// 권한 설정
		member.setMrole("ROLE_USER");
		
		// 회원가입 처리
		memberService.join(member);
		
		// 응답 json 형식에 password 제거
		member.setMpassword(null);
		
		return member;
	}

}
