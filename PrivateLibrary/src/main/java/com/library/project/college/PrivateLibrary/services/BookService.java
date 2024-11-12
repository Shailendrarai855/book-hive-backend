package com.library.project.college.PrivateLibrary.services;

import com.library.project.college.PrivateLibrary.dto.BookDTO;
import com.library.project.college.PrivateLibrary.dto.RecordDTO;
import com.library.project.college.PrivateLibrary.dto.ResponseDTO;
import com.library.project.college.PrivateLibrary.entities.*;
import com.library.project.college.PrivateLibrary.exceptions.ResourceNotFoundException;
import com.library.project.college.PrivateLibrary.repository.*;
import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private  final int PAGE_SIZE = 6;

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final RecordRepository recordRepository;
    private final EmailService emailService;

    public BookService(PublisherRepository publisherRepository, BookRepository bookRepository, ModelMapper modelMapper,
                       AdminRepository adminRepository,
                       MemberRepository memberRepository,
                       RecordRepository recordRepository, EmailService emailService) {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.modelMapper = modelMapper;
        this.adminRepository = adminRepository;
        this.memberRepository = memberRepository;
        this.recordRepository = recordRepository;
        this.emailService = emailService;
    }

    public BookEntity convertToEntity(BookDTO bookDTO) {
        return modelMapper.map(bookDTO, BookEntity.class);
    }

    public BookDTO convertToDTO(BookEntity bookEntity) {
        return modelMapper.map(bookEntity, BookDTO.class);
    }

    public BookDTO addNewBook(@Valid BookDTO bookDTO, String publisherId) {
        PublisherEntity publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid publisher ID: " + publisherId));

        BookEntity newBook = convertToEntity(bookDTO);
        String title = bookDTO.getTitle().toLowerCase();
        newBook.setTitle(title);
        newBook.setPublisher(publisher);
        newBook.setAvailable(true);
        BookEntity savedBook = bookRepository.save(newBook);
        publisher.getMyBooks().add(newBook);
        return convertToDTO(savedBook);
    }

    public List<BookDTO> getAllBooks() {
        List<BookEntity> bookPage = bookRepository.findAllDistinctByIsbn();

        List<BookDTO> bookDTOS = new ArrayList<>();
        for(BookEntity bookEntity : bookPage){
            List<BookEntity> bookEntities = bookRepository.findAllByIsbn(bookEntity.getIsbn());
            List<BookEntity> avalBooks = bookRepository.findAvailableBooksByIsbn(bookEntity.getIsbn());
            BookDTO bookDTO = convertToDTO(bookEntity);
            if(!avalBooks.isEmpty()){
                bookDTO.setBookId(avalBooks.getFirst().getBookId());
            }
            bookDTO.setAvailableBook(avalBooks.size());
            bookDTO.setTotalBook( bookEntities.size());
            bookDTOS.add(bookDTO);
        }
        return bookDTOS;
    }

    public BookDTO updateBook(@Valid BookDTO bookDTO, String id) {
        Optional<BookEntity> exist = bookRepository.findById(id);
        if(exist.isEmpty())
            throw new ResourceNotFoundException("book not found with given id "+ id);

        BookEntity prevState = exist.get();
        BookEntity currentState = convertToEntity(bookDTO);
        prevState.setTitle(currentState.getTitle());
        prevState.setLanguage(currentState.getLanguage());
        BookEntity update = bookRepository.save(prevState);
        return  convertToDTO(update);
    }

    public BookDTO deleteBook(String id) {
        Optional<BookEntity> exist = bookRepository.findById(id);
        if(exist.isEmpty())
            throw new ResourceNotFoundException("book not found with given id "+ id);
        bookRepository.deleteById(id);
        return  convertToDTO(exist.get());
    }

    public List<RecordDTO> getAllIssuedBooks(String memberId) {
        List<RecordEntity> allBooks = recordRepository. findRecordsByMemberIdAndReturnDateIsNull(memberId);
        return allBooks.stream()
                .map(record->modelMapper.map(record,RecordDTO.class))
                .toList();
    }

    public BookDTO getBookByName(String name) {
        List<BookEntity> response = bookRepository.findAllBooksByTitle(name);
        if(response.isEmpty())
            throw  new ResourceNotFoundException("book not found with Given title: " + name);

        String isbn = response.getFirst().getIsbn();
        List<BookEntity> bookEntities = bookRepository.findAllByIsbn(isbn);
        List<BookEntity> avalBooks = bookRepository.findAvailableBooksByIsbn(isbn);
        BookDTO bookDTO = convertToDTO(response.getFirst());
        if(!avalBooks.isEmpty()){
            bookDTO.setBookId(avalBooks.getFirst().getBookId());
        }
        bookDTO.setAvailableBook(avalBooks.size());
        bookDTO.setTotalBook( bookEntities.size());


        return bookDTO;
    }

    public ResponseDTO issueBook(RecordDTO recordDTO) {
        Long adminId = recordDTO.getAdminId() ;
        String memberId = recordDTO.getMemberId();
        String bookId = recordDTO.getBookId();
        AdminEntity admin = adminRepository.findById(adminId).get() ;
        BookEntity book = bookRepository.findById(bookId).get();
        MemberEntity member =  memberRepository.findById(memberId).get();
        RecordEntity record = new RecordEntity();
        record.setBook(book);
        record.setAdmin(admin);
        record.setMember(member);
        record.setIssueDate(LocalDateTime.now());
        recordRepository.save(record);
        ResponseDTO res = ResponseDTO.builder()
                .message("Successfully Issued")
                .build();
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
        return  res;
    }

    public BookDTO getBookById(String bookId) {
        BookEntity book = bookRepository.findById(bookId).get();
        return convertToDTO(book);
    }
}
