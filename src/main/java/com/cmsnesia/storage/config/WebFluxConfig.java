package com.cmsnesia.storage.config;

import com.cmsnesia.sdk.client.TokenService;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.reactive.ReactorFeign;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFlux
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

  private static final String ALLOWED_HEADERS = "*";
  private static final String ALLOWED_EXPOSED_HEADERS = "*";
  private static final String ALLOWED_REQUEST_HEADERS = "*";
  private static final String ALLOWED_METHODS = "*";
  private static final String ALLOWED_ORIGIN = "*";
  private static final String ALLOWED_CREDENTIALS = "true";
  private static final String MAX_AGE = "3600";

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/swagger-ui.html**")
        .addResourceLocations("classpath:/META-INF/resources/");

    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  public WebFilter corsFilter() {
    return (ServerWebExchange ctx, WebFilterChain chain) -> {
      ServerHttpRequest request = ctx.getRequest();
      if (CorsUtils.isCorsRequest(request)) {
        ServerHttpResponse response = ctx.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        headers.add("Access-Control-Allow-Credentials", ALLOWED_CREDENTIALS);
        headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
        headers.add("Access-Control-Max-Age", MAX_AGE);
        headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        headers.add("Access-Control-Expose-Headers", ALLOWED_EXPOSED_HEADERS);
        headers.add("Access-Control-Request-Headers", ALLOWED_REQUEST_HEADERS);

        headers.add("Content-Security-Policy", "upgrade-insecure-requests");
        headers.add("Strict-Transport-Security", "max-age=1000");
        headers.add("X-Xss-Protection", "1; mode=block");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");

        if (request.getMethod() == HttpMethod.OPTIONS) {
          response.setStatusCode(HttpStatus.OK);
          return Mono.empty();
        }
      }
      return chain.filter(ctx);
    };
  }

  @Bean
  public TokenService tokenService(@Value("$(cmsnesia.api)") String baseUrl) {
    TokenService tokenService =
        ReactorFeign.builder()
            .decoder(new JacksonDecoder())
            .encoder(new JacksonEncoder())
            .retryer(new Retryer.Default())
            .target(TokenService.class, baseUrl);
    return tokenService;
  }
}
