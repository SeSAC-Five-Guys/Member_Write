package com.sesac.member_write_server.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.member_write_server.common.dto.ResDto;
import com.sesac.member_write_server.member.dto.MemberModifyInfo;
import com.sesac.member_write_server.member.dto.MemberSignUpInfo;
import com.sesac.member_write_server.member.service.MailService;
import com.sesac.member_write_server.member.service.MemberService;
import com.sesac.member_write_server.member.serviceUtil.MemberServiceMakeResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/api/write/v1/members")
public class MemberController {
	private final MemberService memberService;
	private final MailService mailService;
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
		return new ResponseEntity<>(response, status);
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
		@Pattern(regexp = "^(?=.[가-힣a-zA-Z])[-가-힣a-zA-Z0-9!@#$%&]{5,12}$|(?=.[가-힣])[-가-힣0-9!@#$%&]{5,12}$|(?=.*[a-zA-Z])[-a-zA-Z0-9!@#$%&]{5,12}$", message = "잘못된 닉네임 형식")
		@NotBlank(message = "닉네임 공백 포함 불가")
		@PathVariable("nickname") String nickname
	){
		ResDto response = memberService.isDuplicatedNickname(nickname);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}
	/* 회원 가입 시 이메일 본인 인증 */
	@GetMapping("/authentication/{email}")
	public ResponseEntity<ResDto> memberCreateInMailAuth(
		HttpServletRequest httpServletRequest,
		@Valid
		@Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[A-Za-z]+$", message = "잘못된 이메일 형식")
		@Size(max = 100, message = "이메일 길이 초과")
		@NotBlank(message = "이메일 공백 포함 불가")
		@PathVariable("email") String email
	) throws Exception{
		ResDto response = mailService.sendMail(email);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}

	/* 회원 가입 */
	@PostMapping("/member")
	public ResponseEntity<ResDto> memberCreateAndSendMQ(
		@Valid @RequestBody MemberSignUpInfo memberSignUpInfo
	){
		ResDto response = memberService.createMember(memberSignUpInfo);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}

	/* 회원 정보 수정 중복 확인*/
	@GetMapping("/nickname/{nick1}/{nick2}")
	public ResponseEntity<ResDto> isDuplicatedNicknameInModify(
		@Valid
		@Pattern(regexp = "^(?=.[가-힣a-zA-Z])[-가-힣a-zA-Z0-9!@#$%&]{5,12}$|(?=.[가-힣])[-가-힣0-9!@#$%&]{5,12}$|(?=.*[a-zA-Z])[-a-zA-Z0-9!@#$%&]{5,12}$", message = "잘못된 닉네임 형식")
		@NotBlank(message = "닉네임 공백 포함 불가")
		@PathVariable("nick1") String currentNick,
		@PathVariable("nick2") String changeNick
	){
		ResDto response = memberService.isDuplicatedNicknameInModify(currentNick, changeNick);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}
	@GetMapping("/phone_number/{phoneNum1}/{phoneNum2}")
	public ResponseEntity<ResDto> isDuplicatedPhoneNumInModify(
		@Valid
		@Pattern(regexp = "^01[0-9]{9}$", message = "잘못된 핸드폰 번호 형식")
		@NotBlank(message = "핸드폰 번호 공백 포함 불가")
		@PathVariable("phoneNum1") String currentPhoneNum,
		@PathVariable("phoneNum2") String changePhoneNum
	){
		ResDto response = memberService.isDuplicatedPhoneNumInModify(currentPhoneNum, changePhoneNum);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}


	/* 회원 정보 수정 */
	@PutMapping("/member/{email}")
	public ResponseEntity<ResDto> memberModifytAndSendMQ(
		@Valid
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

	/* 회원 정보 삭제	*/
	@DeleteMapping("/member/{email}")
	public ResponseEntity<ResDto> memberDeleteAnsSendMQ(
		@Valid
		@Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[A-Za-z]+$", message = "잘못된 이메일 형식")
		@Size(max = 100, message = "이메일 길이 초과")
		@NotBlank(message = "이메일 공백 포함 불가")
		@PathVariable("email") String email
	){
		ResDto response = memberService.deleteMember(email);
		HttpStatus status = makeResult.changeStatus(response);
		return new ResponseEntity<>(response, status);
	}
}
