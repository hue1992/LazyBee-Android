package com.born2go.lazzybee.event;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Hue on 7/2/2015.
 */

public class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {
    private String TAG = "RecylerTouchListener";
    GestureDetector gestureDetector;
    private OnItemClickListener onItemClickListener;

    public RecyclerViewTouchListener(Context context, final RecyclerView recyclerView, final OnItemClickListener onItemClickListener) {
        Log.d(TAG, "");
        this.onItemClickListener = onItemClickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp" + e);
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && onItemClickListener != null) {
                    onItemClickListener.onItemClick(child, recyclerView.getChildPosition(child));
                }
                Log.d(TAG, "onLongPress" + e);
                return true;
            }

//                @Override
//                public void onLongPress(MotionEvent e) {
////                    super.onLongPress(e);
//
//                }
        });

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());

        if (child != null && onItemClickListener != null && gestureDetector.onTouchEvent(e)) {
            onItemClickListener.onItemClick(child, rv.getChildPosition(child));
        }

        Log.d(TAG, "onInterceptTouchEvent" + e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onTouchEvent" + e);
    }

    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
}
