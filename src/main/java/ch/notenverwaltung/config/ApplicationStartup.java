package ch.notenverwaltung.config;

import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartup implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        if (userService.countUsers() == 0) {
            String password = generateRandomPassword();

            User adminUser = userService.createAdminUser(password);

            log.info("\n\n=================================================");
            log.info("    DEFAULT ADMIN ACCOUNT CREATED");
            log.info("=================================================");
            log.info("    Username: {}", adminUser.getUsername());
            log.info("    Password: {}", password);
            log.info("=================================================");
            log.info("    PLEASE CHANGE THIS PASSWORD IMMEDIATELY!");
            log.info("=================================================\n\n");
        }
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12]; // 12 bytes = 16 base64 characters
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
