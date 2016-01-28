package org.mariotaku.mediaviewer.library;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * Created by mariotaku on 16/1/20.
 */
@WorkerThread
public interface MediaDownloader {

    @NonNull
    CacheDownloadLoader.DownloadResult get(@NonNull String url, @Nullable Object extra) throws IOException;
}
