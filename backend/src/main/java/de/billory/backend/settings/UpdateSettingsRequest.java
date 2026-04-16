package de.billory.backend.settings;

import jakarta.validation.constraints.NotBlank;

public class UpdateSettingsRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String ownerName;

    @NotBlank
    private String street;

    @NotBlank
    private String zip;

    @NotBlank
    private String city;

    private String phone;

    private String email;

    @NotBlank
    private String taxNumber;

    private String iban;

    private String bankName;

    @NotBlank
    private String passwordHash;

    private String logoPath;

    @NotBlank
    private String archivePath;

    private String backupPath;

    private String receiptsPath;

    private String reminderTemplate;

    private String invoicePrivacyNotice;

    private String offerWithdrawalNotice;

    public UpdateSettingsRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getReceiptsPath() {
        return receiptsPath;
    }

    public void setReceiptsPath(String receiptsPath) {
        this.receiptsPath = receiptsPath;
    }

    public String getReminderTemplate() {
        return reminderTemplate;
    }

    public void setReminderTemplate(String reminderTemplate) {
        this.reminderTemplate = reminderTemplate;
    }

    public String getInvoicePrivacyNotice() {
        return invoicePrivacyNotice;
    }

    public void setInvoicePrivacyNotice(String invoicePrivacyNotice) {
        this.invoicePrivacyNotice = invoicePrivacyNotice;
    }

    public String getOfferWithdrawalNotice() {
        return offerWithdrawalNotice;
    }

    public void setOfferWithdrawalNotice(String offerWithdrawalNotice) {
        this.offerWithdrawalNotice = offerWithdrawalNotice;
    }
}