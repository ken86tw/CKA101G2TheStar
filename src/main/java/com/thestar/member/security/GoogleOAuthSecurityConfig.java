package com.thestar.member.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "app.google-login.enabled", havingValue = "true")
public class GoogleOAuthSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain memberGoogleOAuthFilterChain(
            HttpSecurity http,
            MemberSecurityContextSupport memberSecurityContextSupport,
            MemberOAuth2SuccessHandler successHandler) throws Exception {

        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement((SessionManagementConfigurer<HttpSecurity> sm) ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(context -> context
                        .securityContextRepository(memberSecurityContextSupport.repository()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect("/login.html?googleError=1"))
                )
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
