package com.autoservis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoservisApplication {
  public static void main(String[] args) { 
     SpringApplication.run(AutoservisApplication.class, args); }
}