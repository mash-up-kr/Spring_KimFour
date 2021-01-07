package com.jongnan.communityweb;

import com.jongnan.communityweb.domain.Board;
import com.jongnan.communityweb.domain.User;
import com.jongnan.communityweb.domain.enums.BoardType;
import com.jongnan.communityweb.repository.BoardRepository;
import com.jongnan.communityweb.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class JpaMappingTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @BeforeEach
    public void initialize() {
        User user = userRepository.save(User.builder()
                        .name("jongnan")
                        .password("123123")
                        .email("dirlawhddbs@naver.com")
                        .createdDate(LocalDateTime.now())
                        .build()
        );

        boardRepository.save(Board.builder()
                .title("테스트")
                .subTitle("서브 타이틀")
                .content("콘텐츠")
                .boardType(BoardType.free)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .user(user).build()
        );
    }

    @Test
    public void 유저와_보드가_제대로_생성이_되었는가() {
        User user = userRepository.findByEmail("dirlawhddbs@naver.com");
        assertEquals(user.getName(), "jongnan");
        assertEquals(user.getPassword(), "123123");
        assertEquals(user.getEmail(), "dirlawhddbs@naver.com");

        Board board = boardRepository.findByUser(user);
        assertEquals(board.getTitle(), "테스트");
        assertEquals(board.getSubTitle(), "서브 타이틀");
        assertEquals(board.getContent(), "콘텐츠");
        assertEquals(board.getBoardType(), BoardType.free);
    }
}
