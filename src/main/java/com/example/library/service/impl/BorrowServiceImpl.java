package com.example.library.service.impl;

import com.example.library.model.entity.Book;
import com.example.library.model.entity.Borrow;
import com.example.library.model.entity.Member;
import com.example.library.model.entity.enums.BorrowStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.BorrowService;
import com.example.library.word.BorrowWordExporter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional // đảm bảo tính nhất quán khi mượn và trả sách, tránh lỗi liên quan đến số lượng sách
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowWordExporter exporter;

    @Override
    public ByteArrayResource borrowBook(Long memberId, String bookCode) {

        // kiểm tra số sách đang mượn
        long borrowedCount = borrowRepository
                .countByMemberIdAndStatus(memberId, BorrowStatus.BORROWED);

        if (borrowedCount >= 3) {
            throw new RuntimeException("Mỗi thành viên chỉ được mượn tối đa 3 sách!");
        }

        Book book = bookRepository.findByCode(bookCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với mã: " + bookCode));

        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết !");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên với mã: " + memberId));

        Borrow borrow = new Borrow();
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.BORROWED);
        borrow.setBook(book);
        borrow.setMember(member);

        Borrow savedBorrow = borrowRepository.save(borrow);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        return exporter.exportBorrowSlip(savedBorrow);
    }

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
}
