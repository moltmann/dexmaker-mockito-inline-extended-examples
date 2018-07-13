package priv.moltmann.locationlogger;

import android.annotation.SuppressLint;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.FEATURE_LOCATION_GPS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Activity logging the current location.
 *
 * <p>Once the logging is started by clicking on the {@code resume_pause_button} the {@link
 * android.Manifest.permission#ACCESS_FINE_LOCATION location permission} is requested. If the
 * permission is granted by the user location updates are requested and displayed in the {@code
 * location_history}. If the permission is denied an error message is shown via a {@link Toast}
 */
public class LocationLogger extends AppCompatActivity {
    private static final int LOC_PERM_ID = 1;

    private FloatingActionButton mFab;

    private final LocationAdapter mLocAdapter = new LocationAdapter();
    private final ArrayList<String> mLogs = new ArrayList<>();
    boolean mIsLogging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_logger);
        Toolbar toolbar = requireViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = requireViewById(R.id.resume_pause_button);

        mFab.setOnClickListener(view -> {
            if (mIsLogging) {
                getSystemService(LocationManager.class).removeUpdates(mLocAdapter);

                mIsLogging = false;
                mFab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            } else {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOC_PERM_ID);
            }
        });

        ((ListView) requireViewById(R.id.location_history)).setAdapter(new BaseAdapter() {
            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                mLocAdapter.registerObserver(observer);
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {
                mLocAdapter.unregisterObserver(observer);
            }

            @Override
            public int getCount() {
                return mLogs.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1,
                            parent, false);
                }

                ((TextView) convertView.requireViewById(android.R.id.text1)).setText(mLogs.get
                        (position));

                return convertView;
            }

            @Override
            public Object getItem(int position) {
                return mLogs.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        });
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOC_PERM_ID) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PERMISSION_GRANTED) {
                        LocationManager locationManager = getSystemService(LocationManager.class);

                        if (getPackageManager().hasSystemFeature(FEATURE_LOCATION_GPS)) {
                            locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, mLocAdapter);
                        }

                        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, mLocAdapter);

                        mIsLogging = true;
                        mFab.setImageResource(R.drawable.ic_stop_black_24dp);

                        return;
                    }
                }
            }

            Toast.makeText(this, getString(R.string.error_no_loc_perm), LENGTH_SHORT).show();
        }
    }

    private class LocationAdapter extends DataSetObservable implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            mLogs.add(location.getLatitude() + "°N:" + location.getLatitude() + "°E");
            notifyChanged();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            // do nothing
        }

        @Override
        public void onProviderDisabled(String provider) {
            // do nothing
        }
    }
}