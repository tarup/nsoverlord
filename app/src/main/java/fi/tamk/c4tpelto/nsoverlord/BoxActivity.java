package fi.tamk.c4tpelto.nsoverlord;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

/**
 * Creates a graphical user interface for Penalty Box Timer.
 *
 * @author Taru Peltola, taru.peltola@cs.tamk.fi
 * @version 2016-0510
 * @since 4.4
 */
public class BoxActivity extends AppCompatActivity
                         implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String TAG = "BoxActivity";

    /**
     * Timerlabels are text labels that show each player's penalty time.
     */
    private TextView timerLabel1,
                     timerLabel2,
                     timerLabel3,
                     timerLabel4,
                     timerLabel5,
                     timerLabel6;
    /**
     * Timerlabel for 10 second jammer penalty.
     */
    private TextView timerjammers;

    /**
     * Value to determine when layout for 10 second timer is needed.
     */
    private boolean tenSecTimer;

    /**
     * On/off buttons for starting and stopping each player's penalty timer.
     */
    private ToggleButton penaltyToggle1,
                         penaltyToggle2,
                         penaltyToggle3,
                         penaltyToggle4,
                         penaltyToggle5,
                         penaltyToggle6,
                         penaltyToggle7;

    /**
     * On/off button for starting and stopping penalty timer for 10 second jammer penalty.
     */
    private ToggleButton penaltyToggleJammers;

    /**
     * Button for switching penalty time between jammers.
     */
    private ImageButton swapToggle;

    /**
     * Animation for timers' texts.
     */
    private Animation textAnimation;

    /**
     * Save for the original text color.
     */
    private ColorStateList oldColors;

    private List players;


    /**
     * Creates BoxTimer.
     *
     * Sets specific layout based on user's choice.
     * Creates chosen layout and loads possible preferences.
     *
     * @param savedInstanceState For saving and recovering state information for activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        int value = extras.getInt("layoutNum");

        if(value == 0) {
            // Two simple blocker timer
            setContentView(R.layout.box2blocker_layout);

        } else if (value == 1) {
            // Two jammer timer and one timer for both
            setContentView(R.layout.box2jammer_layout);

        } else {
            // Six timers for all players
            setContentView(R.layout.box_layout);
        }

        createLayout(value);
        loadPreferences();
        textAnimation = AnimationUtils.loadAnimation(this, R.anim.timerfinal_animation);
    }

    /**
     * Creates elements for layout based on the layout choice.
     *
     * Layout choice is made in
     * {@link fi.tamk.c4tpelto.nsoverlord.MainActivity#onClick(View)}.
     *
     * @param layoutNum Defines which layout user has picked.
     */
    public void createLayout(int layoutNum) {

        // Pause & resume in all layouts
        penaltyToggle7 = (ToggleButton) findViewById(R.id.penaltyToggle7);

        switch (layoutNum) {
            case 0:
                create2Blockers();
                break;
            case 1:
                createJammers(true);
                break;
            case 2:
                createAll();
                break;
        }
    }

    /**
     * Creates a layout for two simple blocker timer.
     */
    public void create2Blockers() {

        timerLabel1 = (TextView) findViewById(R.id.timer1);
        timerLabel2 = (TextView) findViewById(R.id.timer2);
        oldColors = timerLabel1.getTextColors();

        penaltyToggle1 = (ToggleButton) findViewById(R.id.penaltyToggle1);
        penaltyToggle2 = (ToggleButton) findViewById(R.id.penaltyToggle2);

        penaltyToggle1.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel1, penaltyToggle1, false));
        penaltyToggle2.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel2, penaltyToggle2, false));

        penaltyToggle7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    penaltyToggle1.setChecked(false);
                    penaltyToggle2.setChecked(false);
                } else {
                    if (!timerLabel1.getText().equals(getResources().getString
                            (R.string.reset_penaltylabel))) {
                        penaltyToggle1.setChecked(true);
                    }
                    if (!timerLabel2.getText().equals(getResources().getString
                            (R.string.reset_penaltylabel))) {
                        penaltyToggle2.setChecked(true);
                    }
                }
            }
        });

        players.add(penaltyToggle1);
        players.add(penaltyToggle2);
    }

    /**
     * Creates a layout for two jammer timer.
     *
     * When creating only two adds also one ten second timer for both.
     *
     * @param tenSec True when creating penalty timer for 2 jammers.
     */
    public void createJammers(boolean tenSec) {
        tenSecTimer = tenSec;

        timerLabel3 = (TextView) findViewById(R.id.timer3);
        timerLabel4 = (TextView) findViewById(R.id.timer4);
        oldColors = timerLabel3.getTextColors();

        penaltyToggle3 = (ToggleButton) findViewById(R.id.penaltyToggle3);
        penaltyToggle4 = (ToggleButton) findViewById(R.id.penaltyToggle4);

        penaltyToggle3.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel3, penaltyToggle3, true));
        penaltyToggle4.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel4, penaltyToggle4, true));

        // Button for switching penalty time
        createSwapToggle();

        // 10sec timer for both
        if (tenSecTimer) {

            timerjammers = (TextView) findViewById(R.id.timerjammers);
            penaltyToggleJammers = (ToggleButton) findViewById(R.id.penaltyToggleJammers);
            penaltyToggleJammers.setOnCheckedChangeListener
                    (new BoxTimer(10000, timerjammers, penaltyToggleJammers, false));
        }


        penaltyToggle7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    penaltyToggle3.setChecked(false);
                    penaltyToggle4.setChecked(false);
                    if (tenSecTimer) {
                        penaltyToggleJammers.setChecked(false);
                    }
                } else {
                    if (!timerLabel3.getText().equals(getResources().getString
                            (R.string.reset_penaltylabel))) {
                        penaltyToggle3.setChecked(true);
                    }
                    if (!timerLabel4.getText().equals(getResources().getString
                            (R.string.reset_penaltylabel))) {
                        penaltyToggle4.setChecked(true);
                    }
                    if (tenSecTimer) {
                        if (!timerjammers.getText().equals(getResources().getString
                                (R.string.reset_penaltylabel_jammers))) {
                            penaltyToggleJammers.setChecked(true);
                        }
                    }
                }
            }
        });

        players.add(penaltyToggle3);
        players.add(penaltyToggle4);
    }

    /**
     * Creates a toggle button for switching the penalty time between jammers.
     *
     * Sets listener for a button. When other jammer is having a penalty
     * and another comes to penalty box, the first penalty timer will be stopped.
     * Another jammer is going to have the same amount of penalty as the first one.
     */
    public void createSwapToggle() {

        swapToggle = (ImageButton) findViewById(R.id.swapButton);
        swapToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only 1st jammer runs & swapToggle is pressed
                if (penaltyToggle3.isChecked()
                        && swapToggle.isClickable()
                        && !penaltyToggle4.isChecked()) {

                    penaltyToggle3.setChecked(false);                // 1st jammer stops
                    penaltyToggle3.setOnCheckedChangeListener(null); // 1st jammer remove listener
                    long timeToRepeat = fetchTime(timerLabel3);      // Gets new penalty time

                    // Animation for unfinished timer
                    textAnimation = AnimationUtils.loadAnimation
                            (getApplicationContext(), R.anim.timerfinal_animation);
                    textAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            timerLabel3.setTextColor(oldColors);
                            timerLabel3.setText(R.string.reset_penaltylabel);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    timerLabel3.startAnimation(textAnimation);

                    // Sets new listener for 1st jammer
                    penaltyToggle3.setOnCheckedChangeListener
                            (new BoxTimer(30000, timerLabel3, penaltyToggle3, true));

                    // Sets new timer running for 2nd jammer
                    penaltyToggle4.setEnabled(true);
                    penaltyToggle4.setOnCheckedChangeListener(null);
                    penaltyToggle4.setOnCheckedChangeListener
                            (new BoxTimer(30000 - timeToRepeat, timerLabel4, penaltyToggle4, true));
                    penaltyToggle4.setChecked(true);

                // Only 2nd jammer runs & swapToggle is pressed
                } else if (penaltyToggle4.isChecked()
                        && swapToggle.isClickable()
                        && !penaltyToggle3.isChecked()) {

                    penaltyToggle4.setChecked(false);                // 2nd jammer stops
                    penaltyToggle4.setOnCheckedChangeListener(null); // 2nd jammer remove listener
                    long timeToRepeat = fetchTime(timerLabel4);      // Gets new penalty time

                    textAnimation = AnimationUtils.loadAnimation
                            (getApplicationContext(), R.anim.timerfinal_animation);
                    textAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            timerLabel4.setTextColor(oldColors);
                            timerLabel4.setText(R.string.reset_penaltylabel);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    timerLabel4.startAnimation(textAnimation);

                    penaltyToggle4.setOnCheckedChangeListener
                            (new BoxTimer(30000, timerLabel4, penaltyToggle4, true));

                    penaltyToggle3.setOnCheckedChangeListener(null);
                    penaltyToggle3.setOnCheckedChangeListener
                            (new BoxTimer(30000 - timeToRepeat, timerLabel3, penaltyToggle3, true));
                    penaltyToggle3.setChecked(true);
                }
            }
        });
    }

    /**
     * Creates a layout for six timers - all players.
     */
    public void createAll() {
        create2Blockers();
        createJammers(false);   // Two jammers without 10 sec timer

        timerLabel5 = (TextView) findViewById(R.id.timer5);
        timerLabel6 = (TextView) findViewById(R.id.timer6);

        penaltyToggle5 = (ToggleButton) findViewById(R.id.penaltyToggle5);
        penaltyToggle6 = (ToggleButton) findViewById(R.id.penaltyToggle6);

        penaltyToggle5.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel5, penaltyToggle5, false));
        penaltyToggle6.setOnCheckedChangeListener
                (new BoxTimer(30000, timerLabel6, penaltyToggle6, false));

        penaltyToggle7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    penaltyToggle1.setChecked(false);
                    penaltyToggle2.setChecked(false);
                    penaltyToggle3.setChecked(false);
                    penaltyToggle4.setChecked(false);
                    penaltyToggle5.setChecked(false);
                    penaltyToggle6.setChecked(false);
                } else {
                    if (!timerLabel1.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle1.setChecked(true);
                    }
                    if (!timerLabel2.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle2.setChecked(true);
                    }
                    if (!timerLabel3.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle3.setChecked(true);
                    }
                    if (!timerLabel4.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle4.setChecked(true);
                    }
                    if (!timerLabel5.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle5.setChecked(true);
                    }
                    if (!timerLabel6.getText().equals(getResources().getString
                                                     (R.string.reset_penaltylabel))) {
                        penaltyToggle6.setChecked(true);
                    }
                }
            }
        });

        // other players are already added!
        players.add(penaltyToggle5);
        players.add(penaltyToggle6);
    }

    /**
     * Performs parsing for text view with a clock time.
     *
     * Separates minutes, seconds and milliseconds from a string like "00:00.0"
     * and turns it into milliseconds for timers to use.
     *
     * @param textView Text label where time is given as a string.
     * @return Milliseconds as a long value for timer use.
     */
    public long fetchTime(TextView textView) {

        long newTime;
        String time = textView.getText().toString();

        String[] substring1 = time.split(":");
        String time2 = substring1[1];
        String[] substring2 = time2.split("[.]");

        int minutes = Integer.parseInt(substring1[0]);
        int seconds = Integer.parseInt(substring2[0]);
        int millis = Integer.parseInt(substring2[1]);

        newTime = (minutes * 10000) + (seconds * 1000) + (millis * 100);

        return newTime;
    }

    /**
     * Creates Penalty Box Timer -menu.
     *
     * Initializes the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which to place the items.
     * @return True for the menu to be displayed - when false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.box_menu, menu);
        return true;
    }

    /**
     * Activates certain functionalities whenever an item in options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return False allows normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case (R.id.boxmenu_settings):
                Intent intent = new Intent(this, TimerPreferences.class);
                startActivity(intent);
                return true;
            case (R.id.clean_button):
                cleanButtonPressed();
                return true;
        }
        return true;
    }

    public void cleanButtonPressed() {
        Log.d(TAG, "clean all!");
        //TODO add cleaning solution.
        int value = players.size();
        Log.d(TAG, value + "");

        for(int i=0; i<players.size(); i++) {
            ToggleButton cleanthisbutton = (ToggleButton) players.get(i);
            if(!cleanthisbutton.isChecked()) {
                cleanthisbutton.setChecked(false);
            }

            cleanthisbutton.setOnCheckedChangeListener
                    (new BoxTimer(30000, timerLabel5, cleanthisbutton, false));
        }
    }

    /**
     * Performs when a shared preference is changed, added, or removed.
     *
     * @param sharedPreferences  The SharedPreferences that received the change.
     * @param key The key of the preference that was changed, added, or removed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadPreferences();
    }

    /**
     * Updates the UI according to changed preferences.
     */
    public void loadPreferences() {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Alerts when user is leaving the Penalty Box Timer view.
     *
     * Creates alert for asking if user is willing to exit timer.
     */
    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Returning to menu")
                .setMessage("Are you sure you want to exit this game?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        Context context = getApplicationContext();
        dialog.getButton(dialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        dialog.getButton(dialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
    }

    /**
     * Constructs needed timers to Penalty Box Timer.
     *
     * Creates a timer and listens when it's supposed to stop and start it.
     * Adds animation to text label in the end of counting.
     */
    public class BoxTimer implements CompoundButton.OnCheckedChangeListener{

        /**
         * Schedules runnable to be executed as some point in the future.
         */
        private Handler handler;

        /**
         * Represents a command that can be executed.
         */
        private Runnable runnable;

        /**
         * Button which this OnCheckedChangeListener is given.
         */
        private ToggleButton toggleButton;

        /**
         * Value what determines length of the timer.
         */
        private long resetTime, timeRemaining;

        /**
         * Time as string, that represents a prime value.
         */
        private String resetText;

        /**
         * Text label which is updated according to timer's state.
         */
        private TextView timerLabel;

        /**
         * Value is true when timer is jammer timer.
         */
        private boolean jammerpenalty;

        /**
         * Constructs a timer.
         *
         * Tracks elapsed time based on given values.
         *
         * @param time Timer's length.
         * @param tw Text label for showing the exact time.
         * @param tb Button for switching timer on and off.
         * @param jammerSecond Determines if timer is used to switched penalty.
         */
        public BoxTimer (long time, TextView tw, ToggleButton tb, boolean jammerSecond) {

            resetTime = time;
            timeRemaining = resetTime;

            timerLabel = tw;
            resetText = timerLabel.getText().toString();

            toggleButton = tb;
            handler = new Handler();

            jammerpenalty = jammerSecond;     // Penalty box layout includes jammers

            runnable = new Runnable() {
                @Override
                public void run() {
                    // Decreases the time on every run
                    timeRemaining = timeRemaining - 100;
                    int minutes = (int) timeRemaining / (1000 * 60);
                    int seconds = (int) (timeRemaining / 1000) % 60;
                    int millis = (int) ((timeRemaining / 100) % 10);

                    String label = String.format("%02d:%02d.%01d", minutes, seconds, millis);
                    timerLabel.setText(label);

                    if (timeRemaining > 0) {
                        // Delays the execution of command for particular period of time
                        handler.postDelayed(this, 100);
                    }

                    if (timeRemaining <= 10000) {
                        timerLabel.setTextColor(Color.RED);
                    }

                    if (timeRemaining == 0) {
                        endingAnimation();
                    }
                }
            };
        }

        /**
         * Ending animation for timer's text label.
         */
        public void endingAnimation() {
            textAnimation.setAnimationListener
                    (new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            toggleButton.setChecked(false);
                            handler.removeCallbacks(runnable);
                            timeRemaining = resetTime;
                            timerLabel.setTextColor(oldColors);
                            timerLabel.setText(resetText);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
            timerLabel.startAnimation(textAnimation);
        }

        /**
         * Starts and stops the timer when toggle button is triggered.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked The new checked state of buttonView.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked) {
                handler.postDelayed(runnable, 100);
            } else {
                handler.removeCallbacks(runnable);

                if (jammerpenalty) {
                    // Button view is other of jammers, always the first in the box
                    swapJammers(toggleButton);
                }
            }

        }
    }

    /**
     * Sets new listener for timer after switching penalties.
     *
     * Adds new 30 seconds time length and removes animation colors.
     *
     * @param tb The first jammer in the penalty box
     */
    public void swapJammers(ToggleButton tb) {

        // First jammer is upper jammer from the layout
        // Penalty is changed to lower one.
        if(tb == penaltyToggle3 && swapToggle.isClickable() && !penaltyToggle7.isChecked()) {
            penaltyToggle3.setOnCheckedChangeListener(null);
            timerLabel3.setTextColor(oldColors);
            penaltyToggle3.setOnCheckedChangeListener
                    (new BoxTimer(30000, timerLabel3, penaltyToggle3, true));
        }

        // First jammer is lower jammer from the layout
        // Penalty is changed to upper one.
        else if (tb == penaltyToggle4 && swapToggle.isClickable() && !penaltyToggle7.isChecked()) {
            penaltyToggle4.setOnCheckedChangeListener(null);
            timerLabel4.setTextColor(oldColors);
            penaltyToggle4.setOnCheckedChangeListener
                    (new BoxTimer(30000, timerLabel4, penaltyToggle4, true));
        }
    }
}
