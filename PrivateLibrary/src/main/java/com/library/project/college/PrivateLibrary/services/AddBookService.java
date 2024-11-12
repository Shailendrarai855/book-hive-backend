package com.library.project.college.PrivateLibrary.services;


import com.library.project.college.PrivateLibrary.dto.*;
import com.library.project.college.PrivateLibrary.entities.*;
import com.library.project.college.PrivateLibrary.exceptions.ResourceNotFoundException;
import com.library.project.college.PrivateLibrary.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AddBookService {
    @Autowired
    private EmailService emailService;
    private final RequestBookRepository requestBookRepository;
    private final MemberRepository memberRepository;
    private final RecordRepository recordRepository;
    private final BookRepository bookRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;

    public AddBookService(RequestBookRepository requestBookRepository, MemberRepository memberRepository, RecordRepository repository, BookRepository bookRepository, AdminRepository adminRepository, ModelMapper modelMapper) {
        this.requestBookRepository = requestBookRepository;
        this.memberRepository = memberRepository;
        this.recordRepository = repository;
        this.bookRepository = bookRepository;
        this.adminRepository = adminRepository;
        this.modelMapper = modelMapper;
    }
    public RequestBookDTO convetToRequestBookDTO(RequestBookEntity requestBookEntity) {
        RequestBookDTO dto = new RequestBookDTO();
        dto.setMemberId(requestBookEntity.getMember().getMemberId());
        dto.setBookId(List.of(requestBookEntity.getBook().getBookId()));
        return dto;
    }



    public ResponseDTO addBookRequest(RequestBookDTO addBookRequestDTO) {
        String memberId = addBookRequestDTO.getMemberId();
        MemberEntity member = memberRepository.findById(memberId).get();
        int prevRequests = requestBookRepository.countByMember(member);


//        if(prevRequests > 0)
//            throw new ResourceNotFoundException("can placed new Book Request , Previous Request Pending");
        int prevBorrows = recordRepository.countByMemberAndReturnDateIsNull(member);
        if(prevBorrows+prevRequests >= 5)
            throw    new ResourceNotFoundException("Book limit exceed");
        List<String> bookIds= addBookRequestDTO.getBookId();
        for (String bookId : bookIds) {
            BookEntity bookEntity = bookRepository.findById(bookId).get();

            bookEntity.setAvailable(false);
            bookRepository.save(bookEntity);
            RequestBookEntity recordBook =
                    RequestBookEntity.builder()
                            .book(bookEntity)
                            .member(member)
                            .localDate(LocalDateTime.now())
                            .build();

            requestBookRepository.save(recordBook);
        }
        return ResponseDTO.builder()
                .message("Request Placed, Collect your book within 10 Hours")
                .build();
    }

    @Scheduled(fixedRate = 1000*60*60*10)
    @Transactional
    public void deleteOldPendingRequests() {
        List<RequestBookEntity> pendingRequests = requestBookRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (RequestBookEntity request : pendingRequests) {
            if (Duration.between(request.getLocalDate(), now).toHours() > 10) {
                BookEntity book = request.getBook();
                book.setAvailable(true);
                bookRepository.save(book);
                requestBookRepository.delete(request);
            }
        }
    }


    public ResponseDTO issueBookToMember(String memberId , Long adminId, String bookId){
            MemberEntity member = memberRepository.findById(memberId).get();
            BookEntity book = bookRepository.findById(bookId).get();
            Optional<RequestBookEntity> resp = requestBookRepository.findByBook(book);
            if (resp.isEmpty())
                throw new ResourceNotFoundException("Record not found");
            RequestBookEntity    request = resp.get();
                requestBookRepository.deleteByRequestId(request.getRequestId());
                System.out.println(request.getRequestId());
                AdminEntity admin = adminRepository.findById(adminId).get();
                RecordEntity record = RecordEntity.builder()
                        .member(member)
                        .book(book)
                        .admin(admin)
                        .issueDate(LocalDateTime.now())
                        .build();
                recordRepository.save(record);

        String to = member.getEmail();
        LocalDate todayDate = LocalDate.now();
        LocalDate dueDate = todayDate.plusDays(30);
        String memberName = member.getName();
        String subject = "Book Issued: Due Date Notification";
        String body = String.format(
                "Dear %s,\n\n" +
                        "We are pleased to inform you that a book has been successfully issued to you. Below are the details:\n\n" +
                        "Issued Date: %s\n" +
                        "Due Date: %s\n\n" +
                        "Please ensure to return the book by the due date to avoid any fines. " +
                        "A fine of ₹1 will be applicable for each day the book is overdue, starting from the due date until the book is returned.\n\n" +
                        "If you have any questions or need assistance, feel free to contact us at [library contact email] or [library phone number]. " +
                        "We are here to help!\n\n" +
                        "Thank you for using our library services!\n\n" +
                        "Best regards"
                ,
                memberName,
                todayDate,
                dueDate
        );
        emailService.sendMail(to,subject,body);

            return  ResponseDTO.builder()
                .message("issues Successfully")
                .build();
    }

    public List<RequestBookDTO> getAllRequestBooks() {
        List<RequestBookEntity> allRequest = requestBookRepository.findAll();
        return  allRequest.stream()
                .map(this::convetToRequestBookDTO)
                .collect(Collectors.toList());
    }

    public ResponseDTO returnBook(ReturnBookDTO returnBookDTO) {

        String memberId = returnBookDTO.getMemberId();
        String bookId = returnBookDTO.getBookId();

        Optional<RecordEntity > record = recordRepository.findActiveRecordByMemberAndBook(memberId,bookId);
        if(record.isEmpty())
                throw new ResourceNotFoundException("issue Record not Found");

        RecordEntity record1 = record.get();

        int fine = (int) ChronoUnit.DAYS.between(record1.getIssueDate().toLocalDate(), LocalDate.now()) - 30;
        fine = Math.max(fine, 0);

        BookEntity book = bookRepository.findById(bookId).get();
        book.setAvailable(true);
        bookRepository.save(book);

        record1.setReturnDate(LocalDate.now());
        record1.setReturnedBy(returnBookDTO.getAdminId());
        record1.setFine(fine);
        recordRepository.save(record1);
        return ResponseDTO.builder()
                .message("Please Pay fine  " + fine)
                .build();
    }

    public List<requestedBookDTO> getAllRequestedBooks(String memberId) {
        List<RequestBookEntity>  resp = requestBookRepository.findByMember_MemberId(memberId);
        System.out.println(resp.getFirst().getBook().getIsbn());

        return resp.stream()
                .map(book->modelMapper.map(book, requestedBookDTO.class))
                .toList();

    }

    @Transactional
    public RequestBookDTO rejectBookRequest(RequestBookDTO req) {
        MemberEntity member = memberRepository.findById(req.getMemberId()).get();
        BookEntity book = bookRepository.findById(req.getBookId().getFirst()).get();

        book.setAvailable(true);
        bookRepository.save(book);
        requestBookRepository.deleteByBookId(book.getBookId());
        String bookName = book.getTitle();
        String bookId = book.getBookId();
        String to = member.getEmail();
        String memberName = member.getName();
        String subject = "Book Request Rejected: " + bookName;
        String body = "Dear " + memberName + ",\n\n" +
                "We regret to inform you that your request for the book has been rejected due to unforeseen reasons. " +
                "We apologize for any inconvenience this may have caused.\n\n" +
                "Book Details:\n" +
                "Title: " + bookName + "\n" +
                "Book ID: " + bookId + "\n\n" +
                "If you have any questions or would like further assistance, please feel free to contact us.\n\n" +
                "Thank you for your understanding.\n\n" +
                "Best regards,\n" +
                "Library Management Team";
        emailService.sendMail(to,subject,body);
        return req;
    }
}
