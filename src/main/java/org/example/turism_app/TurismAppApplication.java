package org.example.turism_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TurismAppApplication {

    public static void main(String[] args) {
        fixLegacyTimeZone();
        SpringApplication.run(TurismAppApplication.class, args);
    }

    private static void fixLegacyTimeZone() {
        if ("Europe/Kiev".equals(TimeZone.getDefault().getID())) {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        }
    }
}
