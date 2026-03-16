package com.example.library.service.impl;

import com.example.library.excel.ExcelExporter;
import com.example.library.excel.ExcelImporter;
import com.example.library.model.dto.ImportMemberDTO;
import com.example.library.model.entity.Member;
import com.example.library.repository.MemberRepository;
import com.example.library.service.ImportMemberService;
import com.example.library.validation.MemberImportValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportMemberServiceImpl implements ImportMemberService {

    private final MemberRepository memberRepository;
    private final MemberImportValidation validation;

    private static final int IMPORT_BATCH_SIZE = 500;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public ByteArrayResource importMembersFromExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được trống");
        }

        List<ImportMemberDTO> result = new ArrayList<>();

        // lấy dữ liệu hiện có để validate
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

                    validation.validateImportData(
                            batch,
                            existingEmails,
                            existingPhones,
                            emailsInFile,
                            phonesInFile
                    );

                    List<ImportMemberDTO> successRows = new ArrayList<>();

                    for (ImportMemberDTO dto : batch) {

                        if ("PENDING".equals(dto.getStatus())) {

                            dto.setStatus("SUCCESS");
                            dto.setDescription("Imported thành công");

                            successRows.add(dto);

                            // cập nhật cache
                            existingEmails.add(dto.getEmail());
                            existingPhones.add(dto.getPhone());
                        }
                    }

                    if (!successRows.isEmpty()) {
                        saveBatchInTransaction(successRows);
                    }

                    result.addAll(batch);
                }
        );

        ByteArrayOutputStream outputStream =
                ExcelExporter.exportToExcel(result, null);

        return new ByteArrayResource(outputStream.toByteArray());
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