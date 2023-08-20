package com.example.muggle.user.dto;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.muggle.user.entity.UserEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@NoArgsConstructor
@Getter @Setter
public class UserDto {

	private Long id;
	private String email;
	private String username;
	private String roles;
	private String photo;
	private String kakao_sub;
	private String naver_sub;
	private String login_type;
	
	@Builder
    public UserDto(Long id, String email, String username, String roles, String photo,  String kakao_sub, String naver_sub) {

        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.photo = photo;
        this.kakao_sub = kakao_sub;
        this.naver_sub = naver_sub;
    }
	
	public UserDto(UserDetails userDetails) {
		
	}
	
	public void kakaoDtoOption(String email, String username, String kakao_sub, String photo) {
		this.email = email;
		this.username = username;
		this.kakao_sub = kakao_sub;
		this.photo = photo;
	}
	
	public void naverDtoOption(String email, String username, String naver_sub, String photo) {
		this.email = email;
		this.username = username;
		this.naver_sub = naver_sub;
		this.photo = photo;
	}
	
	public void clear() {
		this.id = null;
		this.email = null;
		this.username = null;
		this.roles = null;
		this.photo = null;
		this.kakao_sub = null;
		this.naver_sub = null;
		this.login_type = null;
	}
	
	public UserEntity toEntity() {
		
		return UserEntity.builder()
				.username(username)
				.email(email)
				.roles(roles)
				.photo(photo)
				.kakao_sub(kakao_sub)
				.naver_sub(naver_sub)
				.build();
	}
}
