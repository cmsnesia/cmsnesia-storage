package com.cmsnesia.storage.config.security;

import com.cmsnesia.model.AuthDto;
import com.cmsnesia.sdk.client.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    String userDataJson = request.getHeaders().getFirst("X-User-Data");
    if (StringUtils.hasText(userDataJson)) {
      try {
        AuthDto authDto = objectMapper.readValue(userDataJson, AuthDto.class);
        SecurityContext securityContext =
            new SecurityContextImpl(
                new Authentication() {
                  @Override
                  public Collection<? extends GrantedAuthority> getAuthorities() {
                    return authDto.getRoles() == null
                        ? new HashSet<>()
                        : authDto.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role))
                            .collect(Collectors.toSet());
                  }

                  @Override
                  public Object getCredentials() {
                    return authDto.getPassword();
                  }

                  @Override
                  public Object getDetails() {
                    return authDto;
                  }

                  @Override
                  public Object getPrincipal() {
                    return authDto;
                  }

                  @Override
                  public boolean isAuthenticated() {
                    return true;
                  }

                  @Override
                  public void setAuthenticated(boolean b) throws IllegalArgumentException {}

                  @Override
                  public String getName() {
                    return authDto.getFullName();
                  }
                });
        return Mono.just(securityContext);
      } catch (JsonProcessingException e) {
        return Mono.empty();
      }
    }
    return Mono.empty();
  }
}
