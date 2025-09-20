package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.AdminCreateUserRequest;
import ch.notenverwaltung.model.dto.ChangePasswordRequest;
import ch.notenverwaltung.model.dto.RoleRequest;
import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "Admin-only endpoints for managing users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping(produces = "application/json")
    @Operation(
            summary = "List users (paged)",
            description = "Returns a paged list of registered users. Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of users returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
            }
    )
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping(path = "/active", produces = "application/json")
    @Operation(
            summary = "List active users (paged)",
            description = "Returns a paged list of users with active=true. Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of active users returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
            }
    )
    public ResponseEntity<Page<User>> getActiveUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getActiveUsers(pageable));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Create a new user",
            description = "Creates a user with the provided username and password. If no roles are provided, the default role ROLE_USER is assigned. Provided roles are normalized to start with ROLE_. Returns 409 if the username already exists.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<User> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        List<String> roles = request.getRoles() == null || request.getRoles().isEmpty()
                ? Collections.singletonList("ROLE_USER")
                : request.getRoles();
        if (userService.existsByUsername(request.getUsername())) {
            throw new ch.notenverwaltung.exception.AlreadyExistsException("User with username '" + request.getUsername() + "' already exists");
        }

        User created = userService.createUserWithDetails(
                request.getUsername(),
                request.getPassword(),
                roles,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getDateOfBirth()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(path = "/{username}/password", consumes = "application/json")
    @Operation(
            summary = "Change a user's password",
            description = "Sets a new password for the specified user. The password is stored encoded. Returns 404 if the user does not exist.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password changed", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<Void> changePassword(@PathVariable("username") @NotBlank String username,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(username, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{username}/active")
    @Operation(
            summary = "Set user active flag",
            description = "Sets the active flag for the specified user to true or false via query parameter 'active'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    public ResponseEntity<User> setActive(@PathVariable("username") String username,
                                          @RequestParam("active") boolean active) {
        User updated = userService.setActive(username, active);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(path = "/{username}/roles", consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Grant a role to a user",
            description = "Grants the specified role to the user. Role values are normalized to the ROLE_ prefix. Granting an existing role is idempotent.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role granted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<User> grantRole(@PathVariable("username") String username,
                                          @Valid @RequestBody RoleRequest request) {
        User updated = userService.grantRole(username, request.getRole());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/{username}/roles/{role}")
    @Operation(
            summary = "Revoke a role from a user",
            description = "Revokes the specified role from the user. Role values are normalized to the ROLE_ prefix. Revoking a role the user doesn't have is idempotent.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role revoked",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    public ResponseEntity<User> revokeRole(@PathVariable("username") String username,
                                           @PathVariable("role") String role) {
        User updated = userService.revokeRole(username, role);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/{username}")
    @Operation(
            summary = "Delete a user",
            description = "Deletes the specified user by username. Returns 204 if deleted, 404 if not found.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteUser(@PathVariable("username") String username) {
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }
}
