package ch.notenverwaltung.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
@Tag(name = "Public", description = "Publicly accessible endpoints (no authentication required)")
public class PublicController {
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Simple public health endpoint indicating the service status. Accessible to everyone (no role required).",
            security = {},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is up",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
            }
    )
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");

        return ResponseEntity.ok(status);
    }
}
