package fi.tamk.c4tpelto.nsoverlord.utils;

import android.util.Log;

public class Utils {

    public static final int SECONDS_IN_MIN = 60;
    public static final int MILLIS_IN_SEC = 100;

    public static int getMilliseconds(int minutes) {
        int result = minutes * SECONDS_IN_MIN * MILLIS_IN_SEC;
        Log.d("UTILS", result+ "");
        return result;
    }
}
