package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.SemesterDTO;
import ch.notenverwaltung.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/semesters")
@RequiredArgsConstructor
@Tag(name = "Semesters", description = "Endpoints to manage and query semesters")
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "List all semesters (paged)",
            description = "Returns a paged list of semesters. Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of semesters returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterDTO.class)))
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<SemesterDTO>> getAll(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(semesterService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get semester by ID",
            description = "Fetch a single semester by UUID. Accessible to roles: ADMIN, USER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Semester found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Semester not found", content = @Content)
            }
    )
    public ResponseEntity<SemesterDTO> getById(@Parameter(description = "Semester UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(semesterService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a semester",
            description = "Creates a new semester. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Semester created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<SemesterDTO> create(@Valid @RequestBody SemesterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(semesterService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a semester",
            description = "Updates an existing semester by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Semester updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Semester not found", content = @Content)
            }
    )
    public ResponseEntity<SemesterDTO> update(@Parameter(description = "Semester UUID") @PathVariable UUID id, @Valid @RequestBody SemesterDTO dto) {
        return ResponseEntity.ok(semesterService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a semester",
            description = "Deletes a semester by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Semester deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Semester not found", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(@Parameter(description = "Semester UUID") @PathVariable UUID id) {
        semesterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
