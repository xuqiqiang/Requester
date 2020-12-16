package com.xuqiqiang.uikit.requester;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.xuqiqiang.uikit.requester.proxy.RequestResultActivity;

public class ActivityRequester {

    // region Activity
    public static void startActivityForResult(Context context, Intent intent, OnActivityResultListener listener) {
        RequestResultActivity.start(context, intent, listener);
    }

    public static void postOnResume(final Activity a, final Runnable r) {
        a.getApplication().registerActivityLifecycleCallbacks(new ActivityLifecycleAdapter() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (a == activity) {
                    if (r != null) r.run();
                    a.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (a == activity) {
                    a.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }
        });
    }

    public static void postOnDestroyed(final Activity a, final Runnable r) {
        a.getApplication().registerActivityLifecycleCallbacks(new ActivityLifecycleAdapter() {

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (a == activity) {
                    if (r != null) r.run();
                    a.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }
        });
    }
    // endregion

    // region Fragment
    public static void postOnResume(final Fragment f, final Runnable r) {
        postOnResume(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static void postOnResume(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        fm.registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {

                    @Override
                    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
                        if (f == fragment) {
                            if (r != null) r.run();
                            fm.unregisterFragmentLifecycleCallbacks(this);
                        }
                    }

                    @Override
                    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
                        if (f == fragment) {
                            fm.unregisterFragmentLifecycleCallbacks(this);
                        }
                    }
                }, true);
    }

    public static void postOnDestroyed(final Fragment f, final Runnable r) {
        postOnDestroyed(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static void postOnDestroyed(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        fm.registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {

                    @Override
                    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
                        if (f == fragment) {
                            if (r != null) r.run();
                            fm.unregisterFragmentLifecycleCallbacks(this);
                        }
                    }
                }, true);
    }
    // endregion

    public interface OnActivityResultListener {
        void onActivityResult(int resultCode, @Nullable Intent data);
    }

    private static class ActivityLifecycleAdapter implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}