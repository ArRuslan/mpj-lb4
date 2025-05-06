package ua.nure.mpj.lb4.utils;

public class IntUtil {
    public static int parseIntOrZero(String page) {
        try {
            return Integer.parseInt(page);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static long parseLongOrZero(String page) {
        try {
            return Long.parseLong(page);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
