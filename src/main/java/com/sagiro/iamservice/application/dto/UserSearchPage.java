package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.model.User;
import org.springframework.data.domain.Page;

public record UserSearchPage(
    java.util.List<User> users,
    int totalElements,
    int totalPages,
    int page,
    int size
) {}
