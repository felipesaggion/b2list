package br.com.b2list.origin;

public class OriginContext {
    private static final ThreadLocal<String> currentOrigin = new ThreadLocal<>();

    public static void setOrigin(String tenant) {
        currentOrigin.set(tenant);
    }

    public static String getOrigin() {
        return currentOrigin.get();
    }

    public static void clear() {
        currentOrigin.remove();
    }
}