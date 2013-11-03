package com.github.daussalar.quagga;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements MediaRouteAdapter {

        private CastContext mCastContext;
        private MediaRouteButton mMediaRouteButton;
        private MediaRouter mMediaRouter;
        private MediaRouteSelector mMediaRouteSelector;
        private MyMediaRouterCallback mMediaRouterCallback;
        private CastDevice mSelectedDevice;
        private MediaRouteStateChangeListener mRouteStateListener;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Activity activity = getActivity();
            Button sendBtn = (Button) activity.findViewById(R.id.btn_send);
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            mMediaRouteButton = (MediaRouteButton) activity.findViewById(R.id.media_route_button);
            mCastContext = new CastContext(activity.getApplicationContext());
            MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
            mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
            mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(
                    MediaRouteHelper.CATEGORY_CAST);
            mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
            mMediaRouterCallback = new MyMediaRouterCallback();

            mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        }

        @Override public void onDestroyView() {
            MediaRouteHelper.unregisterMediaRouteProvider(mCastContext);
            mCastContext.dispose();
            super.onDestroyView();
        }

        @Override public void onDetach() {
            mMediaRouter.removeCallback(mMediaRouterCallback);
            super.onDetach();
        }

        private class MyMediaRouterCallback extends MediaRouter.Callback {
            @Override
            public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                MediaRouteHelper.requestCastDeviceForRoute(route);
            }

            @Override
            public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
                mSelectedDevice = null;
                mRouteStateListener = null;
            }
        }

        // MediaRouteAdapter

        @Override
        public void onDeviceAvailable(CastDevice castDevice, String s,
                                      MediaRouteStateChangeListener mediaRouteStateChangeListener) {
            mSelectedDevice = castDevice;
            mRouteStateListener = mediaRouteStateChangeListener;
        }

        @Override public void onSetVolume(double volume) {

        }

        @Override public void onUpdateVolume(double volume) {

        }
    }

}
