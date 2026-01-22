package com.autoservis.services;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrevoEmailService {

    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);

    private final String apiKey;
    private final String fromEmail;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public BrevoEmailService(
            @Value("${brevo.api-key:}") String apiKey,
            @Value("${brevo.from-email:}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendEmailWithAttachment(String to, String subject, String text, File attachment) throws Exception {
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            logger.warn("Brevo is not configured. Skipping email to {}", to);
            return;
        }

        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");
        jsonBody.append("\"sender\":{\"email\":\"").append(escape(fromEmail)).append("\"},");
        jsonBody.append("\"to\":[{\"email\":\"").append(escape(to)).append("\"}],");
        jsonBody.append("\"subject\":\"").append(escape(subject)).append("\",");
        jsonBody.append("\"textContent\":\"").append(escape(text)).append("\"");

        if (attachment != null && attachment.exists()) {
            byte[] bytes = Files.readAllBytes(attachment.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            jsonBody.append(",\"attachment\":[{\"content\":\"")
                .append(base64)
                .append("\",\"name\":\"")
                .append(escape(attachment.getName()))
                .append("\"}]");
        }

        jsonBody.append("}");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .header("api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
            .build();

        HttpResponse<String> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            logger.info("Brevo email sent to {} status={}", to, status);
        } else {
            throw new IllegalStateException("Brevo email failed status=" + status + " body=" + response.body());
        }
    }

    public void sendSimple(String to, String subject, String text) throws Exception {
        sendEmailWithAttachment(to, subject, text, null);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }
}
