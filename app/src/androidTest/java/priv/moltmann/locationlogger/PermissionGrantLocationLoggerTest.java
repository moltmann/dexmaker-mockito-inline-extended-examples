package priv.moltmann.locationlogger;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Toast;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoSession;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.doAnswer;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.mock;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.spyOn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.verify;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.timeout;

/**
 * Demonstrates how to use mockito on final and static methods.
 *
 * <ul>
 *     <li>An activity created in a {@link #mActivityRule ActivityTestRule} is made stubbable
 *     via {@link ExtendedMockito#spyOn(Class) spyOn}</li>
 *     <li>The final method {@link Activity#getSystemService(Class)} is stubbed in
 *     {@link #setupMockLocationManager(Activity)}</li>
 *     <li>The static method {@link Toast#makeText(Context, int, int)} is verified in
 *     {@link #denyPermission()}</li>
 * </ul>
 */
@RunWith(AndroidJUnit4.class)
public class PermissionGrantLocationLoggerTest {
    private static final int TIMEOUT_MILLIS = 1000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Rule
    public ActivityTestRule<LocationLogger> mActivityRule = new ActivityTestRule<>(LocationLogger
            .class);

    /**
     * Get the test {@link LocationLogger} activity that has been enabled for stubbing via
     * {@link ExtendedMockito#spyOn(Class))}.
     *
     * @return the test activity
     */
    private LocationLogger getSpyableLocationLogger() {
        LocationLogger locationLogger = mActivityRule.getActivity();
        spyOn(locationLogger);

        return locationLogger;
    }

    /**
     * Stubs {@code activity} so that any time {@link Activity#getSystemService(Class)
     * activity.getSystemService(LocationManager.class)} is called a {@link Mockito#mock mock}
     * {@link LocationManager} is returned.
     *
     * @param activity The activity that should return the mock LocationManager
     *
     * @return The mock location manager
     */
    private LocationManager setupMockLocationManager(Activity activity) {
        LocationManager mockLocationManager = mock(LocationManager.class);
        when(activity.getSystemService(LocationManager.class)).thenReturn(mockLocationManager);

        return mockLocationManager;
    }

    /**
     * Stubs {@code activity} so that any time {@link
     * Activity#requestPermissions(String[], int) a permission is requested} the activity is called
     * back with the {@code grantResult}.
     *
     * <p>This does not actually grant the permission, but this could be done via the {@code pm
     * grant} shell command.
     *
     * @param activity The activity that should have a stubbed changed permission behavior
     */
    private void setupPermissionRequestHandler(Activity activity, int grantResult) {
        doAnswer((methodInvocation) -> {
            String[] permissions = methodInvocation.getArgument(0);
            int requestCode = methodInvocation.getArgument(1);

            // Simulate asynchronous permission grant/deny callback
            mHandler.post(() -> activity.onRequestPermissionsResult(requestCode, permissions,
                    new int[]{grantResult}));

            return null;
        }).when(activity).requestPermissions(any(), anyInt());
    }

    @Test
    public void grantPermission() throws Exception {
        LocationLogger locationLogger = getSpyableLocationLogger();
        LocationManager mockLocationManager = setupMockLocationManager(locationLogger);

        // If the permission will be granted
        setupPermissionRequestHandler(locationLogger, PERMISSION_GRANTED);

        // once we click on the button
        onView(withId(R.id.resume_pause_button)).perform(click());

        // location updates should be requested
        verify(mockLocationManager, timeout(TIMEOUT_MILLIS).atLeastOnce())
                .requestLocationUpdates(any(), anyLong(), anyFloat(), any(LocationListener.class));
    }

    @Test
    public void denyPermission() throws Exception {
        // Start a mockito session that enables to stub static methods on the Toast class
        MockitoSession session = mockitoSession().spyStatic(Toast.class).startMocking();
        try {
            LocationLogger locationLogger = getSpyableLocationLogger();

            // If the permission will be denied
            setupPermissionRequestHandler(locationLogger, PERMISSION_DENIED);

            // once we click on the button
            onView(withId(R.id.resume_pause_button)).perform(click());

            // we expect a toast to be shown
            verify(() -> Toast.makeText(any(), any(), anyInt()), timeout(1000));
        } finally {
            session.finishMocking();
        }
    }
}
