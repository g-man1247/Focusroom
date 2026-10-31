package com.focusroom.entity.enums;

public enum SessionType {
    FOCUS("Focus Session", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15);

    private final String displayName;
    private final int defaultDurationMinutes;

    SessionType(String displayName, int defaultDurationMinutes) {
        this.displayName = displayName;
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }
}
