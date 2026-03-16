package com.example.library.controller;

import com.example.library.model.entity.Member;
import com.example.library.model.request.MemberAddDTO;
import com.example.library.model.request.MemberUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.service.ExportMemberService;
import com.example.library.service.ImportMemberService;
import com.example.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ExportMemberService exportMemberService;
    private final ImportMemberService importMemberService;

    @GetMapping
    public ResponseEntity<APIResponse<PageData<Member>>> getAllMembers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(memberService.findAllMember(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Member>> getMemberById(@PathVariable Long id) {
        return new ResponseEntity<>(memberService.findMemberById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<APIResponse<Member>> createMember(@Valid @RequestBody MemberAddDTO dto) {
        return new ResponseEntity<>(memberService.addMember(dto), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Member>> updateMember(@PathVariable Long id,
                                                            @Valid @RequestBody MemberUpdateDTO dto) {
        return new ResponseEntity<>(memberService.updateMember(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteMember(@PathVariable Long id) {
        return new ResponseEntity<>(memberService.deleteMemberById(id), HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportMembersToExcel() {
        try {
            ByteArrayResource resource = exportMemberService.exportMembersToExcel();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=Export.xlsx")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất dữ liệu: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importBooks(@Valid @RequestParam("file") MultipartFile file) {

        try {

            ByteArrayResource resource = importMemberService.importMembersFromExcel(file);

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
