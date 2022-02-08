package com.ssafy.api.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.api.request.FileSavePostReq;
import com.ssafy.api.request.HomeworkCreatePostReq;
import com.ssafy.api.request.HomeworkPutReq;
import com.ssafy.api.response.HomeworkListRes;
import com.ssafy.api.response.HomeworkNNickname;
import com.ssafy.api.service.HomeworkService;
import com.ssafy.api.service.UserHomeworkService;
import com.ssafy.api.service.UserService;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.common.util.MD5Generator;
import com.ssafy.db.entity.Homework;
import com.ssafy.db.entity.UserHomework;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Homework API", tags = {"Homework"})
@RestController
@RequestMapping("/api/v1/homework")
public class HomeworkController {
	@Autowired
	HomeworkService homeworkService;
	@Autowired
	UserService userService;
	@Autowired
	UserHomeworkService userHomeworkService;
	
	@PostMapping("/create")
	@ApiOperation(value = "과제 생성", notes = "과제 글을 생성한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> create(
			@RequestBody @ApiParam(value="생성할 과제 정보", required = true) HomeworkCreatePostReq HomeworkInfo){	
		if(homeworkService.createHomework(HomeworkInfo))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "성공"));
		else
			return ResponseEntity.status(401).body(BaseResponseBody.of(401, "실패"));	
	}
	
	@GetMapping("/homeworklist/{studyno}")
	@ApiOperation(value = "과제 글 리스트", notes = "스터디 내의 과제 글 리스트를 반환해준다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<HomeworkListRes> getHomeworkList(@PathVariable("studyno") @ApiParam(value = "study pk", required = true) int studyno){
		List<Homework> homeworkList = homeworkService.getHomeworkList(studyno);
		List<HomeworkNNickname> homeworkNNicknameList = new ArrayList<>();
		
		for(int i = 0; i < homeworkList.size(); i++) {
			homeworkNNicknameList.add(new HomeworkNNickname(homeworkList.get(i), userService.getUserNickname(homeworkList.get(i).getUserno())));
		}
		
		return ResponseEntity.status(200).body(HomeworkListRes.of(homeworkNNicknameList));
	};
	
	@PutMapping("/modify")
	@ApiOperation(value = "과제 글 수정", notes = "과제 글을 수정한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> accept(
			@RequestBody @ApiParam(value="과제 글 수정 정보", required = true) HomeworkPutReq homeworkPutInfo){
		if(homeworkService.modifyHomework(homeworkPutInfo))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "성공"));
		else
			return ResponseEntity.status(401).body(BaseResponseBody.of(401, "실패"));
	}
	
	@DeleteMapping("/delete/{homeworkno}")
	@ApiOperation(value = "과제 글 삭제", notes = "과제 글을 삭제한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "실패"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BaseResponseBody> deleteHomework(
			@PathVariable("homeworkno") @ApiParam(value = "삭제할 homework pk", required = true) int homeworkno){
		if(homeworkService.deletehomework(homeworkno))
			return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
		else
			return ResponseEntity.status(401).body(BaseResponseBody.of(401, "실패"));
	}
	
	@PostMapping("/upload")
	@ApiOperation(value = "파일 업로드", notes = "파일을 업로드한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> fileUpload(
			@RequestParam @ApiParam(value="과제파일 정보", required = true) MultipartFile files,
			@RequestParam @ApiParam(value="과제 no", required = true) int homeworkno,
			@RequestParam @ApiParam(value="유저 no", required = true) int userno){
		
		try {
            String origFilename = files.getOriginalFilename();
            String filename = new MD5Generator(origFilename).toString();
            /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
            String savePath = System.getProperty("user.dir") + "\\homeworkfiles";
            /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
            if (!new File(savePath).exists()) {
                try{
                    new File(savePath).mkdir();
                }
                catch(Exception e){
                    e.getStackTrace();
                }
            }
            String filePath = savePath + "\\" + filename;
            files.transferTo(new File(filePath));
            
            FileSavePostReq file = new FileSavePostReq();
            file.setFilepath(filePath);
			file.setOgfilename(origFilename);
			file.setFilename(filename);

            userHomeworkService.saveFile(file, userno, homeworkno);
        } catch(Exception e) {
            e.printStackTrace();
        }
		
		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
	}
	
	@GetMapping("/download/{fileno}")
	@ApiOperation(value = "파일 다운로드", notes = "파일을 다운로드한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공"),
		@ApiResponse(code = 401, message = "실패"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<Resource> fileDownload(
			@PathVariable("fileno") @ApiParam(value = "다운로드할 파일 no", required = true) int fileno) throws IOException {
		
		UserHomework userHomework = userHomeworkService.getFile(fileno);
		Path path = Paths.get(userHomework.getFilepath());
		Resource resource = new InputStreamResource(Files.newInputStream(path));
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userHomework.getOgfilename()+ "\"")
				.body(resource);
	}
}
