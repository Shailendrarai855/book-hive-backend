package com.library.project.college.PrivateLibrary.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AdminEntity {

    private static String PREFIX = "ADMIN-";
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Id
    private String adminId;

    @PrePersist
    public  void generatePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        this.adminId = PREFIX+password;
    }
    private  String name;
    private String gender;
    private String email;
    private  String address;
    private String password;
    private String contactNo;
    @OneToMany(mappedBy = "admin",cascade = CascadeType.ALL)
    private List<MemberEntity> members =  new ArrayList<>();
}
