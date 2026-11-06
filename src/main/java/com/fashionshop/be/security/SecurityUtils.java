package com.fashionshop.be.security;

import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.exception.BusinessException;
import com.fashionshop.be.exception.ErrorCode;
import com.fashionshop.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUserOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(auth.getName());
    }

    public User getCurrentUserRequired() {
        return getCurrentUserOptional()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
    }
}

