package ch.notenverwaltung.service;

import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username, String password, List<String> roles) {
        List<String> normalized = normalizeRoles(roles);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(normalized)
                .build();
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }

    public User createAdminUser(String password) {
        return createUser("admin", password, Collections.singletonList("ROLE_ADMIN"));
    }

    @Transactional
    public User changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User grantRole(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        String normalized = normalizeRole(role);
        if (!user.getRoles().contains(normalized)) {
            user.getRoles().add(normalized);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User revokeRole(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        String normalized = normalizeRole(role);
        user.getRoles().remove(normalized);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        userRepository.delete(user);
    }

    @Transactional
    public User setActive(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setActive(active);
        return userRepository.save(user);
    }

    private List<String> normalizeRoles(List<String> roles) {
        if (roles == null) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (String r : roles) {
            out.add(normalizeRole(r));
        }
        return out;
    }

    private String normalizeRole(String role) {
        if (role == null) return null;
        String r = role.trim();
        if (r.isEmpty()) return r;
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }
}