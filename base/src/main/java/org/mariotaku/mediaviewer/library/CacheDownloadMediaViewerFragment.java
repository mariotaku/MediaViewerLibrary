package org.mariotaku.mediaviewer.library;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by mariotaku on 16/2/1.
 */
public abstract class CacheDownloadMediaViewerFragment extends MediaViewerFragment
        implements LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>,
        CacheDownloadLoader.Listener {

    private static final String EXTRA_IGNORE_CACHE = "ignore_cache";

    private boolean mLoaderInitialized;
    private CacheDownloadLoader.Result mDownloadResult;

    public boolean hasDownloadedData() {
        return mDownloadResult != null && mDownloadResult.cacheUri != null;
    }

    @Nullable
    public CacheDownloadLoader.Result getDownloadResult() {
        return mDownloadResult;
    }

    @Override
    public void onDownloadError(Throwable t, long nonce) {
        if (getActivity() == null || isDetached()) return;
        hideProgress();
    }

    @Override
    public void onDownloadFinished(long nonce) {
        if (getActivity() == null || isDetached()) return;
        hideProgress();
    }

    @Override
    public void onDownloadStart(long total, long nonce) {
        if (getActivity() == null || isDetached()) return;
        showProgress(false, 0);
    }

    @Override
    public void onDownloadRequested(long nonce) {
        if (getActivity() == null || isDetached()) return;
        showProgress(true, 0);
    }

    @Override
    public void onProgressUpdate(long current, long total, long nonce) {
        if (getActivity() == null || isDetached()) return;
        showProgress(false, current / (float) total);
    }

    @Override
    public final Loader<CacheDownloadLoader.Result> onCreateLoader(int id, Bundle args) {
        final Uri downloadUri = getDownloadUri();
        if (downloadUri == null) throw new NullPointerException();
        final boolean ignoreCache = args.getBoolean(EXTRA_IGNORE_CACHE);
        return new CacheDownloadLoader(getContext(), getDownloader(), getFileCache(), this,
                downloadUri, getDownloadExtra(), getResultCreator(), ignoreCache);
    }

    private FileCache getFileCache() {
        return ((IMediaViewerActivity) getActivity()).getFileCache();
    }

    private MediaDownloader getDownloader() {
        return ((IMediaViewerActivity) getActivity()).getDownloader();
    }

    public final void startLoading(boolean ignoreCache) {
        if (!isAbleToLoad()) return;
        getLoaderManager().destroyLoader(0);
        final Bundle args = new Bundle();
        args.putBoolean(EXTRA_IGNORE_CACHE, ignoreCache);
        if (!mLoaderInitialized) {
            getLoaderManager().initLoader(0, args, this);
            mLoaderInitialized = true;
        } else {
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Nullable
    protected abstract Uri getDownloadUri();

    @Nullable
    protected abstract Object getDownloadExtra();

    @Nullable
    protected CacheDownloadLoader.ResultCreator getResultCreator() {
        return null;
    }

    @Override
    public final void onLoadFinished(Loader<CacheDownloadLoader.Result> loader, @NonNull CacheDownloadLoader.Result data) {
        mDownloadResult = data;
        hideProgress();
        displayMedia(data);
    }

    protected abstract void displayMedia(CacheDownloadLoader.Result data);

    @Override
    public final void onLoaderReset(Loader<CacheDownloadLoader.Result> loader) {
        recycleMedia();
    }

    protected abstract boolean isAbleToLoad();
}
