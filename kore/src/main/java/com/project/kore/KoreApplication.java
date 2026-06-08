package com.project.kore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point dell'applicazione Spring Boot. All'avvio forza IPv4 per evitare i
 * timeout SMTP che capitano su IPv6.
 */
@SpringBootApplication
@EnableScheduling
public class KoreApplication {

    public static void main(String[] args) {
        // IPv4 obbligatorio: su IPv6 le connessioni a smtp.gmail.com vanno in timeout.
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(KoreApplication.class, args);
    }
}
