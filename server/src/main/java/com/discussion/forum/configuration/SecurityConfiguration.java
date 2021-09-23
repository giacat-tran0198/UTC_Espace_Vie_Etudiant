package com.discussion.forum.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.discussion.forum.utils.Constant.*;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    AuthUserService authUserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().disable();
        http.headers().frameOptions().sameOrigin();

        http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, API_1_0_LOGIN).authenticated()
                .antMatchers(HttpMethod.PUT, API_1_0_USERS + "/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.POST, API_1_0_DISCUSSION + "/**").authenticated()
                .antMatchers(HttpMethod.DELETE, API_1_0_DISCUSSION + "/{id:[0-9]+}").authenticated()
                .and()
                .authorizeRequests().anyRequest().permitAll();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(authUserService).passwordEncoder(passwordEncoder());
    }

    @Bean()
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
