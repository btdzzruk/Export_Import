package com.example.library.service.impl;

import com.example.library.excel.ExcelExporter;
import com.example.library.model.dto.MemberExportDTO;
import com.example.library.model.entity.Member;
import com.example.library.repository.MemberRepository;
import com.example.library.service.ExportMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ExportMemberServiceImpl implements ExportMemberService {

    private final MemberRepository memberRepository;

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
}
