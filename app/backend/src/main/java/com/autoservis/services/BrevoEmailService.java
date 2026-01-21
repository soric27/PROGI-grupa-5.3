package com.autoservis.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.from-email}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendSimple(String to, String subject, String text) {
        try {
            String jsonBody = """
            {
              "sender": { "email": "%s" },
              "to": [ { "email": "%s" } ],
              "subject": "%s",
              "textContent": "%s"
            }
            """.formatted(
                    fromEmail,
                    to,
                    escape(subject),
                    escape(text)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("Brevo email sent to {} status={}", to, response.statusCode());

        } catch (Exception ex) {
            logger.error("Brevo email failed to {}", to, ex);
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
