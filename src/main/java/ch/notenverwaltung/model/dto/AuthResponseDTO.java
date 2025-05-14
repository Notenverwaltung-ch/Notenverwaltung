package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    
    private String token;
    private String tokenType = "Bearer";
    
    public AuthResponseDTO(String token) {
        this.token = token;
    }
}