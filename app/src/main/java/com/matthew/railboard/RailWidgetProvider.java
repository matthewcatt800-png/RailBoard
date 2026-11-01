package com.matthew.railboard;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import java.util.List;

public class RailWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(final Context ctx, AppWidgetManager mgr, int[] ids) {
        final String cache = Prefs.boardCache(ctx);
        pushCache(ctx, cache, mgr, ids);
        final String crs = Prefs.lastCrs(ctx);
        if (crs.length() == 0) return;
        final String name = Prefs.lastName(ctx);
        RailApi.fetch(ctx, crs, name, new RailApi.Listener() {
            public void onResult(List<Departure> s, List<String> m, boolean live, String error) {
                String fresh = Prefs.boardCache(ctx);
                if (!fresh.equals(cache)) {
                    AppWidgetManager mgr2 = AppWidgetManager.getInstance(ctx);
                    pushCache(ctx, fresh, mgr2, mgr2.getAppWidgetIds(
                        new ComponentName(ctx, RailWidgetProvider.class)));
                }
            }
        });
    }

    private void pushCache(Context ctx, String cache, AppWidgetManager mgr, int[] ids) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_rail);
        if (cache == null || cache.length() == 0) {
            rv.setTextViewText(R.id.widgetStation, "RailBoard");
            rv.setTextViewText(R.id.widgetLines, "Open the app and pick a station to pin your board here");
        } else {
            String[] parts = cache.split("\\|");
            rv.setTextViewText(R.id.widgetStation, (parts.length > 1 ? parts[1] : "") + " (" + parts[0] + ")");
            StringBuilder sb = new StringBuilder();
            String updated = "";
            if (parts.length > 2) updated = "updated " + parts[2];
            for (int i = 3; i < parts.length; i++) {
                sb.append(parts[i]).append("\n");
            }
            if (sb.length() == 0) sb.append("No departures cached yet, open the app to refresh");
            else sb.append(updated);
            rv.setTextViewText(R.id.widgetLines, sb.toString().trim());
        }
        // tapping the widget opens the app
        android.app.PendingIntent pi = android.app.PendingIntent.getActivity(ctx, 0,
            new android.content.Intent(ctx, MainActivity.class),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widgetRoot, pi);
        mgr.updateAppWidget(ids, rv);
    }
}