package com.cmsnesia.storage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GithubResponseDetail implements Serializable {

  private String name;
  private String path;
  private String sha;
  private String size;
  private String url;

  @JsonProperty("html_url")
  private String htmlUrl;

  @JsonProperty("git_url")
  private String gitUrl;

  @JsonProperty("download_url")
  private String downloadUrl;

  private String type;
}
