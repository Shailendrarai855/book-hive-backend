package com.library.project.college.PrivateLibrary.repository;
import com.library.project.college.PrivateLibrary.entities.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity,String> {
    Optional<AdminEntity> findByEmail(String email);
}
