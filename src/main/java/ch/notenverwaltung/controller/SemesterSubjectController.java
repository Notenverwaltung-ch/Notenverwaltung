package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.SemesterSubjectDTO;
import ch.notenverwaltung.service.SemesterSubjectService;
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
@RequestMapping("/semester-subjects")
@RequiredArgsConstructor
@Tag(name = "Semester-Subjects", description = "Endpoints to manage and query semester-subject assignments")
public class SemesterSubjectController {

    private final SemesterSubjectService semesterSubjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "List all semester-subject assignments",
            description = "Returns a list of all semester-subject combinations. Accessible to roles: ADMIN, USER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterSubjectDTO.class)))
            }
    )
    public ResponseEntity<List<SemesterSubjectDTO>> getAll() {
        return ResponseEntity.ok(semesterSubjectService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get semester-subject by ID",
            description = "Fetch a single semester-subject by UUID. Accessible to roles: ADMIN, USER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterSubjectDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    public ResponseEntity<SemesterSubjectDTO> getById(@Parameter(description = "SemesterSubject UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(semesterSubjectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a semester-subject assignment",
            description = "Creates a new semester-subject combination. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterSubjectDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Semester or Subject not found", content = @Content)
            }
    )
    public ResponseEntity<SemesterSubjectDTO> create(@Valid @RequestBody SemesterSubjectDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(semesterSubjectService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a semester-subject assignment",
            description = "Updates an existing semester-subject combination by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemesterSubjectDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    public ResponseEntity<SemesterSubjectDTO> update(@Parameter(description = "SemesterSubject UUID") @PathVariable UUID id,
                                                     @Valid @RequestBody SemesterSubjectDTO dto) {
        return ResponseEntity.ok(semesterSubjectService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a semester-subject assignment",
            description = "Deletes a semester-subject by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(@Parameter(description = "SemesterSubject UUID") @PathVariable UUID id) {
        semesterSubjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
