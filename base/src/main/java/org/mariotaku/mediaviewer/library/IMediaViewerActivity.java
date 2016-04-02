package org.mariotaku.mediaviewer.library;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Locale;

/**
 * Created by mariotaku on 16/4/2.
 */
public interface IMediaViewerActivity {
    boolean isBarShowing();

    void setBarVisibility(boolean visible);

    void toggleBar();

    MediaDownloader getDownloader();

    FileCache getFileCache();

    int getLayoutRes();

    ViewPager findViewPager();

    MediaViewerFragment instantiateMediaFragment(int position);

    int getMediaCount();

    int getInitialPosition();

    class Helper implements ViewPager.OnPageChangeListener {

        private final FragmentActivity mActivity;

        private ViewPager mViewPager;
        private MediaPagerAdapter mPagerAdapter;

        public Helper(FragmentActivity activity) {
            mActivity = activity;
        }

        public void onCreate(Bundle savedInstanceState) {
            mActivity.setContentView(((IMediaViewerActivity) mActivity).getLayoutRes());
            mPagerAdapter = new MediaPagerAdapter(mActivity);
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.addOnPageChangeListener(this);
            final int currentIndex = ((IMediaViewerActivity) mActivity).getInitialPosition();
            if (currentIndex != -1) {
                mViewPager.setCurrentItem(currentIndex, false);
            }
            updatePositionTitle();
        }

        public void onContentChanged() {
            mViewPager = ((IMediaViewerActivity) mActivity).findViewPager();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            updatePositionTitle();
            ((IMediaViewerActivity) mActivity).setBarVisibility(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        private void updatePositionTitle() {
            mActivity.setTitle(String.format(Locale.US, "%d / %d", mViewPager.getCurrentItem() + 1,
                    mPagerAdapter.getCount()));
        }

    }

    class MediaPagerAdapter extends FragmentStatePagerAdapter {

        private final IMediaViewerActivity mActivity;

        public MediaPagerAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
            mActivity = (IMediaViewerActivity) activity;
        }

        @Override
        public int getCount() {
            return mActivity.getMediaCount();
        }

        @Override
        public MediaViewerFragment getItem(int position) {
            return mActivity.instantiateMediaFragment(position);
        }

    }
}
