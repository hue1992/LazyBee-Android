package com.born2go.lazzybee.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.adapter.CustomViewPager;
import com.born2go.lazzybee.db.Card;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.fragment.DetailsView;
import com.born2go.lazzybee.fragment.DialogCompleteStudy;
import com.born2go.lazzybee.fragment.DialogCompleteStudy.ICompleteSutdy;
import com.born2go.lazzybee.fragment.StudyView;
import com.born2go.lazzybee.fragment.StudyView.OnStudyViewListener;
import com.born2go.lazzybee.gtools.LazzyBeeSingleton;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.google.android.gms.tagmanager.DataLayer;

import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends AppCompatActivity
        implements  ICompleteSutdy, OnStudyViewListener {

    private static final String TAG = "StudyActivity";
    private static final String GA_SCREEN = "aStudyScreen";
    private DataLayer mDataLayer;
    private Context context;

    LearnApiImplements dataBaseHelper;

    LinearLayout container;
    MenuItem btnBackBeforeCard;

    List<Card> todayList = new ArrayList<Card>();
    List<Card> againList = new ArrayList<Card>();
    List<Card> dueList = new ArrayList<Card>();
    //Current Card
    Card currentCard = new Card();
    //Define before card
    Card beforeCard;

    boolean learn_more;
    int completeStudy = 0;

    CustomViewPager mViewPager;

    MenuItem itemIgnore;
    MenuItem itemLearn;
    private int currentPage = 0;
    private String detailViewTag;
    ScreenSlidePagerAdapter pagerAdapter;

    public void setBeforeCard(Card beforeCard) {
        this.beforeCard = beforeCard;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        context = this;
        _initActonBar();
        _initDatabase();
        _trackerApplication();
        _initView();
        _setUpStudy();

    }


    private void _setUpStudy() {
        try {
            //get lean_more form intern
            learn_more = getIntent().getBooleanExtra(LazzyBeeShare.LEARN_MORE, false);

            //get custom setting study
            int limit_today = dataBaseHelper._getCustomStudySetting(LazzyBeeShare.KEY_SETTING_TODAY_NEW_CARD_LIMIT);
            int total_learn_per_day = dataBaseHelper._getCustomStudySetting(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT);

            //get card due today & agin
            againList = dataBaseHelper._getListCardByQueue(Card.QUEUE_LNR1, 0);
            dueList = dataBaseHelper._getListCardByQueue(Card.QUEUE_REV2, total_learn_per_day);

            //Define Count due
            int dueCount = dueList.size();

            //get new random card list to day
            //int newCount =
            //if (newCount > 0)
            //  todayList = dataBaseHelper._getRandomCard(newCount);
            if (dueCount == 0) {
                Log.i(TAG, "_setUpStudy()  dueCount == 0");
            } else {

                Log.i(TAG, "_setUpStudy()  dueCount != 0");

                if (dueCount < total_learn_per_day) {

                    Log.i(TAG, "_setUpStudy()  dueCount < total_learn_per_day");

                    if (total_learn_per_day - dueCount < limit_today) {

                        Log.i(TAG, "_setUpStudy()  total_learn_per_day - dueCount < limit_today");
                        limit_today = total_learn_per_day - dueCount;

                    } else if (total_learn_per_day - dueCount > limit_today) {

                        Log.i(TAG, "_setUpStudy()  total_learn_per_day - dueCount > limit_today");
                    }
                } else if (dueCount >= total_learn_per_day) {

                    Log.i(TAG, "_setUpStudy()  dueCount >= total_learn_per_day");
                    limit_today = 0;
                }
                learn_more = false;
            }

            //Define todayList
            todayList = dataBaseHelper._getRandomCard(limit_today, learn_more);

            //Define count card
            int againCount = againList.size();
            int todayCount = todayList.size();

            Log.i(TAG, "againCount:" + againCount);
            Log.i(TAG, "todayCount:" + todayCount);
            Log.i(TAG, "dueCount:" + dueCount + ",limit:" + dueCount + ",today:" + todayCount);

            //Define check_learn
            //check_learn==true Study
            //check_learn==false Complete Study
            boolean check_learn = againCount > 0 || dueCount > 0 || todayCount > 0;

            Log.i(TAG, "check_learn:" + (check_learn));

            if (check_learn) {
                pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(pagerAdapter);
            } else {
                Log.i(TAG, "_completeLean");
                _completeLean(false);
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }



    private void _initActonBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void _trackerApplication() {
        try {
            Log.i(TAG, "Trying to use TagManager");
            mDataLayer = LazzyBeeSingleton.mDataLayer;
            mDataLayer.pushEvent("openScreen", DataLayer.mapOf("screenName", GA_SCREEN));
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }

    private void _completeLean(boolean showCompleteDialog) {
        Log.i(TAG, "----_completeLean----");
        setBeforeCard(null);
        completeStudy = LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000;
        dataBaseHelper._insertOrUpdateToSystemTable(String.valueOf(LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000), String.valueOf(completeStudy));

//        setBeforeCard(null);
//        completeStudy = LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000;
//        dataBaseHelper._insertOrUpdateToSystemTable(String.valueOf(LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000), String.valueOf(completeStudy));
//        setResult(completeStudy, new Intent());
//        finish();
        if (showCompleteDialog) {
            int result = dataBaseHelper._insetStreak();
            Log.d(TAG, "inseart streaks result:" + result);
            if (!(result == -1)) {
                _showDialogComplete();
            } else {
                setResult(completeStudy, new Intent());
                finish();
            }
        } else {
            setResult(completeStudy, new Intent());
            finish();
        }
        Log.i(TAG, "---------END---------");

    }

    private void _showDialogComplete() {
        if (learn_more == false) {
            final DialogCompleteStudy dialogCompleteStudy = new DialogCompleteStudy(context);
            dialogCompleteStudy.show(getFragmentManager().beginTransaction(), LazzyBeeShare.EMPTY);
        } else {
            setResult(completeStudy, new Intent());
            finish();
        }
//        final Dialog dialog = new Dialog(this, R.style.full_screen_dialog) {
//            @Override
//            protected void onCreate(Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                setContentView(R.layout.fragment_dialog_complete_study);
//
//
//
//            }
//        };
//        dialog.show();
    }

    /**
     * Init db sqlite
     */
    private void _initDatabase() {
        dataBaseHelper = LazzyBeeSingleton.learnApiImplements;
    }



    private void _initView() {
        container = (LinearLayout) findViewById(R.id.container);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mViewPager.setPagingEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d(TAG, "State:" + position + ",positionOffset:" + positionOffset + ",positionOffsetPixels:" + positionOffsetPixels);

            }
            @Override
            public void onPageSelected(int position) {
                _setDisplayPageByPosition(position);
                currentPage = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void _setDisplayPageByPosition(int position) {
        if (position == 0) {
            setTitle(R.string.title_activity_study);
        } else {
            setTitle(currentCard.getQuestion());
        }
    }

    public void onlbTipHelpClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_lazzybee_website)));
        startActivity(browserIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_study, menu);
        _defineSearchView(menu);
        return true;
    }



    private void _defineSearchView(Menu menu) {
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Theme the SearchView's AutoCompleteTextView drop down. For some reason this wasn't working in styles.xml
        SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);

        if (autoCompleteTextView != null) {
            int color = Color.parseColor("#ffffffff");
            Drawable drawable = autoCompleteTextView.getDropDownBackground();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

            autoCompleteTextView.setDropDownBackgroundDrawable(drawable);
            autoCompleteTextView.setTextColor(R.color.grey_600);
        }


        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "onMenuItemActionCollapse");
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand");
                return true;
            }

        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Log.d(TAG, "onSuggestionSelect:" + position);
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Log.d(TAG, "onSuggestionClick:" + position);
                try {
                    CursorAdapter c = searchView.getSuggestionsAdapter();
                    if (c != null) {
                        Cursor cur = c.getCursor();
                        cur.moveToPosition(position);

                        String cardID = cur.getString(cur.getColumnIndex(BaseColumns._ID));
                        Log.d(TAG, "cardID:" + cardID);
                        String query = cur.getString(cur.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                        Log.d(TAG, "query:" + query);

                        _gotoCardDetailbyID(cardID);

                        //call back actionbar
                        searchItem.collapseActionView();
                    } else {
                        Log.d(TAG, "NUll searchView.getSuggestionsAdapter()");
                    }
                } catch (Exception e) {
                    LazzyBeeShare.showErrorOccurred(context, e);
                }
                return true;
            }
        });
    }

    private void _gotoCardDetailbyID(String cardID) {
        Intent intent = new Intent(this, CardDetailsActivity.class);
        intent.putExtra(LazzyBeeShare.CARDID, String.valueOf(cardID));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                // I do not want this...
                // Home as up button is to navigate to Home-Activity not previous acitivity
                if (mViewPager.getCurrentItem() == 0) {
                    super.onBackPressed();
                } else {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LazzyBeeShare._cancelNotification(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        int hour = dataBaseHelper.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_HOUR_NOTIFICATION);
        int minute = dataBaseHelper.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_MINUTE_NOTIFICATION);
        LazzyBeeShare._setUpNotification(context, hour, minute);
    }

    @Override
    public void close() {
        setResult(completeStudy, new Intent());
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    public void setDetailViewTag(String detailViewTag) {
        this.detailViewTag = detailViewTag;
    }

    public String getDetailViewTag() {
        return detailViewTag;
    }

    @Override
    public void completeLearn(boolean complete) {
        _completeLean(complete);
    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private int pageCount = 2;


        public void setPageCount(int pageCount1) {
            pageCount = pageCount1;
        }

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

        }


        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return StudyView.newInstance(context, getIntent(), mViewPager, this, currentCard);
            else {
                return DetailsView.newInstance(context, "details");

            }

        }

        @Override
        public int getCount() {
            Log.d(TAG, "pageCount:" + pageCount);
            return pageCount;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    @Override
    public void _displayUserNote(Card card) {
        _showCardNote(card);

    }

    private void _showCardNote(final Card currentCard) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogLearnMore));

        View viewDialog = View.inflate(context, R.layout.view_dialog_user_note, null);
        final EditText txtUserNote = (EditText) viewDialog.findViewById(R.id.txtUserNote);

        txtUserNote.setText(currentCard.getUser_note());

        builder.setView(viewDialog);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String user_note = txtUserNote.getText().toString();
                if (!user_note.isEmpty()) {
                    currentCard.setUser_note(user_note);
                    dataBaseHelper._updateUserNoteCard(currentCard);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();

        dialog.show();

    }

    @Override
    public void setCurrentCard(Card card) {
        currentCard = card;
    }
}
