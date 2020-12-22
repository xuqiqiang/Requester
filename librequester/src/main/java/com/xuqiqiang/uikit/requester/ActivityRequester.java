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

import java.lang.ref.WeakReference;

public class ActivityRequester {

    // region Activity
    public static void startActivityForResult(Context context, Intent intent, OnActivityResultListener listener) {
        RequestResultActivity.start(context, intent, listener);
    }

    public static void postOnResume(final Activity a, final Runnable r) {
        a.getApplication().registerActivityLifecycleCallbacks(new ActivityOnResumeAdapter(a, r));
    }

    public static void postOnDestroyed(final Activity a, final Runnable r) {
        a.getApplication().registerActivityLifecycleCallbacks(new ActivityOnDestroyAdapter(a, r));
    }
    // endregion

    // region Fragment
    public static void postOnResume(final Fragment f, final Runnable r) {
        postOnResume(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static void postOnResume(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        fm.registerFragmentLifecycleCallbacks(new FragmentOnResumeAdapter(f, r), true);
    }

    public static void postOnDestroyed(final Fragment f, final Runnable r) {
        postOnDestroyed(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static void postOnDestroyed(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        fm.registerFragmentLifecycleCallbacks(
                new FragmentOnDestroyAdapter(f, r), true);
    }
    // endregion

    public interface OnActivityResultListener {
        void onActivityResult(int resultCode, @Nullable Intent data);
    }

    private static class ActivityOnResumeAdapter extends ActivityLifecycleAdapter {

        public ActivityOnResumeAdapter(Activity activity, Runnable runnable) {
            super(activity, runnable);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            handleEvent(activity);
        }
    }

    private static class ActivityOnDestroyAdapter extends ActivityLifecycleAdapter {

        public ActivityOnDestroyAdapter(Activity activity, Runnable runnable) {
            super(activity, runnable);
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            handleEvent(activity);
        }
    }

    public static class ActivityLifecycleAdapter implements Application.ActivityLifecycleCallbacks {
        private final WeakReference<Activity> rActivity;
        private final WeakReference<Runnable> rRunnable;

        public ActivityLifecycleAdapter(Activity activity, Runnable runnable) {
            this.rActivity = new WeakReference<>(activity);
            this.rRunnable = new WeakReference<>(runnable);
        }

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
            Activity a = rActivity.get();
            if (a == null || a == activity) {
                clear(activity);
            }
        }

        protected void handleEvent(@NonNull Activity activity) {
            Activity a = rActivity.get();
            if (a == activity) {
                Runnable r = rRunnable.get();
                if (r != null) r.run();
                clear(activity);
            } else if (a == null) {
                clear(activity);
            }
        }

        protected void clear(Activity activity) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            rActivity.clear();
            rRunnable.clear();
        }
    }

    private static class FragmentOnResumeAdapter extends FragmentLifecycleAdapter {

        public FragmentOnResumeAdapter(Fragment fragment, Runnable runnable) {
            super(fragment, runnable);
        }

        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
            handleEvent(fm, fragment);
        }
    }

    private static class FragmentOnDestroyAdapter extends FragmentLifecycleAdapter {

        public FragmentOnDestroyAdapter(Fragment fragment, Runnable runnable) {
            super(fragment, runnable);
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
            handleEvent(fm, fragment);
        }
    }

    public static class FragmentLifecycleAdapter extends FragmentManager.FragmentLifecycleCallbacks {
        private final WeakReference<Fragment> rFragment;
        private final WeakReference<Runnable> rRunnable;

        public FragmentLifecycleAdapter(Fragment fragment, Runnable runnable) {
            this.rFragment = new WeakReference<>(fragment);
            this.rRunnable = new WeakReference<>(runnable);
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
            Fragment f = rFragment.get();
            if (f == null || f == fragment) {
                clear(fm);
            }
        }

        protected void handleEvent(@NonNull FragmentManager fm, @NonNull Fragment fragment) {
            Fragment f = rFragment.get();
            if (f == fragment) {
                Runnable r = rRunnable.get();
                if (r != null) r.run();
                clear(fm);
            } else if (f == null) {
                clear(fm);
            }
        }

        protected void clear(@NonNull FragmentManager fm) {
            fm.unregisterFragmentLifecycleCallbacks(this);
            rFragment.clear();
            rRunnable.clear();
        }
    }
}