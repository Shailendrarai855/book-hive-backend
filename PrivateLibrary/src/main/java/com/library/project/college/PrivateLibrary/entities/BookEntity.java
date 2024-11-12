package com.library.project.college.PrivateLibrary.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BookEntity {

    private static String PREFIX = "BOOK-";
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();
    @Id
    private String bookId;

    @PrePersist
    public  void generatePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        this.bookId = PREFIX+password;
    }

    private String title;
    private String authorName;
    private String edition;
    private String language;
    private String isbn;
    private Integer totalPages;
    private LocalDate publicationYear;
    private  String category;
    private Boolean available;
    @ManyToOne
    @JoinColumn(name = "publisher_id", referencedColumnName = "publisherId")
    private PublisherEntity publisher;

    @OneToOne(mappedBy = "book",cascade = CascadeType.ALL )
    @JsonIgnore
    private  RequestBookEntity requestBooks;

}
