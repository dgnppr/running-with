package com.runningwith.config;

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
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.springframework.security.config.Customizer.withDefaults;


@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig implements WebSecurityCustomizer {

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
                .requestMatchers("/", "/login", "/sign-up", "/check-email-token", "/email-login", "/check/email-login", "/login-link", "/login-by-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin(getFormLoginConfigurerCustomizer())
                .logout(withDefaults())
                .httpBasic(HttpBasicConfigurer::disable)
                .securityContext(getSecurityContextConfigurer());

        return http.build();
    }

    private Customizer<SecurityContextConfigurer<HttpSecurity>> getSecurityContextConfigurer() {
        return (securityContext) -> securityContext
                .requireExplicitSave(true)
                .securityContextRepository(securityContextRepository());
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }


    private Customizer<FormLoginConfigurer<HttpSecurity>> getFormLoginConfigurerCustomizer() {
        return (formLogin) -> formLogin
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
