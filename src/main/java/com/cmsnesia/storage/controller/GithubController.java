package com.cmsnesia.storage.controller;

import com.cmsnesia.model.AuthDto;
import com.cmsnesia.model.MenuDto;
import com.cmsnesia.model.api.Result;
import com.cmsnesia.model.api.StatusCode;
import com.cmsnesia.storage.constant.ConstantKeys;
import com.cmsnesia.storage.model.GithubRequest;
import com.cmsnesia.storage.model.GithubResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(value = "/storeage")
@Api(
    value = "Github Storeage API",
    tags = {"Github Storeage API"})
@Slf4j
public class GithubController {

  private final WebClient webClient;

  public GithubController(
      @Value("${github.owner}") String owner,
      @Value("${github.repo}") String repo,
      @Value("${github.accessToken}") String accessToken) {
    this.webClient =
        WebClient.builder()
            .defaultHeader(ConstantKeys.AUTHORIZATION, "token " + accessToken)
            .baseUrl("https://api.github.com/repos/" + owner + "/" + repo + "/contents/")
            .build();
  }

  @ApiOperation(value = "Request image", response = MenuDto.class, notes = "Result<MediaDto>")
  @ApiImplicitParams({
    @ApiImplicitParam(name = ConstantKeys.AUTHORIZATION, paramType = "header", dataType = "string")
  })
  @PostMapping(
      value = "/upload",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Result<GithubResponse>> upload(
      @RequestBody GithubRequest media, @RequestParam("fileType") String fileType) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> (AuthDto) authentication.getPrincipal())
        .flatMap(
            session -> {
              if (!StringUtils.hasText(media.getContent())) {
                return Mono.empty();
              } else {
                int beginIndex = media.getContent().indexOf(";base64,") + 8;
                if (beginIndex >= 8 && beginIndex < media.getContent().length()) {
                  String base64 = media.getContent().substring(beginIndex);
                  media.setContent(base64);
                }
              }

              String name = UUID.randomUUID().toString().concat(".").concat(fileType);

              WebClient.RequestHeadersSpec requestHeadersSpec =
                  webClient
                      .put()
                      .uri(session.getUsername() + "/" + name)
                      .accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(Mono.just(media), GithubRequest.class);
              WebClient.ResponseSpec responseSpec = requestHeadersSpec.retrieve();
              return responseSpec
                  .onRawStatus(
                      value -> {
                        return value != 201;
                      },
                      clientResponse -> Mono.empty())
                  .bodyToMono(GithubResponse.class)
                  .map(
                      response -> {
                        if (response == null || response.getContent() == null) {
                          return Result.build(response, StatusCode.SAVE_FAILED);
                        }
                        return Result.build(response, StatusCode.SAVE_SUCCESS);
                      });
            });
  }
}
