package com.library.project.college.PrivateLibrary.services;

import com.library.project.college.PrivateLibrary.dto.AdminDTO;
import com.library.project.college.PrivateLibrary.dto.LogInDTO;
import com.library.project.college.PrivateLibrary.dto.RecordDTO;
import com.library.project.college.PrivateLibrary.entities.AdminEntity;
import com.library.project.college.PrivateLibrary.entities.BookEntity;
import com.library.project.college.PrivateLibrary.entities.MemberEntity;
import com.library.project.college.PrivateLibrary.entities.RecordEntity;
import com.library.project.college.PrivateLibrary.exceptions.ResourceNotFoundException;
import com.library.project.college.PrivateLibrary.repository.AdminRepository;
import com.library.project.college.PrivateLibrary.repository.BookRepository;
import com.library.project.college.PrivateLibrary.repository.MemberRepository;
import com.library.project.college.PrivateLibrary.repository.RecordRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private  final ModelMapper modelMapper;
    private final RecordRepository recordRepository;
    private final MemberRepository memberRepository;
    private  final EmailService emailService;
    private final BookRepository bookRepository;

    public AdminService(AdminRepository adminRepository, ModelMapper modelMapper, RecordRepository recordRepository, MemberRepository memberRepository, EmailService emailService, BookRepository bookRepository) {
        this.adminRepository = adminRepository;
        this.modelMapper = modelMapper;
        this.recordRepository = recordRepository;
        this.memberRepository = memberRepository;
        this.emailService = emailService;
        this.bookRepository = bookRepository;

    }

    public AdminEntity createNewAdmin(AdminEntity admin) {
        return  adminRepository.save(admin);
    }

    public AdminDTO login(LogInDTO logInDTO) {
        String email = logInDTO.getEmail();
        String password = logInDTO.getPassword();
        Optional<AdminEntity> res = adminRepository.findByEmail(email);
        if(res.isEmpty())
            throw  new ResourceNotFoundException("email not registered");
        System.out.println("first");
        AdminEntity admin = res.get();

        if(admin.getPassword().equals(password)){
            AdminDTO resp =  modelMapper.map(admin,AdminDTO.class);
            return resp;
        }
        throw  new ResourceNotFoundException("password is incorrect");
    }

    public List<RecordDTO> getDueRecord() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<RecordEntity> records = recordRepository.findOverdueRecords(thirtyDaysAgo);
        return records.stream()
                .map(record->modelMapper.map(record,RecordDTO.class))
                .toList();
    }

    public List<RecordDTO> getRecordWithDate(LocalDateTime from, LocalDateTime to) {
        List<RecordEntity> records = recordRepository. findRecordsBetweenDates(from,to);
        return records.stream()
                .map(record->modelMapper.map(record,RecordDTO.class))
                .toList();
    }

    public RecordDTO sendDueMail(RecordDTO recordDTO) {
        String memberId = recordDTO.getMemberId();
        String bookId = recordDTO.getBookId() ;
        BookEntity bookEntity = bookRepository.findById(bookId).get();
        MemberEntity memberEntity = memberRepository.findById(memberId).get();
        String to = memberEntity.getEmail();
        String bookName = bookEntity.getTitle();
        LocalDateTime issueDate = recordDTO.getIssueDate();
        String subject = "Overdue Book Notice: Immediate Attention Required";
        String body = "Dear " + memberEntity.getName() + ",\n\n" +
                "We hope this message finds you well. Our records indicate that the book you borrowed on " +
                issueDate.toLocalDate() + " is now overdue.\n\n" +
                "Book Details:\n" +
                "Title: " + bookName + "\n" +
                "Book ID: " + bookId + "\n" +
                "Issue Date: " + issueDate.toLocalDate() + "\n\n" +
                "We kindly ask that you return the book as soon as possible to avoid any additional late fees.\n\n" +
                "Please let us know if there are any issues or if you need assistance with the return process.\n\n" +
                "Thank you for your attention to this matter.\n\n" +
                "Best regards,\n" +
                "Library Management Team";

        emailService.sendMail(to,subject,body);
        return recordDTO;
    }

    public List<RecordDTO> getMemberBookDetails(String memberId) {
        MemberEntity memberEntity = memberRepository.findById(memberId).get();
        List<RecordEntity> records = recordRepository.findByMember(memberEntity);
        return records.stream()
                .map(book->modelMapper.map(book,RecordDTO.class))
                .toList();
    }
}
