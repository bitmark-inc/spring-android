package com.bitmark.fbm.util.view.stickyheader.util;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Interface for getting the orientation of a RecyclerView from its LayoutManager
 */
public interface OrientationProvider {

    public int getOrientation(RecyclerView recyclerView);

    public boolean isReverseLayout(RecyclerView recyclerView);
}
