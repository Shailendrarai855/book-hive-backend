package com.library.project.college.PrivateLibrary.repository;

import com.library.project.college.PrivateLibrary.entities.BookEntity;
import com.library.project.college.PrivateLibrary.entities.MemberEntity;
import com.library.project.college.PrivateLibrary.entities.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository  extends JpaRepository<RecordEntity,Long> {
    int countByMemberAndReturnDateIsNull(MemberEntity member);
    @Query("SELECT r FROM RecordEntity r WHERE r.member.memberId = :memberId AND r.book.bookId = :bookId AND r.returnDate IS NULL")
    Optional<RecordEntity> findActiveRecordByMemberAndBook(String memberId, String bookId);

    @Query("SELECT r FROM RecordEntity r WHERE r.member.memberId = :memberId AND r.returnDate IS NULL")
    List<RecordEntity> findRecordsByMemberIdAndReturnDateIsNull(String memberId);
    @Query("SELECT r FROM RecordEntity r WHERE r.returnDate IS NULL AND r.issueDate < :thirtyDaysAgo")
    List<RecordEntity> findOverdueRecords(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    @Query("SELECT r FROM RecordEntity r WHERE r.issueDate BETWEEN :startDate AND :endDate")
    List<RecordEntity> findRecordsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


//    @Query("SELECT b FROM Book b WHERE b.memberId = :memberId")
    List<RecordEntity> findByMember(MemberEntity memberId);
}
