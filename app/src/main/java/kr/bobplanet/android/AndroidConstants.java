package kr.bobplanet.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by hkjinlee on 15. 9. 29..
 */
public class AndroidConstants {
    private static final String ANDROID_CLIENT_ID_RELEASE =
            "603054087850-2kjko3ai98mdk3j2igap589ovni27jbp.apps.googleusercontent.com";
    private static final String ANDROID_CLIENT_ID_DEV =
            "603054087850-2e4d6q2t8992f9j6nedl8qp1ejr9ibaj.apps.googleusercontent.com";
    public static String ANDROID_CLIENT_ID = BuildConfig.DEV_VERSION ?
            ANDROID_CLIENT_ID_DEV : ANDROID_CLIENT_ID_RELEASE;

    static final String KEY_CURRENT_DATE = "KEY_CURRENT_DATE";

    static final DateFormat DATEFORMAT_YMD = new SimpleDateFormat("yyyy-MM-dd");
}
