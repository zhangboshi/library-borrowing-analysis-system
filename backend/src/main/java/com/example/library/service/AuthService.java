package com.example.library.service;

import com.example.library.model.Member;
import com.example.library.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Member> authenticate(String username, String password) {
        return memberRepository.findByUsername(username)
                .filter(member -> passwordEncoder.matches(password, member.getPasswordHash()));
    }

    @Transactional
    public String issueToken(Member member) {
        String token = UUID.randomUUID().toString();
        member.setActiveToken(token);
        memberRepository.save(member);
        return token;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByToken(String token) {
        return memberRepository.findByActiveToken(token);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
