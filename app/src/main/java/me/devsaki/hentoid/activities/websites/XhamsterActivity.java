package me.devsaki.hentoid.activities.websites;

import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.enums.Site;
import me.devsaki.hentoid.listener.ResultListener;
import me.devsaki.hentoid.retrofit.XhamsterGalleryServer;
import timber.log.Timber;

/**
 * Created by Robb on 01/2019
 * Implements Xhamster source
 */
public class XhamsterActivity extends BaseWebActivity {

    private static final String DOMAIN_FILTER = "xhamster.com";
    private static final String GALLERY_FILTER = "/gallery/";

    Site getStartSite() {
        return Site.XHAMSTER;
    }


    @Override
    protected CustomWebViewClient getWebClient() {
        CustomWebViewClient client = new XhamsterWebViewClient(GALLERY_FILTER, getStartSite(), this);
        client.restrictTo(DOMAIN_FILTER);
        return client;
    }

    private class XhamsterWebViewClient extends CustomWebViewClient {

        XhamsterWebViewClient(String filteredUrl, Site startSite, ResultListener<Content> listener) {
            super(filteredUrl, startSite, listener);
        }

        @Override
        protected void onGalleryFound(String url) {
            String[] galleryUrlParts = url.split("/");
            compositeDisposable.add(XhamsterGalleryServer.API.getGalleryMetadata(galleryUrlParts[galleryUrlParts.length - 1])
                    .subscribe(
                            metadata -> listener.onResultReady(metadata.toContent(), 1), throwable -> {
                                Timber.e(throwable, "Error parsing content.");
                                listener.onResultFailed("");
                            })
            );
        }
    }
}
