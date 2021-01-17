package com.jongnan.communityweb.config;

import com.jongnan.communityweb.oauth2.CustomOAuth2Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.jongnan.communityweb.domain.enums.SocialType.*;

@Configuration
@EnableWebSecurity          //시큐리티 기능 사용 어노테이션
// @EnableOAuth2Client         //OAuth2 설정 어노테이션, spring boot 1.5 기반
public class SecurityConfig extends WebSecurityConfigurerAdapter {
                                    //최적화 설정을 위한 상속

    // spring boot 1.5 기반
    // private final OAuth2ClientContext oAuth2ClientContext;

    // 시큐리티 설정을 위한 오버로딩
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http.authorizeRequests() //HttpServletRequest 기반 설정
                .antMatchers("/", "/oauth2/**", "/login/**", "/css/**", "/images/**", "/js/**", "/console/**").permitAll() //요청 패턴을 리스트 형식으로 설정 및 누구나 접근 가능
                //각가의 소셜 미디어용 경로 지정
                .antMatchers("/facebook").hasAnyAuthority(FACEBOOK.getRoleType())
                .antMatchers("/google").hasAnyAuthority(GOOGLE.getRoleType())
                .antMatchers("/kakao").hasAnyAuthority(KAKAO.getRoleType())
                .anyRequest().authenticated() //나머지 요청은 인증된 사용자만 사용가능
                .and()
                    .oauth2Login()
                    .defaultSuccessUrl("/loginSuccess")
                    .failureUrl("/loginFailure")
                .and()
                    .headers().frameOptions().disable() //XFrameOptionsHeaderWriter의 최적화 설정을 허용 X
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")) //인증의 진입 지점, 인증이 안되어있는 사용자는 "/login"으로 이동
                .and()
                    .formLogin()
                    .successForwardUrl("/board/list") //로그인에 성공하면 "/board/list"로 포워딩
                .and()
                //로그아웃 설정
                    .logout()
                    .logoutUrl("/logout")           //로그아웃이 수행될 URL
                    .logoutSuccessUrl("/")          //포워딩 URL
                    .deleteCookies("JSESSIONID")    //성공시 삭제될 쿠키값
                    .invalidateHttpSession(true)    //성공시 세션 무효화
                .and()
                //첫번째 인자보다 먼저 실행될 필터 등록
                    .addFilterBefore(filter, CsrfFilter.class)
                    //.addFilterBefore(oauth2Filter(), BasicAuthenticationFilter.class)
                    .csrf().disable();
    }

    // kakao 로그인 연동
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            OAuth2ClientProperties oAuth2ClientProperties,
            @Value("${custom.oauth2.kakao.client-id}") String kakaoClientId
    ) {
        // 구글과 페이스북 인증 정보 빌드
        List<ClientRegistration> registrations =
                oAuth2ClientProperties
                        .getRegistration()
                        .keySet()
                        .stream()
                        .map(client ->
                                getRegistration(oAuth2ClientProperties, client))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        // 카카오 인증 정보 추가
        registrations.add(CustomOAuth2Provider.KAKAO
                        .getBuilder(KAKAO.getValue())
                        .clientId(kakaoClientId)
                        .clientSecret("abcd")   //null이면 실행 X, 따라서 임시값
                        .jwkSetUri("abcd")      //null이면 실행 X, 따라서 임시값
                        .build());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(
            OAuth2ClientProperties clientProperties,
            String client
    ) {

        OAuth2ClientProperties.Registration registration;

        if(GOOGLE.getValue().equals(client)) {
            registration = clientProperties.getRegistration()
                    .get(GOOGLE.getValue());

            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .scope("email", "profile")
                    .build();
        }

        if(FACEBOOK.getValue().equals(client)) {
            registration = clientProperties.getRegistration()
                    .get(FACEBOOK.getValue());
            final String USER_INFO_URI =
                    "https://graph.facebook.com/me?fields=id,name,email,link";

            return CommonOAuth2Provider.FACEBOOK.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .userInfoUri(USER_INFO_URI)
                    .scope("email")
                    .build();
        }
        return null;
    }


    // spring boot 1.5 기반
    // OAuth2 클라이언트용 시큐리티 필터를 불러와 올바른 순서로 필터가 동작하도록 설정
//    @Bean
//    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(filter);
//        registration.setOrder(-100);
//        return registration;
//    }

    // spring boot 1.5 기반
    //각 소셜 미디어 타입을 받아 필터 설정
//    private Filter oauth2Filter() {
//        CompositeFilter filter = new CompositeFilter();
//        List<Filter> filters = new ArrayList<>();
//        filters.add(oauth2Filter(facebook(), FACEBOOK));
//        filters.add(oauth2Filter(google(), GOOGLE));
//        filters.add(oauth2Filter(kakao(), KAKAO));
//        filter.setFilters(filters);
//        return filter;
//    }

    // spring boot 1.5 기반
    //각 소셜 미디어 필터를 리스트형식으로 한꺼번에 설정
//    private Filter oauth2Filter(ClientResources client,
//                                SocialType socialType) {
//
//        //인증이 수행될 경로(path)를 넣어 OAuth2 클라이언트용 인증 처리 필터 생성
//        OAuth2ClientAuthenticationProcessingFilter filter =
//                new OAuth2ClientAuthenticationProcessingFilter("/login/oauth2/client/" + socialType.getValue());
//
//        //권한 서버와의 통신을 위해 OAuth2RestTemplate 생성
//        //생성하기 위해서는 client 프로퍼티 정보와 OAuth2ClientContext 필요
//        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
//
//        filter.setRestTemplate(template);
//        filter.setTokenServices(new UserTokenService(client, socialType));
//
//        //인증 성공시 필터에 리다이렉트 될 URL
//        filter.setAuthenticationSuccessHandler(
//                (request, response, authentication) ->
//                        response.sendRedirect("/" + socialType.getValue() + "/complete")
//        );
//
//        //인증 실패시 리다이렉트 될 URL
//        filter.setAuthenticationFailureHandler(
//                (request, response, exception) ->
//                        response.sendRedirect("/error")
//        );
//
//        return filter;
//    }

    // spring boot 1.5 기반
//    @Bean
//    @ConfigurationProperties("facebook")
//    public ClientResources facebook() {
//        return new ClientResources();
//    }
//
//    @Bean
//    @ConfigurationProperties("google")
//    public ClientResources google() {
//        return new ClientResources();
//    }
//
//    @Bean
//    @ConfigurationProperties("kakao")
//    public ClientResources kakao() {
//        return new ClientResources();
//    }
}
