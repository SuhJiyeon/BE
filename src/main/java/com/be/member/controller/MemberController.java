package com.be.member.controller;


import com.be.auth.JwtProvider;
import com.be.auth.JwtUtils;
import com.be.cart.service.CartService;
import com.be.common.dto.DefaultResDto;
import com.be.member.domain.Member;
import com.be.member.dto.req.*;
import com.be.member.dto.res.MemberDefaultResDto;
import com.be.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.be.common.code.SuccessCode.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final CartService cartService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<DefaultResDto<Object>> register(HttpServletRequest servletRequest, @RequestBody @Valid MemberRegisterReqDto reqDto) {
        //jwtProvider.authorizeGuestAccessJwt(servletRequest.getHeader(AUTHORIZATION));

        log.info("register = " + reqDto.toString());
        Member member = memberService.registerMember(reqDto);

        HttpHeaders headers = jwtProvider.generateUserJwt(member.getMemberNum(), member.getRoles());
        MemberDefaultResDto response = new MemberDefaultResDto(member);

        return ResponseEntity.status(USER_REGISTERED.getHttpStatus())
                .headers(headers)
                .body(DefaultResDto.singleDataBuilder()
                        .responseCode(USER_REGISTERED.name())
                        .responseMessage(USER_REGISTERED.getMessage())
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<DefaultResDto<Object>> login(@RequestBody @Valid MemberLoginReqDto memberLoginReqDto, HttpServletRequest request) {
        Member member = memberService.login(memberLoginReqDto);
        log.info("member Roles = " + member.getRoles().toString());

        HttpHeaders headers = jwtProvider.generateUserJwt(member.getMemberNum(), member.getRoles());
        MemberDefaultResDto response = new MemberDefaultResDto(member);

        // 로그인 시 장바구니 리스트 세션에 저장(-> cartController로 이동 예정)
//        HttpSession session = request.getSession();
//        List<CartItemResDto> cartList = cartService.initCartList(member.getMemberNum());
//        session.setAttribute("cartList", cartList);
//
//        log.info("{}", session);
//        log.info(session.getAttribute("cartList").toString());
        //

        log.info(headers.toString());

        log.info(response.toString());
        return ResponseEntity.status(USER_LOGIN.getHttpStatus())
                .headers(headers)
                .body(DefaultResDto.singleDataBuilder()
                        .responseCode(USER_LOGIN.name())
                        .responseMessage(USER_LOGIN.getMessage())
                        .data(response)
                        .build());
    }



    // 투자 성향 점수 업데이트 요청 처리
    @PostMapping("/investPreference")
    public ResponseEntity<String> updateInvestPreference(@RequestBody InvestPreferenceReqDto request, HttpServletRequest servletRequest) {
        long memberNum = jwtUtils.extractMemberNum(servletRequest);

        int updatedRows = memberService.updateMemberPreference(memberNum, request.getInvestScore());
        if (updatedRows > 0) {
            return ResponseEntity.ok("Update success");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("No update item");
        }
    }


    // 사용자의 투자 성향 점수와 성향을 조회하는 메서드 (GET)
    @GetMapping("/{memberNum}/investPreference")
    public ResponseEntity<MemberResponseReqDto> getInvestPreference(@PathVariable("memberNum") Long memberNum) {
        MemberResponseReqDto memberResponse = memberService.getMemberPreference(memberNum);

        if (memberResponse != null) {
            return ResponseEntity.ok(memberResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 해당 사용자가 없을 경우
        }
    }

    @GetMapping
    public ResponseEntity<DefaultResDto<Object>> findMyInfo(HttpServletRequest servletRequest) {
        Member member = jwtProvider.authorizeUserAccessJwt(servletRequest.getHeader(AUTHORIZATION));

        MemberDefaultResDto response = new MemberDefaultResDto(member);

        return ResponseEntity.status(SELF_USER_FOUND.getHttpStatus())
                .body(DefaultResDto.singleDataBuilder()
                        .responseCode(SELF_USER_FOUND.name())
                        .responseMessage(SELF_USER_FOUND.getMessage())
                        .data(response)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<DefaultResDto<Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute("cartList");
            session.invalidate();
        }
        return ResponseEntity.status(USER_LOGOUT.getHttpStatus())
                .body(DefaultResDto.noDataBuilder()
                        .responseCode(USER_LOGOUT.name())
                        .responseMessage(USER_LOGOUT.getMessage())
                        .build());
    }

    @PostMapping("/password/update")
    public ResponseEntity<DefaultResDto<Object>> updatePassword(@RequestBody @Valid UpdateMemberPasswordReqDto request,
                                                                HttpServletRequest servletRequest) {
        Member member = jwtProvider.authorizeUserAccessJwt(servletRequest.getHeader(AUTHORIZATION));
        memberService.updatePassword(member, request.getPassword(), request.getNewPassword());

        return ResponseEntity.status(PASSWORD_UPDATED.getHttpStatus())
                .body(DefaultResDto.noDataBuilder()
                        .responseCode(PASSWORD_UPDATED.name())
                        .responseMessage(PASSWORD_UPDATED.getMessage())
                        .build());
    }




}
