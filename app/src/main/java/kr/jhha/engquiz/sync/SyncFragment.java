package kr.jhha.engquiz.sync;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kr.jhha.engquiz.R;
import kr.jhha.engquiz.MainActivity;
import kr.jhha.engquiz.addscript.AddScriptFragment;
import kr.jhha.engquiz.data.local.SyncModel;
import kr.jhha.engquiz.z_legacy.DownloadSampleFragment;

import static kr.jhha.engquiz.sync.SyncFragment.TabView.UPDATE;
import static kr.jhha.engquiz.sync.SyncFragment.TabView.UPLOAD;

/**
 * Created by Junyoung on 2016-06-23.
 */

public class SyncFragment extends Fragment implements SyncContract.View
{
    private SyncContract.UserActionsListener mActionListener;

    private final String mTITLE = "Sync Quizs";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private FragmentTabHost mTabHost;


    public class TabView {
        public static final int UPDATE = 0;
        public static final int UPLOAD = 1;
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionListener = new SyncPresenter( this, SyncModel.getInstance() );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.content_sync, container, false);

        // 탭 셋팅
        mTabLayout = (TabLayout) view.findViewById(R.id.sync_layout_tab);
        mTabLayout.addTab(mTabLayout.newTab().setText("Update"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Upload"));

        // 탭 페이지 넘김 관련 (view pager) 셋팅
        mViewPager = (ViewPager) view.findViewById(R.id.sync_pager);
        TabPagerAdapter pagerAdapter = new TabPagerAdapter( getChildFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);

        //  ViewPager에서 페이지의 상태가 변경될 때 페이지 변경 이벤트를 TabLayout에 전달하여  탭의 선택 상태를 동기화해주는 역할
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        // 탭 선택 리스너 이벤트
        mTabLayout.addOnTabSelectedListener(mTabSelectedListener);

        return view;
    }

    @Override
    public void onResume() {
        // 툴바에 현 프래그먼트 제목 출력
        ((MainActivity)getActivity()).setActionBarTitle( mTITLE );
        super.onResume();
    }

    TabLayout.OnTabSelectedListener mTabSelectedListener = new TabLayout.OnTabSelectedListener()
    {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab){}

        @Override
        public void onTabReselected(TabLayout.Tab tab){}
    };

    private void changeFragment() {

    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        // Count number of tabs
        private int tabCount;
        Fragment tab1;
        Fragment tab2;

        public TabPagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            tab1 = new DownloadSampleFragment();
            tab2 = new AddScriptFragment();
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position)
        {
            Fragment currentTab = tab1;
            // Returning the current tabs
            switch (position) {
                case UPDATE:
                    currentTab = tab1;
                    break;
                case UPLOAD:
                    currentTab = tab2;
                    break;
            }
            return currentTab;
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    @Override
    public void onDrawSyncNeededImg() {
        Log.i("###############","onDrawSyncNeededImg() is called");
        // draw alarm 'sync needed' img
    }
}