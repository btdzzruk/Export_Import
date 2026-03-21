package com.example.library.model.entity;

import com.example.library.model.entity.enums.BorrowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borrowDate")
    private LocalDateTime borrowDate;

    @Column(name = "returnDate")
    private LocalDateTime returnDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false)
    private Boolean notified = false;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private BorrowRequest request;

    @PrePersist
    public void prePersist() {
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
        if (status == null) {
            status = BorrowStatus.BORROWED;
        }
    }
}