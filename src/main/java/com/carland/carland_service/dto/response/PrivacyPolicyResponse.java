package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacyPolicyResponse {
    private String lastUpdated;
    private String version;
    private String language;
    private String company;
    private String companyAz;
    private String email;
    private String website;
    private String location;
    private String country;
    private String responseTime;

    private String headerTitle;
    private String headerSubtitle;

    private List<String> sections;

    private String highlightedBoxTitle;
    private String highlightedBoxContent;

    private String contactTitle;
    private String contactSubtitle;
    private String contactResponseNote;

    private String consentText;
}