package com.example.library.service.impl;

import com.example.library.model.entity.Member;
import com.example.library.model.request.MemberAddDTO;
import com.example.library.model.request.MemberUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.model.response.Pagination;
import com.example.library.repository.MemberRepository;
import com.example.library.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public APIResponse<PageData<Member>> findAllMember(int page, int size) {
        Page<Member> bookPage = memberRepository.findAll(PageRequest.of(page - 1, size));

        Pagination pagination = new Pagination();
        pagination.setCurrentPage(page);
        pagination.setPageSize(size);
        pagination.setTotalPages(bookPage.getTotalPages());
        pagination.setTotalItems(bookPage.getTotalElements());

        PageData<Member> pageData = new PageData<>();
        pageData.setItems(bookPage.getContent());
        pageData.setPagination(pagination);

        return new APIResponse<>(true, "Lấy danh sách thành công !", pageData);
    }

    @Override
    public APIResponse<Member> findMemberById(Long id) {
        Member member = memberRepository.findById(id).orElse(null);

        APIResponse<Member> response = new APIResponse<>();
        if (member == null) {
            response.setSuccess(false);
            response.setMessage("Không tìm thấy thành viên với id: " + id);
        } else {
            response.setSuccess(true);
            response.setMessage("Lấy thông tin thành viên thành công !");
            response.setData(member);
        }
        return response;
    }

    @Transactional
    @Override
    public APIResponse<Member> addMember(MemberAddDTO request) {
        // check trùng email
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại !");
        }
        // check trùng phone
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại !");
        }

        Member member = new Member();
        member.setCode(request.getCode());
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        Member savedMember = memberRepository.save(member);

        APIResponse<Member> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Thêm thành viên thành công !");
        response.setData(savedMember);
        return response;
    }

    @Transactional
    @Override
    public APIResponse<Member> updateMember(Long id, MemberUpdateDTO request) {
        // check trùng email
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại !");
        }
        // check trùng phone
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại !");
        }

        Member member = memberRepository.findById(id).orElseThrow(()->
                new RuntimeException("Không tìm thấy thành viên với id: " + id));

        member.setCode(request.getCode());
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        memberRepository.save(member);

        APIResponse<Member> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Cập nhật thành viên thành công !");
        response.setData(member);
        return response;
    }

    @Transactional
    @Override
    public APIResponse<String> deleteMemberById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(()->
                new RuntimeException("Không tìm thấy thành viên với id: " + id));
        memberRepository.delete(member);

        APIResponse<String> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Xóa thành công !");
        response.setData("Đã xóa thành viên với id: " + id);
        return response;
    }
}
