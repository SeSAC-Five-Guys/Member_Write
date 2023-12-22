package com.sesac.db_access_write.member.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.db_access_write.common.dto.ResDto;
import com.sesac.db_access_write.kafka.KafkaProducerService;
import com.sesac.db_access_write.member.entity.Member;
import com.sesac.db_access_write.member.entity.MemberRole;
import com.sesac.db_access_write.member.persistence.MemberRepository;
import com.sesac.db_access_write.member.serviceUtil.MemberServiceValidating;
import com.sesac.db_access_write.member.serviceUtil.MemberServiceMakeResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService{
	private final MemberRepository memberRepository;

	private final KafkaProducerService kafkaProducerService;

	private final MemberServiceValidating validating;
	private final MemberServiceMakeResult makeResult;
	@Override
	@Transactional
	public ResDto signup(Map<String, String> signUpInfo) {
		try {
			if (validating.isDuplicatedValCnt(
				memberRepository.countByEmail(signUpInfo.get("email"))
			)){
				return makeResult.makeDuplicateEmailResult();
			}

			if (validating.isDuplicatedValCnt(
				memberRepository.countByPhoneNumber(signUpInfo.get("phoneNumber"))
			)){
				return makeResult.makeDuplicatedPhoneNumResult();
			}

			if (validating.isDuplicatedValCnt(
				memberRepository.countByNickname(signUpInfo.get("nickname"))
			)){
				return makeResult.makeDuplicatedNicknameResult();
			}

			Member member = Member.builder()
				.email(signUpInfo.get("email"))
				.phoneNumber(signUpInfo.get("phoneNumber"))
				.nickname(signUpInfo.get("nickname"))
				.password(validating.encodingPwd(signUpInfo.get("password")))
				.memberRole(Collections.singleton(MemberRole.USER_1))
				.build();

			memberRepository.save(member);
			Member savedMember = memberRepository.findByEmail(signUpInfo.get("email"));

			signUpInfo.remove("password");
			signUpInfo.put("memberId", savedMember.getMemberId().toString());
			signUpInfo.put("password", savedMember.getPassword());
			signUpInfo.put("createdAt", savedMember.getCreatedAt().toString());
			signUpInfo.put("updatedAt", savedMember.getUpdatedAt().toString());

			if (!kafkaProducerService.sendCreateMessage(signUpInfo)) {
				return makeResult.makeSendingToKafkaFailedResult();
			}

			return makeResult.makeSuccessResultNoData();

		} catch (Exception e){
			return makeResult.makeInternalServerErrorResult(e);
		}
	}

}
