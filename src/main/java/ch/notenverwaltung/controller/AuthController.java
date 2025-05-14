package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.AuthRequestDTO;
import ch.notenverwaltung.model.dto.AuthResponseDTO;
import ch.notenverwaltung.model.dto.UserRegistrationDTO;
import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.service.JwtTokenProviderNew;
import ch.notenverwaltung.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/public/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProviderNew jwtTokenProvider;
    private final UserService userService;

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponseDTO(jwt));
        } catch (Exception e) {
            log.debug("Login failed for username: {}", loginRequest.getUsername());
            log.debug("Error message: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Username is already taken");
        }

        User user = userService.createUser(
                registrationDTO.getUsername(),
                registrationDTO.getPassword(),
                Collections.singletonList("ROLE_USER")
        );

        String jwt = jwtTokenProvider.generateToken(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(jwt));
    }
}
