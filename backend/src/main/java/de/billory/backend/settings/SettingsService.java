package de.billory.backend.settings;

import de.billory.backend.common.NotFoundException;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@Service
public class SettingsService {

    private static final Integer SETTINGS_ID = 1;
    private final PasswordEncoder passwordEncoder;
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository, PasswordEncoder passwordEncoder) {
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SettingsResponse getSettings() {
        Settings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

        return toResponse(settings);
    }

    public SettingsResponse createSettings(CreateSettingsRequest request) {
        if (settingsRepository.existsById(SETTINGS_ID)) {
            throw new IllegalStateException("Settings already exist");
        }

        String now = LocalDateTime.now().toString();

        String basePath =
        System.getProperty("user.home")
                + "/Documents/Billory";

        createDefaultDirectories();

        Settings settings = new Settings();
        settings.setId(SETTINGS_ID);
        settings.setCompanyName(request.getCompanyName());
        settings.setOwnerName(request.getOwnerName());
        settings.setStreet(request.getStreet());
        settings.setZip(request.getZip());
        settings.setCity(request.getCity());
        settings.setPhone(request.getPhone());
        settings.setEmail(request.getEmail());
        settings.setTaxNumber(request.getTaxNumber());
        settings.setIban(request.getIban());
        settings.setBankName(request.getBankName());
        settings.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        settings.setLogoPath(request.getLogoPath());
        settings.setArchivePath(basePath);
        settings.setBackupPath(basePath + "/Backups");
        settings.setReceiptsPath(basePath + "/Belege");
        settings.setReminderTemplate(request.getReminderTemplate());
        settings.setInvoicePrivacyNotice(request.getInvoicePrivacyNotice());
        settings.setOfferWithdrawalNotice(request.getOfferWithdrawalNotice());
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);

        return toResponse(settingsRepository.save(settings));
    }

    public SettingsResponse updateSettings(UpdateSettingsRequest request) {
        Settings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

        String basePath =
        System.getProperty("user.home")
                + "/Documents/Billory";

        createDefaultDirectories();

        settings.setCompanyName(request.getCompanyName());
        settings.setOwnerName(request.getOwnerName());
        settings.setStreet(request.getStreet());
        settings.setZip(request.getZip());
        settings.setCity(request.getCity());
        settings.setPhone(request.getPhone());
        settings.setEmail(request.getEmail());
        settings.setTaxNumber(request.getTaxNumber());
        settings.setIban(request.getIban());
        settings.setBankName(request.getBankName());
        settings.setLogoPath(request.getLogoPath());
        settings.setArchivePath(basePath);
        settings.setBackupPath(basePath + "/Backups");
        settings.setReceiptsPath(basePath + "/Belege");
        settings.setReminderTemplate(request.getReminderTemplate());
        settings.setInvoicePrivacyNotice(request.getInvoicePrivacyNotice());
        settings.setOfferWithdrawalNotice(request.getOfferWithdrawalNotice());
        settings.setUpdatedAt(LocalDateTime.now().toString());

        return toResponse(settingsRepository.save(settings));
    }

    private void createDefaultDirectories() {
        try {
            String documentsPath =
                    System.getProperty("user.home")
                            + "/Documents/Billory";

            Files.createDirectories(Path.of(documentsPath, "Rechnungen"));
            Files.createDirectories(Path.of(documentsPath, "Angebote"));
            Files.createDirectories(Path.of(documentsPath, "Mahnungen"));
            Files.createDirectories(Path.of(documentsPath, "Belege"));
            Files.createDirectories(Path.of(documentsPath, "Backups"));

        } catch (IOException e) {
            throw new RuntimeException("Failed to create Billory directories", e);
        }
    }

    private SettingsResponse toResponse(Settings settings) {
        SettingsResponse response = new SettingsResponse();

        response.setId(settings.getId());
        response.setCompanyName(settings.getCompanyName());
        response.setOwnerName(settings.getOwnerName());
        response.setStreet(settings.getStreet());
        response.setZip(settings.getZip());
        response.setCity(settings.getCity());
        response.setPhone(settings.getPhone());
        response.setEmail(settings.getEmail());
        response.setTaxNumber(settings.getTaxNumber());
        response.setIban(settings.getIban());
        response.setBankName(settings.getBankName());
        response.setLogoPath(settings.getLogoPath());
        response.setArchivePath(settings.getArchivePath());
        response.setBackupPath(settings.getBackupPath());
        response.setReceiptsPath(settings.getReceiptsPath());
        response.setReminderTemplate(settings.getReminderTemplate());
        response.setInvoicePrivacyNotice(settings.getInvoicePrivacyNotice());
        response.setOfferWithdrawalNotice(settings.getOfferWithdrawalNotice());
        response.setCreatedAt(settings.getCreatedAt());
        response.setUpdatedAt(settings.getUpdatedAt());

        return response;
    }
}