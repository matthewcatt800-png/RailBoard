package com.matthew.railboard;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("railboard", Context.MODE_PRIVATE);
    }
    public static String lastCrs(Context c) { return sp(c).getString("lastCrs", ""); }
    public static String lastName(Context c) { return sp(c).getString("lastName", ""); }
    public static void setLast(Context c, String crs, String name) {
        sp(c).edit().putString("lastCrs", crs).putString("lastName", name).apply();
    }
    public static String boardCache(Context c) { return sp(c).getString("board", ""); }
    public static void setBoardCache(Context c, String v) { sp(c).edit().putString("board", v).apply(); }
    public static long boardAgeMs(Context c) { return sp(c).getLong("boardAt", 0L); }
    public static void setBoardAt(Context c, long t) { sp(c).edit().putLong("boardAt", t).apply(); }
}