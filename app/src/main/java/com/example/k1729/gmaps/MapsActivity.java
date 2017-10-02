package com.example.k1729.gmaps;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void fetchData() {
        String url = "http://ptm.fi/materials/golfcourses/golf_courses.json";
        FetchDataTask task = new FetchDataTask();
        task.execute(url);
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        private JSONArray courses;

        @Override
        protected JSONObject doInBackground(String... urls) {
                HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
            e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
            if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }

        protected float getColor(String color) {
            switch (color) {
                case "Kulta":
                case "Kulta/Etu":
                    return BitmapDescriptorFactory.HUE_YELLOW;
                case "Etu":
                    return BitmapDescriptorFactory.HUE_GREEN;
                default :
                    return BitmapDescriptorFactory.HUE_AZURE;
            }
        }

        protected String getDescription(JSONObject gc) {
            try {
                return gc.getString("address") + "\n" + gc.getString("phone") + "\n" + gc.getString("email") + "\n" + gc.getString("web");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {
            StringBuffer text = new StringBuffer("");
            try {
                courses = json.getJSONArray("courses");

                for (int i=0; i < courses.length(); i++) {
                    JSONObject gc = courses.getJSONObject(i);
                    // create one marker
                    final Marker m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(gc.getDouble("lat"), gc.getDouble("lng")))
                            .title(gc.getString("course"))
                            .icon(BitmapDescriptorFactory.defaultMarker(getColor(gc.getString("type"))))
                            .snippet(getDescription(gc))
                    );
                    // marker listener
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(final Marker marker) {
                            if (marker.equals(m)){
                                Toast.makeText(getApplicationContext(), "Marker = " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            return false;
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // store map object to member variable
        mMap = googleMap;
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in ITC
        LatLng ITC = new LatLng(62.2416223, 25.7597309);
        // point to jamk and zoom a little
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ITC, 5));
        fetchData();
    }
}
