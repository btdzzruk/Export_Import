package com.example.library.repository;

import com.example.library.model.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByTitle(String title);

    // Thêm mới: load toàn bộ title 1 lần thay vì N queries
    @Query("SELECT b.title FROM Book b")
    Set<String> findAllTitles();

    Optional<Book> findByCode(String code);
}
