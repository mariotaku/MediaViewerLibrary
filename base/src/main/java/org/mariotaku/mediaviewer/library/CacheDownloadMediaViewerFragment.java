package org.mariotaku.mediaviewer.library;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by mariotaku on 16/2/1.
 */
public abstract class CacheDownloadMediaViewerFragment extends MediaViewerFragment implements LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>, CacheDownloadLoader.Listener {
    private boolean mLoaderInitialized;
    private CacheDownloadLoader.Result mData;

    protected boolean hasDownloadedData() {
        return mData != null && mData.cacheUri != null;
    }

    @Override
    public final void onDownloadError(Throwable t) {
        hideProgress();
    }

    @Override
    public final void onDownloadFinished() {
        hideProgress();
    }

    @Override
    public final void onDownloadStart(long total) {
        showProgress(true, 0);
    }

    @Override
    public final void onProgressUpdate(long current, long total) {
        showProgress(false, current / (float) total);
    }

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

    protected abstract boolean isAbleToLoad();
}
