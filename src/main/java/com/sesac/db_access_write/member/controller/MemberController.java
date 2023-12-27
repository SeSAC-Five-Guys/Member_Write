package com.sesac.db_access_write.member.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.db_access_write.common.dto.ResDto;
import com.sesac.db_access_write.member.dto.MemberModifyInfo;
import com.sesac.db_access_write.member.dto.MemberSignUpInfo;
import com.sesac.db_access_write.member.service.MemberService;
import com.sesac.db_access_write.member.serviceUtil.MemberServiceMakeResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/v1/members")
public class MemberController {
	private final MemberService memberService;
	private final MemberServiceMakeResult makeResult;

	/* 회원 가입 및 그 절차를 위한 중복 확인 및 유효성 검사*/
	@GetMapping("/email/{email}")
	public ResponseEntity<ResDto> isDuplicatedEmail(
		@Valid
		@Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[A-Za-z]+$", message = "잘못된 이메일 형식")
		@Size(max = 100, message = "이메일 길이 초과")
		@NotBlank(message = "이메일 공백 포함 불가")
		@PathVariable("email") String email
	){
		ResDto response = memberService.isDuplicatedEmail(email);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/phone_number/{phoneNumber}")
	public ResponseEntity<ResDto> isDuplicatedPhoneNumber(
		@Valid
		@Pattern(regexp = "^01[0-9]{9}$", message = "잘못된 핸드폰 번호 형식")
		@NotBlank(message = "핸드폰 번호 공백 포함 불가")
		@PathVariable("phoneNumber") String phoneNumber
	){
		ResDto response = memberService.isDuplicatedPhoneNumber(phoneNumber);

		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}

	@GetMapping("/nickname/{nickname}")
	public ResponseEntity<ResDto> isDuplicatedNickname(
		@Valid
		@Pattern(regexp = "^[^!@#_]{1,12}$", message = "잘못된 닉네임 형식")
		@NotBlank(message = "닉네임 공백 포함 불가")
		@PathVariable("nickname") String nickname
	){
		ResDto response = memberService.isDuplicatedNickname(nickname);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}

	@PostMapping("/member")
	public ResponseEntity<ResDto> MemberCreateAndSendMQ(
		@Valid @RequestBody MemberSignUpInfo memberSignUpInfo
	){
		ResDto response = memberService.signup(memberSignUpInfo);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}

	/* 회원 정보 수정 */
	@PutMapping("/member/{email}")
	public ResponseEntity<ResDto> MemberModifytAndSendMQ(
		@Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[A-Za-z]+$", message = "잘못된 이메일 형식")
		@Size(max = 100, message = "이메일 길이 초과")
		@NotBlank(message = "이메일 공백 포함 불가")
		@PathVariable("email") String email,
		@RequestBody MemberModifyInfo memberModifyInfo
	){
		ResDto response = memberService.modifyMember(email, memberModifyInfo);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}
}
