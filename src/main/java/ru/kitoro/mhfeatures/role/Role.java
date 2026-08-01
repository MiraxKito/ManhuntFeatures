package ru.kitoro.mhfeatures.role;

public enum Role {
    HUNTER,
    RUNNER,
    NONE;

    public static Role parse(String value) {
        switch (value.toLowerCase()) {
            case "hunter":
            case "hunters":
                return HUNTER;
            case "runner":
            case "runners":
                return RUNNER;
            case "clear":
            case "none":
            case "remove":
                return NONE;
            default:
                return null;
        }
    }
}
