package com.tunjid.projects.hellomaps.abstractclasses;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.interfaces.Observation;
import com.tunjid.projects.hellomaps.services.HelloApi;

import rx.Observer;
import rx.Subscription;

/**
 * A simple {@link Fragment} subclass holding boiler plate for oft used methods and fields.
 */
public abstract class CoreFragment extends Fragment
        implements Observer<Observation> {

    public Subscription apiSubscription;
    protected AlertDialog errorAlertDialog;

    public CoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_STOPPED, false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_PAUSED, false);
            args.putBoolean(Utils.WAS_STOPPED, false);
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, false);
        }
    }

    @Override
    public void onPause() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_PAUSED, true);
        }

        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (viewDeconstructed()) {
            reconstructView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_STOPPED, true);
        }
        deconstructView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (apiSubscription != null) {
            apiSubscription.unsubscribe();
        }

        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DESTROYED, true);
        }

        if (!viewDeconstructed()) {
            deconstructView();
        }

    }

    /**
     * Method used to clean up resources when the fragment is stopped
     */
    public void deconstructView() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, true);
        }
    }

    /**
     * Method used to restore Fragment
     */
    public void reconstructView() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, false);
        }
    }

    /**
     * Convenience callback after a fragment calls {@link  #onActivityCreated(Bundle)}
     */
    public void callAfterActivityCreated() {
        ((CoreActivity) getActivity()).afterActivityCreated(
                getArguments());
    }

    @Override
    public void onCompleted() {
        // Handled in subclass
    }

    @Override
    public void onError(Throwable e) {
        // Handled in subclass
    }

    @Override
    public void onNext(Observation observation) {
        // Handled in subclass
    }

    public boolean wasStopped() {
        return getArguments().containsKey(Utils.WAS_STOPPED)
                && getArguments().getBoolean(Utils.WAS_STOPPED);
    }

    /**
     * Used to check if a fragment called the {@link #deconstructView()} method
     *
     * @return Whether the view was destroyed
     */
    public boolean viewDeconstructed() {
        return getArguments().containsKey(Utils.VIEW_DECONSTRUCTED)
                && getArguments().getBoolean(Utils.VIEW_DECONSTRUCTED);
    }

    /**
     * Used to check if a fragment is being returned from the backstack after it's view was destroyed
     *
     * @return Whether the view was destroyed
     */
    public boolean viewDestroyed() {
        return getArguments().containsKey(Utils.VIEW_DESTROYED)
                && getArguments().getBoolean(Utils.VIEW_DESTROYED);
    }

    public void showErrorDialog() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorAlertDialog == null || !errorAlertDialog.isShowing()) {

                    try {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                        builder.setTitle(R.string.network_error);
                        builder.setMessage(getString(R.string.unable_to_retrieve));

                        errorAlertDialog = builder.create();

                        errorAlertDialog.show();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public HelloApi getApi() {
        return ((CoreActivity) getActivity()).getApi();
    }

}
