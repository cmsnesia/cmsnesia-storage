package com.cmsnesia.storage.config;

import com.cmsnesia.storage.config.security.SecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

  private static final String[] AUTH_WHITELIST = {
    "/v2/api-docs",
    "/resources/**",
    "/configuration/**",
    "/swagger*/**",
    "/webjars/**",
    "/token/**",
    "/favicon.ico",
    "/public/**"
    //    "/*/**"
  };

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityWebFilterChain securitygWebFilterChain(
      ServerHttpSecurity http, SecurityContextRepository contextRepository) {
    return http.exceptionHandling()
        .authenticationEntryPoint(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    }))
        .accessDeniedHandler(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    }))
        .and()
        .csrf()
        .disable()
        .formLogin()
        .disable()
        .securityContextRepository(contextRepository)
        .authorizeExchange()
        .pathMatchers(AUTH_WHITELIST)
        .permitAll()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .build();
  }
}
