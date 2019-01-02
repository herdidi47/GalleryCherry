package me.devsaki.hentoid.parsers;

import android.net.Uri;

import com.google.gson.Gson;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.parsers.content.XhamsterGalleryContent;
import me.devsaki.hentoid.parsers.content.XhamsterGalleryQuery;
import me.devsaki.hentoid.util.Consts;
import okhttp3.Interceptor;
import okhttp3.Request;

/**
 * Created by avluis on 07/26/2016.
 * Handles parsing of content from Xhamster
 */
public class XhamsterParser extends BaseParser {

    final static String SERVICE_URL = "https://fr.xhamster.com/x-api";

    @Override
    protected List<String> parseImages(Content content) throws IOException {
        List<String> result = new ArrayList<>();

//        CompositeDisposable compositeDisposable = new CompositeDisposable();

        for (int i = 0; i < Math.ceil(content.getQtyPages() / 16.0); i++) {
            XhamsterGalleryQuery query = new XhamsterGalleryQuery(content.getUniqueSiteId(), i);

            Uri.Builder searchUri = new Uri.Builder()
                    .scheme("https")
                    .authority("fr.xhamster.com")
                    .path("x-api")
                    .query(query.toString());

            Document doc = getOnlineDocument(searchUri.build().toString(), XhamsterParser::onIntercept);
            if (doc != null) {
                XhamsterGalleryContent galleryContent = new Gson().fromJson(doc.toString(), XhamsterGalleryContent.class);
                result.addAll(galleryContent.toImageUrlList());
            }

/*
            compositeDisposable.add(XhamsterImagesServer.API.getGalleryContent(query.toString()
                    .subscribe(
                            metadata -> listener.onResultReady(metadata.toContent(), 1), throwable -> {
                                Timber.e(throwable, "Error parsing content.");
                                listener.onResultFailed("");
                            })
            );
*/
        }
/*
        String url = content.getReaderUrl();
        String html = HttpClientHelper.call(url);
        Timber.d("Parsing: %s", url);
        Document doc = Jsoup.parse(html);
        Elements imgElements = doc.select(".img-url");
        // New Hitomi image URLs starting from june 2018
        //  If book ID is even, starts with 'aa'; else starts with 'ba'
        int referenceId = Integer.parseInt(content.getUniqueSiteId()) % 10;
        if (1 == referenceId) referenceId = 0; // Yes, this is what Hitomi actually does (see common.js)
        String imageHostname = Character.toString((char) (HOSTNAME_PREFIX_BASE + (referenceId % NUMBER_OF_FRONTENDS) )) + HOSTNAME_SUFFIX;

        for (Element element : imgElements) {
            result.add("https:" + element.text().replace("//g.", "//" + imageHostname + "."));
        }
*/
//        compositeDisposable.clear();

        return result;
    }

    private static okhttp3.Response onIntercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("User-Agent", Consts.USER_AGENT)
                .header("x-requested-with", "XMLHttpRequest")
                .build();
        return chain.proceed(request);
    }
}
