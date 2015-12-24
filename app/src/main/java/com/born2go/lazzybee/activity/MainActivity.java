package com.born2go.lazzybee.activity;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.born2go.lazzybee.R;
import com.born2go.lazzybee.adapter.DownloadFileandUpdateDatabase;
import com.born2go.lazzybee.adapter.DownloadFileandUpdateDatabase.DownloadFileDatabaseResponse;
import com.born2go.lazzybee.db.Card;
import com.born2go.lazzybee.db.DataBaseHelper;
import com.born2go.lazzybee.db.DatabaseUpgrade;
import com.born2go.lazzybee.db.impl.LearnApiImplements;
import com.born2go.lazzybee.fragment.DialogHelp;
import com.born2go.lazzybee.fragment.DialogStatistics;
import com.born2go.lazzybee.fragment.NavigationDrawerFragment;
import com.born2go.lazzybee.gtools.ContainerHolderSingleton;
import com.born2go.lazzybee.gtools.LazzyBeeSingleton;
import com.born2go.lazzybee.shared.LazzyBeeShare;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.DataLayer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DownloadFileDatabaseResponse {

    private Context context = this;
    private static final String TAG = "MainActivity";
    private static final Object GA_SCREEN = "aHomeScreen";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    DataBaseHelper myDbHelper;
    DatabaseUpgrade databaseUpgrade;
    LearnApiImplements dataBaseHelper;

    FrameLayout container;
    DrawerLayout drawerLayout;

    CardView mCardViewStudy;
    CardView mCardViewReView;
    CardView mCardViewLearnMore;

    CardView mCardViewCustomStudy;

    TextView lbReview;

    TextView lbStudy;
    TextView lbCustomStudy;


    InterstitialAd mInterstitialAd;

    boolean appPause = false;
    boolean studyComplete = false;

    SearchView searchView;

    SharedPreferences sharedpreferences;
    private int KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _initSQlIte();
        _initSettingApplication();

        _initToolBar();
        _intInterfaceView();

        _initInterstitialAd();

        _trackerApplication();


    }


    private void _initInterstitialAd() {
        try {
            Container container = ContainerHolderSingleton.getContainerHolder().getContainer();
            String admob_pub_id = null;
            String adv_fullscreen_id = null;
            if (container != null) {
                admob_pub_id = container.getString(LazzyBeeShare.ADMOB_PUB_ID);
                adv_fullscreen_id = container.getString(LazzyBeeShare.ADV_FULLSCREEB_ID);
                Log.d(TAG, "admob_pub_id:" + admob_pub_id);
                Log.d(TAG, "adv_fullscreen_id:" + adv_fullscreen_id);
            }
            if (admob_pub_id != null || adv_fullscreen_id != null) {
                String advId = admob_pub_id + "/" + adv_fullscreen_id;
                Log.d(TAG, "InterstitialAdId:" + advId);
                mInterstitialAd = new InterstitialAd(this);
                mInterstitialAd.setAdUnitId(advId);

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        _gotoStudy(getResources().getInteger(R.integer.goto_study_code1));
                    }
                });

                requestNewInterstitial();
            } else {
                Log.d(TAG, "InterstitialAdId null");
                mInterstitialAd = null;
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }

    private void requestNewInterstitial() {
        if (mInterstitialAd != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(getResources().getStringArray(R.array.devices)[0])
                    .addTestDevice(getResources().getStringArray(R.array.devices)[1])
                    .addTestDevice(getResources().getStringArray(R.array.devices)[2])
                    .addTestDevice(getResources().getStringArray(R.array.devices)[3])
                    .build();

            mInterstitialAd.loadAd(adRequest);
        } else {
            Log.d(TAG, "mInterstitialAd null");
        }
    }


    private void _setUpNotification(boolean nextday) {
        Log.i(TAG, "---------setUpNotification-------");
        try {
            int hour = dataBaseHelper.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_HOUR_NOTIFICATION);
            int minute = dataBaseHelper.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_MINUTE_NOTIFICATION);
            //Check currentTime
            Calendar currentCalendar = Calendar.getInstance();
            int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);


            Calendar calendar = Calendar.getInstance();
            if (hour <= currentHour || nextday) {
                calendar.add(Calendar.DATE, 1);
            }
            // Define a time
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            //
            Long alertTime = calendar.getTimeInMillis();
            //Toast.makeText(context, "Alert time:" + alertTime, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Alert " + 0 + ",time:" + alertTime);

            //set notificaion by time
            LazzyBeeShare.scheduleNotification(context, 0, alertTime);
            Log.e(TAG, "Set notificarion time:" + hour + ":" + minute);
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
        Log.i(TAG, "---------END-------");
    }


    private void _initSettingApplication() {
        sharedpreferences = getSharedPreferences(LazzyBeeShare.MyPREFERENCES, Context.MODE_PRIVATE);
        if (_checkSetting(LazzyBeeShare.KEY_SETTING_AUTO_CHECK_UPDATE)) {
            _checkUpdate();
        }
        LazzyBeeShare._cancelNotification(context);
        boolean first_run_app = sharedpreferences.getBoolean(LazzyBeeShare.KEY_FIRST_RUN_APP, false);
        if (!first_run_app) {
            _showHelp();
            sharedpreferences.edit().putBoolean(LazzyBeeShare.KEY_FIRST_RUN_APP, true).commit();
        }
        KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT = dataBaseHelper.getSettingIntergerValuebyKey(String.valueOf(LazzyBeeShare.KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT));


    }


    private boolean _checkSetting(String key) {
        String auto = dataBaseHelper._getValueFromSystemByKey(key);
        if (auto == null) {
            return false;
        } else if (auto.equals(LazzyBeeShare.ON)) {
            return true;
        } else if (auto.equals(LazzyBeeShare.OFF)) {
            return false;
        } else {
            return false;
        }
    }

    private void _intInterfaceView() {
        container = (FrameLayout) findViewById(R.id.container);
        //Define Card View
        mCardViewStudy = (CardView) findViewById(R.id.mCardViewStudy);
        mCardViewReView = (CardView) findViewById(R.id.mCardViewReView);
        mCardViewLearnMore = (CardView) findViewById(R.id.mCardViewLearnMore);
        mCardViewCustomStudy = (CardView) findViewById(R.id.mCardViewCustomStudy);

        lbStudy = (TextView) findViewById(R.id.lbStudy);
        lbCustomStudy = (TextView) findViewById(R.id.lbCustomStudy);

        lbReview = (TextView) findViewById(R.id.lbReview);


        TextView lbTipHelp = (TextView) findViewById(R.id.lbTipHelp);
        lbTipHelp.setText("****************************" + getString(R.string.url_lazzybee_website) + "****************************");
        lbTipHelp.setSelected(true);
        //lbTipHelp.setTypeface(null, Typeface.BOLD);
        lbTipHelp.setSingleLine();
        lbTipHelp.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        lbTipHelp.setHorizontallyScrolling(true);

    }

    public void onlbTipHelpClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_lazzybee_website)));
        startActivity(browserIntent);
    }


    private void _showDialogCongraturation(String messgage_congratilation) {
        final Snackbar snackbar =
                Snackbar
                        .make(mCardViewReView, messgage_congratilation, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackBarView.setBackgroundColor(getResources().getColor(R.color.snackbar_background_color));
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                snackbar.show();
            }
        }.start();

    }

    private void _initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _initNavigationDrawerFragment(toolbar);

    }


    /**
     * Init Sql
     */
    private void _initSQlIte() {
        myDbHelper = LazzyBeeSingleton.dataBaseHelper;
        databaseUpgrade = LazzyBeeSingleton.databaseUpgrade;
        dataBaseHelper = LazzyBeeSingleton.learnApiImplements;
    }


    /**
     * Init NavigationDrawerFragment
     *
     * @param toolbar
     */
    private void _initNavigationDrawerFragment(Toolbar toolbar) {
        try {
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer, toolbar,
                    drawerLayout);
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        try {
            switch (position) {
                case LazzyBeeShare.DRAWER_ABOUT_INDEX:
                    //Toast.makeText(context, R.string.under_construction, Toast.LENGTH_SHORT).show();
                    _gotoAbout();
                    break;
                case LazzyBeeShare.DRAWER_ADD_COURSE_INDEX:
                    //_gotoAddCourse();
                    Toast.makeText(context, R.string.under_construction, Toast.LENGTH_SHORT).show();
                    break;
                case LazzyBeeShare.DRAWER_SETTINGS_INDEX:
                    _gotoSetting();
                    break;
                case LazzyBeeShare.DRAWER_USER_INDEX:
                    //Toast.makeText(context, R.string.action_login, Toast.LENGTH_SHORT).show();
                    break;
                case LazzyBeeShare.DRAWER_COURSE_INDEX:
                    break;
                case LazzyBeeShare.DRAWER_DICTIONARY_INDEX:
                    _gotoDictionary();
                    break;
                case LazzyBeeShare.DRAWER_MAJOR_INDEX:
                    showSelectSubject();
                    break;
                case LazzyBeeShare.DRAWER_HELP_INDEX:
                    _showHelp();
                    break;
                case LazzyBeeShare.DRAWER_STATISTICAL_INDEX:
                    _showStatistical();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }


    }

    private void _showStatistical() {
        DialogStatistics dialogStatistics = new DialogStatistics(context);
        dialogStatistics.show(getSupportFragmentManager(), DialogStatistics.TAG);
    }

    private void showSelectSubject() {
        View mSelectMajor = View.inflate(context, R.layout.view_select_major, null);
        final CheckBox cbIt = (CheckBox) mSelectMajor.findViewById(R.id.cbIt);
        final CheckBox cbEconomy = (CheckBox) mSelectMajor.findViewById(R.id.cbEconomy);
        final CheckBox cbScience = (CheckBox) mSelectMajor.findViewById(R.id.cbScience);
        final CheckBox cbMedicine = (CheckBox) mSelectMajor.findViewById(R.id.cbMedicine);
        final CheckBox cbIelts = (CheckBox) mSelectMajor.findViewById(R.id.cbIelts);

        //get my subbject
        String my_subject = dataBaseHelper._getValueFromSystemByKey(LazzyBeeShare.KEY_SETTING_MY_SUBJECT);
        if (my_subject == null) {
            cbEconomy.setChecked(false);
            cbScience.setChecked(false);
            cbMedicine.setChecked(false);
            cbIelts.setChecked(false);
            cbIt.setChecked(false);
        } else if (my_subject.equals(getString(R.string.subject_it_value))) {
            cbIt.setChecked(true);
        } else if (my_subject.equals(getString(R.string.subject_economy_value))) {
            cbEconomy.setChecked(true);
        } else if (my_subject.equals(getString(R.string.subject_science_value))) {
            cbScience.setChecked(true);
        } else if (my_subject.equals(getString(R.string.subject_medical_value))) {
            cbMedicine.setChecked(true);
        } else if (my_subject.equals(getString(R.string.subject_ielts_value))) {
            cbIelts.setChecked(true);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogLearnMore));
        builder.setTitle(context.getString(R.string.select_subject_title));
        final CharSequence[] items_value = context.getResources().getStringArray(R.array.subjects_value);
        final int[] seleted_index = {0};
        final int[] checker = {-1};
        cbIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    // Put some meat on the sandwich
                    cbEconomy.setChecked(false);
                    cbScience.setChecked(false);
                    cbMedicine.setChecked(false);
                    cbIelts.setChecked(false);
                    checker[0] = 0;
                } else {
                    checker[0] = -2;
                }
            }
        });
        cbEconomy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    // Put some meat on the sandwich
                    cbIt.setChecked(false);
                    cbScience.setChecked(false);
                    cbMedicine.setChecked(false);
                    cbIelts.setChecked(false);
                    checker[0] = 1;
                } else {
                    checker[0] = -2;
                }
            }
        });
        cbScience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    // Put some meat on the sandwich
                    cbIt.setChecked(false);
                    cbEconomy.setChecked(false);
                    cbMedicine.setChecked(false);
                    cbIelts.setChecked(false);
                    checker[0] = 2;
                } else {
                    checker[0] = -2;
                }
            }
        });
        cbMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    // Put some meat on the sandwich
                    cbIt.setChecked(false);
                    cbEconomy.setChecked(false);
                    cbScience.setChecked(false);
                    cbIelts.setChecked(false);
                    checker[0] = 3;
                } else {
                    checker[0] = -2;
                }
            }
        });
        cbIelts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    // Put some meat on the sandwich
                    cbIt.setChecked(false);
                    cbEconomy.setChecked(false);
                    cbScience.setChecked(false);
                    cbMedicine.setChecked(false);
                    checker[0] = 4;
                } else {
                    checker[0] = -2;
                }
            }
        });
        builder.setView(mSelectMajor);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int mylevel = dataBaseHelper.getSettingIntergerValuebyKey(LazzyBeeShare.KEY_SETTING_MY_LEVEL);
                String subjectSelected;
                if (checker[0] > -1) {
                    subjectSelected = String.valueOf(items_value[checker[0]]);
                    //save my subjects
                    dataBaseHelper._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_MY_SUBJECT, subjectSelected);

                    //reset incomming list
                    dataBaseHelper._initIncomingCardIdListbyLevelandSubject(mylevel, subjectSelected);
                } else if (checker[0] == -2) {
                    //save my subjects
                    dataBaseHelper._insertOrUpdateToSystemTable(LazzyBeeShare.KEY_SETTING_MY_SUBJECT, LazzyBeeShare.EMPTY);

                    //reset incomming list
                    dataBaseHelper._initIncomingCardIdList();
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void _gotoDictionary() {
        _gotoSeachOrDictionary(LazzyBeeShare.GOTO_DICTIONARY, LazzyBeeShare.GOTO_DICTIONARY_CODE);

    }

    private void _gotoAbout() {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
    }

    private void _showDialogWithMessage(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogLearnMore));
        builder.setTitle("Ops!");
        builder.setMessage(Html.fromHtml(message));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void _showHelp() {
        DialogHelp dialogHelp = new DialogHelp();
        dialogHelp.show(getSupportFragmentManager(), DialogHelp.TAG);
    }


    public void _restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.app_name));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                MenuInflater inflater = getMenuInflater();
                // Inflate menu to add items to action bar if it is present.
                inflater.inflate(R.menu.main, menu);
                // Associate searchable configuration with the SearchView
                _defineSearchView(menu);
                _restoreActionBar();
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void _defineSearchView(Menu menu) {
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
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

                        _gotoCardDetailbyCardId(cardID);

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

    private void _gotoCardDetailbyCardId(String cardId) {
        Intent intent = new Intent(this, CardDetailsActivity.class);
        intent.putExtra(LazzyBeeShare.CARDID, cardId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivityForResult(intent, getResources().getInteger(R.integer.code_card_details_updated));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //switch id action action bar
        switch (id) {
            case android.R.id.home:
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    private boolean _checkUpdate() {
        try {
            if (dataBaseHelper._checkUpdateDataBase()) {
                Log.i(TAG, "Co Update");
                Toast.makeText(context, "Co Update", Toast.LENGTH_SHORT).show();
                _showComfirmUpdateDatabase(LazzyBeeShare.DOWNLOAD_UPDATE);
                return true;
            } else {
                Toast.makeText(context, "Khong co Update", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Khong co Update");
                return false;
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
            return false;
        }
    }

    private void _showComfirmUpdateDatabase(final int type) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogLearnMore));

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_update)
                .setTitle(R.string.dialog_title_update);

        // Add the buttons
        builder.setPositiveButton(R.string.btn_update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Update button
                //1.Download file from server
                //2.Open database
                //3.Upgade to my database
                //4.Remove file update
                if (type == LazzyBeeShare.DOWNLOAD_UPDATE) {
                    _downloadFile();
                } else {
                    _updateDB(type);
                }

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

    private void _updateDB(int type) {
        try {

            databaseUpgrade.copyDataBase(type);
            List<Card> cards = databaseUpgrade._getAllCard();
            for (Card card : cards) {
                dataBaseHelper._insertOrUpdateCard(card);
            }
            dataBaseHelper._insertOrUpdateToSystemTable(LazzyBeeShare.DB_VERSION, String.valueOf(databaseUpgrade._getVersionDB()));
            databaseUpgrade.close();
        } catch (Exception e) {
            Log.e(TAG, "Update DB Error:" + e.getMessage());
            e.printStackTrace();
        }


    }

    private void _downloadFile() {
        Container container = ContainerHolderSingleton.getContainerHolder().getContainer();
        String base_url;
        if (container == null) {
            base_url = getString(R.string.url_lazzybee_website);
        } else {
            base_url = container.getString(LazzyBeeShare.BASE_URL_DB);

        }
        String db_v = dataBaseHelper._getValueFromSystemByKey(LazzyBeeShare.DB_VERSION);
        int version = LazzyBeeShare.DEFAULT_VERSION_DB;
        if (db_v != null) {
            version = Integer.valueOf(db_v);
        }
        String dbUpdateName = (version + 1) + ".db";
        String download_url = base_url + dbUpdateName;
        Log.i(TAG, "download_url=" + download_url);

        if (!base_url.isEmpty() || base_url != null) {

            DownloadFileandUpdateDatabase downloadFileandUpdateDatabase = new DownloadFileandUpdateDatabase(context, version + 1);

            //downloadFileandUpdateDatabase.execute(LazzyBeeShare.URL_DATABASE_UPDATE);
            downloadFileandUpdateDatabase.execute(download_url);
            downloadFileandUpdateDatabase.downloadFileDatabaseResponse = this;
        } else {
            Toast.makeText(context, R.string.message_download_database_fail, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Goto setting
     */
    private void _gotoSetting() {
        //_initInterstitialAd inten Setting
        Intent intent = new Intent(this, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Start Intent
        this.startActivity(intent);
    }


    /**
     * Goto FragemenSearch with query_text
     */
    private void _gotoSeachOrDictionary(String query, int type) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.DISPLAY_TYPE, type);
        intent.putExtra(SearchActivity.QUERY_TEXT, query);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivityForResult(intent, LazzyBeeShare.CODE_SEARCH_RESULT);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void processFinish(int code) {
        if (code == 1) {
            //Dowload and update Complete
            if (!_checkUpdate())
                Toast.makeText(context, context.getString(R.string.mesage_update_database_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.mesage_update_database_fails), Toast.LENGTH_SHORT).show();
        }
    }

    public void _onBtnStudyOnClick(View view) {
        int countDue = dataBaseHelper._getCountListCardByQueue(Card.QUEUE_REV2, KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT);
        if (countDue > 0) {
            Log.d(TAG, "onBtnStudyOnClick\t-countDue:" + countDue);
            _gotoStudy(getResources().getInteger(R.integer.goto_study_code0));
        } else {
            int countAgain = dataBaseHelper._getCountListCardByQueue(Card.QUEUE_LNR1, 0);
            if (countAgain > 0) {
                Log.d(TAG, "onBtnStudyOnClick\t-countAgain:" + countAgain);
                _gotoStudy(getResources().getInteger(R.integer.goto_study_code0));
            } else {
                int check = dataBaseHelper._checkListTodayExit();
                Log.d(TAG, "onBtnStudyOnClick\t-queueList:" + check);
                if (check == -1 || check == -2 || check > 0) {
                    _gotoStudy(getResources().getInteger(R.integer.goto_study_code0));
                } else if (check == 0) {
                    String message = getString(R.string.congratulations_learnmore, "<b>" + getString(R.string.learn_more) + "</b>");
                    _showDialogWithMessage(message);
                }
            }
        }
    }

    private void _gotoStudy(int type) {
        //goto_study_code0 study
        //goto_study_code1 learnmore

        //Toast.makeText(context, "Goto Study", Toast.LENGTH_SHORT).show();
        studyComplete = false;
        if (type == getResources().getInteger(R.integer.goto_study_code0)) {
            Intent intent = new Intent(getApplicationContext(), StudyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            this.startActivityForResult(intent, LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000);

            //set Slide ac
//            overridePendingTransition(R.anim.slide_right, 0);
            //this.startActivityForResult(intent, RESULT_OK);

        } else if (type == getResources().getInteger(R.integer.goto_study_code1)) {
            Intent intent = new Intent(getApplicationContext(), StudyActivity.class);
            intent.putExtra(LazzyBeeShare.LEARN_MORE, true);
            this.startActivityForResult(intent, LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000);
            String key = String.valueOf(LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000);
            dataBaseHelper._insertOrUpdateToSystemTable(key, String.valueOf(1));
            // _setUpNotification();
        }
    }


    public void _onLearnMoreClick(View view) {
        int countDue = dataBaseHelper._getCountListCardByQueue(Card.QUEUE_REV2, KEY_SETTING_TOTAL_CARD_LEARN_PRE_DAY_LIMIT);
        if (countDue > 0) {
            Log.d(TAG, "_onLearnMoreClick:\t -countDue:" + countDue);
            _showDialogWithMessage(getString(R.string.message_you_not_complete));
        } else {
            int countAgain = dataBaseHelper._getCountListCardByQueue(Card.QUEUE_LNR1, 0);
            if (countAgain > 0) {
                Log.d(TAG, "_onLearnMoreClick:\t -countAgain:" + countAgain);
                _showDialogWithMessage(getString(R.string.message_you_not_complete));
            } else {
                int check = dataBaseHelper._checkListTodayExit();
                Log.d(TAG, "_onLearnMoreClick:\t -queueList:" + check);
                if (check == -1 || check == -2 || check > 0) {
                    _showDialogWithMessage(getString(R.string.message_you_not_complete));
                } else if (check == 0) {
                    _learnMore();
                }
            }
        }
    }

    private void _learnMore() {
        // Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DialogLearnMore));

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_message_learn_more)
                .setTitle(R.string.dialog_title_learn_more);

        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if (mInterstitialAd == null) {
                    _gotoStudy(getResources().getInteger(R.integer.goto_study_code1));
                } else {
                    if (mInterstitialAd.isLoaded()) {
                        // Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                        mInterstitialAd.show();

                    } else {
                        //ko load van sang study
                        _gotoStudy(getResources().getInteger(R.integer.goto_study_code1));

                    }
                }

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


    public void _onbtnReviewOnClick(View view) {
        //Toast.makeText(this, "Goto Review", Toast.LENGTH_SHORT).show();
        _gotoReviewToday();
    }

    private void _gotoReviewToday() {
        //_initInterstitialAd inten
        Intent intent = new Intent(this, ReviewCardActivity.class);
        //start intent
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i(TAG, "onActivityResult \t requestCode:" + requestCode + ",resultCode:" + resultCode);
        if (requestCode == LazzyBeeShare.CODE_SEARCH_RESULT) {
            if (resultCode == 1
                    || requestCode == LazzyBeeShare.CODE_SEARCH_RESULT) {
                // completeStudyCode = _checkCompleteLearn();
                // _getCountCard();
            }
            _restoreSearchView();
        }
        if (requestCode == LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000) {
            if (resultCode == LazzyBeeShare.CODE_COMPLETE_STUDY_RESULTS_1000) {
                LazzyBeeShare._cancelNotification(context);
                _setUpNotification(true);
                String messgage_congratilation = getString(R.string.congratulations);
                _showDialogCongraturation(messgage_congratilation);
                studyComplete = true;

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putLong(LazzyBeeShare.KEY_TIME_COMPLETE_LEARN, new Date().getTime());
                editor.commit();
            } else {
                _showDialogTip();
            }
            // completeStudyCode = _checkCompleteLearn();
            // _getCountCard();
        }
    }

    private void _restoreSearchView() {
        if (searchView != null) {
            searchView.setQuery(LazzyBeeShare.EMPTY, false);
//            searchView.clearFocus();
            searchView.setIconified(true);
        }
    }

    private void _showDialogTip() {
        try {
            Container container = ContainerHolderSingleton.getContainerHolder().getContainer();
            String pop_up_maxnum;
            String popup_text;
            String popup_url = LazzyBeeShare.EMPTY;
            if (container == null) {
                popup_text = null;
                Log.d(TAG, "ContainerHolderSingleton Null");
            } else {
                pop_up_maxnum = container.getString(LazzyBeeShare.POPUP_MAXNUM);
                if (pop_up_maxnum == null || pop_up_maxnum.equals(LazzyBeeShare.EMPTY)) {
                    popup_text = container.getString(LazzyBeeShare.POPUP_TEXT);
                    popup_url = container.getString(LazzyBeeShare.POPUP_URL);
                    Log.d(TAG, "pop_up_maxnum Null");
                } else {

                    Log.d(TAG, "pop_up_maxnum:" + pop_up_maxnum);
                    int number = LazzyBeeShare.showRandomInteger(1, Integer.valueOf(pop_up_maxnum), new Random());
                    Log.d(TAG, "Random pop:" + number);
                    popup_text = container.getString(LazzyBeeShare.POPUP_TEXT + number);
                    popup_url = container.getString(LazzyBeeShare.POPUP_URL + number);
                    Log.d(TAG, "popup_text:" + popup_text + ",popup_url:" + popup_url);
                }

            }
            if (popup_text != null) {
                final Snackbar snackbar =
                        Snackbar
                                .make(mCardViewReView, popup_text, Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                final String finalPopup_url = popup_url;
                snackBarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            snackbar.dismiss();
                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalPopup_url));
                            startActivity(myIntent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "No application can handle this request."
                                    + " Please install a webbrowser");
                        }
                    }
                });
                snackBarView.setBackgroundColor(getResources().getColor(R.color.snackbar_background_color));

                snackbar.setDuration(7000).show();
            } else {
                Log.e(TAG, "popup_text null");
            }
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        LazzyBeeShare._cancelNotification(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        appPause = true;
        Log.i(TAG, "studyComplete ?" + studyComplete);
        _setUpNotification(studyComplete);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    private void _trackerApplication() {
        try {
            DataLayer mDataLayer = LazzyBeeSingleton.mDataLayer;
            mDataLayer.pushEvent("openScreen", DataLayer.mapOf("screenName", GA_SCREEN));
        } catch (Exception e) {
            LazzyBeeShare.showErrorOccurred(context, e);
        }
    }

}
