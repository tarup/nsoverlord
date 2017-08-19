package fi.tamk.c4tpelto.nsoverlord;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

/**
 * Creates Main Menu where user is able to choose a specific Timer.
 *
 * @author Taru Peltola, taru.peltola@cs.tamk.fi
 * @version 2016-0510
 * @since 4.4
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";

    /**
     * Array that holds references to different penalty box layouts.
     */
    private String[] AMOUNTS = {"2 Blockers", "Jammers", "Rule them all!"};

    /**
     * Buttons for choosing either penalty box timer or jam timer.
     */
    private Button boxBtn, jamBtn;

    /**
     * Creates the main layout.
     *
     * Holds two buttons for choosing which timer to use.
     *
     * @param savedInstanceState For saving and recovering state information for activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boxBtn = (Button) findViewById(R.id.boxBtn);
        jamBtn = (Button) findViewById(R.id.jamBtn);

        boxBtn.setOnClickListener(this);
        jamBtn.setOnClickListener(this);
    }

    /**
     * Triggers different function based on button which was clicked.
     *
     * Clicked Box Timer button creates alert dialog of wanted layout,
     * then starts BoxActivity activity.
     * Clicked Jam Timer button starts JamActivity activity.
     *
     * @param v Widget which invoked this method.
     */
    @Override
    public void onClick(View v) {
        if (v == boxBtn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("How many players you rule?");
            builder.setItems(AMOUNTS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, BoxActivity.class);
                intent.putExtra("layoutNum", which);
                startActivity(intent);
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else if (v == jamBtn) {
            Intent intent = new Intent(this, JamActivity.class);
            startActivity(intent);
        }
    }
}
