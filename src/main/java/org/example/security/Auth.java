package org.example.security;

import lombok.Getter;
import lombok.Setter;


public enum Auth {
    INSTANCE;

    @Getter
    @Setter
    private Principal principal;
}
