package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.ClassDTO;
import ch.notenverwaltung.service.SchoolClassService;
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
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Endpoints to manage and query classes")
public class ClassController {

    private final SchoolClassService schoolClassService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "List all classes (paged)",
            description = "Returns a paged list of classes. Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of classes returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassDTO.class)))
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<ClassDTO>> getAll(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(schoolClassService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get class by ID",
            description = "Fetch a single class by UUID. Accessible to roles: ADMIN, USER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Class found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Class not found", content = @Content)
            }
    )
    public ResponseEntity<ClassDTO> getById(@Parameter(description = "Class UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(schoolClassService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a class",
            description = "Creates a new class. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Class created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<ClassDTO> create(@Valid @RequestBody ClassDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolClassService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a class",
            description = "Updates an existing class by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Class updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Class not found", content = @Content)
            }
    )
    public ResponseEntity<ClassDTO> update(@Parameter(description = "Class UUID") @PathVariable UUID id, @Valid @RequestBody ClassDTO dto) {
        return ResponseEntity.ok(schoolClassService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a class",
            description = "Deletes a class by UUID. Accessible to role: ADMIN only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Class deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Class not found", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(@Parameter(description = "Class UUID") @PathVariable UUID id) {
        schoolClassService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
