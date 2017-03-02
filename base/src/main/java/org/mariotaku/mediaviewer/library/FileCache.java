package org.mariotaku.mediaviewer.library;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariotaku on 16/1/20.
 */
@WorkerThread
public interface FileCache {
    @WorkerThread
    @Nullable
    File get(@NonNull String key) throws IOException;

    @WorkerThread
    @Nullable
    byte[] getExtra(@NonNull String key) throws IOException;

    @WorkerThread
    void remove(@NonNull String key) throws IOException;

    @WorkerThread
    void save(@NonNull final String key, @NonNull final InputStream stream,
            @Nullable final byte[] extra, @Nullable final CopyListener listener) throws IOException;

    @NonNull
    Uri toUri(@NonNull String key);

    @NonNull
    String fromUri(@NonNull Uri uri);

    interface CopyListener {
        @WorkerThread
        boolean onCopied(int current);
    }
}
