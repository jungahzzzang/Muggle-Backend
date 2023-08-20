package com.example.muggle.user.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.example.muggle.user.dto.UserDto;
import com.example.muggle.user.entity.UserEntity;
import com.example.muggle.user.repository.UserRepository;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserOAuthService extends DefaultOAuth2UserService{

	private final UserDto user;
	
	private final UserService userService;
	
	private final UserRepository userRepository;
	
	private RestOperations restOperations;
	
	private static final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";

    private static final String MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE = "missing_user_name_attribute";

    private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";

    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
            new ParameterizedTypeReference<Map<String, Object>>() {};
            
    private Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();
	
    public UserOAuthService(UserDto user, UserService userService, UserRepository userRepository) {
    	
    	this.user = user;
    	this.userService = userService;
    	this.userRepository = userRepository;
    	RestTemplate restTemplate = new RestTemplate();
    	restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
    	this.restOperations = restTemplate;
    }
    
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		Assert.notNull(userRequest, "userRequest cannot be null");
		
		if (!StringUtils.hasText(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())) {
			OAuth2Error oAuth2Error = new OAuth2Error(
					MISSING_USER_INFO_URI_ERROR_CODE,
					"Missing required UserInfo Uri in UserInfoEndpoint for Client Registration: " +
							userRequest.getClientRegistration().getRegistrationId(),
					null);
			throw new OAuth2AuthenticationException(oAuth2Error, oAuth2Error.toString());
			
		}
		
		String userAttributeName = userRequest.getClientRegistration().getProviderDetails()
				.getUserInfoEndpoint().getUserNameAttributeName();
		
		// UserName Error 거르기
		if (!StringUtils.hasText(userAttributeName)) {
			OAuth2Error oAuth2Error = new OAuth2Error(
					MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE,
					"Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: " +
                            userRequest.getClientRegistration().getRegistrationId(),
                    null);
			throw new OAuth2AuthenticationException(oAuth2Error, oAuth2Error.toString());
		}
		
		//request 컬렉션에 저장
		RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
        //response 저장할 객체 선언
        ResponseEntity<Map<String, Object>> response;
        
        try {
        	response = this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
        }catch (OAuth2AuthorizationException e) {
        	
        	OAuth2Error oAuth2Error = e.getError();
        	StringBuilder errorDetails = new StringBuilder();
        	
        	errorDetails.append("Error details: [");
        	errorDetails.append("UserInfo Uri: ").append(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
        	errorDetails.append(" , Error Code: ").append(oAuth2Error.getErrorCode());
        	
        	if (oAuth2Error.getDescription() != null) {
        		errorDetails.append(", Error Description: ").append(oAuth2Error.getDescription());
        	}
        	errorDetails.append("]");
        	
        	oAuth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
        			"An error occurred while attempting to retrieve the UserInfo Resource: " + errorDetails.toString(), null);
        	
        	throw new OAuth2AuthenticationException(oAuth2Error, oAuth2Error.toString(), e);
		}catch (RestClientException e) {
			
			OAuth2Error oAuth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
					"An error occurred while attempting to retrieve the UserInfo Resource: " + e.getMessage(), null);
			
			throw new OAuth2AuthenticationException(oAuth2Error, oAuth2Error.toString(), e);
					
		}
        
        Map<String, Object> userAttributes = getUserAttributes(response);
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new OAuth2UserAuthority(userAttributes));
        OAuth2AccessToken token = userRequest.getAccessToken();
        
        for (String authority : token.getScopes()) {
        	authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
        }
        
        log.info("userAttribute = {}", userAttributes);
        
        user.clear();
        
        if (userAttributeName == "id" ) {
        	//네이버 
        	if (userAttributes.containsKey("resultcode")) {
        		String naver_sub = String.valueOf(userAttributes.get("id"));
        		String email = String.valueOf(userAttributes.get("email"));
        		String name = String.valueOf(userAttributes.get("name"));
                String picture = String.valueOf(userAttributes.get("picture"));
                
                user.naverDtoOption(email, name, naver_sub, picture);
            //카카오
        	} else {
        		String kakao_sub = String.valueOf(userAttributes.get("id"));
                Map<String,String> properties = (Map<String, String>) userAttributes.get("properties");
                Map<String,String> profile = (Map<String, String>) userAttributes.get("kakao_account");

                System.out.println(properties.get("nickname"));
                System.out.println(profile.get("email"));
                System.out.println(userAttributes.get("email"));

                String email = String.valueOf(profile.get("email"));
                String name = String.valueOf(properties.get("nickname"));
                String picture = String.valueOf(properties.get("profile_image"));
                
                user.kakaoDtoOption(email, name, kakao_sub, picture);
        	}
        }
        //저장 (존재 시 업데이트)
        saveOrUpdate(user);
        
        return new DefaultOAuth2User(authorities, userAttributes, userAttributeName);
	}
	
	// 네이버는 HTTP response body에 response 안에 id값을 포함한 유저 정보를 넣어주므로 유저 정보를 빼내기 위한 메소
	private Map<String, Object> getUserAttributes(ResponseEntity<Map<String, Object>> response) {
		
		Map<String, Object> userAttributes = response.getBody();
		
		if (userAttributes.containsKey("response")) {
			
			LinkedHashMap responseData = (LinkedHashMap) userAttributes.get("response");
			userAttributes.putAll(responseData);
			userAttributes.remove("response");
		}
		
		return userAttributes;
	}
	
	public void saveOrUpdate(UserDto user) {
		
		String login_type = user.getLogin_type();
		boolean user_check = true;
		
		System.out.println("소셜 로그인 유형 : " + user.getLogin_type());
		
		if (login_type == "naver") {
			
			user_check = userService.checkUserByNaver(user.getNaver_sub());
			
			if(!user_check) {
				UserEntity userEntity = userService.loadUserByNaver(user.getNaver_sub());
				userRepository.save(userEntity);
				
			} else {
				userService.save(user);
			}
			
		} else if (login_type == "kakao") {
			
			user_check = userService.checkUserByKakao(user.getKakao_sub());
			
			if(!user_check) {
				
				UserEntity userEntity = userService.loadUserByKakao(user.getKakao_sub());
				userRepository.save(userEntity);
				
			} else {
				userService.save(user);
			}
		} else {
			System.out.println("문제 발");
		}
	}
	
}
