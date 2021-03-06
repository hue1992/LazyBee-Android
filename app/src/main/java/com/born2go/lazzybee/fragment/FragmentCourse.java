package com.born2go.lazzybee.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.activity.StudyActivity;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.shared.LazzyBeeShare;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCourse extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String COURSE_ID = "courseId";
    public static final String TAG = "FragmentCourse";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LearnApiImplements dataBaseHelper;

    CardView mCardViewStudy;

    TextView lbNameCourse;

    Button btnStudy, btnCustomStudy;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentCourse.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCourse newInstance(String param1, String param2) {
        FragmentCourse fragment = new FragmentCourse();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentCourse() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);

        _intInterfaceView(view);

        _initDatabase();


        //Update DEFAULT_MAX_NEW_LEARN_PER_DAY
        //dataBaseHelper._insertOrUpdateToSystemTable("DEFAULT_MAX_NEW_LEARN_PER_DAY", LazzyBeeShare.convertJsonObjMaxLearnPerDayToString((10)));
        _checkListTodayExit();


        btnStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _checkListTodayExit();
                Log.i(TAG, "btnStudy:" + LazzyBeeShare.LEARN_MORE + ":" + btnStudy.getTag());
                if (btnStudy.getTag() != null) {
                    Intent intent = new Intent(getActivity(), StudyActivity.class);
                    //intent.putExtra(LazzyBeeShare.LEARN_MORE, /*Cast tag to boolean*/(Boolean) btnStudy.getTag());
                    getActivity().startActivityForResult(intent, 1);
                }
            }
        });
        btnCustomStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "btnCustomStudy:" + LazzyBeeShare.LEARN_MORE + ":" + btnCustomStudy.getTag());
                _checkListTodayExit();
                if (btnCustomStudy.getTag() != null) {
                    Intent intent = new Intent(getActivity(), StudyActivity.class);
                    intent.putExtra(LazzyBeeShare.LEARN_MORE, /*Cast tag to boolean*/(Boolean) btnCustomStudy.getTag());
                    getActivity().startActivityForResult(intent, 1);
                }
            }
        });


        return view;
    }

    private void _checkListTodayExit() {
        int checkTodayExit = dataBaseHelper._checkListTodayExit();
        Log.i(TAG, "checkTodayExit: " + checkTodayExit);
        if (checkTodayExit > -1) {
            //
            if (checkTodayExit > 0) {
                btnStudy.setText("Study");
                btnStudy.setTag(false);
                btnCustomStudy.setTag(false);
                Log.i(TAG, "Study");
                mCardViewStudy.setVisibility(View.VISIBLE);
            } else if (checkTodayExit == 0) {
                btnCustomStudy.setTag(true);
                btnStudy.setTag(false);
                btnStudy.setText("Complete Learn");
                Log.i(TAG, "Learn more");
                mCardViewStudy.setVisibility(View.GONE);
            }

        } else if (checkTodayExit == -1) {
            Log.i(TAG, "New");
            btnStudy.setText("Study");
            btnStudy.setTag(true);
            btnCustomStudy.setTag(false);
            Log.i(TAG, "Study");
            mCardViewStudy.setVisibility(View.VISIBLE);

        }
    }

    private void _intInterfaceView(View view) {
        mCardViewStudy = (CardView) view.findViewById(R.id.mCardViewStudy);
        btnStudy = (Button) view.findViewById(R.id.btnStudy);
        btnCustomStudy = (Button) view.findViewById(R.id.btnCustomStudy);
        lbNameCourse= (TextView) view.findViewById(R.id.lbNameCourse);
    }


    /**
     * Init db sqlite
     */
    private void _initDatabase() {
        dataBaseHelper = new LearnApiImplements(getActivity());
    }


}
