package com.jongnan.communityweb.service;

import com.jongnan.communityweb.domain.Board;
import com.jongnan.communityweb.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public Page<Board> findBoardList(Pageable pageable) {
        return boardRepository.findAll(
                PageRequest.of(getValidPageNumber(pageable), pageable.getPageSize()));
    }

    private int getValidPageNumber(Pageable pageable) {
        return pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1;
    }

    public Board findBoardByIdx(Long idx) {
        return boardRepository.findById(idx).orElse(new Board());
    }
}
