package ru.smole.utils;

public class StringUtils {

    public static String formatDouble(double d) {
        if (d < 1000.0)
            return String.format("%.1f", d).replace(",", ".");
        if (d < 1000000.0)
            return String.format("%.1f", d / 1000.0).replace(",", ".") + "K";
        if (d < 1.0E9)
            return String.format("%.1f", d / 1000000.0).replace(",", ".") + "M";
        if (d < 1.0E12D)
            return String.format("%.1f", d / 1.0E9D).replace(",", ".") + "B";
        if (d < 1.0E15D)
            return String.format("%.1f", d / 1.0E12D).replace(",", ".") + "T";
        if (d < 1.0E18D)
            return String.format("%.1f", d / 1.0E15D).replace(",", ".") + "Qr";
        if (d < 1.0E21D)
            return String.format("%.1f", d / 1.0E18D).replace(",", ".") + "Qn";
        if (d < 1.0E24D)
            return String.format("%.1f", d / 1.0E21D).replace(",", ".") + "Sx";
        if (d < 1.0E27D)
            return String.format("%.1f", d / 1.0E24D).replace(",", ".") + "Sp";
        return String.format("%.1f", d / 1.0E27D).replace(",", ".") + "Oc";
    }

    public static String _formatDouble(double d) {
        return String.format("%f", d);
    }

//    public static Double fixDouble(int i, double d) {
//        return Double.valueOf(String.format("%." + i + "f", d).replace(",", "."));
//    }
//
//    public static String trim(String string) {
//        if (string.length() > 16)
//            return string.substring(0, 16);
//        return string;
//    }
}
