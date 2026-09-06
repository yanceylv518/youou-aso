package com.youou.aso.modules.support.domain;

import java.time.LocalDateTime;

public class CustomerServiceConfig {
    private Long id;
    private String serviceName;
    private String qrCodeUrl;
    private String contactHint;
    private String email;
    private boolean emailVisible;
    private String phone;
    private boolean phoneVisible;
    private String teamsUrl;
    private String telegramUrl;
    private String telegramQrUrl;
    private boolean telegramQrVisible;
    private String wechatQrUrl;
    private boolean wechatQrVisible;
    private String whatsappUrl;
    private boolean enabled;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getContactHint() {
        return contactHint;
    }

    public void setContactHint(String contactHint) {
        this.contactHint = contactHint;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVisible() {
        return emailVisible;
    }

    public void setEmailVisible(boolean emailVisible) {
        this.emailVisible = emailVisible;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPhoneVisible() {
        return phoneVisible;
    }

    public void setPhoneVisible(boolean phoneVisible) {
        this.phoneVisible = phoneVisible;
    }

    public String getTeamsUrl() {
        return teamsUrl;
    }

    public void setTeamsUrl(String teamsUrl) {
        this.teamsUrl = teamsUrl;
    }

    public String getTelegramUrl() {
        return telegramUrl;
    }

    public void setTelegramUrl(String telegramUrl) {
        this.telegramUrl = telegramUrl;
    }

    public String getTelegramQrUrl() {
        return telegramQrUrl;
    }

    public void setTelegramQrUrl(String telegramQrUrl) {
        this.telegramQrUrl = telegramQrUrl;
    }

    public boolean isTelegramQrVisible() {
        return telegramQrVisible;
    }

    public void setTelegramQrVisible(boolean telegramQrVisible) {
        this.telegramQrVisible = telegramQrVisible;
    }

    public String getWechatQrUrl() {
        return wechatQrUrl;
    }

    public void setWechatQrUrl(String wechatQrUrl) {
        this.wechatQrUrl = wechatQrUrl;
    }

    public boolean isWechatQrVisible() {
        return wechatQrVisible;
    }

    public void setWechatQrVisible(boolean wechatQrVisible) {
        this.wechatQrVisible = wechatQrVisible;
    }

    public String getWhatsappUrl() {
        return whatsappUrl;
    }

    public void setWhatsappUrl(String whatsappUrl) {
        this.whatsappUrl = whatsappUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
