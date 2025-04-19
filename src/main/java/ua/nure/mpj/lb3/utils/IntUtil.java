package ua.nure.mpj.lb3.utils;

public class IntUtil {
    public static int parseIntOrZero(String page) {
        try {
            return Integer.parseInt(page);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
