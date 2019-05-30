package app.com.camera_lib;

import java.util.Calendar;

class Utils {
    public static long getCurrentUnixTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis() / 1000;
    }
}
