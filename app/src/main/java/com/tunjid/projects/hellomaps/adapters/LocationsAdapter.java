package com.tunjid.projects.hellomaps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.abstractclasses.CoreAdapter;
import com.tunjid.projects.hellomaps.model.pojo.Location;
import com.tunjid.projects.hellomaps.services.HelloApi;

import java.util.ArrayList;

/**
 * Adapter for holding the list gotten off the Locations Endpoint
 */
public class LocationsAdapter extends CoreAdapter<LocationsAdapter.ViewHolder> {

    private static final int LOADING_DATA = 0;
    private static final int LOCATIONS = 2;
    private static final int NO_DATA = 3;

    ArrayList<Location> locations;

    /**
     * Default constructor
     */

    public LocationsAdapter(ArrayList<Location> locations) {
        setHasStableIds(true);
        this.locations = locations;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        View itemView;

        switch (viewType) {
            case LOADING_DATA:   // Load a progress bar
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_loading, viewGroup, false);
                break;
            default:            // Load a cardview
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_locations_row, viewGroup, false);
                break;
        }

        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int recyclerViewPosition) {

        // Switch depending on the kind of View
        switch (getItemViewType(recyclerViewPosition)) {
            case LOADING_DATA:
                // Do nothing
                break;
            case LOCATIONS:

                final Location location = locations.get(recyclerViewPosition);

                viewHolder.officeName.setText(location.getName() != null ? location.getName() : "");
                viewHolder.officeAddress.setText(location.getAddress() != null ? location.getAddress() : "");
                viewHolder.distance.setText(location.formattedDistanceFrom(HelloApi.getUserLastLocation()));

                viewHolder.viewHolderListener = new ViewHolder.ViewHolderListener() {
                    @Override
                    public void onLocationClicked() {
                        getAdapterListener().onLocationClicked(location);
                    }

                };
                break;
            case NO_DATA:
                viewHolder.officeName.setText(viewHolder.officeName.getContext().getString(R.string.network_error));
                viewHolder.officeAddress.setText(viewHolder.officeAddress.getContext().getString(R.string.unable_to_retrieve));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return loadingData
                ? LOADING_DATA
                : locations.size() == 0
                ? NO_DATA
                : LOCATIONS;
    }

    @Override
    public int getItemCount() {
        return loadingData
                ? 1
                : locations.size() == 0
                ? 1
                : locations.size();
    }

    @Override
    public long getItemId(int position) {
        return loadingData
                ? LOADING_DATA
                : locations.size() == 0
                ? NO_DATA
                : locations.get(position).hashCode();
    }

    @Override
    protected AdapterListener getAdapterListener() {
        return (AdapterListener) super.getAdapterListener();
    }

    // ViewHolder for actual content
    public final static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public int viewType;        // Used to specify the view type.

        public TextView officeName;
        public TextView officeAddress;
        public TextView distance;

        public ViewHolderListener viewHolderListener;


        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            viewType = ViewType;

            switch (ViewType) {
                case LOCATIONS:
                case NO_DATA:
                    officeName = (TextView) itemView.findViewById(R.id.office_name);
                    officeAddress = (TextView) itemView.findViewById(R.id.office_address);
                    distance = (TextView) itemView.findViewById(R.id.distance);
                    if (viewType == LOCATIONS) {
                        itemView.setOnClickListener(this); // Set listener for the whole layout
                    }
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch (viewType) {
                case LOCATIONS:
                    viewHolderListener.onLocationClicked();
                    break;
            }
        }

        public interface ViewHolderListener {
            void onLocationClicked();
        }

    }

    /**
     * Set the adapterListener to be notified when an item has been clicked.
     */
    public void setAdapterListener(AdapterListener listener) {
        this.adapterListener = listener;
    }

    public interface AdapterListener extends CoreAdapter.AdapterListener {
        void onLocationClicked(Location location);
    }
}
