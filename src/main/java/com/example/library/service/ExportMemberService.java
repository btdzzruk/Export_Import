package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

public interface ExportMemberService {

    ByteArrayResource exportMembersToExcel() throws IOException;
}
