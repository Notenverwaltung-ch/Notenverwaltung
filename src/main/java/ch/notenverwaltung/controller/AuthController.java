package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.AuthRequestDTO;
import ch.notenverwaltung.model.dto.AuthResponseDTO;
import ch.notenverwaltung.model.dto.UserRegistrationDTO;
import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.service.JwtTokenProvider;
import ch.notenverwaltung.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Login and retrieve JWT",
            description = "Authenticates a user with username and password and returns a JWT access token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful authentication",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
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
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT access token for the created user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation failed or username taken")
            }
    )
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
