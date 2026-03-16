package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportMemberService {

    ByteArrayResource importMembersFromExcel(MultipartFile file) throws IOException;
}
