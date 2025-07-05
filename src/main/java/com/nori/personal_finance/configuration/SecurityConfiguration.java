// src/main/java/com/nori/personal_finance/configuration/SecurityConfiguration.java

package com.nori.personal_finance.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private static final String LOGIN_URL = "/login";

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain userSecurityFilterChain(
      final HttpSecurity http, final UserDetailsService userDetailsService) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
               .authorizeHttpRequests(
                   auth ->
                       auth.requestMatchers(
                               "/",
                               "/css/**",
                               "/js/**",
                               "/register/**",
                               LOGIN_URL, // Check this line
                               LOGIN_URL + "?error=true",
                               LOGIN_URL + "?logout=true",
                               "/registration-success")
                           .permitAll()
                           .anyRequest()
                           .authenticated())
               .userDetailsService(userDetailsService)
               .formLogin(
                   form ->
                       form.loginPage(LOGIN_URL) // Check this line
                           .loginProcessingUrl(LOGIN_URL) // Check this line
                           .successHandler(
                               (request, response, authentication) -> response.sendRedirect("/dashboard"))
                           .failureUrl(LOGIN_URL + "?error=true"))
               .logout(
                   logout ->
                       logout
                           .logoutUrl("/logout")
                           .logoutSuccessUrl(LOGIN_URL + "?logout=true")
                           .invalidateHttpSession(true)
                           .deleteCookies("JSESSIONID"))
               .build();
  }
}