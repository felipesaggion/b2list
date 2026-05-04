package br.com.b2list.enums;

public enum OrderOrigin {
    API,
    MOBILE,
    SYNC;

    public static OrderOrigin fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        for (OrderOrigin origin : OrderOrigin.values()) {
            if (origin.name().equalsIgnoreCase(value.trim())) {
                return origin;
            }
        }
        throw new IllegalArgumentException("Unknown OrderOrigin: " + value);
    }
}
