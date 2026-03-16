package com.example.library.service.impl;

import com.example.library.excel.ExcelExporter;
import com.example.library.excel.ExcelImporter;
import com.example.library.model.dto.ImportBookDTO;
import com.example.library.model.entity.Book;
import com.example.library.repository.BookRepository;
import com.example.library.service.ImportBookService;
import com.example.library.validation.BookImportValidator;
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
public class ImportBookServiceImpl implements ImportBookService {

    private final BookRepository bookRepository;
    private final BookImportValidator validator;

    private static final int IMPORT_BATCH_SIZE = 500;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public ByteArrayResource importBooksFromExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được trống!");
        }

        List<ImportBookDTO> result = new ArrayList<>();

        // lấy danh sách tiêu đề sách đã tồn tại trong DB để validate
        Set<String> existingTitles = bookRepository.findAllTitles();
        Set<String> titlesInFile = new HashSet<>();

        ExcelImporter.importStreaming(
                file.getInputStream(),
                ImportBookDTO.class,
                IMPORT_BATCH_SIZE,
                batch -> {

                    validator.validate(batch, existingTitles, titlesInFile);

                    List<ImportBookDTO> successRows = new ArrayList<>();

                    for (ImportBookDTO dto : batch) {

                        if ("PENDING".equals(dto.getStatus())) {

                            dto.setStatus("SUCCESS");
                            dto.setDescription("Imported thành công");

                            successRows.add(dto);

                            // cập nhật cache
                            existingTitles.add(dto.getTitle());
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
    public void saveBatchInTransaction(List<ImportBookDTO> batch) {

        List<Book> books = batch.stream().map(dto -> {

            Book book = new Book();

            book.setCode(dto.getCode());
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setQuantity(dto.getQuantity());
            book.setPrice(dto.getPrice());

            return book;

        }).toList();

        bookRepository.saveAll(books);
    }
}