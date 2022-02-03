package com.ssafy.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.api.request.StudyAcceptPutReq;
import com.ssafy.api.request.StudyApplyPostReq;
import com.ssafy.api.request.StudyCreatePostReq;
import com.ssafy.api.response.StudyInfo;
import com.ssafy.api.response.StudyInfoListRes;
import com.ssafy.api.response.StudyListRes;
import com.ssafy.api.response.StudyRes;
import com.ssafy.api.response.UserListRes;
import com.ssafy.api.service.StudyService;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Study;
import com.ssafy.db.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "study API", tags = {"Study"})
@RestController
@RequestMapping("/api/v1/study")
public class StudyController {

	@Autowired
	StudyService studyService;
	
	/*
	 *  /study/create post o
	 *  /study/{userno} get
	 *  /study/list/{userno} get o
	 *  /study/simple-detail/{studyno} 
	 *  /study/all-user/{studyno}
	 *  /study/apply post
	 *  /study/accept put
	 */
	
	
	//interests 추가하기!!!!!
	@PostMapping("/create")
	@ApiOperation(value = "스터디 방 생성", notes = "스터디 방에 대한 정보를 받고 스터디 방을 생성한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> create(
			@RequestBody @ApiParam(value="생성 스터디 방 정보", required = true) StudyCreatePostReq studyInfo){	
		if(studyService.createStudy(studyInfo))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "성공"));
		else
			return ResponseEntity.status(200).body(BaseResponseBody.of(401, "실패"));
	}
	
	@GetMapping("/{userno}")
	@ApiOperation(value = "참여 중인 스터디의 정보", notes = "회원 번호에 따른 참여 스터디 목록을 반환한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<StudyInfoListRes> getStudyListNMemberInfo(@PathVariable("userno") @ApiParam(value = "유저 pk", required = true) int userno){
		List<Study> studyList = studyService.getStudyList(userno);
		List<StudyInfo> studyInfoList = new ArrayList<StudyInfo>();
		
		for(int i = 0; i < studyList.size(); i++)
			studyInfoList.add(studyService.getStudyInfo(studyList.get(i).getStudyno(), studyList.get(i).getStudyname(), studyList.get(i).getCategory()));

		return ResponseEntity.status(200).body(StudyInfoListRes.of(studyInfoList));
	};
	
	
	@GetMapping("/list/{userno}")
	@ApiOperation(value = "참여 중인 스터디 목록", notes = "회원 번호에 따른 참여 스터디 목록을 반환한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<StudyListRes> getStudyList(@PathVariable("userno") @ApiParam(value = "유저 pk", required = true) int userno){
		List<Study> studyList = studyService.getStudyList(userno);
		return ResponseEntity.status(200).body(StudyListRes.of(studyList));
	};
	
	@GetMapping("/list/simple-detail/{studyno}")
	@ApiOperation(value = "스터디 간략 정보", notes = "스터디번호에 따른 간략 정보를 반환해 준다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<StudyRes> getStudySimpleInfo(@PathVariable("studyno") @ApiParam(value = "study pk", required = true) int studyno){
		Study study = studyService.getStudyInfo(studyno);
		return ResponseEntity.status(200).body(StudyRes.of(study));
	};
	
	
	
	@PostMapping("/apply")
	@ApiOperation(value = "스터디 가입 신청", notes = "스터디에 가입 신청을 한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> apply(
			@RequestBody @ApiParam(value="스터디 가입 신청 유저와 스터디 pk", required = true) StudyApplyPostReq applyInfo){
		if(studyService.applyStudy(applyInfo))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "성공"));
		else
			return ResponseEntity.status(200).body(BaseResponseBody.of(401, "실패"));
	}
	
	@PutMapping("/accept")
	@ApiOperation(value = "스터디 가입 수락/거절", notes = "스터디에 가입을 수락 혹은 거절한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> accept(
			@RequestBody @ApiParam(value="스터디 pk, 유저 pk와 가입 수락 여부", required = true) StudyAcceptPutReq acceptInfo){
		if(studyService.acceptStudy(acceptInfo))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "성공"));
		else
			return ResponseEntity.status(200).body(BaseResponseBody.of(401, "실패"));
	}
	
	@GetMapping("/all-user/{studyno}")
	@ApiOperation(value = "스터디 지원자 정보", notes = "스터디에 지원한 유저와 참가중인 유저를 반환해준다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<UserListRes> getUserList(@PathVariable("studyno") @ApiParam(value = "study pk", required = true) int studyno){
		List<User> userList = studyService.getUserList(studyno);
		return ResponseEntity.status(200).body(UserListRes.of(userList));
	};
	
	@GetMapping("/name_check/{studyname}")
	@ApiOperation(value = "스터디 이름 중복체크", notes = "사용하려는 스터디 이름이 존재하는지 여부를 응답한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용중인 이름"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BaseResponseBody> studynameCheck (
			@PathVariable("studyname") @ApiParam(value = "확인할 스터디 이름", required = true) String studyname) throws NoSuchElementException{
		try {
			Study study = studyService.getStudyByStudyname(studyname);
		} catch (Exception e) {
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));	
		}
		return ResponseEntity.status(404).body(BaseResponseBody.of(404, "이미 존재하는 스터디입니다."));
	}	
}