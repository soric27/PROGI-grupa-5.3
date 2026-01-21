package com.autoservis.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  // JavaMailSender je opcionalan – ako ga nema, app se i dalje diže
  public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  private boolean isMailEnabled() {
    return mailSender != null;
  }

  public void sendEmailWithAttachment(String to, String subject, String text, File attachment) throws MessagingException {
    if (!isMailEnabled()) {
      logger.warn("Mail is disabled (JavaMailSender not configured). Skipping email to {}", to);
      return;
    }

    MimeMessage message = mailSender.createMimeMessage();

    try {
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

    } catch (MessagingException ex) {
      logger.error("Failed to send email to {}", to, ex);
      throw ex;
    } catch (MailException ex) {
      logger.error("Failed to send email to {}", to, ex);
      throw ex;
    }
  }

  public void sendSimple(String to, String subject, String text) throws MessagingException {
    sendEmailWithAttachment(to, subject, text, null);
  }
}
