package com.example.microservices.user.service;

import com.example.microservices.user.dto.UserRequest;
import com.example.microservices.user.dto.UserResponse;
import com.example.microservices.user.entity.UserAccount;
import com.example.microservices.user.exception.DuplicateResourceException;
import com.example.microservices.user.exception.ResourceNotFoundException;
import com.example.microservices.user.repository.UserAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository repository;

    public UserAccountService(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public UserResponse create(UserRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email " + request.email());
        }
        UserAccount user = new UserAccount();
        apply(request, user);
        return toResponse(repository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        UserAccount user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        repository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User already exists with email " + request.email());
                });
        apply(request, user);
        return toResponse(repository.save(user));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        repository.deleteById(id);
    }

    private void apply(UserRequest request, UserAccount user) {
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }
}
