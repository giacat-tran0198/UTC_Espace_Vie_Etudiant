package com.discussion.forum;

import com.discussion.forum.domain.User;
import com.discussion.forum.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;
import java.util.stream.IntStream;

@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }


    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    @Bean
    @Profile("dev")
    CommandLineRunner run(UserService userService) {
        return (args) -> {
            IntStream.rangeClosed(1, 15)
                    .mapToObj(i -> {
                        User user = new User();
                        user.setUsername("user" + i);
                        user.setDisplayName("display" + i);
                        user.setPassword("P4ssword");
                        return user;
                    })
                    .forEach(userService::save);
        };
    }
}
