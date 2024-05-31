package com.mycompany.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mycompany.webapp.dao.MemberDao;

@Service
public class MemberService {
   @Autowired
   private MemberDao memberDao;
}