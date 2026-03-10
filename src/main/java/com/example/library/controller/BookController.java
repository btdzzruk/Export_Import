package com.example.library.controller;

import com.example.library.model.entity.Book;
import com.example.library.model.request.BookAddDTO;
import com.example.library.model.request.BookUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<APIResponse<PageData<Book>>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {

        return ResponseEntity.ok(bookService.findAllBook(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Book>> findBookById(@PathVariable Long id) {
        return new ResponseEntity<>(bookService.findBookById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<APIResponse<Book>> addBook(@Valid @RequestBody BookAddDTO dto) {
        return new ResponseEntity<>(bookService.addBook(dto), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Book>> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateDTO dto) {
        return new ResponseEntity<>(bookService.updateBook(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteBook(@PathVariable Long id) {
        return new ResponseEntity<>(bookService.deleteBookById(id), HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportBooksToExcel() {
        try {
            ByteArrayResource resource = bookService.exportBooksToExcel();
            String filename = "Books_" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new APIResponse<>(false, e.getMessage(), null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi khi xử lý file: " + e.getMessage(), null));
        } catch (Throwable e) { // ← catch OutOfMemoryError và các Error khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importBooks(@RequestParam("file") MultipartFile file) {
        try {
            // Giới hạn file size tại controller layer
            long maxFileSize = 50 * 1024 * 1024; // 50MB
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest()
                        .body(new APIResponse<>(false, "File quá lớn, tối đa 50MB", null));
            }

            ByteArrayResource resource = bookService.importBooksFromExcel(file);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=Import_Result.xlsx")
                    .header("Content-Type", "application/octet-stream")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new APIResponse<>(false, e.getMessage(), null));
        }
    }
}
