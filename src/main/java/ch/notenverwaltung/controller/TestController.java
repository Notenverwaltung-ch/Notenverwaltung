package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.TestDTO;
import ch.notenverwaltung.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
@Tag(name = "Tests", description = "Endpoints to manage tests")
public class TestController {

    private final TestService testService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "List all tests (paginated)",
            description = "Returns a paginated list of tests. Accepts standard Spring Data pagination params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of tests returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestDTO.class)))
            }
    )
    public ResponseEntity<Page<TestDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(testService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Get test by ID",
            description = "Fetch a single test by UUID. Returns 404 if not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
            }
    )
    public ResponseEntity<TestDTO> getById(@Parameter(description = "Test UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(testService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a test",
            description = "Creates a new test. Only users with ADMIN role can create.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Test created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<TestDTO> create(@Valid @RequestBody TestDTO dto) {
        TestDTO created = testService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a test",
            description = "Updates an existing test by UUID. Only ADMINs can update. Returns 404 if not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
            }
    )
    public ResponseEntity<TestDTO> update(@Parameter(description = "Test UUID") @PathVariable UUID id,
                                          @Valid @RequestBody TestDTO dto) {
        return ResponseEntity.ok(testService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a test",
            description = "Deletes a test by UUID. Only ADMINs can delete.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Test deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(@Parameter(description = "Test UUID") @PathVariable UUID id) {
        testService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
