package me.devsaki.hentoid.retrofit;

import io.reactivex.Single;
import me.devsaki.hentoid.BuildConfig;
import me.devsaki.hentoid.enums.Site;
import me.devsaki.hentoid.model.UpdateInfoJson;
import me.devsaki.hentoid.parsers.content.XhamsterGalleryContent;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class XhamsterImagesServer {

    public static final Api API = new Retrofit.Builder()
            .baseUrl(Site.XHAMSTER.getUrl())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api.class);

    // TODO add meta

    public interface Api {

        @GET("x-api")
        Single<XhamsterGalleryContent> getGalleryContent(@Query(value="r", encoded=true) String query);
    }
}
