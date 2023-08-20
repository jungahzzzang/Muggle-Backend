package com.example.muggle.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.muggle.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{
	
	Optional<UserEntity> findByKakaosub(String kakao_sub);
	
	Optional<UserEntity> findByNaversub(String naver_sub);
	
	Optional<UserEntity> findByUsername(String username);

}