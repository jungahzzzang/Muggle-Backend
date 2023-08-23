package com.example.muggle.user.controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.muggle.config.jwt.JwtProvider;
import com.example.muggle.user.dto.UserDto;
import com.example.muggle.user.service.UserOAuthService;
import com.example.muggle.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
	private final UserOAuthService userOAuthService;
	
	private final JwtProvider jwtProvider;

	@PostMapping("/ouath/callback/kakao")
	public JSONObject kakaoLogin(@RequestBody JSONObject object) throws ParseException {
		
		JSONObject kakao_obj = new JSONObject();
		JSONParser parser = new JSONParser();
		JSONObject kakao_parse = (JSONObject) parser.parse(String.valueOf(object));
		
		JSONObject kakao_profile = (JSONObject) kakao_parse.get("kakaoProfileResult");
		JSONObject kakao_token = (JSONObject) kakao_parse.get("accessToken");
		
		System.out.println(kakao_token);
		System.out.println(kakao_profile);
		
		//String kakao_sub = (String) kakao_profile.get("id");
		String kakao_sub = String.valueOf(kakao_profile.get("id"));
		String email = (String) kakao_profile.get("email");
		String username = (String) kakao_profile.get("nickname");
		String picture = (String) kakao_profile.get("profileImageUrl");
		
		UserDto user = new UserDto();
		user.kakaoDtoOption(email, username, kakao_sub, picture);
		user.setLogin_type("kakao");
		user.setRoles("ROLE_USER");
		user.setEmail(email);
		
		userOAuthService.saveOrUpdate(user);
		username = userService.loadUserByKakao(kakao_sub).getUsername();
		
		kakao_obj.put("token", getToken(user.getKakao_sub()));
		kakao_obj.put("email", email);
		kakao_obj.put("status", 200);
		kakao_obj.put("username", username);
		
		return kakao_obj;
	}
	
	private String getToken(String token) {
		
		String role = "ROLE_USER";
		
		return jwtProvider.createToken(token, role);
	}

}
