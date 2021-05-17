package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.model.NotificationEmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Contains the logic necessary to be able
 * to send the password reset emails to user
 */
@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailContentBuilder mailContentBuilder;

    /**
     * Sends the reset password email
     * @param resetPasswordEmail NotificationEmail object containing the information necessary to send the mail
     */
    public void sendMail(NotificationEmail resetPasswordEmail){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper= new
                    MimeMessageHelper(mimeMessage,
                                      MimeMessageHelper.MULTIPART_MODE_MIXED,
                                      StandardCharsets.UTF_8.name());
            messageHelper.setFrom("goodcookieapp@gmail.com");
            messageHelper.setTo(resetPasswordEmail.getRecipient());
            messageHelper.setSubject(resetPasswordEmail.getSubject());
            messageHelper.setText(
                    mailContentBuilder
                            .build(resetPasswordEmail.getUsername(),
                                    resetPasswordEmail.getEndpoint(),
                                    resetPasswordEmail.getToken()),
                    true);
        };
        try{
            mailSender.send(messagePreparator);
            log.info("Reset password email sent.");
        }catch (MailException e){
            log.error("Error occurred while sending mail", e);
            throw new GoodCookieBackendException("Error occurred while sending mail to " + resetPasswordEmail.getRecipient(), e);
        }
    }
}
