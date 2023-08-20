package com.example.muggle.user.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity implements UserDetails{
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String email;
	
	private String username;
	
	private String roles;
	
	private String photo;
	
	@Column(name = "Kakao_sub")
	private String kakaosub;
	
	@Column(name = "Naver_sub")
	private String naversub;
	
	
	@Builder
    public UserEntity(Long id, String email, String username, String roles, String photo,  String kakao_sub, String naver_sub) {

        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.photo = photo;
        this.kakaosub = kakao_sub;
        this.naversub = naver_sub;
    }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		String str = getRoles();
		
		if (str.equals("ROLE_USER")) {
			
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			
		} else if (str.equals("ROLE_ADMIN")) {
			
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
		}
		
		return authorities;
		
	}
		
	@Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

}
