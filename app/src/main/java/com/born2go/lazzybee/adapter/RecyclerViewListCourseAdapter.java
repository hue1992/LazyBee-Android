package com.born2go.lazzybee.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.db.Course;

import java.util.List;

/**
 * Created by Hue on 7/2/2015.
 */
public class RecyclerViewListCourseAdapter extends RecyclerView.Adapter<RecyclerViewListCourseAdapter.RecyclerViewListCourseAdapterViewHoler> {
    private static final String TAG = "ListCourseAdapter";
    List<Course> objectList;

    public RecyclerViewListCourseAdapter(List<Course> objectList) {
        this.objectList = objectList;
    }

    @Override
    public RecyclerViewListCourseAdapterViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_coures, parent, false); //Inflating the layout
        //init viewholder
        RecyclerViewListCourseAdapterViewHoler recyclerViewListCourseAdapterViewHoler = new RecyclerViewListCourseAdapterViewHoler(view);
        return recyclerViewListCourseAdapterViewHoler;
    }

    @Override
    public void onBindViewHolder(RecyclerViewListCourseAdapterViewHoler holder, int position) {
        //get view form holder
        View view = holder.view;
        //init lbNameCourse
        TextView lbNameCourse = (TextView) view.findViewById(R.id.lbNameCourse);
        //get course form list by position
        Course course = objectList.get(position);
        //set data
        lbNameCourse.setText(course.getName());
        //setBackgroundColor
        if (position % 2 == 0) {
            view.setBackgroundColor(R.color.material_deep_teal_500);
        } else {

        }


    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public class RecyclerViewListCourseAdapterViewHoler extends RecyclerView.ViewHolder {
        private View view;

        public RecyclerViewListCourseAdapterViewHoler(View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }
}
