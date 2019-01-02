package me.devsaki.hentoid.parsers;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.parsers.content.XhamsterGalleryContent;
import me.devsaki.hentoid.parsers.content.XhamsterGalleryQuery;
import me.devsaki.hentoid.util.Consts;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import timber.log.Timber;

/**
 * Created by avluis on 07/26/2016.
 * Handles parsing of content from Xhamster
 */
public class XhamsterParser extends BaseParser {

    @Override
    protected List<String> parseImages(Content content) throws IOException {
        List<String> result = new ArrayList<>();

        Gson gson = new Gson();

        for (int i = 0; i < Math.ceil(content.getQtyPages() / 16.0); i++) {
            XhamsterGalleryQuery query = new XhamsterGalleryQuery(content.getUniqueSiteId(), i);

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("xhamster.com")
                    .addPathSegment("x-api")
                    .addQueryParameter("r", "[" + gson.toJson(query) + "]") // Not a 100% JSON-compliant format
                    .build();

            Document doc = getOnlineDocument(url, XhamsterParser::onIntercept);
            if (doc != null) {
                // JSON response is wrapped between <html><head></head><body> [ ... ] </body></html>
                String body = doc.toString()
                        .replace("<html>\n" + " <head></head>\n" + " <body>\n" + "  [", "")
                        .replace("]\n" + " </body>\n" + "</html>", "");

                XhamsterGalleryContent galleryContent = gson.fromJson(body, XhamsterGalleryContent.class);
                result.addAll(galleryContent.toImageUrlList());
            }
        }

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
