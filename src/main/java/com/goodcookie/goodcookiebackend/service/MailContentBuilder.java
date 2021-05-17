package com.goodcookie.goodcookiebackend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 * Helps build the reset password email to be sent to the user.
 */
@Service
@AllArgsConstructor
public class MailContentBuilder {

//    private final SpringTemplateEngine templateEngine;
    private final TemplateEngine templateEngine;
    /**
     * Builds the reset password email to be sent to the user
     * @param username The username that requested password request
     * @param url The url endpoint for resetting password
     * @param token The password request token
     */
    String build(String username, String url, String token){
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("url", url);
        context.setVariable("token", token);
        return templateEngine.process("resetPasswordMailTemplate", context);
    }
}
