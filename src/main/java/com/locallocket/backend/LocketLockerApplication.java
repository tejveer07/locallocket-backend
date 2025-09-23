package com.locallocket.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LocketLockerApplication {


	public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load(); // loads .env
        System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("SPRING_DATASOURCE_USERNAME", dotenv.get("SPRING_DATASOURCE_USERNAME"));
        System.setProperty("SPRING_DATASOURCE_PASSWORD", dotenv.get("SPRING_DATASOURCE_PASSWORD"));
        System.setProperty("SPRING_SECURITY_JWT_SECRET", dotenv.get("SPRING_SECURITY_JWT_SECRET"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("RAZORPAY_KEY_ID", dotenv.get("RAZORPAY_KEY_ID"));
        System.setProperty("RAZORPAY_KEY_SECRET", dotenv.get("RAZORPAY_KEY_SECRET"));
        System.setProperty("RAZORPAY_WEBHOOK_SECRET", dotenv.get("RAZORPAY_WEBHOOK_SECRET"));
        System.setProperty("RAZORPAY_CURRENCY", dotenv.get("RAZORPAY_CURRENCY"));


        SpringApplication.run(LocketLockerApplication.class, args);
	}

}
