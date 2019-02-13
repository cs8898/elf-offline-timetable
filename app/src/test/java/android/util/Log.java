package android.util;

public class Log {
    private static boolean PRINT_OUTPUT = true;

    public static int d(String tag, String msg) {
        if("PRINT_OUTPUT".equals(tag) && ("true".equalsIgnoreCase(msg) || "false".equalsIgnoreCase(msg))){
            PRINT_OUTPUT = Boolean.parseBoolean(msg);
            return 0;
        }
        if (PRINT_OUTPUT)
            System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        if (PRINT_OUTPUT)
            System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        if (PRINT_OUTPUT)
            System.out.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        if (PRINT_OUTPUT)
            System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }

    // add other methods if required...
}
