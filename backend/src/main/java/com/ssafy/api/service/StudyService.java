package com.ssafy.api.service;

import java.util.List;

import com.ssafy.api.request.StudyCreatePostReq;
import com.ssafy.db.entity.Study;

public interface StudyService {
	boolean createStudy(StudyCreatePostReq studyInfo);
	List<Study> getStudyList(int userno);
}
