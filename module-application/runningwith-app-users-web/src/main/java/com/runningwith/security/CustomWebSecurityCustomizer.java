package com.runningwith.security;

import com.runningwith.users.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.context.SecurityContextRepository;


@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class CustomWebSecurityCustomizer implements WebSecurityCustomizer {

    private final UsersService usersService;
    private final SecurityContextRepository securityContextRepository;
    private final PersistentTokenRepository persistentTokenRepository;

    @Override
    public void customize(WebSecurity web) {
        web.ignoring()
                .requestMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .requestMatchers("/", "/login", "/sign-up", "/check-email-token", "/check/email-login", "/email-login", "/login-by-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin(getFormLoginConfigurerCustomizer())
                .httpBasic(HttpBasicConfigurer::disable)
                .rememberMe(getRememberMeConfigurer())
                .logout(getLogoutConfigurer())
                .securityContext(getSecurityContextConfigurer());
        // TODO session id change
        return http.build();
    }

    private Customizer<SecurityContextConfigurer<HttpSecurity>> getSecurityContextConfigurer() {
        return (securityContext) -> securityContext
                .requireExplicitSave(true)
                .securityContextRepository(securityContextRepository);
    }


    private Customizer<RememberMeConfigurer<HttpSecurity>> getRememberMeConfigurer() {
        return (rememberMeConfigurer) -> rememberMeConfigurer
                .userDetailsService(usersService)
                .tokenRepository(persistentTokenRepository);
    }


    private Customizer<FormLoginConfigurer<HttpSecurity>> getFormLoginConfigurerCustomizer() {
        return (formLogin) -> formLogin
                .loginPage("/login")
                .permitAll();
    }

    private Customizer<LogoutConfigurer<HttpSecurity>> getLogoutConfigurer() {
        return (logout) -> logout
                .logoutSuccessUrl("/");
    }

}
