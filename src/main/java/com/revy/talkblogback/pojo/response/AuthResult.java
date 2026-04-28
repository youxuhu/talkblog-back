package com.revy.talkblogback.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {

    private String tokenType;
    private String accessToken;
    private UserProfile user;
}
