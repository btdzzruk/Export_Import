package com.example.library.service.impl;

import com.example.library.model.entity.Book;
import com.example.library.model.entity.Borrow;
import com.example.library.model.entity.Member;
import com.example.library.model.entity.enums.BorrowStatus;
import com.example.library.model.request.BorrowSlipExportRequest;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.BorrowService;
import com.example.library.word.BorrowWordExporter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // đảm bảo tính nhất quán khi mượn và trả sách, tránh lỗi liên quan đến số lượng sách
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowWordExporter exporter;

    @Override
    public void returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi mượn !"));

        // Chỉ cho trả nếu đang ở trạng thái BORROWED
        if (borrow.getStatus() != BorrowStatus.BORROWED) {
            throw new RuntimeException("Không thể trả vì sách chưa được mượn hoặc đã trả trước đó !");
        }

        // cập nhật trạng thái
        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.RETURNED);

        Book book = borrow.getBook();

        // tăng lại số lượng sách
        book.setQuantity(book.getQuantity() + 1);

        bookRepository.save(book);
        borrowRepository.save(borrow);
    }

    @Override
    public ByteArrayResource exportBorrowSlip(BorrowSlipExportRequest request) {
        // Validate input
        if (request.getCccd() == null || request.getCccd().trim().isEmpty()) {
            throw new RuntimeException("CCCD không được để trống!");
        }
        if (request.getBookCode() == null || request.getBookCode().trim().isEmpty()) {
            throw new RuntimeException("Mã sách không được để trống!");
        }
        if (request.getDueDate() == null) {
            throw new RuntimeException("Hạn trả sách không được để trống!");
        }

        // Tìm member dựa trên CCCD
        Member member = memberRepository.findByCccd(request.getCccd())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên với CCCD: " + request.getCccd()));

        // Tìm book dựa trên mã sách
        Book book = bookRepository.findByCode(request.getBookCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với mã: " + request.getBookCode()));

        // Kiểm tra sách còn số lượng không
        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết!");
        }

        // Tạo object Borrow tạm thời để xuất phiếu (chưa lưu vào DB)
        Borrow borrowSlip = new Borrow();
        borrowSlip.setMember(member);
        borrowSlip.setBook(book);
        borrowSlip.setBorrowDate(LocalDateTime.now());
        borrowSlip.setDueDate(LocalDate.from(request.getDueDate()));
        borrowSlip.setStatus(BorrowStatus.BORROWED);

        // Xuất file Word
        return exporter.exportBorrowSlip(borrowSlip);
    }

    @Override
    public List<Borrow> findOverdueBorrows() {
        // Lấy tất cả borrow chưa trả và quá hạn
        return borrowRepository.findAll().stream()
                .filter(b -> b.getStatus() == BorrowStatus.BORROWED)
                .filter(b -> b.getDueDate().isBefore(LocalDate.now()))
                .filter(b -> !b.getNotified()) // chỉ chưa gửi email
                .collect(Collectors.toList());
    }

    @Override
    public void markNotified(Borrow borrow) {
        borrow.setNotified(true);
        borrowRepository.save(borrow);
    }
}
