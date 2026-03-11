package com.example.library.service.impl;

import com.example.library.model.ExcelExporter;
import com.example.library.model.ExcelImporter;
import com.example.library.model.dto.ImportMemberDTO;
import com.example.library.model.dto.MemberExportDTO;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private static final int IMPORT_BATCH_SIZE = 500;

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

            int pageSize = 10_000;
            int pageNum = 0;

            List<List<MemberExportDTO>> pages = new ArrayList<>();

            AtomicLong index = new AtomicLong(1);

            Page<Member> page;

            do {

                page = memberRepository.findAll(PageRequest.of(pageNum++, pageSize));

                List<MemberExportDTO> dtoPage = page.getContent().stream().map(member -> {

                    MemberExportDTO dto = new MemberExportDTO();

                    dto.setSTT(index.getAndIncrement());
                    dto.setCode(member.getCode());
                    dto.setName(member.getFullName());
                    dto.setEmail(member.getEmail());
                    dto.setPhone(member.getPhone());
                    return dto;

                }).toList();

                pages.add(dtoPage);

            } while (page.hasNext());

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportStreaming(
                            pages,
                            MemberExportDTO.class,
                            "Members",
                            100
                    );
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new IOException("Lỗi khi export Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public ByteArrayResource importMembersFromExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được trống !");
        }

        List<ImportMemberDTO> result = new ArrayList<>();

        // cache DB
        Set<String> existingEmails = memberRepository.findAll()
                .stream()
                .map(Member::getEmail)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        Set<String> existingPhones = memberRepository.findAll()
                .stream()
                .map(Member::getPhone)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        Set<String> emailsInFile = new HashSet<>();
        Set<String> phonesInFile = new HashSet<>();

        ExcelImporter.importStreaming(
                file.getInputStream(),
                ImportMemberDTO.class,
                IMPORT_BATCH_SIZE,
                batch -> {

                    validateImportData(batch, existingEmails, existingPhones, emailsInFile, phonesInFile);

                    int errorIndex = -1;

                    for (int i = 0; i < batch.size(); i++) {
                        if ("FAIL".equals(batch.get(i).getStatus())) {
                            errorIndex = i;
                            break;
                        }
                    }

                    if (errorIndex == -1) {

                        try {

                            saveBatchInTransaction(batch);

                            batch.forEach(dto -> {
                                dto.setStatus("SUCCESS");
                                dto.setDescription("Imported thành công !");
                            });

                        } catch (Exception e) {

                            batch.forEach(dto -> {
                                dto.setStatus("FAIL");
                                dto.setDescription("DB error: " + e.getMessage());
                            });
                        }

                    } else {

                        List<ImportMemberDTO> successRows = new ArrayList<>();

                        for (int i = 0; i < batch.size(); i++) {

                            ImportMemberDTO dto = batch.get(i);

                            if (i < errorIndex) {

                                dto.setStatus("SUCCESS");
                                dto.setDescription("Imported thành công !");
                                successRows.add(dto);

                            } else if (i > errorIndex) {

                                dto.setStatus("FAIL");
                                dto.setDescription("Batch này có lỗi ở row trước đó, nên không được import !");
                            }
                        }

                        if (!successRows.isEmpty()) {
                            saveBatchInTransaction(successRows);
                        }
                    }

                    result.addAll(batch);
                }
        );

        ByteArrayOutputStream outputStream =
                ExcelExporter.exportToExcel(result, null);

        return new ByteArrayResource(outputStream.toByteArray());
    }

    private void validateImportData(
            List<ImportMemberDTO> batch,
            Set<String> existingEmails,
            Set<String> existingPhones,
            Set<String> emailsInFile,
            Set<String> phonesInFile
    ) {

        for (ImportMemberDTO dto : batch) {

            List<String> errors = new ArrayList<>();

            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                errors.add("Email không được trống");
            }

            if (dto.getPhone() == null || dto.getPhone().isBlank()) {
                errors.add("Phone không được trống");
            }

            if (existingEmails.contains(dto.getEmail())) {
                errors.add("Email đã tồn tại trong DB");
            }

            if (existingPhones.contains(dto.getPhone())) {
                errors.add("Phone đã tồn tại trong DB");
            }

            if (!emailsInFile.add(dto.getEmail())) {
                errors.add("Email bị trùng trong file");
            }

            if (!phonesInFile.add(dto.getPhone())) {
                errors.add("Phone bị trùng trong file");
            }

            if (!errors.isEmpty()) {
                dto.setStatus("FAIL");
                dto.setDescription(String.join(" | ", errors));
            } else {
                dto.setStatus("PENDING");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatchInTransaction(List<ImportMemberDTO> batch) {

        List<Member> members = batch.stream().map(dto -> {

            Member member = new Member();

            member.setCode(dto.getCode());
            member.setFullName(dto.getName());
            member.setEmail(dto.getEmail());
            member.setPhone(dto.getPhone());

            return member;

        }).toList();

        memberRepository.saveAll(members);
    }
}
