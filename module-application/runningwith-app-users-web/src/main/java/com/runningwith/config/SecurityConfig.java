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
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig implements WebSecurityCustomizer {

    @Override
    public void customize(WebSecurity web) {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> authorize
                        .requestMatchers("/","/login","/sign-up","/check-email-token","/email-login","/check/email-login","/login-link","/login-by-email").permitAll()
                        .requestMatchers(HttpMethod.GET,"/profile/*").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(getFormLoginConfigurerCustomizer())
                .logout((logout)->logout.logoutSuccessUrl("/")) // 로그아웃 시 쿠키랑 세션 정보 날려야됌
                .httpBasic(withDefaults());
        return http.build();
    }

    private Customizer<FormLoginConfigurer<HttpSecurity>> getFormLoginConfigurerCustomizer() {
        return (formLogin) -> formLogin
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .permitAll();
    }

}
