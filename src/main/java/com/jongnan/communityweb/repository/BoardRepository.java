package com.jongnan.communityweb.repository;

import com.jongnan.communityweb.domain.Board;
import com.jongnan.communityweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Board findByUser(User user);
}
