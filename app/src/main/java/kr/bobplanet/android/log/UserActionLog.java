package kr.bobplanet.android.log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * 사용자의 액션에 의해 일어나는 이벤트를 측정하기 위한 객체.
 * 현재는 로그인 이벤트 등을 측정하는 용도로 사용.
 *
 * @author heonkyu.jin
 * @version 2015. 10. 11
 */
public final class UserActionLog extends Log {
    private static final String LOGIN = "LOGIN";
    private static final String ACCOUNT_SELECT = "ACCOUNT_SELECT";
    private static final String BEACON_SEEN = "BEACON_SEEN";

    private String category;
    private String label;
    private long value;

    private UserActionLog(String category, String label, long value) {
        this.category = category;
        this.label = label;
        this.value = value;
    }

    private UserActionLog(String category, String label) {
        this(category, label, -1);
    }

    /**
     * 사용자 로그인 완료.
     */
    public static void login(String accountType) {
        new UserActionLog(LOGIN, accountType).dispatch();
    }

    /**
     * 로그인 계정 선택 완료.
     * 선택한 계정이 화면에서 몇번째 위치에 있었는지를 함께 기록함.
     *
     * @param accountType
     * @param displayOrder
     */
    public static void accountSelect(String accountType, int displayOrder) {
        new UserActionLog(ACCOUNT_SELECT, accountType, displayOrder).dispatch();
    }

    public static void beaconSeen(String beaconAddress, double distance) {
        new UserActionLog(BEACON_SEEN, beaconAddress, (long) (distance * 100)).dispatch();
    }

    @Override
    protected void dispatchGA(Tracker tracker) {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setLabel(label);

        if (value > 0) {
            builder.setValue(value);
        }

        tracker.send(builder.build());
    }

    @Override
    protected void dispatchMixpanel(MixpanelAPI mixpanel) {
    }
}
