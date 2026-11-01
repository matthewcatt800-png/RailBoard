package com.matthew.railboard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RailApi {
    public interface Listener {
        void onResult(List<Departure> services, List<String> messages, boolean live, String error);
    }

    private static final String BASE = "https://national-rail-api.davwheat.dev/departures/";

    public static void fetch(final Context ctx, final String crs, final String stationName, final Listener listener) {
        final Handler main = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            public void run() {
                List<Departure> out = new ArrayList<Departure>();
                List<String> msgs = new ArrayList<String>();
                String error = null;
                try {
                    URL url = new URL(BASE + crs + "?numRows=12&expand=false");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(12000);
                    conn.setReadTimeout(12000);
                    conn.setRequestProperty("User-Agent", "RailBoard personal app");
                    int code = conn.getResponseCode();
                    if (code != 200) throw new IllegalStateException("HTTP " + code);
                    InputStream in = conn.getInputStream();
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
                    in.close();
                    String body = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    JSONObject root = new JSONObject(body);
                    JSONArray ms = root.optJSONArray("nrccMessages");
                    if (ms != null) {
                        for (int i = 0; i < ms.length() && i < 3; i++) {
                            String v = ms.getJSONObject(i).optString("value", "");
                            v = stripHtml(v).trim();
                            if (v.length() > 0) msgs.add(v);
                        }
                    }
                    JSONArray ts = root.optJSONArray("trainServices");
                    if (ts != null) {
                        for (int i = 0; i < ts.length(); i++) {
                            JSONObject t = ts.getJSONObject(i);
                            Departure d = new Departure();
                            d.time = t.optString("std", "");
                            d.expected = t.optString("etd", "");
                            d.platform = t.optString("platform", null);
                            d.operator = t.optString("operator", "");
                            d.cancelled = t.optBoolean("isCancelled", false);
                            JSONArray dest = t.optJSONArray("destination");
                            d.destination = dest != null && dest.length() > 0
                                ? dest.getJSONObject(0).optString("locationName", "") : "";
                            if (d.time.length() > 0) out.add(d);
                        }
                    }
                } catch (Exception e) {
                    error = e.getMessage() == null ? "could not reach the live feed" : e.getMessage();
                }
                if (out.size() > 0) cacheBoard(ctx, crs, stationName, out);
                final List<Departure> fOut = out;
                final List<String> fMsgs = msgs;
                final String fErr = error;
                main.post(new Runnable() {
                    public void run() {
                        listener.onResult(fOut, fMsgs, fErr == null, fErr);
                    }
                });
            }
        }).start();
    }

    private static String stripHtml(String s) {
        if (s == null) return "";
        s = s.replace("<br/>", " ").replace("<br>", " ").replace("<br />", " ");
        s = s.replaceAll("<[^>]*>", " ");
        s = s.replace("&amp;", "&").replace("&nbsp;", " ")
             .replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"");
        return s.replaceAll(" +", " ").trim();
    }

    /** cache format: crs|name|HH:MM dest [plat X]|line2|... lines already composed */
    private static void cacheBoard(Context ctx, String crs, String name, List<Departure> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(crs).append("|").append(name == null ? "" : name);
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.UK);
        sb.append("|").append(hm.format(new Date()));
        int i = 0;
        for (Departure d : list) {
            if (i >= 4) break;
            sb.append("|").append(d.time).append(" ").append(d.destination)
              .append(d.platform == null || d.platform.length() == 0 ? "" : " [Plat " + d.platform + "]")
              .append(d.cancelled ? " CANCELLED" : "");
            i++;
        }
        Prefs.setBoardCache(ctx, sb.toString());
        Prefs.setBoardAt(ctx, System.currentTimeMillis());
    }
}