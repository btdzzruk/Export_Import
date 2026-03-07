package com.example.library.service.impl;

import com.example.library.model.ExcelExporter;
import com.example.library.model.ExcelImporter;
import com.example.library.model.dto.ImportBookDTO;
import com.example.library.model.dto.ImportMemberDTO;
import com.example.library.model.dto.MemberExportDTO;
import com.example.library.model.entity.Book;
import com.example.library.model.entity.Member;
import com.example.library.model.request.MemberAddDTO;
import com.example.library.model.request.MemberUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.model.response.Pagination;
import com.example.library.repository.MemberRepository;
import com.example.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private static final int DEFAULT_PAGE_SIZE = 4;

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

    @Override
    public ByteArrayResource exportMembersToExcel() throws IOException {
        try {
            List<Member> members = memberRepository.findAll();
            if (members == null) {
                throw new RuntimeException("Không có dữ liệu để xuất !");
            }

            List<MemberExportDTO> exportData = buildMemberExportData(members);
            if (exportData == null || exportData.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu để xuất !");
            }

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportToExcel(exportData, null);
            return new ByteArrayResource(outputStream.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }

    @Override
    public void importMembersFromExcel(MultipartFile file) throws IOException {
        try {

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File Excel không được rỗng!");
            }

            List<ImportMemberDTO> importData =
                    ExcelImporter.importFromExcel(
                            file.getInputStream(),
                            ImportMemberDTO.class
                    );

            if (importData == null || importData.isEmpty()) {
                throw new RuntimeException("File Excel không có dữ liệu!");
            }

            List<Member> members = buildMembers(importData);

            memberRepository.saveAll(members);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Lỗi khi import Excel: " + e.getMessage(), e);
        }
    }

    // Hàm này sẽ chuyển đổi danh sách Member thành danh sách MemberExportDTO để xuất Excel
    private List<MemberExportDTO> buildMemberExportData(List<Member> members) {
        AtomicLong counter = new AtomicLong(1);
        return members.stream().map(member -> {
            MemberExportDTO dto = new MemberExportDTO();
            dto.setSTT(counter.getAndIncrement());
            dto.setCode(member.getCode());
            dto.setName(member.getFullName());
            dto.setEmail(member.getEmail());
            dto.setPhone(member.getPhone());
            return dto;
        }).toList();
    }

    // Hàm này sẽ kiểm tra trùng email và phone trước khi tạo đối tượng Member
    private List<Member> buildMembers(List<ImportMemberDTO> importData) {

        return importData.stream().map(dto -> {

            if (memberRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại: " + dto.getEmail());
            }

            if (memberRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại: " + dto.getPhone());
            }

            Member member = new Member();
            member.setCode(dto.getCode());
            member.setFullName(dto.getName());
            member.setEmail(dto.getEmail());
            member.setPhone(dto.getPhone());
            return member;

        }).toList();
    }
}
