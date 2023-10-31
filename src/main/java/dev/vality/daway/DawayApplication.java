package dev.vality.daway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"dev.vality.daway"})
public class DawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawayApplication.class, args);
    }

}
