package com.example.library.repository;

import com.example.library.model.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

}
