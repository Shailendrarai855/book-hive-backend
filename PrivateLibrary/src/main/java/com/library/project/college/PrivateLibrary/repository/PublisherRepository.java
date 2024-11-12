package com.library.project.college.PrivateLibrary.repository;

import com.library.project.college.PrivateLibrary.entities.PublisherEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherEntity,String> {
    @Query(value = "SELECT * FROM publisher_entity WHERE publisher_id = ?1", nativeQuery = true)
   Optional< PublisherEntity > findByPublisherId(String publisherId);


    Optional<PublisherEntity> findByEmail(@NotNull(message = "Email can't be null") @NotBlank(message = "Email can't be blank") @Email(message = "Email should be valid") String email);
}
