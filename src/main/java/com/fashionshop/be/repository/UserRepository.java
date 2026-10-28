package com.fashionshop.be.repository;

import com.fashionshop.be.domain.entity.User;
import com.fashionshop.be.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
    long countByRole(Role role);
}
