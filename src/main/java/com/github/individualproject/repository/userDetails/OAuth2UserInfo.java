package com.github.individualproject.repository.userDetails;

import lombok.AllArgsConstructor;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();

}
