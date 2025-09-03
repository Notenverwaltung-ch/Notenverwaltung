package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.dto.StudentDTO;
import ch.notenverwaltung.service.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Endpoints to manage and query students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "List all students (paged)",
            description = "Returns a paged list of students in the system. Requires authentication (USER or ADMIN). Use standard Spring Data pageable params: page, size, sort.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of students returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class)))
            }
    )
    public ResponseEntity<org.springframework.data.domain.Page<StudentDTO>> getAllStudents(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(studentService.getAllStudents(pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "List active students",
            description = "Returns only students that are currently active as defined by business rules in StudentService.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of active students returned",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class)))
            }
    )
    public ResponseEntity<List<StudentDTO>> getActiveStudents() {
        return ResponseEntity.ok(studentService.getActiveStudents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Get student by ID",
            description = "Fetch a single student by UUID. Returns 404 if not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
            }
    )
    public ResponseEntity<StudentDTO> getStudentById(@Parameter(description = "Student UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a student",
            description = "Creates a new student. Only users with ADMIN role can create.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Student created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
            }
    )
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a student",
            description = "Updates an existing student by UUID. Only ADMINs can update. Returns 404 if not found.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
            }
    )
    public ResponseEntity<StudentDTO> updateStudent(@Parameter(description = "Student UUID") @PathVariable UUID id, @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a student",
            description = "Deletes a student by UUID. Only ADMINs can delete. Operation is idempotent; deleting a non-existent student has no effect but typically results in 404 depending on service implementation.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Student deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteStudent(@Parameter(description = "Student UUID") @PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
