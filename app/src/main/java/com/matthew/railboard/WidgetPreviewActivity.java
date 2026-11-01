package com.matthew.railboard;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WidgetPreviewActivity extends Activity {
    private AppWidgetHost host;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_widget_preview);

        host = new AppWidgetHost(this, 7710);
        host.startListening();
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        int id = host.allocateAppWidgetId();
        ComponentName cn = new ComponentName(this, RailWidgetProvider.class);
        boolean bound = mgr.bindAppWidgetIdIfAllowed(id, cn);
        LinearLayout holder = (LinearLayout) findViewById(R.id.widgetHolder);
        TextView note = (TextView) findViewById(R.id.previewNote);
        if (bound) {
            // a generous two-column-phone size so the real provider lays it out
            Bundle opts = new Bundle();
            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300);
            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 360);
            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180);
            opts.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 220);
            AppWidgetProviderInfo info = mgr.getAppWidgetInfo(id);
            AppWidgetHostView view = host.createView(this, id, info);
            view.updateAppWidgetOptions(opts);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(24, 24, 24, 24);
            holder.addView(view, lp);
            note.setText("This is the real home-screen widget, rendered by the actual widget provider.");
        } else {
            note.setText("Widget binding was refused by the system.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { host.stopListening(); } catch (Exception e) { }
    }
}