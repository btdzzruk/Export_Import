package com.example.library.repository;

import com.example.library.model.entity.Borrow;
import com.example.library.model.entity.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);
}