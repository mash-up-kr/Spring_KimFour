package com.jongnan.communityweb.controller.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jongnan.communityweb.domain.Board;
import com.jongnan.communityweb.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

// org.springframework.hateoas.PagedResources 에서
// org.springframework.hateoas.PagedModel 로 변경
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// org.springframework.hateoas.mvc.ControllerLinkBuilder 에서
// org.springframework.hateoas.server.mvc.WebMvcLinkBuilder 로 변경
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rest-api/boards")
public class BoardRestController {

    private final BoardRepository boardRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoards(@PageableDefault Pageable pageable) {
        // 게시물을 페이지네이션을 통해 가져옴
        Page<Board> boards = boardRepository.findAll(pageable);

        // 현재 페이지 수, 총 게시판 수, 한 페이지의 게시판 수 등 페이징 처리에 관한 리소스를 만드는 객체를 만들기 위해 PageMetadata 객체 생성
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageable.getPageSize(), boards.getNumber(), boards.getTotalElements());

        // HATEOAS가 적용되고 페이징 값까지 생성된 REST형의 데이터를 만들어줌
        // PageModel 생성자가 deprecated 되어 of 메소드 사용
        PagedModel<Board> resources = PagedModel.of(boards.getContent(), pageMetadata);

        // 여기서 링크도 추가가 가능
        resources.add(linkTo(methodOn(BoardRestController.class).getBoards(pageable)).withSelfRel());

        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<?> postBoard(@RequestBody Board board) {
        board.setCreatedDateNow();
        boardRepository.save(board);
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @PutMapping("/{idx}")
    public ResponseEntity<?> putBoard(@PathVariable("idx") Long idx, @RequestBody Board board) {
        Board persistBoard = boardRepository.getOne(idx);
        persistBoard.update(board);
        boardRepository.save(persistBoard);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @DeleteMapping("/{idx}")
    public ResponseEntity<?> deleteBoard(@PathVariable("idx") Long idx) {
        boardRepository.deleteById(idx);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
