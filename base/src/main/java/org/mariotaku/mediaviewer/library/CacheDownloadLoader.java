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
import android.support.annotation.UiThread;
import android.support.v4.content.AsyncTaskLoader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public final class CacheDownloadLoader extends AsyncTaskLoader<CacheDownloadLoader.Result> {

    private final Uri mUri;
    private final Handler mHandler;
    private final MediaDownloader mDownloader;
    private final Listener mListener;
    private final FileCache mFileCache;
    private final Object mExtra;

    public CacheDownloadLoader(final Context context, @NonNull final MediaDownloader downloader,
                               @NonNull final FileCache cache, @NonNull final Listener listener,
                               final Uri uri, final Object extra) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
        mUri = uri;
        mExtra = extra;
        mDownloader = downloader;
        mFileCache = cache;
        mListener = listener;
    }

    private static boolean isValid(File entry) {
        return entry != null;
    }

    @Override
    @NonNull
    public CacheDownloadLoader.Result loadInBackground() {
        if (mUri == null) {
            return Result.nullInstance();
        }
        final String scheme = mUri.getScheme();
        File cacheFile = null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            final String uriString = mUri.toString();
            if (uriString == null) return Result.nullInstance();
            try {
                cacheFile = mFileCache.get(uriString);
                if (isValid(cacheFile)) {
                    return Result.getInstance(mFileCache.toUri(uriString));
                }
                // from SD cache
                final DownloadResult result = mDownloader.get(uriString, mExtra);
                try {
                    final long length = result.length;
                    mHandler.post(new DownloadStartRunnable(this, mListener, length));

                    mFileCache.save(uriString, result.stream, new FileCache.CopyListener() {
                        @Override
                        public boolean onCopied(int current) {
                            mHandler.post(new ProgressUpdateRunnable(mListener, current, length));
                            return !isAbandoned();
                        }

                    });
                    mHandler.post(new DownloadFinishRunnable(this, mListener));
                } finally {
                    Utils.closeSilently(result);
                }
                cacheFile = mFileCache.get(uriString);
                if (isValid(cacheFile)) {
                    return Result.getInstance(mFileCache.toUri(uriString));
                } else {
                    mFileCache.remove(uriString);
                    throw new IOException();
                }
            } catch (final Exception e) {
                mHandler.post(new DownloadErrorRunnable(this, mListener, e));
                return Result.getInstance(e);
            }
        }
        return Result.getInstance(mUri);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    public interface Listener {
        @UiThread
        void onDownloadError(Throwable t);

        @UiThread
        void onDownloadFinished();

        @UiThread
        void onDownloadStart(long total);

        @UiThread
        void onProgressUpdate(long current, long total);
    }

    public static final class DownloadResult implements Closeable {
        long length;
        InputStream stream;

        public DownloadResult(long length, InputStream stream) {
            this.length = length;
            this.stream = stream;
        }

        public long getLength() {
            return length;
        }

        public InputStream getStream() {
            return stream;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    public static class Result {
        public final Uri cacheUri;
        public final Exception exception;

        public Result(final Uri cacheUri, final Exception exception) {
            this.cacheUri = cacheUri;
            this.exception = exception;
        }

        public static Result getInstance(final Uri uri) {
            return new Result(uri, null);
        }

        public static Result getInstance(final Exception e) {
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

        DownloadErrorRunnable(final CacheDownloadLoader loader, final Listener listener, final Throwable t) {
            this.loader = loader;
            this.listener = listener;
            this.t = t;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadError(t);
        }
    }

    private final static class DownloadFinishRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;

        DownloadFinishRunnable(final CacheDownloadLoader loader, final Listener listener) {
            this.loader = loader;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadFinished();
        }
    }

    private final static class DownloadStartRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final Listener listener;
        private final long total;

        DownloadStartRunnable(final CacheDownloadLoader loader, final Listener listener, final long total) {
            this.loader = loader;
            this.listener = listener;
            this.total = total;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadStart(total);
        }
    }

    private final static class ProgressUpdateRunnable implements Runnable {

        private final Listener listener;
        private final long current, total;

        ProgressUpdateRunnable(final Listener listener, final long current, final long total) {
            this.listener = listener;
            this.current = current;
            this.total = total;
        }

        @Override
        public void run() {
            if (listener == null) return;
            listener.onProgressUpdate(current, total);
        }
    }
}
