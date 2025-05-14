package ch.notenverwaltung.service;

import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username, String password, List<String> roles) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
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

    public User createAdminUser(String password) {
        return createUser("admin", password, Collections.singletonList("ROLE_ADMIN"));
    }
}