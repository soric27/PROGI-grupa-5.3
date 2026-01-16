package com.autoservis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  @Autowired
  private JavaMailSender mailSender;

  public void sendEmailWithAttachment(String to, String subject, String text, File attachment) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);

    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(text, false);

    if (attachment != null && attachment.exists()) {
      FileSystemResource file = new FileSystemResource(attachment);
      helper.addAttachment(attachment.getName(), file);
    }
    logger.info("Sending email to {} with subject {}", to, subject);
    mailSender.send(message);
    logger.info("Email sent to {}", to);

  }

  public void sendSimple(String to, String subject, String text) throws MessagingException {
    sendEmailWithAttachment(to, subject, text, null);
  }
}