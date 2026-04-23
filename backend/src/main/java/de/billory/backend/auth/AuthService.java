package de.billory.backend.auth;

import de.billory.backend.common.NotFoundException;
import de.billory.backend.settings.Settings;
import de.billory.backend.settings.SettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Integer SETTINGS_ID = 1;

    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SettingsRepository settingsRepository, PasswordEncoder passwordEncoder) {
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Settings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

        boolean success = passwordEncoder.matches(request.getPassword(), settings.getPasswordHash());

        return new LoginResponse(success);
    }

    public void changePassword(ChangePasswordRequest request) {
        Settings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

        boolean oldPasswordMatches = passwordEncoder.matches(request.getOldPassword(), settings.getPasswordHash());

        if (!oldPasswordMatches) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        settings.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        settingsRepository.save(settings);
    }
}