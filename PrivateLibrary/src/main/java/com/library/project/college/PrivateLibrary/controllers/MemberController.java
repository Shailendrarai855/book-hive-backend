package com.library.project.college.PrivateLibrary.controllers;

import com.library.project.college.PrivateLibrary.dto.*;
import com.library.project.college.PrivateLibrary.services.AddBookService;
import com.library.project.college.PrivateLibrary.services.BookService;
import com.library.project.college.PrivateLibrary.services.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/member")
public class MemberController {
    final private BookService bookService;
    final  private MemberService memberService;
    final private AddBookService addBookService;

    public MemberController(BookService bookService, MemberService memberService, AddBookService addBookService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.addBookService = addBookService;
    }

    @GetMapping("/getBookByTitle/{title}")
    public ResponseEntity<BookDTO> getBookByName(@PathVariable String title){
        title = title.toLowerCase();
        BookDTO  book = bookService.getBookByName(title);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/getAllBooks")
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> allBooks = bookService.getAllBooks();
        return new ResponseEntity<>(allBooks, HttpStatus.OK);
    }

    @PutMapping("/updateMember")
    public ResponseEntity<MemberDTO> updateMemberDetails(@RequestBody MemberDTO memberDTO){
        MemberDTO response = memberService.updateMemberDetails(memberDTO);
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/addBookRequest")
    public ResponseEntity<ResponseDTO> addBookRequest(@RequestBody RequestBookDTO requestBookDTO ){
        ResponseDTO response = addBookService.addBookRequest(requestBookDTO);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<MemberDTO> loginMember(@RequestBody LogInDTO logInDTO){
        MemberDTO member = memberService.loginMember( logInDTO);
        return  ResponseEntity.ok(member);
    }

    @GetMapping("/allIssuedBook/{memberId}")
    public ResponseEntity<List<RecordDTO>> getAllIssuedBooks(@PathVariable String  memberId){
        List<RecordDTO> resp =  bookService.getAllIssuedBooks(memberId);
        return  ResponseEntity.ok(resp);
    }

    @GetMapping("/allRequestedBook/{memberId}")
    public ResponseEntity<List<requestedBookDTO>> getAllRequestedBooks(@PathVariable String  memberId){
        List<requestedBookDTO> resp =  addBookService.getAllRequestedBooks(memberId);
        return  ResponseEntity.ok(resp);
    }

    @GetMapping("/getBookById/{bookId}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable String bookId) {
        return  ResponseEntity.ok(bookService.getBookById(bookId));
    }


}
