package com.youou.aso.modules.appmanagement.domain;

public class MarketRegion {
    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private boolean enabled;
    private boolean supportsAppStore = true;
    private boolean supportsGooglePlay = true;
    private boolean supportsIpadStore = true;
    private int sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSupportsAppStore() {
        return supportsAppStore;
    }

    public void setSupportsAppStore(boolean supportsAppStore) {
        this.supportsAppStore = supportsAppStore;
    }

    public boolean isSupportsGooglePlay() {
        return supportsGooglePlay;
    }

    public void setSupportsGooglePlay(boolean supportsGooglePlay) {
        this.supportsGooglePlay = supportsGooglePlay;
    }

    public boolean isSupportsIpadStore() {
        return supportsIpadStore;
    }

    public void setSupportsIpadStore(boolean supportsIpadStore) {
        this.supportsIpadStore = supportsIpadStore;
    }

    public boolean supports(StoreType storeType) {
        return switch (storeType) {
            case APP_STORE -> supportsAppStore;
            case GOOGLE_PLAY -> supportsGooglePlay;
            case IPAD_STORE -> supportsIpadStore;
        };
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
