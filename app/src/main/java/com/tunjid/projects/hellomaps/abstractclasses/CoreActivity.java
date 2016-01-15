package com.tunjid.projects.hellomaps.abstractclasses;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.customcomponents.FloatingActionButton;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.services.HelloApi;

/**
 * Abstract Activty that holds the basic components that all activities use.
 */
public abstract class CoreActivity extends AppCompatActivity
        implements
        ServiceConnection {

    private boolean wasStopped = false;
    protected boolean isSavedInstance = false;

    protected CharSequence toolbarTitle;
    protected String currentFragment = "";

    protected Toolbar toolbar;
    protected FloatingActionButton fab;
    protected ProgressBar progressBar;
    protected RelativeLayout mainContent;

    protected HelloApi api;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        // Overriden in child subclasses
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        // Overidden in child subclasses
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isSavedInstance = true;
            wasStopped = savedInstanceState.getBoolean(Utils.WAS_STOPPED);
            if (savedInstanceState.containsKey(Utils.TOOLBAR_TITLE)) {
                toolbarTitle = savedInstanceState.getString(Utils.TOOLBAR_TITLE);
            }
        }
        else {
            isSavedInstance = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasStopped = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Utils.WAS_STOPPED, wasStopped);
        if (toolbarTitle != null) {
            outState.putString(Utils.TOOLBAR_TITLE, toolbarTitle.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (api != null) {
            api = null;
        }

        try {
            unbindService(this);
        }
        catch (Exception e) { // Service might not be bound
            e.printStackTrace();
        }
    }

    /**
     * Gets the ToolBar used as the toolbar in this activity
     */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the FAB used in this activity
     */
    public FloatingActionButton getFAB() {
        return fab;
    }

    /**
     * Gets the bound service that is the Api instance
     */
    public HelloApi getApi() {
        return api;
    }

    @Nullable
    public CoreFragment getFragment(String tag) {
        Fragment coreFragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (coreFragment instanceof CoreFragment) {
            return (CoreFragment) coreFragment;
        }
        return null;
    }

    @Nullable
    public CoreFragment getCurrentFragment() {
        Fragment coreFragment = getSupportFragmentManager().findFragmentByTag(currentFragment);

        if (coreFragment instanceof CoreFragment) {
            return (CoreFragment) coreFragment;
        }
        return null;
    }

    public void setToolbarTitle(CharSequence toolbarTitle) {
        this.toolbarTitle = toolbarTitle;
        restoreActionBar();
    }

    public boolean wasStopped() {
        return wasStopped;
    }

    /**
     * Call back within an activity to perform certain actions after
     * a fragment view has been notified that it's parent activity exists
     *
     * @param fragmentArgs The arguments passed to the fragment
     */
    public void afterActivityCreated(final Bundle fragmentArgs) {
        String fragmentTag = fragmentArgs.getString(Utils.FRAGMENT_TAG);
        if (fragmentTag == null) {
            throw new IllegalArgumentException("Fragment tag may not be null");
        }
        this.currentFragment = fragmentTag;
    }

    /**
     * Refreshes the toolbar title
     */
    protected void restoreActionBar() {
        if (toolbar != null) {
            toolbar.setTitle(toolbarTitle);
        }
    }

    /**
     * Method used for creating a default animated {@link FragmentTransaction}
     * that slides up. Used for photos and restoring from instance state.
     *
     * @return an uncommited {@link FragmentTransaction} to build upon
     */
    @SuppressLint("CommitTransaction")
    public FragmentTransaction slideUpTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out,
                        R.anim.abc_fade_in, R.anim.abc_slide_out_bottom);
    }

    /**
     * Method used for creating a default animated {@link FragmentTransaction}
     *
     * @return an uncommited {@link FragmentTransaction} to build upon
     */
    @SuppressLint("CommitTransaction")
    public FragmentTransaction defaultTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.abc_fade_out,
                        R.anim.abc_fade_in, R.anim.slide_out_left);
    }
}
