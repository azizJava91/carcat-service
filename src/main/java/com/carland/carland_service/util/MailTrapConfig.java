package com.carland.carland_service.util;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MailTrapConfig {

    @Value("${spring.mail.password}")
    private String apiToken;

    @Bean
    public MailtrapClient mailtrapClient() {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .token(apiToken)
                .build();
        System.err.println("mailtrap token ================= "+apiToken);
        return MailtrapClientFactory.createMailtrapClient(config);
    }


    @Bean
    public RestTemplate getRest(){
        return new RestTemplate();
    }
}