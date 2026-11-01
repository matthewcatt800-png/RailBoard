package com.matthew.railboard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private List<Stations.Near> items = new ArrayList<Stations.Near>();
    private StationAdapter adapter;
    private String query = "";
    private boolean showingNearest = false;
    private static final int REQ_LOC = 41;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        ListView list = (ListView) findViewById(R.id.stationList);
        View header = getLayoutInflater().inflate(R.layout.header_main, list, false);
        list.addHeaderView(header, null, false);
        adapter = new StationAdapter();
        list.setAdapter(adapter);
        allStations();

        Button last = (Button) header.findViewById(R.id.btnLast);
        String crs = Prefs.lastCrs(this);
        if (crs.length() > 0) {
            last.setVisibility(View.VISIBLE);
            last.setText("Open " + Prefs.lastName(this) + " (" + crs + ")");
            last.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { openStation(Prefs.lastCrs(MainActivity.this), Prefs.lastName(MainActivity.this)); }
            });
        } else {
            last.setVisibility(View.GONE);
        }

        header.findViewById(R.id.btnLocation).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { useLocation(); }
        });

        EditText search = (EditText) header.findViewById(R.id.searchBox);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) { }
            public void onTextChanged(CharSequence s, int a, int c, int d) { }
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                if (query.length() > 0) { showingNearest = false; searchStations(); }
                else if (showingNearest) { } // keep nearest list
                else allStations();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void allStations() {
        items = new ArrayList<Stations.Near>();
        for (String row : Stations.DATA) items.add(new Stations.Near(row, -1));
        showingNearest = false;
        adapter.notifyDataSetChanged();
    }

    private void searchStations() {
        items = new ArrayList<Stations.Near>();
        String q = query.toLowerCase();
        for (String row : Stations.DATA) {
            if (Stations.name(row).toLowerCase().contains(q) || Stations.crs(row).toLowerCase().contains(q))
                items.add(new Stations.Near(row, -1));
        }
        adapter.notifyDataSetChanged();
    }

    private void useLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOC);
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location best = null;
        try {
            for (String p : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER}) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
        } catch (SecurityException e) {
            best = null;
        }
        if (best == null) {
            Toast.makeText(this, "No location fix yet, turn on location and try again", Toast.LENGTH_LONG).show();
            return;
        }
        List<Stations.Near> near = new ArrayList<Stations.Near>();
        for (String row : Stations.DATA) {
            double d = Stations.distanceMeters(best.getLatitude(), best.getLongitude(),
                Stations.lat(row), Stations.lon(row));
            near.add(new Stations.Near(row, d));
        }
        Collections.sort(near, new Comparator<Stations.Near>() {
            public int compare(Stations.Near a, Stations.Near b) {
                return Double.compare(a.meters, b.meters);
            }
        });
        items = near.subList(0, Math.min(10, near.size()));
        showingNearest = true;
        query = "";
        ((EditText) findViewById(R.id.searchBox)).setText("");
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Nearest stations by GPS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        if (req == REQ_LOC && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            useLocation();
        } else if (req == REQ_LOC) {
            Toast.makeText(this, "Location permission needed to find your nearest station", Toast.LENGTH_LONG).show();
        }
    }

    private void openStation(String crs, String name) {
        Prefs.setLast(this, crs, name);
        Intent i = new Intent(this, DeparturesActivity.class);
        i.putExtra("crs", crs);
        i.putExtra("name", name);
        startActivity(i);
    }

    private class StationAdapter extends BaseAdapter {
        public int getCount() { return items.size(); }
        public Object getItem(int pos) { return items.get(pos); }
        public long getItemId(int pos) { return pos; }
        public View getView(int pos, View convert, ViewGroup parent) {
            View v = convert;
            if (v == null) v = getLayoutInflater().inflate(R.layout.item_station, parent, false);
            final Stations.Near nr = items.get(pos);
            TextView name = (TextView) v.findViewById(R.id.stationName);
            TextView sub = (TextView) v.findViewById(R.id.stationSub);
            name.setText(Stations.name(nr.row));
            if (nr.meters >= 0) {
                double miles = nr.meters / 1609.34;
                sub.setText(Stations.crs(nr.row) + " - " + (miles < 0.1 ? "under 0.1 mi" : String.format(java.util.Locale.UK, "%.1f mi away", miles)));
            } else {
                sub.setText(Stations.crs(nr.row));
            }
            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) { openStation(Stations.crs(nr.row), Stations.name(nr.row)); }
            });
            return v;
        }
    }
}