package org.mariotaku.mediaviewer.library;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by mariotaku on 16/1/2.
 */
public abstract class MediaViewerFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>, CacheDownloadLoader.Listener {

    private boolean mLoaderInitialized;
    private CacheDownloadLoader.Result mData;

    private ProgressWheel mProgressBar;

    protected boolean hasDownloadedData() {
        return mData != null && mData.cacheUri != null;
    }

    @Override
    public final void onDownloadError(Throwable t) {
        hideProgress();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (ProgressWheel) view.findViewById(R.id.load_progress);
    }

    @Override
    public final void onDownloadFinished() {
        hideProgress();
    }

    @Override
    public final void onDownloadStart(long total) {
        showProgress(true, 0);
    }

    protected void showProgress(boolean indeterminate, float progress) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (indeterminate) {
            if (mProgressBar.isSpinning()) {
                mProgressBar.spin();
            }
        } else {
            mProgressBar.setProgress(progress);
        }
    }

    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }


    @Override
    public final void onProgressUpdate(long current, long total) {
        showProgress(false, current / (float) total);
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup inflate = (ViewGroup) inflater.inflate(R.layout.fragment_media_viewer, container, false);
        inflate.addView(onCreateMediaView(inflater, (ViewGroup) inflate.findViewById(R.id.media_container),
                savedInstanceState));
        return inflate;
    }

    protected abstract View onCreateMediaView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public final Loader<CacheDownloadLoader.Result> onCreateLoader(int id, Bundle args) {
        return new CacheDownloadLoader(getContext(), getDownloader(), getFileCache(), this,
                getDownloadUri(), getDownloadExtra());
    }

    private FileCache getFileCache() {
        return ((AbsMediaViewerActivity) getActivity()).getFileCache();
    }

    private MediaDownloader getDownloader() {
        return ((AbsMediaViewerActivity) getActivity()).getDownloader();
    }


    public final void startLoading() {
        if (!isAbleToLoad()) return;
        getLoaderManager().destroyLoader(0);
        if (!mLoaderInitialized) {
            getLoaderManager().initLoader(0, null, this);
            mLoaderInitialized = true;
        } else {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    protected abstract boolean isAbleToLoad();

    protected void setMediaViewVisible(boolean visible) {
        final View view = getView();
        if (view == null) return;
        final View mediaContainer = view.findViewById(R.id.media_container);
        mediaContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected abstract Uri getDownloadUri();

    protected abstract Object getDownloadExtra();

    @Override
    public final void onLoadFinished(Loader<CacheDownloadLoader.Result> loader, @NonNull CacheDownloadLoader.Result data) {
        mData = data;
        hideProgress();
        displayMedia(data);
        getActivity().supportInvalidateOptionsMenu();
    }

    protected abstract void displayMedia(CacheDownloadLoader.Result data);

    @Override
    public final void onLoaderReset(Loader<CacheDownloadLoader.Result> loader) {
        recycleMedia();
    }

    protected abstract void recycleMedia();

}
