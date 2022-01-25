package com.ssafy.api.service;

import com.ssafy.api.request.UserRegisterPostReq;
import com.ssafy.db.entity.User;

public interface UserService {
	User signUp(UserRegisterPostReq userRegisterInfo);
	User getUserByEmail(String email);
}