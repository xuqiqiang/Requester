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
import com.xuqiqiang.uikit.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

public class ActivityRequester {

    //    private static final Map<String, WeakReference<Event>> mEventMap = new HashMap<>();
//    private static final Set<ActivityLifecycleAdapter> mActivityLifecycleAdapterMap = new HashSet<>();
    private static final List<Event> mEventList = new LinkedList<>();

    // region Activity
    public static void startActivityForResult(Context context, Intent intent, OnActivityResultListener listener) {
        RequestResultActivity.start(context, intent, listener);
    }

    public static ActivityLifecycleAdapter postOnResume(Runnable r) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return null;
        return postOnResume(topActivity, r);
    }

    public static ActivityLifecycleAdapter postOnResume(Activity a, Runnable r) {
        ActivityOnResumeAdapter adapter = new ActivityOnResumeAdapter(a, r);
        a.getApplication().registerActivityLifecycleCallbacks(adapter);
        return adapter;
    }

    public static ActivityLifecycleAdapter postOnDestroyed(Runnable r) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return null;
        return postOnDestroyed(topActivity, r);
    }

    public static ActivityLifecycleAdapter postOnDestroyed(Activity a, Runnable r) {
        ActivityOnDestroyAdapter adapter = new ActivityOnDestroyAdapter(a, r);
        a.getApplication().registerActivityLifecycleCallbacks(adapter);
        return adapter;
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        Activity topActivity = Utils.getTopActivity();
        if (topActivity == null) return false;
        return postDelayed(topActivity, r, delayMillis);
    }

    public static boolean postDelayed(@NonNull Activity a, Runnable r, long delayMillis) {
        if (r == null) return false;
        Event event = new DelayedEvent(r);
        if (mMainHandler.postDelayed(event, delayMillis)) {
            mEventList.add(event);
            event.setActivityLifecycleAdapter(postOnDestroyed(a, new ClearRunnable(event)));
            return true;
        }
        return false;
    }

    public static void removeCallbacks(@NonNull Runnable r) {
        Event event = new Event(r);
        int index = mEventList.indexOf(event);
        if (index >= 0) {
            event = mEventList.get(index);
            event.clear();
            mEventList.remove(event);
            mMainHandler.removeCallbacks(event);
        }
    }
    // endregion

    // region Fragment
    public static FragmentLifecycleAdapter postOnResume(final Fragment f, final Runnable r) {
        return postOnResume(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static FragmentLifecycleAdapter postOnResume(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        FragmentOnResumeAdapter adapter = new FragmentOnResumeAdapter(f, r);
        fm.registerFragmentLifecycleCallbacks(adapter, true);
        return adapter;
    }

    public static FragmentLifecycleAdapter postOnDestroyed(final Fragment f, final Runnable r) {
        return postOnDestroyed(f.getActivity().getSupportFragmentManager(), f, r);
    }

    public static FragmentLifecycleAdapter postOnDestroyed(@NonNull FragmentManager fm, final Fragment f, final Runnable r) {
        FragmentOnDestroyAdapter adapter = new FragmentOnDestroyAdapter(f, r);
        fm.registerFragmentLifecycleCallbacks(adapter, true);
        return adapter;
    }

    public static boolean postDelayed(@NonNull Fragment f, Runnable r, long delayMillis) {
        if (r == null) return false;
        Event event = new DelayedEvent(r);
        if (mMainHandler.postDelayed(event, delayMillis)) {
            mEventList.add(event);
            event.setFragmentLifecycleAdapter(postOnDestroyed(f, new ClearRunnable(event)));
            return true;
        }
        return false;
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
        final WeakReference<Activity> rActivity;
        private final WeakReference<Event> rEvent;

        public ActivityLifecycleAdapter(Activity activity, Runnable runnable) {
            this.rActivity = new WeakReference<>(activity);
            Event event = new Event(runnable);
            event.setActivityLifecycleAdapter(this);
            mEventList.add(event);
            this.rEvent = new WeakReference<>(event);
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
                Event e = rEvent.get();
                if (e != null) e.run();
                clear(activity);
            } else if (a == null) {
                clear(activity);
            }
        }

        protected void clear(Activity activity) {
            if (activity == null) Utils.getApp().unregisterActivityLifecycleCallbacks(this);
            else activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            rActivity.clear();
            Event e = rEvent.get();
            if (e != null) mEventList.remove(e);
            rEvent.clear();
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
        final WeakReference<Fragment> rFragment;
        //        private final WeakReference<Runnable> rRunnable;
        private final WeakReference<Event> rEvent;

        public FragmentLifecycleAdapter(Fragment fragment, Runnable runnable) {
            this.rFragment = new WeakReference<>(fragment);
//            this.rRunnable = new WeakReference<>(runnable);
            Event event = new Event(runnable);
            event.setFragmentLifecycleAdapter(this);
            mEventList.add(event);
            this.rEvent = new WeakReference<>(event);
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
                Event e = rEvent.get();
                if (e != null) e.run();
                clear(fm);
            } else if (f == null) {
                clear(fm);
            }
        }

        protected void clear(FragmentManager fm) {
            if (fm != null) fm.unregisterFragmentLifecycleCallbacks(this);
            else {
                Fragment f = rFragment.get();
                if (f != null) {
                    fm = f.getFragmentManager();
                    if (fm != null) fm.unregisterFragmentLifecycleCallbacks(this);
                }
            }
            rFragment.clear();
            Event e = rEvent.get();
            if (e != null) mEventList.remove(e);
            rEvent.clear();
        }
    }

    private static class ClearRunnable implements Runnable {

        private Event event;

        public ClearRunnable(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            mEventList.remove(event);
            mMainHandler.removeCallbacks(event);
            event = null;
        }
    }

    public static class DelayedEvent extends Event {

        public DelayedEvent(Runnable runnable) {
            super(runnable);
        }

        @Override
        public void run() {
            clear();
            super.run();
        }
    }

    public static class Event implements Runnable {
        private final WeakReference<Runnable> rRunnable;
        protected WeakReference<ActivityLifecycleAdapter> rActivityAdapter;
        protected WeakReference<FragmentLifecycleAdapter> rFragmentAdapter;

        public Event(Runnable runnable) {
            this.rRunnable = new WeakReference<>(runnable);
        }

        public void setActivityLifecycleAdapter(ActivityLifecycleAdapter activityLifecycleAdapter) {
            this.rActivityAdapter = new WeakReference<>(activityLifecycleAdapter);
        }

        public void setFragmentLifecycleAdapter(FragmentLifecycleAdapter fragmentLifecycleAdapter) {
            this.rFragmentAdapter = new WeakReference<>(fragmentLifecycleAdapter);
        }

        @Override
        public void run() {
            mEventList.remove(this);
            Runnable r = rRunnable.get();
            if (r != null) r.run();
        }

        public void clear() {
            if (this.rActivityAdapter != null) {
                ActivityLifecycleAdapter adapter = rActivityAdapter.get();
                if (adapter != null) {
                    adapter.clear(adapter.rActivity.get());
                }
            } else if (this.rFragmentAdapter != null) {
                FragmentLifecycleAdapter adapter = rFragmentAdapter.get();
                if (adapter != null) {
                    adapter.clear(null);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;
            Event event = (Event) o;
            return rRunnable.get() == event.rRunnable.get();
        }

        @Override
        public int hashCode() {
            Runnable r = rRunnable.get();
            if (r != null) return r.hashCode();
            return super.hashCode();
        }
    }
}