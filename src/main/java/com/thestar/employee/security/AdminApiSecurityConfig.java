package com.thestar.employee.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 後台員工 API / 頁面專用的 SecurityFilterChain。
 * 只匹配 /admin/login、/logout、/me、/employee/**、/role/**、/permission/**，
 * 其餘既有路徑（訂單、住宿、會員、金流等）完全不受影響，交給 {@link DefaultSecurityConfig} 放行。
 *
 * 登入採真正的表單登入（比照 security.md guideline 的 SecurityFilterChain 表單登入設計），
 * 而非 AJAX/JSON 登入：GET /admin/login 顯示登入頁，
 * POST /admin/login 由 Spring Security 的登入 filter 直接處理帳密驗證。
 */
@Configuration
@EnableWebSecurity
public class AdminApiSecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager adminAuthenticationManager(UserDetailsService employeeUserDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(employeeUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminApiFilterChain(HttpSecurity http,
                                                     SecurityContextRepository securityContextRepository,
                                                     AuthenticationManager adminAuthenticationManager,
                                                     AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler,
                                                     RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                                     RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
        http
                .securityMatcher("/admin/login", "/admin/logout", "/admin/home", "/admin/me",
                        "/admin/content/**",
                        "/admin/employee/**", "/admin/role/**", "/admin/permission/**")
                .authenticationManager(adminAuthenticationManager)
                .csrf(csrf -> csrf.disable())
                .sessionManagement((SessionManagementConfigurer<HttpSecurity> sm) ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(ctx -> ctx.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/home").authenticated()
                        .requestMatchers("/admin/content/**").authenticated()
                        .requestMatchers("/admin/me").authenticated()
                        // 指派角色屬於權限提升操作，必須比一般員工管理更嚴格，只給 SUPER_ADMIN，
                        // 避免只有 EMPLOYEE_MANAGE 權限的人資主管把自己升級成 SUPER_ADMIN。
                        .requestMatchers("/admin/employee/*/roles").hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/role/**", "/admin/permission/**")
                            .hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/employee/**")
                            .hasAuthority(PermissionCodes.EMPLOYEE_MANAGE)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(adminAuthenticationSuccessHandler)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
