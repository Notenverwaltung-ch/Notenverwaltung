package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.GradeDTO;
import ch.notenverwaltung.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Endpoints to manage grades")
public class GradeController {

    private final GradeService gradeService;
    private final ch.notenverwaltung.repository.UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "List grades",
            description = "Admins get all grades (paged). Users get only their own grades.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of grades returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class)))
            }
    )
    public ResponseEntity<Page<GradeDTO>> getAll(
            Pageable pageable,
            @Parameter(description = "Filter by studentId (ADMIN only, optional)") @RequestParam(value = "studentId", required = false) UUID studentId,
            @Parameter(description = "Filter by testId (optional)") @RequestParam(value = "testId", required = false) UUID testId,
            @Parameter(description = "Minimum grade value (optional)") @RequestParam(value = "valueMin", required = false) BigDecimal valueMin,
            @Parameter(description = "Maximum grade value (optional)") @RequestParam(value = "valueMax", required = false) BigDecimal valueMax,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID effectiveStudentId = isAdmin ? studentId : null;
        Page<GradeDTO> page = gradeService.find(pageable, effectiveStudentId, testId, valueMin, valueMax, isAdmin, auth.getName());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Get grade by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Grade found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Grade not found", content = @Content)
            })
    public ResponseEntity<GradeDTO> getById(@PathVariable UUID id, Authentication auth) {
        GradeDTO dto = gradeService.getById(id);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            UUID userId = getUserIdFromPrincipal(auth);
            if (!dto.getStudentId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create grade",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Grade created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            })
    public ResponseEntity<GradeDTO> create(@RequestBody GradeDTO dto) {
        GradeDTO created = gradeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update grade",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Grade updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Grade not found", content = @Content)
            })
    public ResponseEntity<GradeDTO> update(@PathVariable UUID id, @RequestBody GradeDTO dto) {
        return ResponseEntity.ok(gradeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete grade",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserIdFromPrincipal(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + username))
                .getId();
    }
}
