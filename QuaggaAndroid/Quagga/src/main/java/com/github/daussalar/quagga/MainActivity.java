package com.github.daussalar.quagga;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.ContentMetadata;
import com.google.cast.MediaProtocolCommand;
import com.google.cast.MediaProtocolMessageStream;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;

import java.io.IOException;

public class MainActivity extends ActionBarActivity {

    private static final String APPLICATION_NAME = "";
    private static final String CONTENT_URL = "";
    private static final String CONTENT_TITLE = "";

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
        private MediaProtocolMessageStream mMessageStream;
        private ContentMetadata mMetaData;

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

            mMetaData = new ContentMetadata();
            mMetaData.setTitle(CONTENT_TITLE);

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
            openSession();

        }

        private void openSession() {
            final ApplicationSession session = new ApplicationSession(mCastContext, mSelectedDevice);
            ApplicationSession.Listener listener = new ApplicationSession.Listener() {
                @Override public void onSessionStarted(ApplicationMetadata applicationMetadata) {
                    ToastHelper.showLongToast(getActivity(), "onSessionStarted");

                    ApplicationChannel channel = session.getChannel();
                    if (channel == null) {
                        ToastHelper.showLongToast(getActivity(), "Channel is null");
                        return;
                    }
                    mMessageStream = new MediaProtocolMessageStream();
                    channel.attachMessageStream(mMessageStream);
                    if (mMessageStream.getPlayerState() == null) {
                        loadMedia();
                    }
                }

                @Override public void onSessionStartFailed(SessionError sessionError) {
                    ToastHelper.showLongToast(getActivity(), sessionError.toString());
                }

                @Override public void onSessionEnded(SessionError sessionError) {
                    if (sessionError == null) {
                        // The session ended normally.
                        ToastHelper.showLongToast(getActivity(), "onSessionEnded normally");
                    } else {
                        // The session ended due to an error.
                        ToastHelper.showLongToast(getActivity(), sessionError.toString());
                    }
                }
            };

            session.setListener(listener);
            try {
                session.startSession(APPLICATION_NAME);
            } catch (IOException e) {
                ToastHelper.showLongToast(getActivity(), e.getMessage());
            }


        }

        @Override public void onSetVolume(double volume) {

        }

        @Override public void onUpdateVolume(double volume) {

        }

        private void loadMedia() {
            try {
                MediaProtocolCommand command = mMessageStream.loadMedia(CONTENT_URL, mMetaData);
                command.setListener(new MediaProtocolCommand.Listener() {
                    @Override public void onCompleted(MediaProtocolCommand mediaProtocolCommand) {
                        ToastHelper.showLongToast(getActivity(), "onCompleted");
                    }

                    @Override public void onCancelled(MediaProtocolCommand mediaProtocolCommand) {
                        ToastHelper.showLongToast(getActivity(), "onCancelled");
                    }
                });
            } catch (IOException e) {
                ToastHelper.showLongToast(getActivity(), e.getMessage());
            }
        }

    }

    public static class ToastHelper {
        public static void showLongToast(Context context, String message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

}
