package com.example.library.service;

import com.example.library.model.entity.Member;
import com.example.library.model.request.MemberAddDTO;
import com.example.library.model.request.MemberUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;

import java.io.IOException;

public interface MemberService {

    APIResponse<PageData<Member>> findAllMember(int page, int size);

    APIResponse<Member> findMemberById(Long id);

    APIResponse<Member> addMember(MemberAddDTO request);

    APIResponse<Member> updateMember(Long id, MemberUpdateDTO request);

    APIResponse<String> deleteMemberById(Long id);
}
