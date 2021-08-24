package ru.smole.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

    public static String formatDouble(int i, double d) {
        if (d < 1000.0)
            return _fixDouble(i, d).replace(",", ".");
        if (d < 1000000.0)
            return _fixDouble(i, d / 1000.0).replace(",", ".") + "K";
        if (d < 1.0E9)
            return _fixDouble(i, d / 1000000.0).replace(",", ".") + "M";
        if (d < 1.0E12D)
            return _fixDouble(i, d / 1.0E9D).replace(",", ".") + "B";
        if (d < 1.0E15D)
            return _fixDouble(i, d / 1.0E12D).replace(",", ".") + "T";
        if (d < 1.0E18D)
            return _fixDouble(i, d / 1.0E15D).replace(",", ".") + "Qr";
        if (d < 1.0E21D)
            return _fixDouble(i, d / 1.0E18D).replace(",", ".") + "Qn";
        if (d < 1.0E24D)
            return _fixDouble(i, d / 1.0E21D).replace(",", ".") + "Sx";
        if (d < 1.0E27D)
            return _fixDouble(i, d / 1.0E24D).replace(",", ".") + "Sp";
        return _fixDouble(i, d / 1.0E27D).replace(",", ".") + "Oc";
    }

    public static Double fixDouble(int i, double d) {
        return Double.valueOf(String.format("%." + i + "f", d).replace(",", "."));
    }

    public static String _fixDouble(int i, double d) {
        return String.format("%." + i + "f", d);
    }

    public static String replaceComma(double d) {
        return String.format("%,.0f", d);
    }

    public static String unReplaceComma(String text) {
        return text.replaceAll(",", "");
    }

    public static String generateString(int length) {
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    public static String getRandomString(List<String> strings) {
        Random random = new Random();

        return strings.get(random.nextInt(strings.size() - 1));
    }

//
//    public static String trim(String string) {
//        if (string.length() > 16)
//            return string.substring(0, 16);
//        return string;
//    }
}
