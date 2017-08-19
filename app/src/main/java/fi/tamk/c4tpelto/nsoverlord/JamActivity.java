package fi.tamk.c4tpelto.nsoverlord;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Creates a graphical user interface for Jam Timer.
 *
 * @author Taru Peltola, taru.peltola@cs.tamk.fi
 * @version 2016-0510
 * @since 4.4
 */
public class JamActivity extends AppCompatActivity
                         implements View.OnClickListener,
                                    SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "JamActivity";

    /**
     * Buttons on Jam Timer to start team timeout, official review or official timeout.
     */
    private Button toA1, toA2, toA3,
                   toB1, toB2, toB3,
                   orA1, orA2,
                   orB1, orB2,
                   officialTo;

    /**
     * Tag to set for official review buttons after succesful retaining.
     */
    private String orTAG = "retained";

    /**
     * Handles all timers and when is clicked off, stops them all.
     */
    private ToggleButton pauseButton;

    /**
     * Triggers timing jams on and off.
     */
    private ToggleButton jamTimerLabel;

    /**
     * Keeps count of periods and the jam number in a game.
     */
    private TextView periodNumber,
                     jamNumber;
    /**
     * Timer for counting passed time of periods.
     */
    private TextView periodTimerLabel;

    /**
     * Timer for counting passed time between jams.
     */
    private TextView betweenJamsLabel;

    /**
     * Schedules runnable to be executed as some point in the future.
     */
    private Handler handler;

    /**
     * Represents a command that can be executed.
     */
    private Runnable runPeriod;

    /**
     * Determines how long period time is and how much there's left.
     */
    private long periodtime;

    /**
     * Resets period time after one period has ended.
     */
    private long resetPeriod;

    /**
     * Dialog that appears on team timeout, official review or official timeout.
     */
    private AlertDialog alertDialog;

    /**
     * Tells if period has ended between a jam.
     */
    private boolean runPeriodpls;

    /**
     * Resets original text colors after animation.
     */
    private ColorStateList oldColors;

    /**
     * Creates JamTimer.
     *
     * Creates layout and loads possible preferences.
     *
     * @param savedInstanceState For saving and recovering state information for activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jam_layout);

        createLayout();
        loadPreferences();
    }

    private boolean periodEnded;

    /**
     * Creates elements for layout.
     *
     * Sets needed listeners for using timers.
     */
    public void createLayout() {

        periodNumber = (TextView) findViewById(R.id.periodNum);             // Number of period
        jamNumber = (TextView) findViewById(R.id.jamNum);                   // Number of jam
        periodTimerLabel = (TextView) findViewById(R.id.periodTimerText);   // Period timer
        betweenJamsLabel = (TextView) findViewById(R.id.jamTimerText);      // Jam timeout timer
        final String betweenJamsLabelOrigin = betweenJamsLabel.getText().toString();

        pauseButton = (ToggleButton) findViewById(R.id.pauseButton);        // Pause/resume
        jamTimerLabel = (ToggleButton) findViewById(R.id.jamTimerLabel);    // Jam timer
        final String jamtimerLabelOrigin = jamTimerLabel.getTextOff().toString();

        jamTimerLabel.setOnCheckedChangeListener
                (new JamTimer(25000, jamTimerLabel, jamNumber, betweenJamsLabel));

        jamTimerLabel.setEnabled(false);
        betweenJamsLabel.setEnabled(false);

        handler = new Handler();
        periodtime = 20000; //1800000;
        resetPeriod = periodtime;
        periodEnded = false;

        // periodclock
        runPeriod = new Runnable() {
            @Override
            public void run() {
            periodtime = periodtime - 100;
            int minutes = (int) periodtime / (1000 * 60);
            int seconds = (int) (periodtime / 1000) % 60;
            int millis = (int) ((periodtime / 100) % 10);

            String label = String.format("%02d:%02d.%01d", minutes, seconds, millis);
            periodTimerLabel.setText(label);

            if (periodtime == 0) {
                Log.d(TAG, "runperiod: periodtime is 0");
                handler.removeCallbacks(runPeriod);

                // new periodnumber and start jamnumber counting from beginning
                int periodnumber = Integer.parseInt(periodNumber.getText().toString());
                String periodnumberStr = String.valueOf((periodnumber + 1));
                periodNumber.setText(periodnumberStr);
                jamNumber.setText("1"); // Counting jams start from the beginning

                periodtime = resetPeriod;
                // TODO baaad, very baaaad
                periodTimerLabel.setText("00:30.0");
                runPeriodpls = false;
                periodEnded = true;
            }
            }
        };

        // Team Timeouts
        toA1 = (Button) findViewById(R.id.toA1);
        toA2 = (Button) findViewById(R.id.toA2);
        toA3 = (Button) findViewById(R.id.toA3);
        toB1 = (Button) findViewById(R.id.toB1);
        toB2 = (Button) findViewById(R.id.toB2);
        toB3 = (Button) findViewById(R.id.toB3);
        // Team Official Reviews
        orA1 = (Button) findViewById(R.id.orA1);
        orA2 = (Button) findViewById(R.id.orA2);
        orB1 = (Button) findViewById(R.id.orB1);
        orB2 = (Button) findViewById(R.id.orB2);
        // Official Timeout
        officialTo = (Button) findViewById(R.id.btnOto);

        // Pause is used for stopping the gameplay
        pauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Game is on!");
                    // Clocks are visible and JamTimer is ticking
                    jamTimerLabel.setEnabled(true);
                    betweenJamsLabel.setEnabled(true);
                    jamTimerLabel.setChecked(true);
                    runPeriodpls = false;

                    jamTimerLabel.setTextOn(jamtimerLabelOrigin);
                    jamTimerLabel.setTextOff(jamtimerLabelOrigin);
                    jamTimerLabel.setText(jamtimerLabelOrigin);
                    betweenJamsLabel.setText(betweenJamsLabelOrigin);

                    jamTimerLabel.setTextColor(oldColors);
                    betweenJamsLabel.setTextColor(oldColors);

                } else {
                    Log.d(TAG, "Game is off!");
                    // Clocks are invisible and JamTimer is off.
                    jamTimerLabel.setEnabled(false);
                    betweenJamsLabel.setEnabled(false);
                    jamTimerLabel.setChecked(false);
                    jamTimerLabel.setOnCheckedChangeListener(null);
                    jamTimerLabel.setOnCheckedChangeListener
                            (new JamTimer(25000, jamTimerLabel, jamNumber, betweenJamsLabel));
                    Log.d(TAG, betweenJamsLabel+"");
                }
            }
        });
    }

    /**
     * Activates review and time-out timers.
     *
     * Official timeout works same as pause button.
     * Reviews and team timeouts are shown as a new dialog.
     *
     * @param v Widget which has been clicked.
     */
    @Override
    public void onClick(View v) {

        Button clicked = (Button) v;
        // All other timers are off when timeout or review is chosen
        pauseButton.setChecked(false);

        if(clicked == officialTo) {

            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Official Timeout");
            alertDialog.setCancelable(false);
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Finish",
                                 new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pauseButton.setChecked(true);
                }
            });
            alertDialog.show();

        } else {

            if(clicked == toA1 || v == toA2 || v == toA3) {

                startTimer("Team Timeout - Black", clicked);
            } else if (v == toB1 || v == toB2 || v == toB3) {

                startTimer("Team Timeout - White", clicked);
            } else if (v == orA1 || v == orA2) {

                startTimer("Official Review - Black", clicked);
            } else if (v == orB1 || v== orB2) {

                startTimer("Official Review - White", clicked);
            }
        }
    }

    /**
     * Triggers a minute long countdown timer for clicked button.
     *
     * Timer is stoppable and it will check timeout used.
     * Reviews can be once retained.
     *
     * @param title Name of the dialog.
     * @param b Widget which was clicked to activate timeout or review.
     */
    public void startTimer(String title, final Button b) {

        final CountDownTimer countDownTimer = new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage((millisUntilFinished / (1000 * 60)) +
                                    ":"+ ((millisUntilFinished / 1000) % 60) +
                                       "." + ((millisUntilFinished / 100) % 10));
                if (millisUntilFinished <= 0) {
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                if (b == orA1 || b == orA2 || b == orB1 || b == orB2) {
                    if (b.getTag() == null) {
                        checkRetained("Official Review - Home Team", b);
                    } else {
                        b.setText("--");
                        b.setEnabled(false);
                    }
                } else {
                    b.setText("--");
                    b.setEnabled(false);
                }
                cancel();
            }
        }.start();

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("01:00.0");
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                              "Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                countDownTimer.onFinish();
                pauseButton.setChecked(true);
            }
        });
        alertDialog.show();

    }

    /**
     * Creates dialog which asks user if chosen official review is retained.
     *
     * Dialog will be shown only once for every button,
     * because review can be retained only once also.
     *
     * @param title title of the alert dialog
     * @param b button which was clicked
     */
    public void checkRetained(String title, final Button b) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Was Official Review retained or not?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        b.setText(R.string.official_review);
                        b.setTag(orTAG);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        b.setText(R.string.request_used);
                        b.setEnabled(false);
                    }
                })
                .show();
    }

    /**
     * Alert when leaving the Jam Timer.
     *
     * Pressing back will erase all the game statics,
     * so user is able to cancel this action.
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
     * Creates Jam Timer -menu.
     *
     * Initializes the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which to place the items.
     * @return True for the menu to be displayed - when false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.jamtimer_menu, menu);
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
            case (R.id.jamtimer_settings):
                Intent intent = new Intent(this, TimerPreferences.class);
                startActivity(intent);
                return true;
        }
        return true;
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
     * Constructs needed timers to Jam Timer.
     *
     * Creates timers and listens when it's supposed to stop and start them.
     */
    public class JamTimer implements CompoundButton.OnCheckedChangeListener{

        /**
         * Two commands that can be executed work as timers.
         */
        private Runnable runnable, betweenJams;

        /**
         * Button which this OnCheckedChangeListener is given.
         */
        private ToggleButton toggleButton;

        /**
         * Time values for following and reset elapsed time of jam timer.
         */
        private long resetTime, timeRemaining;

        /**
         * Time value for following elapsed time of jam timeout timer.
         */
        private long betweenTime = 30000;

        /**
         * Saving from the original timer text to set when clock is stopped.
         */
        private String resetText;

        /**
         * Label computes played jams.
         */
        private TextView jamLabel;

        /**
         * Jam timeout timer's text label.
         */
        private TextView jampause;

        /**
         * Saves text to reset jam timeout timer.
         */
        private String resetJampause;

        /**
         * Determines which timer is on - 2 minutes jam timer or 30 seconds timeout timer.
         */
        private boolean jamTimerOn;

        /**
         * Constructor for JamTimer layout's timers
         *
         * @param time Timer's length.
         * @param tb Text label for showing the exact time of jam timer.
         * @param tv Button for switching timer on and off.
         * @param between Text label for showing the exact time of jam timeout timer.
         */
        public JamTimer (long time, ToggleButton tb, TextView tv, TextView between) {
            jampause = between;
            resetJampause = "00:30.0";
                    //TODO gummy solution
                    //jampause.getText().toString();
            jamLabel = tv;
            resetTime = time;
            timeRemaining = resetTime;
            toggleButton = tb;
            resetText = "02:00.0";
                    //TODO gummy solution
                    // toggleButton.getTextOff().toString();
            oldColors = toggleButton.getTextColors();

            handler = new Handler();

            jamTimerOn = false;

            // 2 min jam timer
            runnable = new Runnable() {
                @Override
                public void run() {
                    // Ensures that period is not ended
                    if (runPeriodpls) {
                        handler.postDelayed(runPeriod, 100);
                    }
                    // Decreases the time on every run
                    timeRemaining = timeRemaining - 100;
                    int minutes = (int) timeRemaining / (1000 * 60);
                    int seconds = (int) (timeRemaining / 1000) % 60;
                    int millis = (int) ((timeRemaining / 100) % 10);

                    String label = String.format("%02d:%02d.%01d", minutes, seconds, millis);
                    toggleButton.setText(label);
                    toggleButton.setTextOff(label);
                    toggleButton.setTextOn(label);

                    if (timeRemaining > 0 && pauseButton.isChecked()) {
                        // Delays the execution of command for particular period of time
                        handler.postDelayed(this, 100);
                    }

                    if (timeRemaining <= 10000) {
                        toggleButton.setTextColor(Color.RED);
                    }

                    if (!runPeriodpls && timeRemaining == 0) {
                        Log.d(TAG, "only ending");
                        ending();
                        pauseButton.setChecked(false);
                        //TODO fix this gummy and pasted code down here
                        periodTimerLabel.setText("00:30.0");
                        // new periodnumber and start jamnumber counting from beginning
                        int periodnumber = Integer.parseInt(periodNumber.getText().toString());
                        String periodnumberStr = String.valueOf((periodnumber + 1));
                        periodNumber.setText(periodnumberStr);
                        jamNumber.setText("1"); // Counting jams start from the beginning
                    } else if (timeRemaining == 0) {
                        Log.d(TAG, "ending and start");
                        ending();
                        handler.postDelayed(betweenJams, 100);
                    }
                }
            };

            // 30 sec jam timeout timer
            betweenJams = new Runnable() {
                @Override
                public void run() {

                    if (runPeriodpls) {
                        handler.postDelayed(runPeriod, 100);
                    }

                    if (!runPeriodpls && periodEnded) {
                        handler.removeCallbacks(betweenJams);
                        jampause.setTextColor(oldColors);
                        pauseButton.setChecked(false);
                        periodEnded = false;
                    }

                    betweenTime = betweenTime - 100;
                    int minutes = (int) betweenTime / (1000 * 60);
                    int seconds = (int) (betweenTime / 1000) % 60;
                    int millis = (int) ((betweenTime / 100) % 10);

                    String label = String.format("%02d:%02d.%01d", minutes, seconds, millis);
                    jampause.setText(label);

                    if (betweenTime > 0 && pauseButton.isChecked()) {
                        jampause.postDelayed(this, 100);
                    }

                    if (betweenTime <= 5000) {
                        jampause.setTextColor(Color.RED);
                    }

                    if (betweenTime == 0) {
                        handler.removeCallbacks(betweenJams);

                        betweenTime = 30000;
                        jampause.setTextColor(oldColors);
                        jampause.setText(resetJampause);

                        toggleButton.setEnabled(true);
                        handler.postDelayed(runnable, 100);
                        jamTimerOn = true;
                        // Set period timer on
                        runPeriodpls = true;
                    }
                }
            };
        }

        /**
         * Ending animation for timer's text label.
         */
        public void ending() {
            toggleButton.setChecked(false);

            handler.removeCallbacks(runnable);
            // handler.removeCallbacks(runPeriod);
            // Reset
            timeRemaining = resetTime;
            toggleButton.setTextColor(oldColors);
            toggleButton.setTextOff(resetText);
            toggleButton.setTextOn(resetText);

            jamTimerOn = false;
            toggleButton.setChecked(true);
        }

        /**
         * Starts and stops the timer when toggle button is triggered.
         *
         * Toggle button will alternate between two timers when it's on:
         * two minutes long jam timer and a half minute long jam timeout timer.
         * Jam timer can be ended at any time (when jam is called).
         * Jam timeout timer can be skipped to 5 minutes.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked The new checked state of buttonView.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Game is on

            if (pauseButton.isChecked()) {
                Log.d(TAG, "pause button IS checked, game is on");
                // One of the timers is running normally
                if(isChecked) {
                    //  At start, when Jam Timer is created, jamTimerOn == false
                    if (!jamTimerOn) {

                        handler.postDelayed(betweenJams, 100);  // Timeout timer
                    }  else {

                        handler.postDelayed(runnable, 100);     // Jam timer
                    }

                // Timer is clicked
                } else {
                    // Jam timeout timer is running with over 5 seconds in a clock
                    if (!jamTimerOn && betweenTime > 5000) {

                        betweenTime = 5000;
                        toggleButton.setEnabled(false);
                        toggleButton.setChecked(true);
                    }
                    // Jam timer is running, jam is called
                    else if (jamTimerOn && timeRemaining > 0) {

                        ending();
                        handler.postDelayed(betweenJams, 100);

                        // Increases the amount of the jams
                        int jamnum = Integer.parseInt(jamLabel.getText().toString());
                        String jamnumStr = String.valueOf((jamnum + 1));
                        jamLabel.setText(jamnumStr);
                    // Jam timer is running, it comes to an end after two minutes
                    } else {

                        ending();
                        int jamnum = Integer.parseInt(jamLabel.getText().toString());
                        String jamnumStr = String.valueOf((jamnum + 1));
                        jamLabel.setText(jamnumStr);
                    }
                }   // Game is off
            }       else { Log.d(TAG, "Pause button is NOT checked, game is off"); }
        }
    }
}
