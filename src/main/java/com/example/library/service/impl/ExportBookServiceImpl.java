package com.example.library.service.impl;

import com.example.library.excel.ExcelExporter;
import com.example.library.model.dto.ExportBookDTO;
import com.example.library.model.entity.Book;
import com.example.library.repository.BookRepository;
import com.example.library.service.ExportBookService;
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
public class ExportBookServiceImpl implements ExportBookService {

    private final BookRepository bookRepository;

    @Override
    public ByteArrayResource exportBooksToExcel() throws IOException {

        try {

            int pageSize = 10_000;
            int pageNum = 0;

            List<List<ExportBookDTO>> pages = new ArrayList<>();

            AtomicLong index = new AtomicLong(1);

            Page<Book> page;

            do {

                page = bookRepository.findAll(PageRequest.of(pageNum++, pageSize));

                List<ExportBookDTO> dtoPage = page.getContent().stream().map(book -> {

                    ExportBookDTO dto = new ExportBookDTO();

                    dto.setSTT(index.getAndIncrement());
                    dto.setCode(book.getCode());
                    dto.setTitle(book.getTitle());
                    dto.setAuthor(book.getAuthor());
                    dto.setQuantity(book.getQuantity());
                    dto.setPrice(book.getPrice());

                    return dto;

                }).toList();

                pages.add(dtoPage);

            } while (page.hasNext());

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportStreaming(
                            pages,
                            ExportBookDTO.class,
                            "Books",
                            100
                    );
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new IOException("Lỗi khi export Excel: " + e.getMessage(), e);
        }
    }
}
