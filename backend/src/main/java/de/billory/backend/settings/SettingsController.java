package de.billory.backend.settings;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse getSettings() {
        return settingsService.getSettings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettingsResponse createSettings(@Valid @RequestBody CreateSettingsRequest request) {
        return settingsService.createSettings(request);
    }

    @PutMapping
    public SettingsResponse updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        return settingsService.updateSettings(request);
    }
}