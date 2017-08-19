package fi.tamk.c4tpelto.nsoverlord;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Launches settings fragment.
 *
 * @author Taru Peltola, taru.peltola@cs.tamk.fi
 * @version 2016-0510
 * @since 4.4
 */
public class TimerPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content
                , new PrefsFragment()).commit();
    }
}
