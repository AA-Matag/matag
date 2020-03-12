package com.matag.admin.auth.login;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class LoginResponse {
  private final String token;

  @JsonCreator
  public LoginResponse(@JsonProperty("token") String token) {
    this.token = token;
  }
}
