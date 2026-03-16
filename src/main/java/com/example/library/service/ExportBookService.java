package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

public interface ExportBookService {

    ByteArrayResource exportBooksToExcel() throws IOException;
}
