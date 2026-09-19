package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;

public final class FastMath {

    public static final double PI = Math.PI;
    public static final double E = Math.E;

    private FastMath() {
    }

    @Contract(pure = true)
    public static double safeDivide(double numerator, double denominator, double fallback) {
        if (denominator == 0.0 || Double.isNaN(denominator) || Double.isInfinite(denominator)) {
            return fallback;
        }
        double result = numerator / denominator;
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return fallback;
        }
        return result;
    }

    @Contract(pure = true)
    public static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    @Contract(pure = true)
    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    @Contract(pure = true)
    public static long clamp(long val, long min, long max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    @Contract(pure = true)
    public static int min(int a, int b) {
        return a <= b ? a : b;
    }

    @Contract(pure = true)
    public static int max(int a, int b) {
        return a >= b ? a : b;
    }

    @Contract(pure = true)
    public static long min(long a, long b) {
        return a <= b ? a : b;
    }

    @Contract(pure = true)
    public static long max(long a, long b) {
        return a >= b ? a : b;
    }

    @Contract(pure = true)
    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    @Contract(pure = true)
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    @Contract(pure = true)
    public static double sqrt(double val) {
        return Math.sqrt(val);
    }

    @Contract(pure = true)
    public static int roundToInt(double val) {
        return (int) Math.round(val);
    }

    @Contract(pure = true)
    public static int abs(int val) {
        return val < 0 ? -val : val;
    }

    @Contract(pure = true)
    public static double abs(double val) {
        return val < 0.0 ? -val : val;
    }

    @Contract(pure = true)
    public static double ceil(double val) {
        return Math.ceil(val);
    }

    @Contract(pure = true)
    public static int ceilToInt(double val) {
        return (int) Math.ceil(val);
    }

    @Contract(pure = true)
    public static double floor(double val) {
        return Math.floor(val);
    }

    @Contract(pure = true)
    public static int floorToInt(double val) {
        return (int) Math.floor(val);
    }

    @Contract(pure = true)
    public static double map(double val, double fromMin, double fromMax, double toMin, double toMax) {
        if (fromMax - fromMin == 0.0) return toMin;
        return toMin + (val - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    @Contract(pure = true)
    public static double sin(double x) {
        x = x % (2.0 * Math.PI);
        if (x < -Math.PI) x += 2.0 * Math.PI;
        else if (x > Math.PI) x -= 2.0 * Math.PI;
        if (x < 0.0) {
            double nx = -x;
            return -16.0 * nx * (Math.PI - nx) / (5.0 * Math.PI * Math.PI - 4.0 * nx * (Math.PI - nx));
        } else {
            return 16.0 * x * (Math.PI - x) / (5.0 * Math.PI * Math.PI - 4.0 * x * (Math.PI - x));
        }
    }

    @Contract(pure = true)
    public static double cos(double x) {
        return sin(x + Math.PI * 0.5);
    }
}
