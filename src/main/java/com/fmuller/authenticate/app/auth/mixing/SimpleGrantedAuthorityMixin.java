package com.fmuller.authenticate.app.auth.mixing;

import com.fasterxml.jackson.annotation.*;

public abstract class SimpleGrantedAuthorityMixin {

    @JsonCreator
    public SimpleGrantedAuthorityMixin(@JsonProperty("authority") String role) {

    }
}
