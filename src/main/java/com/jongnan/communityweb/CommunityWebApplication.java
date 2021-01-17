package com.jongnan.communityweb;

import com.jongnan.communityweb.domain.Board;
import com.jongnan.communityweb.domain.User;
import com.jongnan.communityweb.domain.enums.BoardType;
import com.jongnan.communityweb.repository.BoardRepository;
import com.jongnan.communityweb.repository.UserRepository;
import com.jongnan.communityweb.resolver.UserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@SpringBootApplication
public class CommunityWebApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(CommunityWebApplication.class, args);
    }

    private final UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }

    @Bean
    public CommandLineRunner runner(UserRepository userRepository, BoardRepository boardRepository) throws Exception {
        return args -> {
            User user = userRepository.save(User.builder()
                    .name("jongnan")
                    .password("123123")
                    .email("dirlawhddbs@naver.com")
                    .createdDate(LocalDateTime.now())
                    .build());

            IntStream.rangeClosed(1, 200)
                    .forEach(index ->
                            boardRepository.save(Board.builder()
                                    .title("게시글"+ index)
                                    .subTitle("순서" + index)
                                    .content("콘텐츠")
                                    .boardType(BoardType.free)
                                    .createdDate(LocalDateTime.now())
                                    .updatedDate(LocalDateTime.now())
                                    .user(user).build())
                    );
        };
    }
}
