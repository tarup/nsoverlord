package fi.tamk.c4tpelto.nsoverlord;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Creates a fragment of settings.
 *
 * @author Taru Peltola, taru.peltola@cs.tamk.fi
 * @version 2016-0510
 * @since 4.4
 */
public class PrefsFragment extends PreferenceFragment
                           implements Preference.OnPreferenceClickListener{

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
}
