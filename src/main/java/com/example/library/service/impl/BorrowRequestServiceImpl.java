package com.example.library.service.impl;

import com.example.library.model.entity.Book;
import com.example.library.model.entity.Borrow;
import com.example.library.model.entity.BorrowRequest;
import com.example.library.model.entity.Member;
import com.example.library.model.entity.enums.BorrowRequestStatus;
import com.example.library.model.entity.enums.BorrowStatus;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.BorrowRequestRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.BorrowRequestService;
import com.example.library.word.BorrowRequestWordExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowRequestServiceImpl implements BorrowRequestService {

    private final BorrowRequestRepository requestRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRepository borrowRepository;
    private final BorrowRequestWordExporter exporter;

    @Override
    public ByteArrayResource createRequest(String cccd, String bookCode) {
        Book book = bookRepository.findByCode(bookCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với mã: " + bookCode));

        Member member = memberRepository.findByCccd(cccd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy member với CCCD: " + cccd));

        BorrowRequest request = new BorrowRequest();
        request.setCode(book.getCode());
        request.setCccd(cccd);
        request.setFullName(member.getFullName());
        request.setBookCode(bookCode);
        request.setStatus(BorrowRequestStatus.REQUESTED);
        request.setCreatedAt(LocalDateTime.now());
        BorrowRequest savedRequest = requestRepository.save(request);

        return exporter.exportRequestSlip(savedRequest);
    }

    @Override
    public void approveRequest(Long requestId) {
        BorrowRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu với id: " + requestId));

        if (!request.getStatus().equals(BorrowRequestStatus.REQUESTED)) {
            throw new RuntimeException("Phiếu đã được xử lý trước đó !");
        }

        Member member = memberRepository.findByCccd(request.getCccd())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên với CCCD: " + request.getCccd()));

        Book book = bookRepository.findByCode(request.getBookCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với mã sách: " + request.getBookCode()));

        long borrowedCount = borrowRepository
                .countByMemberIdAndStatus(member.getId(), BorrowStatus.BORROWED);

        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết, không thể mượn !");
        }
        if (borrowedCount >= 3) {
            throw new RuntimeException("Thành viên đã mượn 3 cuốn sách, không thể mượn thêm !");
        }

        Borrow borrow = new Borrow();
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.BORROWED);
        borrow.setBook(book);
        borrow.setMember(member);
        borrow.setRequest(request);
        borrowRepository.save(borrow);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        request.setStatus(BorrowRequestStatus.APPROVED);
        requestRepository.save(request);
    }

    @Override
    public void rejectRequest(Long requestId) {
        BorrowRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu với id: " + requestId));

        if (request.getStatus() != BorrowRequestStatus.REQUESTED) {
            throw new RuntimeException("Phiếu đã được xử lý trước đó !");
        }

        request.setStatus(BorrowRequestStatus.REJECTED);
        requestRepository.save(request);
    }
}
