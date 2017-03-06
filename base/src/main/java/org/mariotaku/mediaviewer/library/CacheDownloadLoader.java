/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.mediaviewer.library;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.AsyncTaskLoader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;


public final class CacheDownloadLoader extends AsyncTaskLoader<CacheDownloadLoader.Result> {

    private final Handler mHandler;
    private final MediaDownloader mDownloader;
    private final FileCache mFileCache;
    private final WeakReference<Listener> mListener;
    @NonNull
    private final Uri mUri;
    private final Object mExtra;
    private final boolean mIgnoreCache;
    private final ResultCreator mCreator;

    public CacheDownloadLoader(final Context context, @NonNull final MediaDownloader downloader,
                               @NonNull final FileCache cache, @NonNull final Listener listener,
                               @NonNull final Uri uri, @Nullable final Object extra,
                               @Nullable ResultCreator creator, boolean ignoreCache) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
        mUri = uri;
        mExtra = extra;
        mDownloader = downloader;
        mFileCache = cache;
        mListener = new WeakReference<>(listener);
        mCreator = creator != null ? creator : new DefaultResultCreator();
        mIgnoreCache = ignoreCache;
    }

    private static boolean isValid(File entry) {
        return entry != null;
    }

    @Override
    @NonNull
    public CacheDownloadLoader.Result loadInBackground() {
        final String scheme = mUri.getScheme();
        DownloadResult result = null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            final String uriString = mUri.toString();
            if (uriString == null) return Result.nullInstance();
            File cacheFile;
            final long nonce = System.currentTimeMillis();
            try {
                if (!mIgnoreCache) {
                    cacheFile = mFileCache.get(uriString);
                    if (isValid(cacheFile)) {
                        return mCreator.create(mFileCache.toUri(uriString));
                    }
                }
                mHandler.post(new DownloadRequestedRunnable(this, mListener.get(), nonce));
                // from SD cache
                result = mDownloader.get(uriString, mExtra);
                final long length = result.getLength();
                mHandler.post(new DownloadStartRunnable(this, mListener.get(), nonce, length));

                final byte[] extra = result.getExtra();
                mFileCache.save(uriString, result.getStream(), extra, new FileCache.CopyListener() {
                    @Override
                    public boolean onCopied(int current) {
                        mHandler.post(new ProgressUpdateRunnable(mListener.get(), current, length, nonce));
                        return !isAbandoned();
                    }

                });
                mHandler.post(new DownloadFinishRunnable(this, mListener.get(), nonce));
                cacheFile = mFileCache.get(uriString);
                if (isValid(cacheFile)) {
                    return mCreator.create(mFileCache.toUri(uriString));
                } else {
                    mFileCache.remove(uriString);
                    throw new IOException("Invalid cache file");
                }
            } catch (final Exception e) {
                mHandler.post(new DownloadErrorRunnable(this, mListener.get(), e, nonce));
                return Result.getInstance(e);
            } finally {
                Utils.closeSilently(result);
            }
        }
        return mCreator.create(mUri);
    }

    @Override
    protected void onReset() {
        super.onReset();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    public interface Listener {
        @UiThread
        void onDownloadError(Throwable t, long nonce);

        @UiThread
        void onDownloadFinished(long nonce);

        @UiThread
        void onDownloadStart(long total, long nonce);

        @UiThread
        void onDownloadRequested(long nonce);

        @UiThread
        void onProgressUpdate(long current, long total, long nonce);
    }

    public interface DownloadResult extends Closeable {

        long getLength() throws IOException;

        @NonNull
        InputStream getStream() throws IOException;

        @Nullable
        byte[] getExtra() throws IOException;

    }

    public interface ResultCreator {
        Result create(Uri uri);
    }

    static class DefaultResultCreator implements ResultCreator {

        @Override
        public Result create(Uri uri) {
            return Result.getInstance(uri);
        }
    }

    public static class Result {
        @Nullable
        public final Uri cacheUri;
        @Nullable
        public final Exception exception;

        public Result(@Nullable final Uri cacheUri, @Nullable final Exception exception) {
            this.cacheUri = cacheUri;
            this.exception = exception;
        }

        public static Result getInstance(@NonNull final Uri uri) {
            return new Result(uri, null);
        }

        public static Result getInstance(@NonNull final Exception e) {
            return new Result(null, e);
        }

        public static Result nullInstance() {
            return new Result(null, null);
        }
    }

    private final static class DownloadErrorRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;
        private final Throwable t;
        private final long nonce;

        DownloadErrorRunnable(final CacheDownloadLoader loader, final Listener listener, final Throwable t, long nonce) {
            this.loader = loader;
            this.listener = listener;
            this.t = t;
            this.nonce = nonce;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadError(t, nonce);
        }
    }

    private final static class DownloadFinishRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;
        private final long nonce;

        DownloadFinishRunnable(final CacheDownloadLoader loader, final Listener listener, long nonce) {
            this.loader = loader;
            this.listener = listener;
            this.nonce = nonce;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadFinished(nonce);
        }
    }

    private final static class DownloadStartRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;
        private final long nonce;
        private final long total;

        DownloadStartRunnable(final CacheDownloadLoader loader, final Listener listener, final long nonce, final long total) {
            this.loader = loader;
            this.listener = listener;
            this.nonce = nonce;
            this.total = total;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadStart(total, nonce);
        }
    }

    private final static class DownloadRequestedRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;
        private final long nonce;

        DownloadRequestedRunnable(final CacheDownloadLoader loader, final Listener listener, final long nonce) {
            this.loader = loader;
            this.listener = listener;
            this.nonce = nonce;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadRequested(nonce);
        }
    }

    private final static class ProgressUpdateRunnable implements Runnable {

        private final Listener listener;
        private final long current, total;
        private final long nonce;

        ProgressUpdateRunnable(final Listener listener, final long current, final long total, long nonce) {
            this.listener = listener;
            this.current = current;
            this.total = total;
            this.nonce = nonce;
        }

        @Override
        public void run() {
            if (listener == null) return;
            listener.onProgressUpdate(current, total, nonce);
        }
    }
}
