package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.FeedbackRequest;
import com.carland.carland_service.dto.response.FeedBackResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FeedbackService {

    FeedBackResponse pushFeedback(MultipartFile file, FeedbackRequest feedbackRequest, String phoneNumber, String userIdHeader, String acceptLanguage);







}
