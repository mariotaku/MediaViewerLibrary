package org.mariotaku.mediaviewer.library.subsampleimageview;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment;
import org.mariotaku.mediaviewer.library.IMediaViewerActivity;


/**
 * Created by mariotaku on 16/1/20.
 */
public class SubsampleImageViewerFragment extends CacheDownloadMediaViewerFragment
        implements CacheDownloadLoader.Listener, LoaderManager.LoaderCallbacks<CacheDownloadLoader.Result>,
        View.OnClickListener {

    public static final String EXTRA_MEDIA_URI = "media_url";

    private SubsamplingScaleImageView mImageView;
    private boolean mHasPreview;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mImageView.setOnClickListener(this);
        mImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {

            boolean previewLoadError, imageLoadError;

            @Override
            public void onReady() {
                previewLoadError = false;
                imageLoadError = false;
                onMediaLoadStateChange(State.READY);
            }

            @Override
            public void onImageLoaded() {
                previewLoadError = false;
                imageLoadError = false;
                onMediaLoadStateChange(State.LOADED);
            }

            @Override
            public void onPreviewLoadError(Exception e) {
                previewLoadError = true;
                if (mHasPreview && imageLoadError) {
                    onMediaLoadStateChange(State.ERROR);
                }
            }

            @Override
            public void onImageLoadError(Exception e) {
                imageLoadError = true;
                if (mHasPreview && previewLoadError) {
                    onMediaLoadStateChange(State.ERROR);
                }
            }

            @Override
            public void onTileLoadError(Exception e) {

            }
        });
        setupImageView(mImageView);
        startLoading(false);
        showProgress(true, 0);
        setMediaViewVisible(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
    }

    @Override
    public void onClick(View v) {
        final IMediaViewerActivity activity = (IMediaViewerActivity) getActivity();
        if (activity == null) return;
        activity.toggleBar();
    }


    @Override
    protected View onCreateMediaView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_media_viewer_subsample_image_view, container, false);
    }

    @Override
    protected boolean isAbleToLoad() {
        return getDownloadUri() != null;
    }

    @Nullable
    @Override
    protected Uri getDownloadUri() {
        return getArguments().getParcelable(EXTRA_MEDIA_URI);
    }

    @Override
    protected Object getDownloadExtra() {
        return null;
    }

    @Override
    protected void displayMedia(CacheDownloadLoader.Result data) {
        onMediaLoadStateChange(State.NONE);
        if (data.cacheUri != null) {
            setMediaViewVisible(true);
            final ImageSource previewSource = getPreviewImageSource(data);
            mHasPreview = previewSource != null;
            mImageView.setImage(getImageSource(data), previewSource);
        } else {
            setMediaViewVisible(false);
        }
    }

    @NonNull
    protected ImageSource getImageSource(@NonNull CacheDownloadLoader.Result data) {
        assert data.cacheUri != null;
        final ImageSource imageSource = ImageSource.uri(data.cacheUri);
        imageSource.tilingEnabled();
        return imageSource;
    }

    @Nullable
    protected ImageSource getPreviewImageSource(@NonNull CacheDownloadLoader.Result data) {
        return null;
    }

    @Override
    protected void recycleMedia() {
        mImageView.recycle();
    }

    protected void onMediaLoadStateChange(@State int state) {

    }

    protected void setupImageView(SubsamplingScaleImageView imageView) {
    }

    public static SubsampleImageViewerFragment get(Uri mediaUri) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA_URI, mediaUri);
        final SubsampleImageViewerFragment f = new SubsampleImageViewerFragment();
        f.setArguments(args);
        return f;
    }

    @IntDef({State.NONE, State.READY, State.LOADED, State.ERROR})
    public @interface State {

        int NONE = 0;
        int READY = 1;
        int LOADED = 2;
        int ERROR = -1;
    }
}
