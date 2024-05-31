package com.mycompany.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/board")
public class BoardController {
	@Autowired
	private BoardService boardService;

	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
		// 페이징 대상이 되는 전체 행수 얻기
		int totalRows = boardService.getCount();
		// 페이지 객체 생성
		Pager pager = new Pager(10, 5, totalRows, pageNo);
		// 해당 페이지의 게시물 목록 가져오기
		List<Board> list = boardService.getList(pager);
		// 여러 객체를 리턴하기 위해 Map 객체 생성
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);

		return map; // {"boards": [...], "pager": {...}}
	}

	/**
	 * 게시물 생성
	 */
	@PostMapping("/create")
	public Board create(Board board) {
		// 첨부가 넘어왔을 경우 처리
		if (board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			// 파일 이름 설정
			board.setBattachoname(mf.getOriginalFilename());
			// 파일 종류 설정
			board.setBattachtype(mf.getContentType());
			try {
				// 파일 데이터 설정
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
			}
		}

		// Board 객체 DB에 저장
		board.setBwriter("user");
		boardService.insert(board);

		// JSON 으로 변환되지 않는 필드는 NULL 처리
		// byte[]랑 MultipartFile 은 JSON 으로 변환 X -> NULL 처리 필요
		board.setBattach(null);
		board.setBattachdata(null);

		return board; // {"bno": 1, "btitle": "...", ...}
	}

	/**
	 * 게시물 단건 조회
	 */
	@GetMapping("/read/{bno}")
	public Board read(@PathVariable int bno) {
		Board board = boardService.getBoard(bno);

		// JSON 으로 변환되지 않는 필드는 NULL 처리
		// byte[]랑 MultipartFile 은 JSON 으로 변환 X -> NULL 처리 필요
		board.setBattach(null);
		board.setBattachdata(null);

		return board;
	}

	// 게시물 수정
	@PutMapping("/update")
	public Board update(Board board) {
		// 첨부가 넘어왔을 경우 처리
		if (board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			// 파일 이름 설정
			board.setBattachoname(mf.getOriginalFilename());
			// 파일 종류 설정
			board.setBattachtype(mf.getContentType());
			try {
				// 파일 데이터 설정
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
			}
		}

		// 게시물 내용 수정
		boardService.update(board);

		// 수정된 board 객체
		board = boardService.getBoard(board.getBno());
		board.setBattach(null);
		board.setBattachdata(null);

		return board;
	}

	// 게시물 단건 삭제
	@DeleteMapping("/delete/{bno}")
	public void delete(@PathVariable int bno) {
		boardService.delete(bno);
	}

	// 첨부파일 다운로드
	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		// 해당 게시물 가져오기 (첨부파일 battachdata 포함)
		Board board = boardService.getBoard(bno);
		
		// 파일 이름이 한글일 경우, 브라우저에서 한글 이름으로 다운로드 받기 위해 헤더에 추가할 내용
		try {
			// 한글 파일의 이름 -> 인코딩 변경
			String fileName = new String(
					board.getBattachoname().getBytes("UTF-8"), "ISO-8859-1");
			
			response.setHeader("Content-Disposition", 
					"attachment; filename=\"" + fileName + "\"");
			
			// 파일 타입을 헤더에 추가
			response.setContentType(board.getBattachtype());
			
			// 응답 바디에 파일 데이터를 출력
			OutputStream os = response.getOutputStream();
			os.write(board.getBattachdata());
			os.flush();
			os.close();
			
		} catch (IOException e) {
			log.error(e.getMessage()); // error 출력
		}
	}

}
