package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportBookService {

    ByteArrayResource importBooksFromExcel(MultipartFile file) throws IOException;

}
