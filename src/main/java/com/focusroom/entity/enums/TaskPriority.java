package com.focusroom.entity.enums;

public enum TaskPriority {
    LOW("Low", "bg-emerald-500/20 text-emerald-400 border-emerald-500/30"),
    MEDIUM("Medium", "bg-amber-500/20 text-amber-400 border-amber-500/30"),
    HIGH("High", "bg-orange-500/20 text-orange-400 border-orange-500/30"),
    URGENT("Urgent", "bg-rose-500/20 text-rose-400 border-rose-500/30");

    private final String label;
    private final String badgeClass;

    TaskPriority(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
