package com.autoservis.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final BrevoEmailService brevoEmailService;

  public EmailService(BrevoEmailService brevoEmailService) {
    this.brevoEmailService = brevoEmailService;
  }

  public void sendEmailWithAttachment(String to, String subject, String text, File attachment) throws MessagingException {
    try {
      brevoEmailService.sendEmailWithAttachment(to, subject, text, attachment);
      logger.info("Email sent to {}", to);
    } catch (Exception ex) {
      logger.error("Failed to send email to {}", to, ex);
      throw new MessagingException("Brevo email failed", ex);
    }
  }

  public void sendSimple(String to, String subject, String text) throws MessagingException {
    sendEmailWithAttachment(to, subject, text, null);
  }
}
