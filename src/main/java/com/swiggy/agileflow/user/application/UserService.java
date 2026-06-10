package com.swiggy.agileflow.user.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.user.api.CreateUserRequest;
import com.swiggy.agileflow.user.api.UserResponse;
import com.swiggy.agileflow.user.domain.User;
import com.swiggy.agileflow.user.infrastructure.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new BusinessRuleException("Username already taken: " + req.username());
        }
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new BusinessRuleException("Email already registered: " + req.email());
        }
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setDisplayName(req.displayName());
        user.setPasswordHash(req.password() != null ? "{noop}" + req.password() : null);
        user.setActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(userRepository.findById(id)
            .orElseThrow(() -> NotFoundException.of("User", id)));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
