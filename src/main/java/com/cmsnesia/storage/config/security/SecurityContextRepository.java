package com.cmsnesia.storage.config.security;

import com.cmsnesia.model.AuthDto;
import com.cmsnesia.model.response.TokenResponse;
import com.cmsnesia.sdk.client.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

  private final TokenService tokenService;

  @Override
  public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String authToken = authHeader.substring(7);
      Mono<AuthDto> authDtoMono = tokenService.validate(new TokenResponse(authToken, "", "Bearer"));
      return authDtoMono.map(
          authDto -> {
            Set<GrantedAuthority> authorities =
                authDto.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role))
                    .collect(Collectors.toSet());
            Authentication authentication =
                new UsernamePasswordAuthenticationToken(authDto, null, authorities);
            return new SecurityContextImpl(authentication);
          });
    } else {
      return Mono.empty();
    }
  }
}
