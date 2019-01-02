package me.devsaki.hentoid.parsers.content;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class XhamsterGalleryQuery {

    public final List<XHamsterGalleryQueryElement> list;

    public XhamsterGalleryQuery(String galleryId, int page)
    {
        list = new ArrayList<>();
        list.add(new XHamsterGalleryQueryElement(galleryId, page));
    }

    class XHamsterGalleryQueryElement {
        @Expose
        public final String name = "photosGalleryPagedCollectionFetch";
        @Expose
        public final XHamsterGalleryRequestData requestData;

        public XHamsterGalleryQueryElement(String galleryId, int page) {
            requestData = new XHamsterGalleryRequestData(galleryId, page);
        }
    }

    class XHamsterGalleryRequestData {
        @Expose
        public final String entityID;
        @Expose
        public final Integer page;

        public XHamsterGalleryRequestData(String galleryId, int page) {
            entityID = galleryId;
            this.page = page;
        }
    }
}
