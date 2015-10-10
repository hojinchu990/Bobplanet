package kr.bobplanet.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import kr.bobplanet.backend.bobplanetApi.model.DailyMenu;
import kr.bobplanet.backend.bobplanetApi.model.Menu;

/**
 * 특정 일자의 아침-점심-저녁 메뉴리스트를 보여주는 Activity.
 * <p/>
 * 실행 intent의 extra에 날짜가 지정되어있을 경우(가령, 푸쉬메시지를 통한 실행) 그 날짜를,
 * 없을 경우에는 오늘 날짜를 사용한다.
 * <p/>
 * - Fragment와의 통신을 위해 EventBus 이용 (@see https://github.com/greenrobot/EventBus/)
 * - Fragment의 동적 추가를 위해 ArrayPagerAdapter 이용 (@see https://github.com/commonsguy/cwac-pager)
 * - ViewPager를 이용하여 DayViewFragment를 좌우 swipe로 넘겨볼 수 있음
 * - 체감속도 향상을 위해 DayViewFragment는 좌우 1개씩 미리 생성
 * - Fragment가 데이터 로딩을 끝내면 PagerAdapter에 추가
 */
public class DayViewActivity extends ActivitySkeleton {
    private static final String TAG = DayViewActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG_PREFIX = "DayViewFragment-";

	/**
	 * Fragment 관리용 Adapter
	 */
    private DayPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DailyViewFragment가 보내주는 데이터로딩완료 메시지 수신을 위해 EventBus 등록
        EventBus.getDefault().register(this);

        // intent에 날짜가 있으면 그 날짜, 없으면 오늘 날짜 이용
        Date start_date;
        try {
            String date = getIntent().getStringExtra(AppConstants.DATE_ARGUMENT);
            start_date = DATEFORMAT_YMD.parse(date);
        } catch (Exception e) {
            start_date = new Date();
        }

        List<PageDescriptor> descriptors = Collections.singletonList(
                newPageDescriptor(DATEFORMAT_YMD.format(start_date))
        );
        adapter = new DayPagerAdapter(getSupportFragmentManager(), descriptors);

        ViewPager pager = (ViewPager) findViewById(R.id.daily_view_pager);
        pager.setAdapter(adapter);

		// 처음 사용하는 사람들을 위해 좌우스와이프 안내메시지 노출
        showSwipeNotice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Fragment의 tag로는 FRAGMENT_TAG_PREFIX + 날짜를 지정
     */
    private PageDescriptor newPageDescriptor(String date) {
        return new SimplePageDescriptor(FRAGMENT_TAG_PREFIX + date, date);
    }

    /**
     * DailyViewFragment가 데이터 로딩을 끝냈을 때 호출.
     * 식당이 매일 문열지는 않으므로, 이 때까지는 전날-다음날 메뉴가 있는지 없는지만 알 수 있음.
     * 메뉴가 있을 경우 PagerAdapter에 추가해서 swipe scroll이 가능하게 함
     */
    @SuppressWarnings("unused")
    public void onEvent(DayViewFragment.DataLoadCompleteEvent e) {
        DailyMenu d = e.getDailyMenu();
        Log.d(TAG, "Data load complete: " + d.toString());

        if (d.getPreviousDate() != null && !isExistingPage(d.getPreviousDate())) {
            Log.i(TAG, "Creating fragment of the previous day: " + d.getPreviousDate());
            adapter.insert(newPageDescriptor(d.getPreviousDate()), 0);
        }
        if (d.getNextDate() != null && !isExistingPage(d.getNextDate())) {
            Log.i(TAG, "Creating fragment of the next day: " + d.getNextDate());
            adapter.add(newPageDescriptor(d.getNextDate()));
        }
    }

	/**
	 * 식단메뉴 상세페이지로 이동.
	 * 식단메뉴가 클릭되었을 때 EventBus에 의해 호출됨.
	 * 관련 View가 ViewPager 밑의 fragment 밑의 RecyclerView 밑에 있어서 그냥 EventBus로 한방에 통과.
	 */
    @SuppressWarnings("unused")
    public void onEventMainThread(MenuListAdapter.MenuClickEvent e) {
        startMenuViewActivity(e.menuViewHolder);
    }

    private void startMenuViewActivity(MenuListAdapter.MenuViewHolder menuViewHolder) {
        Menu menu = menuViewHolder.menu;

        Intent intent = new Intent(this, MenuViewActivity.class);
        intent.putExtra(MENU_ARGUMENT, menu.toString());
        intent.putExtra(EXTRA_MENU_ICON, menuViewHolder.icon.getImageURL());
        intent.putExtra(EXTRA_MENU_TITLE, menuViewHolder.title.getText());

        /*
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                new Pair<View, String>(menuViewHolder.icon, EXTRA_MENU_ICON),
                new Pair<View, String>(menuViewHolder.title, EXTRA_MENU_TITLE));

        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        */
        startActivity(intent);
    }


    /**
	 * 이미 해당날짜의 Fragment가 있는지 확인한다.
	 * 10월 5일 화면을 띄우는 순간 10월 4일과 6일 화면을 미리 만들어두는데,
	 * 10월 4일 화면으로 이동하면 10월 3일(이건 괜찮음)과 5일(이건 안됨) 화면을 또 만들려고 하기 때문.
	 *
	 */
    private boolean isExistingPage(String date) {
        int page_count = adapter.getCount();
        for (int i = 0; i < page_count; i++) {
            String title = adapter.getPageTitle(i);
            if (title.equals(date)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * 좌우로 swipe하면 다른 날짜 메뉴도 볼 수 있음을 Snackbar로 알려줌
	 */
    private void showSwipeNotice() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        if (prefs.getBoolean(HAS_DISMISSED_SWIPE_NOTICE, false)) {
            return;
        }

        Snackbar.make(
                layout, R.string.swipe_notice, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.swipe_notice_goaway, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prefs.edit().putBoolean(HAS_DISMISSED_SWIPE_NOTICE, true).apply();
                    }
                }).show();
    }

    /**
     * DayViewFragment를 동적으로 추가하기 위해 ArrayPagerAdapter를 이용
     *
     * @see {https://github.com/commonsguy/cwac-pager}
     */
    private class DayPagerAdapter extends ArrayPagerAdapter<DayViewFragment> {
        public DayPagerAdapter(FragmentManager fragmentManager, List<PageDescriptor> descriptors) {
            super(fragmentManager, descriptors);
        }

        @Override
        protected DayViewFragment createFragment(PageDescriptor pageDescriptor) {
            return DayViewFragment.newInstance(pageDescriptor.getTitle());
        }
    }

}
