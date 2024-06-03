package com.mycompany.webapp.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mycompany.webapp.dao.MemberDao;
import com.mycompany.webapp.dto.Member;

@Service
public class AppUserDetailsService implements UserDetailsService {
   @Autowired
   private MemberDao memberDao;   
   
   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Member member = memberDao.selectByMid(username); 
      
      // 아이디가 존재하지 않는 경우, 예외 발생
      if(member == null) {
         throw new UsernameNotFoundException(username + " 아이디가 존재하지 않습니다.");
      }
      
      // 사용자는 여러개의 권한을 갖고 있을 수 있기 때문에 List 로 관리
      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority(member.getMrole()));
      
      AppUserDetails userDetails = new AppUserDetails(member, authorities);
      return userDetails;
   }
}