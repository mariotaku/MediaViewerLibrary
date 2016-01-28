/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.mediaviewer.library;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;


public abstract class AbsMediaViewerActivity extends FragmentActivity implements OnPageChangeListener {

    private ViewPager mViewPager;
    private MediaPagerAdapter mPagerAdapter;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getInitialPosition();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        mPagerAdapter = new MediaPagerAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        final int currentIndex = getInitialPosition();
        if (currentIndex != -1) {
            mViewPager.setCurrentItem(currentIndex, false);
        }
        updatePositionTitle();
    }

    protected abstract int getLayoutRes();

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mViewPager = findViewPager();
    }

    protected abstract ViewPager findViewPager();

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        updatePositionTitle();
        setBarVisibility(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public abstract boolean isBarShowing();

    public abstract void setBarVisibility(boolean visible);

    public void toggleBar() {
        setBarVisibility(!isBarShowing());
    }

    private void updatePositionTitle() {
        setTitle(String.format("%d / %d", mViewPager.getCurrentItem() + 1, mPagerAdapter.getCount()));
    }


    public abstract MediaDownloader getDownloader();

    public abstract FileCache getFileCache();

    protected abstract MediaViewerFragment instantiateMediaFragment(int position);

    protected abstract int getMediaCount();

    private static class MediaPagerAdapter extends FragmentStatePagerAdapter {

        private final AbsMediaViewerActivity mActivity;

        public MediaPagerAdapter(AbsMediaViewerActivity activity) {
            super(activity.getSupportFragmentManager());
            mActivity = activity;
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
