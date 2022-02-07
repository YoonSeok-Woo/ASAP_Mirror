package com.ssafy.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.api.request.UserRegisterPostReq;
import com.ssafy.api.response.BoardMember;
import com.ssafy.db.entity.Board;
import com.ssafy.db.entity.Homework;
import com.ssafy.db.entity.StudyMember;
import com.ssafy.db.entity.User;
import com.ssafy.db.repository.BoardRepository;
import com.ssafy.db.repository.HomeworkRepository;
import com.ssafy.db.repository.ScheduleRepository;
import com.ssafy.db.repository.StudyBoardRepository;
import com.ssafy.db.repository.StudyMemberRepository;
import com.ssafy.db.repository.UserHomeworkRepository;
import com.ssafy.db.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	UserRepository userRepository;
	@Autowired
	HomeworkRepository homeworkRepository;
	@Autowired
	StudyMemberRepository studyMemberRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	BoardService boardService;
	@Autowired
	UserHomeworkRepository userHomeworkRepository;
	@Autowired
	BoardRepository boardRepository;
	@Autowired
	ScheduleRepository scheduleRepository;
	@Autowired
	StudyBoardRepository studyBoardRepository;
	
	@Override
	public User signUp(UserRegisterPostReq registerInfo) {
		User user = new User();
		user.setEmail(registerInfo.getEmail());
		user.setPassword(passwordEncoder.encode(registerInfo.getPassword()));
		user.setNickname(registerInfo.getNickname());
		user.setMainCategory(registerInfo.getMainCategory());
		StringBuilder sb = new StringBuilder();
		for (String str : registerInfo.getInterests()) {
			sb.append("#"+str);
		}
		user.setInterests(sb.toString());
		return userRepository.save(user);
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepository.findByEmail(email).get();
		return user;
	}

	@Override
	public User getUserByUserno(int userno) {
		User user = userRepository.findById(userno).get();
		return user;
	}

	@Override
	public User modifyUser(int userno, UserRegisterPostReq userModifyInfo) {
		User user = getUserByUserno(userno);
		if(!userModifyInfo.getPassword().equals("")) 
			user.setPassword(passwordEncoder.encode(userModifyInfo.getPassword()));
		if(!userModifyInfo.getNickname().equals("")) 
			user.setNickname(userModifyInfo.getNickname());
		if(!userModifyInfo.getMainCategory().equals("")) 
			user.setMainCategory(userModifyInfo.getMainCategory());
		if(!userModifyInfo.getInterests().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String str : userModifyInfo.getInterests()) {
				sb.append("#"+str);
			}
			user.setInterests(sb.toString());
		}
		if(userModifyInfo.getImage() != null) 
			user.setImage(userModifyInfo.getImage());
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public boolean deleteUser(int userno) {
		User user = userRepository.findById(userno).get();
		user.setDelFlag(1);
		userRepository.save(user);
		//board테이블 삭제
		List<Integer> boardlist = boardService.getBoardnoByUserno(userno);
		for (Integer boardno : boardlist) {
			boardService.deleteBoard(boardno);
		}
		//study 위임 -> 아무도 없으면 삭제
		studyMemberRepository.deleteByUserno(userno);
		//push테이블 삭제
		return true;
	}
	
	//추가
	@Override
	public List<BoardMember> getBoardMember(int studyno) {
		List<BoardMember> list = userRepository.findBoardMemberByStudyno(studyno).get();
		return list;
	}

	@Override
	public String getNicknameByUserno(int userno) {
		String nickname = userRepository.findNicknameByUserno(userno);
		return nickname;
	}

	@Override
	public List<User> getUserList() {
		List<User> userlist = userRepository.findAll();
		return userlist;
	}
	
	// flag 0 = 진행중 과제 / 1 = 완료 과제 / 2 = 전체 과제 
	// homework service 만들까
	@Override
	public List<Homework> getHomeworkListbyUserno(int userno, int flag) {
		List<Homework> homeworkList = homeworkRepository.findHomeworkByuserno(userno, flag).get();
		return homeworkList;
	}

	@Override
	public boolean kickUser(int userno, int studyno) {
		StudyMember studyMember = studyMemberRepository.findByUsernoNStudyNo(userno, studyno);
		
		if(studyMember.getPosition() == 0) {
			StudyMember studyMemberMandate = studyMemberRepository.findByStudynoMandate(studyno);
			if(studyMemberMandate == null) {
				//System.out.println("스터디 폭파 시키자");
				//스터디 신청 인원 studymember에서 삭제
				List<StudyMember> studyMemberList = studyMemberRepository.findByStudyno(studyno);
				for(int i = 0; i < studyMemberList.size(); i++)
					studyMemberRepository.kickStudyMember(studyMemberList.get(i).getUserno(), studyMemberList.get(i).getStudyno());
				
				//board 삭제
				boardRepository.deleteByStudyno(studyno);
				
				//나중에 해줘야하나...? conference 삭제
				
				//schedule 삭제
				scheduleRepository.deleteByStudyno(studyno);
				
				//user_homework 삭제
				List<Homework> homeworkList = homeworkRepository.findByStudyno(studyno).get();
				for(int i = 0; i < homeworkList.size(); i++)
					userHomeworkRepository.deleteByHomeworkno(homeworkList.get(i).getHomeworkno());
				
				//homework 삭제
				homeworkRepository.deleteByStudyno(studyno);
				
				//file 삭제
				
				//study_board 삭제
				studyBoardRepository.deleteByStudyno(studyno);
			}
			else {
				//System.out.println("스터디장 위임 시키자");
				studyMemberRepository.studyLeaderMandate(studyMemberMandate.getUserno(), studyno);
			}
		}
		
		//스터디 homework 삭제
		List<Homework> homeworkList = homeworkRepository.findByStudyno(studyno).get();
		for(int i = 0; i < homeworkList.size(); i++) {
			userHomeworkRepository.deleteByIdNUserno(homeworkList.get(i).getHomeworkno(), userno);
		}
		
		studyMemberRepository.kickStudyMember(userno, studyno);
		return true;
	}

	@Override
	public String getUserNickname(Integer userno) {
		return userRepository.findById(userno).get().getNickname();
	}

}
