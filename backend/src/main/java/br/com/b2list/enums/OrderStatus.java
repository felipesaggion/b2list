package br.com.b2list.enums;

public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELED;

    public static OrderStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        for (OrderStatus origin : OrderStatus.values()) {
            if (origin.name().equalsIgnoreCase(value.trim())) {
                return origin;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus: " + value);
    }
}
