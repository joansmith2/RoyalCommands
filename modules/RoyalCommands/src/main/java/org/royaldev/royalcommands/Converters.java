/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

public class Converters {

    /*
    Strongly based upon code from Bukkit.
     */

    public static boolean toBoolean(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        try {
            return Boolean.valueOf(o.toString());
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    public static byte toByte(Object o) {
        if (o instanceof Number) {
            return ((Number) o).byteValue();
        }
        try {
            return Byte.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return (byte) 0;
    }

    public static double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        try {
            return Double.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return 0D;
    }

    public static float toFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        try {
            return Float.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return 0F;
    }

    public static int toInt(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return 0;
    }

    public static long toLong(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return 0L;
    }

    public static short toShort(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        }
        try {
            return Short.valueOf(o.toString());
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return (short) 0;
    }

    public static String toStringy(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        try {
            return o.toString();
        } catch (NullPointerException ignored) {
        }
        return "";
    }

}
