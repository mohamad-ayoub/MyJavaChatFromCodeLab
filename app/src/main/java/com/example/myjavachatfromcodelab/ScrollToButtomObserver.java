package com.example.myjavachatfromcodelab;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScrollToButtomObserver extends RecyclerView.AdapterDataObserver {
    private RecyclerView recycler;
    private MessageAdapter adapter;
    private LinearLayoutManager manager;

    public ScrollToButtomObserver(RecyclerView recycler, MessageAdapter adapter, LinearLayoutManager manager) {
        this.recycler=recycler;
        this.adapter=adapter;
        this.manager=manager;
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);

        int count = adapter.getItemCount();
        int lastVisiblePosition = manager.findLastVisibleItemPosition();

        boolean loading = lastVisiblePosition == -1;
        boolean atBottom = positionStart >= count - 1 && lastVisiblePosition == positionStart - 1;
        if (loading || atBottom) {
            recycler.scrollToPosition(positionStart);
        }
    }
}
