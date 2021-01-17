package com.jongnan.communityweb.resolver;

import com.jongnan.communityweb.annotation.SocialUser;
import com.jongnan.communityweb.domain.User;
import com.jongnan.communityweb.domain.enums.SocialType;
import com.jongnan.communityweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.jongnan.communityweb.domain.enums.SocialType.*;

@Component
@RequiredArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // SocialUser 어노테이션을 명시했는가, User 타입인가 체크
        // 한번 체크된 부분은 캐시
        return parameter.getParameterAnnotation(SocialUser.class) != null
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        HttpSession session = requestAttributes.getRequest().getSession();

        User user = (User) session.getAttribute("user");

        return getUser(user, session);
    }

    private User getUser(User user, HttpSession session) {
        if(user != null) return user;
        try {
            OAuth2AuthenticationToken authentication =
                    (OAuth2AuthenticationToken) SecurityContextHolder
                            .getContext().getAuthentication();
            Map<String, Object> map =
                    authentication.getPrincipal().getAttributes();
            User convertUser = convertUser(authentication.getAuthorizedClientRegistrationId(), map);

            // 이메일로 유저 정보 가져오기
            user = userRepository.findByEmail(convertUser.getEmail());

            // 유저가 DB에 없다면 저장
            if(user == null) {
                user = userRepository.save(convertUser);
            }

            setRoleIfNotSame(user, authentication, map);
            session.setAttribute("user", user);
            return user;
        }catch(ClassCastException e) {
            return user;
        }
    }

    private User convertUser(String authority, Map<String, Object> map) {
        if(FACEBOOK.getValue().equals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.getValue().equals(authority)) return getModernUser(GOOGLE, map);
        else if(KAKAO.getValue().equals(authority)) return getKakaoUser(map);
        return null;
    }

    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private User getKakaoUser(Map<String, Object> map) {
        Map<String, String> propertyMap = (HashMap<String, String>)map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(String.valueOf(map.get("kaccount_email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }

    // 인증된 authentication이 권한을 갖고 있는지 체크
    private void setRoleIfNotSame(User user,
                                  OAuth2AuthenticationToken authentication,
                                  Map<String, Object> map) {

        String userSocialRoleType = user.getSocialType().getRoleType();
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(userSocialRoleType);

        // 유저에 권한이 없다면 SecurityContextHolder를 통해 소셜 미디어 타입으로 권한 저장
        if(!authentication.getAuthorities().contains(grantedAuthority)) {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(map, "N/A",
                    AuthorityUtils.createAuthorityList(userSocialRoleType));
            SecurityContextHolder.getContext().setAuthentication(token);
        }
    }
}
