package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.FeedbackRequest;
import com.carland.carland_service.dto.response.FeedBackResponse;
import com.carland.carland_service.entity.Feedback;
import com.carland.carland_service.enums.EnumFeedbackSuccessResponse;
import com.carland.carland_service.enums.EnumFeedbackTypes;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.repository.FeedbackRepository;
import com.carland.carland_service.service.interfaces.MailService;
import com.carland.carland_service.service.interfaces.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MailService mailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedBackResponse pushFeedback(MultipartFile file, FeedbackRequest feedbackRequest, String phoneNumber,
            String userIdHeader, String acceptLanguage) {

        Feedback feedback = Feedback.builder()
                .type(feedbackRequest.getType())
                .subject(feedbackRequest.getSubject())
                .description(feedbackRequest.getDescription())
                .rating(feedbackRequest.getRating())
                .customerId(Long.valueOf(userIdHeader))
                .customerPhone(phoneNumber)
                .build();

        if (Stream.of(EnumFeedbackTypes.feedback, EnumFeedbackTypes.support, EnumFeedbackTypes.bug_report)
                .noneMatch(e -> e.name().equalsIgnoreCase(feedbackRequest.getType()))) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        feedback = feedbackRepository.save(feedback);

        mailService.sendFeedbackMail(feedbackRequest, file, feedback.getFeedbackId().toString(), phoneNumber);

        return FeedBackResponse.builder()
                .ticketId(feedback.getFeedbackId())
                .message(EnumFeedbackSuccessResponse.SUCCESS_MESSAGE.getMessageByLang(acceptLanguage))
                .estimatedResponseTime(EnumFeedbackSuccessResponse.ESTIMATED_TIME.getMessageByLang(acceptLanguage))
                .build();
    }
}
