package com.library.project.college.PrivateLibrary.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class RecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne
    @JoinColumn(name = "issuedBy" ,referencedColumnName = "adminId")
    private AdminEntity admin;

    @ManyToOne
    @JoinColumn(name = "memberId" ,referencedColumnName = "memberId")
    private MemberEntity member;

    @ManyToOne
    @JoinColumn(name = "bookId",referencedColumnName = "bookId")
    private BookEntity book;

    private String returnedBy;

    private LocalDateTime issueDate;
    private int fine;
    private LocalDate returnDate;

}
