package com.example.library.model.entity;

import com.example.library.model.entity.enums.BorrowRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "cccd", nullable = false, length = 20)
    private String cccd;

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    @Column(name = "book_code", nullable = false, length = 20)
    private String bookCode; // Mã sách mà thành viên muốn mượn

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BorrowRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
