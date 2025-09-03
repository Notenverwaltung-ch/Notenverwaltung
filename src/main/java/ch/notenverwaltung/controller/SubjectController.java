package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.SubjectDTO;
import ch.notenverwaltung.service.SubjectService;
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
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Endpoints to manage and query subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "List all subjects (paged)",
            description = "Returns a paged list of subjects. Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of subjects returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubjectDTO.class)))
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<SubjectDTO>> getAll(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(subjectService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get subject by ID",
            description = "Fetch a single subject by UUID. Accessible to roles: ADMIN, USER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subject found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubjectDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Subject not found", content = @Content)
            }
    )
    public ResponseEntity<SubjectDTO> getById(@Parameter(description = "Subject UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(subjectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a subject",
            description = "Creates a new subject. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Subject created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubjectDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<SubjectDTO> create(@Valid @RequestBody SubjectDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a subject",
            description = "Updates an existing subject by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subject updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubjectDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Subject not found", content = @Content)
            }
    )
    public ResponseEntity<SubjectDTO> update(@Parameter(description = "Subject UUID") @PathVariable UUID id, @Valid @RequestBody SubjectDTO dto) {
        return ResponseEntity.ok(subjectService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a subject",
            description = "Deletes a subject by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Subject deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Subject not found", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(@Parameter(description = "Subject UUID") @PathVariable UUID id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
