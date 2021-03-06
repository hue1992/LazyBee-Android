package com.born2go.lazzybee.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.gtools.LazzyBeeSingleton;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.born2go.lazzybee.view.dialog.DialogSetTimeShowAnswer;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hue on 8/24/2015.
 */
public class RecyclerViewCustomStudyAdapter extends
        RecyclerView.Adapter<RecyclerViewCustomStudyAdapter.RecyclerViewCustomStudyAdapterViewHolder> {
    private static final String TAG = "CustomStudyAdapter";
    Context context;
    List<String> customStudys;
    LearnApiImplements learnApiImplements;
    //    Dialog main;
    private static final int TYPE_TITLE_0 = 0;
    private static final int TYPE_SETTING_NAME_1 = 1;
    private static final int TYPE_SETTING_MEANING_2 = 2;
    private static final int TYPE_SETTING_AUTO_PLAY_SOUND_3 = 3;
    private static final int TYPE_LINE = -1;
    RecyclerView recyclerView;
    FragmentManager supportFragmentManager;
    RecyclerViewCustomStudyAdapter adapter;

    public RecyclerViewCustomStudyAdapter(FragmentManager supportFragmentManager, Context context, List<String> customStudys, RecyclerView recyclerView) {
        this.supportFragmentManager = supportFragmentManager;
        this.context = context;
        this.customStudys = customStudys;
        this.learnApiImplements = LazzyBeeSingleton.learnApiImplements;
//        this.main = dialog;
        this.recyclerView = recyclerView;
        this.adapter = this;
    }

    public void _reloadRecylerView() {
        recyclerView.setAdapter(this);
    }


    @Override
    public RecyclerViewCustomStudyAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_TITLE_0 || viewType == TYPE_SETTING_NAME_1 || viewType == TYPE_SETTING_AUTO_PLAY_SOUND_3) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_settings, parent, false); //Inflating the layout
        } else if (viewType == TYPE_LINE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_setting_line_child, parent, false); //Inflating the layout
        } else if (viewType == TYPE_SETTING_MEANING_2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_custom_study_display_meaning, parent, false); //Inflating the layout
        }
        RecyclerViewCustomStudyAdapterViewHolder recyclerViewCustomStudyAdapterViewHolder = new RecyclerViewCustomStudyAdapterViewHolder(view, viewType);
        return recyclerViewCustomStudyAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewCustomStudyAdapterViewHolder holder, int position) {
        View view = holder.view;
        RelativeLayout mCardView = (RelativeLayout) view.findViewById(R.id.mCardView);

        TextView lbSettingName = (TextView) view.findViewById(R.id.lbSettingName);
        String setting = customStudys.get(position);
        final Switch mSwitch = (Switch) view.findViewById(R.id.mSwitch);
        TextView lbLimit = (TextView) view.findViewById(R.id.lbLimit);
        try {
            if (holder.viewType == TYPE_TITLE_0) {
                lbSettingName.setText(customStudys.get(position));
                mSwitch.setVisibility(View.GONE);
                // mCardView.setRadius(0f);
                lbLimit.setVisibility(View.GONE);
                lbSettingName.setTextSize(15f);
                lbSettingName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            } else if (holder.viewType == TYPE_SETTING_NAME_1) {
                lbSettingName.setText(customStudys.get(position));
                // mCardView.setRadius(0f);
                mSwitch.setVisibility(View.GONE);
                if (setting.equals(context.getString(R.string.setting_today_new_card_limit))) {
                    String limit = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT);
                    lbLimit.setTag(limit);
                    getSettingLimitOrUpdate(mCardView, lbLimit, LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT, limit);
                } else if (setting.equals(context.getString(R.string.setting_total_learn_per_day))) {
                    String limit = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT);
                    lbLimit.setTag(limit);
                    getSettingLimitOrUpdate(mCardView, lbLimit, LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT, limit);

                } else if (setting.equals(context.getString(R.string.setting_max_learn_more_per_day))) {
                    String limit = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT);
                    lbLimit.setTag(limit);
                    getSettingLimitOrUpdate(mCardView, lbLimit, LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT, limit);

                } else if (setting.equals(context.getString(R.string.setting_time_deday_show_answer))) {
                    int timeSet = learnApiImplements.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_TIME_SHOW_ANSWER);
                    lbLimit.setText(((timeSet == -1) ? context.getString(R.string.show_answer_now) : timeSet + "s"));
                    _defineSetTimeShowAnswer(mCardView, timeSet);
                } else if (setting.equals(context.getString(R.string.setting_reset_to_default))) {
                    lbLimit.setVisibility(View.GONE);
                    mCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _showConfirmResetToDefauls();
                        }
                    });

                } else if (setting.equals(context.getString(R.string.setting_my_level))) {
                    getLevelandShowDialogChangeLevel(mCardView, lbLimit);
                } else if (setting.equals(context.getString(R.string.setting_position_meaning))) {
                    // _showDialogSetPositionMeaning(mCardView, lbLimit);
                }
            } else if (holder.viewType == TYPE_SETTING_MEANING_2) {
                RelativeLayout mSetPositionMeaning = (RelativeLayout) view.findViewById(R.id.mSetPositionMeaning);
                _getSettingDisplayMeaningAndUpdateWithSwitch(mCardView, mSetPositionMeaning);

            } else if (holder.viewType == TYPE_SETTING_AUTO_PLAY_SOUND_3) {
                lbSettingName.setText(customStudys.get(position));
                mSwitch.setVisibility(View.VISIBLE);
                // mCardView.setRadius(0f);
                lbLimit.setVisibility(View.GONE);
                lbSettingName.setTextSize(15f);

                String mAutoPlaySound = LazzyBeeSingleton.learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_AUTO_PLAY_SOUND);
                if (mAutoPlaySound == null) {
                    mSwitch.setChecked(true);
                } else if(mAutoPlaySound.equals(LazzyBeeShare.ON)) {
                    mSwitch.setChecked(true);
                }else if(mAutoPlaySound.equals(LazzyBeeShare.OFF)) {
                    mSwitch.setChecked(false);
                }else {
                    mSwitch.setChecked(false);
                }



                mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String value;
                        if (isChecked) {
                            value = LazzyBeeShare.ON;
                        } else {
                            value = LazzyBeeShare.OFF;
                        }
                        learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_AUTO_PLAY_SOUND, value);
                    }
                });
            }
            //_reloadRecylerView();
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, "onBindViewHolder", e);
        }
    }

    private void _getSettingDisplayMeaningAndUpdateWithSwitch(RelativeLayout mCardView, final RelativeLayout mSetPositionMeaning) {
        final Switch mSwitch = (Switch) mCardView.findViewById(R.id.mSwitch);

        //Define value
        String value = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_DISPLAY_MEANING);
        if (value == null) {
            mSwitch.setChecked(true);
            mSetPositionMeaning.setVisibility(View.VISIBLE);
        } else if (value.equals(LazzyBeeShare.ON)) {
            mSetPositionMeaning.setVisibility(View.VISIBLE);
            mSwitch.setChecked(true);
        } else if (value.equals(LazzyBeeShare.OFF)) {
            mSetPositionMeaning.setVisibility(View.GONE);
            mSwitch.setChecked(false);
        } else {
            mSwitch.setChecked(false);
            mSetPositionMeaning.setVisibility(View.GONE);
        }

        //Hander switch
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String value;
                if (isChecked) {
                    value = LazzyBeeShare.ON;
                    mSetPositionMeaning.setVisibility(View.VISIBLE);
                } else {
                    value = LazzyBeeShare.OFF;
                    mSetPositionMeaning.setVisibility(View.GONE);

                }
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_DISPLAY_MEANING, value);
            }
        });

        int index = 0;

        TextView lbPositionMeaning = (TextView) mSetPositionMeaning.findViewById(R.id.lbPositionMeaning);
        String htmlPosition = "<font color='" + context.getResources().getColor(R.color.grey_600) + " '> " + context.getString(R.string.setting_position_meaning) + " </font> : " +
                "<font color='" + context.getResources().getColor(R.color.position_meaning_display_text_color) + " '> " + context.getString(R.string.position_meaning_up) + " </font>";


        String position_meaing = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_POSITION_MEANIG);
        if (position_meaing != null && position_meaing.equals(LazzyBeeShare.DOWN)) {
            index = 1;
            htmlPosition = "<font color='" + context.getResources().getColor(R.color.grey_600) + " '> " + context.getString(R.string.setting_position_meaning) + " </font> : " +
                    "<font color='" + context.getResources().getColor(R.color.position_meaning_display_text_color) + " '> " + context.getString(R.string.position_meaning_down) + " </font>";
        }
        lbPositionMeaning.setText(Html.fromHtml(htmlPosition));
        final int finalIndex = index;


        View.OnClickListener setPositionMeaning = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showDialogSetPositionMeaning(finalIndex);
            }
        };
        mSetPositionMeaning.setOnClickListener(setPositionMeaning);
        lbPositionMeaning.setOnClickListener(setPositionMeaning);
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitch.isChecked())
                    mSwitch.setChecked(false);
                else
                    mSwitch.setChecked(true);
            }
        });

    }

    private void _defineSetTimeShowAnswer(RelativeLayout mCardView, final int timeSet) {
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogSetTimeShowAnswer dialogSetTimeShowAnswer = new DialogSetTimeShowAnswer(adapter, context, timeSet);
                dialogSetTimeShowAnswer.show(supportFragmentManager, DialogSetTimeShowAnswer.TAG);
            }
        });

    }

    private void getLevelandShowDialogChangeLevel(RelativeLayout mCardView, TextView lbLimit) {

        String strlevel = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_MY_LEVEL);
        int level;
        if (strlevel == null) {
            level = LazzyBeeShare.DEFAULT_MY_LEVEL;
        } else {
            level = Integer.valueOf(strlevel);
        }
        lbLimit.setText(String.valueOf(level));

        //handel oncllick
        final int finalLevel = level;
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showDialogSelectLevel(finalLevel);
            }
        });
    }

    private void _showDialogSelectLevel(int finalLevel) {

        final String[] strlevels = {"1", "2", "3", "4", "5", "6"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogLearnMore);
        builder.setTitle(context.getString(R.string.dialog_title_change_my_level));

        builder.setSingleChoiceItems(strlevels, (finalLevel == 1) ? 0 : finalLevel - 1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
                firebaseAnalytics.setUserProperty("Selected_level", String.valueOf(strlevels[item]));
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_MY_LEVEL, strlevels[item]);
                learnApiImplements._initIncomingCardIdListbyLevel(Integer.valueOf(strlevels[item]));
                dialog.dismiss();
                _reloadRecylerView();

            }
        });
        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void _showConfirmResetToDefauls() {
        // Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogLearnMore);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_title_reset_custom_study).setMessage(R.string.dialog_message_reset_custom_study);

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT
                //KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT
                //KEY_SETTING_TODAY_NEW_CARD_LIMIT
                // learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT, String.valueOf(LazzyBeeShare.DEFAULT_MAX_LEARN_MORE_PER_DAY));
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT, String.valueOf(LazzyBeeShare.DEFAULT_TOTAL_LEAN_PER_DAY));
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT, String.valueOf(LazzyBeeShare.DEFAULT_MAX_NEW_LEARN_PER_DAY));
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_MY_LEVEL, String.valueOf(LazzyBeeShare.DEFAULT_MY_LEVEL));
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_TIME_SHOW_ANSWER, String.valueOf(LazzyBeeShare.DEFAULT_TIME_SHOW_ANSWER));
//                studyInferface._finishCustomStudy();
                _reloadRecylerView();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return customStudys.size();
    }

    @Override
    public int getItemViewType(int position) {
        String setting = customStudys.get(position);
        if (setting.equals(context.getString(R.string.custom_study)))
            return TYPE_TITLE_0;
        else if (setting.equals(context.getString(R.string.setting_today_new_card_limit))
                || setting.equals(context.getString(R.string.setting_total_learn_per_day))
                || setting.equals(context.getString(R.string.setting_reset_to_default))
                || setting.equals(context.getString(R.string.setting_my_level))
                || setting.equals(context.getString(R.string.setting_position_meaning))
                || setting.equals(context.getString(R.string.setting_time_deday_show_answer))
                || setting.equals(context.getString(R.string.setting_max_learn_more_per_day)))
            return TYPE_SETTING_NAME_1;
        else if (setting.equals(context.getString(R.string.setting_display_meaning)))
            return TYPE_SETTING_MEANING_2;
        else if (setting.equals(context.getString(R.string.setting_auto_play_sound)))
            return TYPE_SETTING_AUTO_PLAY_SOUND_3;

        else
            return -1;
    }

    public class RecyclerViewCustomStudyAdapterViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private int viewType;

        public RecyclerViewCustomStudyAdapterViewHolder(View itemView, int viewType) {
            super(itemView);
            this.view = itemView;
            this.viewType = viewType;
        }
    }

    private void getSettingLimitOrUpdate(View mCardView, final TextView lbLimit, final String key, String limit) {
        // lbLimit.setVisibility(View.VISIBLE);
        int value = 0;
        if (limit == null) {
            if (key.equals(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT)) {
                value = LazzyBeeShare.DEFAULT_MAX_NEW_LEARN_PER_DAY;
            } else if (key.equals(LazzyBeeShare.KEY_SETTING_TODAY_REVIEW_CARD_LIMIT)) {
                value = LazzyBeeShare.MAX_REVIEW_LEARN_PER_DAY;
            } else if (key.equals(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT)) {
                value = LazzyBeeShare.DEFAULT_TOTAL_LEAN_PER_DAY;
            } else if (key.equals(LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT)) {
                value = LazzyBeeShare.DEFAULT_MAX_LEARN_MORE_PER_DAY;
            }

        } else {
            value = Integer.valueOf(limit);
        }
        lbLimit.setText(context.getString(R.string.setting_limit_card_number, value));
        final int finalValue = value;
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _showDialogConfirmSetLimitCard(key, finalValue);
            }
        });

    }

    private void _showDialogConfirmSetLimitCard(final String key, final int value) {
        // Instantiate an AlertDialog.Builder with its constructor
        String title = LazzyBeeShare.EMPTY;
        String message = LazzyBeeShare.EMPTY;
        View viewDialog = View.inflate(context, R.layout.dialog_limit_card, null);
        TextView lbSettingLimitName = (TextView) viewDialog.findViewById(R.id.lbSettingLimitName);
        final TextView lbEror = (TextView) viewDialog.findViewById(R.id.lbEror);
        final EditText txtLimit = (EditText) viewDialog.findViewById(R.id.txtLimit);

        if (key == LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT) {
            message = context.getString(R.string.dialog_message_setting_today_new_card_limit_by);
        } else if (key == LazzyBeeShare.KEY_SETTING_TODAY_REVIEW_CARD_LIMIT) {
            message = context.getString(R.string.dialog_message_setting_today_review_card_limit_by);
        } else if (key == LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT) {
            message = context.getString(R.string.dialog_message_setting_total_card_learn_pre_day_by);
        } else if (key == LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT) {
            message = context.getString(R.string.dialog_message_setting_max_learn_more_per_day_by);
        }

        lbSettingLimitName.setText(message);
        txtLimit.setText(String.valueOf(value));
        txtLimit.setFocusableInTouchMode(true);
        txtLimit.setFocusable(true);
        txtLimit.requestFocus();

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogLearnMore);

        // Chain together various setter methods to set the dialog characteristics
        // builder.setMessage(R.string.dialog_message_setting_today_new_card_limit_by);
        builder.setView(viewDialog);

        // Add the buttons
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
                //main.hide();
            }
        });
        builder.setPositiveButton(R.string.ok, null);

        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog1) {

                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try {
                            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);

                            String limit = LazzyBeeShare.EMPTY;
                            if (key == LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT) {
                                String erorr_message = LazzyBeeShare.EMPTY;
                                if (txtLimit.getText().toString() == null) {
                                    erorr_message = context.getString(R.string.custom_study_error_input_value);
                                    lbEror.setText(erorr_message);
                                } else {
                                    limit = txtLimit.getText().toString();
                                    Log.e(TAG, LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT + ":" + limit);
                                    int total = learnApiImplements._getCustomStudySetting(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT);
                                    if (Integer.valueOf(limit) > total) {
                                        erorr_message = context.getString(R.string.custom_study_eror_limit) + " < " + context.getString(R.string.setting_total_learn_per_day) + "(" + context.getString(R.string.setting_limit_card_number, total) + ")";
                                        Log.e(TAG, erorr_message);
                                        lbEror.setText(erorr_message);
                                    } else if (Integer.valueOf(limit) < total && Integer.valueOf(limit) > LazzyBeeShare.MAX_NEW_PRE_DAY) {
                                        erorr_message = context.getString(R.string.custom_study_eror_limit) + " < (" + context.getString(R.string.setting_limit_card_number, LazzyBeeShare.MAX_NEW_PRE_DAY) + ")";
                                        Log.e(TAG, erorr_message);
                                        lbEror.setText(erorr_message);
                                    } else {
                                        firebaseAnalytics.setUserProperty("Daily_new_word", String.valueOf(limit));
                                        Bundle bundle = new Bundle();
                                        bundle.putString("count", limit);
                                        firebaseAnalytics.logEvent("Daily_total_word", bundle);


                                        learnApiImplements._insertOrUpdateToSystemTable(key, limit);
                                        _resetQueueList(limit);
                                        dialog.dismiss();
                                        _reloadRecylerView();
                                    }
                                }
                            } else if (key == LazzyBeeShare.KEY_SETTING_TODAY_REVIEW_CARD_LIMIT) {

                            } else if (key == LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT) {
                                String erorr_message = LazzyBeeShare.EMPTY;
                                if (txtLimit.getText().toString() == null) {
                                    erorr_message = context.getString(R.string.custom_study_error_input_value);
                                    lbEror.setText(erorr_message);
                                } else {
                                    limit = txtLimit.getText().toString();
                                    Log.e(TAG, LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT + ":" + limit);
                                    int total = learnApiImplements._getCustomStudySetting(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT);
                                    if (Integer.valueOf(limit) < total) {
                                        erorr_message = context.getString(R.string.custom_study_eror_limit) + " > " + context.getString(R.string.setting_today_new_card_limit) + "(" + context.getString(R.string.setting_limit_card_number, total) + ")";
                                        Log.e(TAG, erorr_message);
                                        lbEror.setText(erorr_message);
                                    } else {
                                        learnApiImplements._insertOrUpdateToSystemTable(key, limit);
                                        //main.hide();
                                        dialog.dismiss();
                                        Log.e(TAG, "Update 2");
//                                studyInferface._finishCustomStudy();
                                        _reloadRecylerView();
                                    }
                                }
                            } else if (key == LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT) {
                                String erorr_message = LazzyBeeShare.EMPTY;
                                if (txtLimit.getText().toString() == null) {
                                    erorr_message = context.getString(R.string.custom_study_error_input_value);
                                    lbEror.setText(erorr_message);
                                } else {
                                    limit = txtLimit.getText().toString();
                                    Log.e(TAG, LazzyBeeShare.KEY_SETTING_TODAY_LEARN_MORE_PER_DAY_LIMIT + ":" + limit);
                                    int total = learnApiImplements._getCustomStudySetting(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT);
                                    if (Integer.valueOf(limit) > total) {
                                        erorr_message = context.getString(R.string.custom_study_eror_limit) + " < " + context.getString(R.string.setting_today_new_card_limit) + "(" + context.getString(R.string.setting_limit_card_number, total) + ")";
                                        Log.e(TAG, erorr_message);
                                        lbEror.setText(erorr_message);
                                    } else {
                                        firebaseAnalytics.setUserProperty("Daily_total_word", String.valueOf(limit));
                                        Bundle bundle = new Bundle();
                                        bundle.putString("count", limit);
                                        firebaseAnalytics.logEvent("Daily_total_word", bundle);
                                        learnApiImplements._insertOrUpdateToSystemTable(key, limit);
                                        // main.hide();
                                        dialog.dismiss();
                                        Log.e(TAG, "Update 3");
//                                studyInferface._finishCustomStudy();
                                        _reloadRecylerView();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LazzyBeeShare.showErrorOccurred(context, "on Click set Limit", e);
                            LazzyBeeSingleton.getCrashlytics().logException(e);
                        }
                    }
                });
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    private void _resetQueueList(String limit) {
        String queueListstr = learnApiImplements._getValueFromSystemByKey(LazzyBeeShare.QUEUE_LIST);
        if (queueListstr != null) {
            //Get Card list id form system tabele
            List<String> cardListId = learnApiImplements._getListCardIdFromStringArray(queueListstr);
            int limitNew = Integer.valueOf(limit);
            List<String> cardListLimit = new ArrayList<String>(limitNew);
            if (cardListId.size() > limitNew) {
                for (int i = 0; i < limitNew; i++) {
                    cardListLimit.add(cardListId.get(i));
                }
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.QUEUE_LIST, learnApiImplements._listCardTodayToArrayListCardId(null, cardListLimit));
            }
        }
    }

    private void _showDialogSetPositionMeaning(int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogLearnMore);
        builder.setTitle(context.getString(R.string.title_change_position_meaning));
        final CharSequence[] items = {context.getString(R.string.position_meaning_up), context.getString(R.string.position_meaning_down)};
        final CharSequence[] values = {LazzyBeeShare.UP, LazzyBeeShare.DOWN};


        builder.setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                //Update position
                learnApiImplements._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_POSITION_MEANIG, values[item].toString());
                dialog.cancel();
                _reloadRecylerView();

            }
        });
        //builder.setView(viewDialog);
        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}
