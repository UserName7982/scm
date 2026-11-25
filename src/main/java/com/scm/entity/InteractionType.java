package com.scm.entity;

public enum InteractionType {
    CALL("Call", "fa-phone"),
    MESSAGE("Message", "fa-message"),
    MEETING("Meeting", "fa-handshake"),
    EMAIL("Email", "fa-envelope"),
    OTHER("Other", "fa-circle-info");

    private final String label;
    private final String icon;

    InteractionType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }
}
