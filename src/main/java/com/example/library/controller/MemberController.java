package com.example.library.controller;

import com.example.library.model.entity.Member;
import com.example.library.model.request.MemberAddDTO;
import com.example.library.model.request.MemberUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
            ByteArrayResource resource = memberService.exportMembersToExcel();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=Export.xlsx")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất dữ liệu: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importMembers(@RequestParam("file") MultipartFile file) {

        try {

            memberService.importMembersFromExcel(file);

            return ResponseEntity.ok(
                    new APIResponse<>(true, "Import thành công", null)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(new APIResponse<>(false, e.getMessage(), null));

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi file Excel: " + e.getMessage(), null));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse<>(false, "Lỗi import Excel: " + e.getMessage(), null));
        }
    }
}
