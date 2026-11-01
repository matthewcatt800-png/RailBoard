package com.matthew.railboard;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeparturesActivity extends Activity {
    private String crs;
    private String name;
    private List<Departure> services = new ArrayList<Departure>();
    private List<String> messages = new ArrayList<String>();
    private boolean live;
    private DepartureAdapter adapter;
    private final Handler timer = new Handler();
    private final Runnable tick = new Runnable() {
        public void run() { load(); timer.postDelayed(this, 60000); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_departures);
        crs = getIntent().getStringExtra("crs");
        name = getIntent().getStringExtra("name");
        if (crs == null || crs.length() == 0) { finish(); return; }
        if (name == null) name = Stations.nameOf(crs);
        Prefs.setLast(this, crs, name);

        TextView title = (TextView) findViewById(R.id.boardTitle);
        TextView sub = (TextView) findViewById(R.id.boardSub);
        title.setText(name);
        sub.setText(crs + " - live departures board");

        Button refresh = (Button) findViewById(R.id.btnRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { load(); }
        });

        ListView list = (ListView) findViewById(R.id.boardList);
        View head = getLayoutInflater().inflate(R.layout.item_departure_head, list, false);
        list.addHeaderView(head, null, false);
        adapter = new DepartureAdapter();
        list.setAdapter(adapter);
        load();
    }

    private void load() {
        final TextView banner = (TextView) findViewById(R.id.demoBanner);
        final TextView msgs = (TextView) findViewById(R.id.nrccMessages);
        RailApi.fetch(this, crs, name, new RailApi.Listener() {
            public void onResult(List<Departure> s, List<String> m, boolean isLive, String error) {
                services = s;
                messages = m;
                live = isLive;
                adapter.notifyDataSetChanged();
                if (isLive) {
                    banner.setText("LIVE - National Rail data, refreshed " + clockNow());
                    banner.setBackgroundResource(R.color.railGreen);
                } else {
                    banner.setText("COULD NOT REACH THE LIVE FEED - tap refresh to try again");
                    banner.setBackgroundResource(R.color.railRed);
                }
                if (m != null && m.size() > 0) {
                    msgs.setText(TextUtils.join("\n", m));
                    msgs.setVisibility(View.VISIBLE);
                } else {
                    msgs.setVisibility(View.GONE);
                }
                TextView empty = (TextView) findViewById(R.id.boardEmpty);
                if (s.isEmpty()) {
                    empty.setText("No departures showing right now (service may have ended for today)");
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            }
        });
    }

    private String clockNow() {
        return new java.text.SimpleDateFormat("HH:mm", java.util.Locale.UK).format(new java.util.Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.removeCallbacks(tick);
    }

    private class DepartureAdapter extends BaseAdapter {
        public int getCount() { return services.size(); }
        public Object getItem(int pos) { return services.get(pos); }
        public long getItemId(int pos) { return pos; }
        public View getView(int pos, View convert, ViewGroup parent) {
            View v = convert;
            if (v == null) v = getLayoutInflater().inflate(R.layout.item_departure, parent, false);
            Departure d = services.get(pos);
            TextView time = (TextView) v.findViewById(R.id.depTime);
            TextView dest = (TextView) v.findViewById(R.id.depDest);
            TextView expected = (TextView) v.findViewById(R.id.depExpected);
            TextView plat = (TextView) v.findViewById(R.id.depPlatform);
            TextView op = (TextView) v.findViewById(R.id.depOperator);
            time.setText(d.time);
            dest.setText(d.destination);
            expected.setText(d.expectedText());
            expected.setTextColor(getResources().getColor(
                d.cancelled ? R.color.railRed
                : (d.expectedText().equals("On time") ? R.color.railGreen : R.color.railAmber)));
            expected.setTypeface(null, Typeface.BOLD);
            plat.setText(d.platform == null || d.platform.length() == 0 ? "Plat -" : "Plat " + d.platform);
            op.setText(d.operator == null ? "" : d.operator);
            return v;
        }
    }
}