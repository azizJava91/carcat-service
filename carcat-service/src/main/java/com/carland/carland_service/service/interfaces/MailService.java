package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.FeedbackRequest;
import org.springframework.web.multipart.MultipartFile;

public interface MailService {
    void sendFeedbackMail(FeedbackRequest feedbackRequest, MultipartFile file, String string, String customerPhone);



}
