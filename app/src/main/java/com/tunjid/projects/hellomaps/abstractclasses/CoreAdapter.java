package com.tunjid.projects.hellomaps.abstractclasses;

import android.support.v7.widget.RecyclerView;

/**
 * Class for adapters that load data.
 */
@SuppressWarnings("unused")
public abstract class CoreAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected boolean loadingData = true;
    protected boolean onDeconstructViewCalled = false;
    protected AdapterListener adapterListener;

    /**
     * Used for adapters whose size depends on more than just the backing data set size
     *
     * @param recyclerViewPosition the position specified by {@link #getItemViewType(int)}
     * @return the recyclerView position of the model
     */
    public int getModelPosition(int recyclerViewPosition) {
        return recyclerViewPosition;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        adapterListener = null;
    }

    public void setLoadingData(boolean loadingData) {
        this.loadingData = loadingData;
        notifyDataSetChanged();
    }

    public void onDeconstructView() {
        onDeconstructViewCalled = true;
        notifyDataSetChanged();
    }

    public void refreshData() {
        loadingData = false;
        onDeconstructViewCalled = false;
        notifyDataSetChanged();
    }

    protected AdapterListener getAdapterListener() {
        return adapterListener;
    }

    public boolean isLoadingData() {
        return loadingData;
    }

    public interface AdapterListener {

    }
}
