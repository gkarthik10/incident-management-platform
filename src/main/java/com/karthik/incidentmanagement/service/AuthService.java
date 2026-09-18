package com.karthik.incidentmanagement.service;

import com.karthik.incidentmanagement.dto.LoginRequestDto;
import com.karthik.incidentmanagement.dto.LoginResponseDto;
import com.karthik.incidentmanagement.dto.RegisterRequestDto;
import com.karthik.incidentmanagement.dto.UserSummaryDto;

public interface AuthService {

    String register(RegisterRequestDto dto);

    LoginResponseDto login(LoginRequestDto dto);

    UserSummaryDto getCurrentUser(String email);
}
