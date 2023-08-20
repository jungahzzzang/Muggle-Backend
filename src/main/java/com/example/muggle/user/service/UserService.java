package com.example.muggle.user.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.muggle.user.dto.UserDto;
import com.example.muggle.user.entity.UserEntity;
import com.example.muggle.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService implements UserDetailsService{
	
	private final UserRepository userRepository;
	
	@Transactional
	public void save(UserDto user) throws UsernameNotFoundException {
		
		if(user.getRoles().equals("ROLE_USER")) {
			user.setRoles("ROLE_USER");
		}
		if(user.getRoles().equals("ROLE_ADMIN")) {
			user.setRoles("ROLE_ADMIN");
		}
		userRepository.save(user.toEntity());
	}
	
	@Override
	public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
		
		if (!this.checkUserByNaver(username)) {
			
			return loadUserByNaver(username);
		}
		if (!this.checkUserByKakao(username)) {
			
			return loadUserByKakao(username);
			
		} else {
			
			return userRepository.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException(username));
		}
	}
	
	public boolean checkUserByNaver(String naver_sub) {//throws UsernameNotFoundException,NullPointerException {
        Optional<UserEntity> check = userRepository.findByNaversub(naver_sub);
        return check.isEmpty();
    }

    public UserEntity loadUserByNaver(String naver_sub) throws UsernameNotFoundException,NullPointerException {
        return userRepository.findByNaversub(naver_sub)
                .orElseThrow(() -> new UsernameNotFoundException(naver_sub));
    }
    
    public boolean checkUserByKakao(String kakao_sub) {//throws UsernameNotFoundException,NullPointerException {
        Optional<UserEntity> check = userRepository.findByKakaosub(kakao_sub);
        return check.isEmpty();
    }

    public UserEntity loadUserByKakao(String kakao_sub) throws UsernameNotFoundException,NullPointerException {
        return userRepository.findByKakaosub(kakao_sub)
                .orElseThrow(() -> new UsernameNotFoundException(kakao_sub));
    }

}
