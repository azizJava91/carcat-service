package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.FeedbackRequest;
import com.carland.carland_service.dto.response.FeedBackResponse;
import com.carland.carland_service.enums.EnumFeedbackTypes;
import com.carland.carland_service.service.interfaces.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/push", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE} , produces = MediaType.APPLICATION_JSON_VALUE)
    public FeedBackResponse pushFeedback(@RequestPart(value = "file", required = false) MultipartFile file,
                                         @RequestPart("data") FeedbackRequest feedbackRequest,
                                         @RequestBody(required = false) byte[] rawBody,
                                         @RequestHeader("phoneNumber") String phoneNumber,
                                         @RequestHeader("X-User-Id") String userIdHeader,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return feedbackService.pushFeedback(file, feedbackRequest, phoneNumber, userIdHeader, acceptLanguage);
    }


    @GetMapping("/get/types")
    public List<String> getFeedbackTypeList() {
        return Arrays.stream(EnumFeedbackTypes.values())
                .map(Enum::name)
                .toList();
    }

}
