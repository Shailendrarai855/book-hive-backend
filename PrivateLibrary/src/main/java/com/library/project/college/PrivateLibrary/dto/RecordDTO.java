package com.library.project.college.PrivateLibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecordDTO {
    private String memberId;
    private String adminId;
    private String bookId;
    private LocalDateTime issueDate;
    private LocalDate returnDate;
    private String returnedBy;
}
