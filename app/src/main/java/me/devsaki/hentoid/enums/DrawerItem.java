package me.devsaki.hentoid.enums;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.activities.AboutActivity;
import me.devsaki.hentoid.activities.DownloadsActivity;
import me.devsaki.hentoid.activities.PrefsActivity;
import me.devsaki.hentoid.activities.QueueActivity;
import me.devsaki.hentoid.activities.websites.XhamsterActivity;

public enum DrawerItem {

    NHENTAI("XHAMSTER", R.drawable.ic_menu_xhamster, XhamsterActivity.class),
    HOME("HOME", R.drawable.ic_menu_downloads, DownloadsActivity.class),
    QUEUE("QUEUE", R.drawable.ic_menu_queue, QueueActivity.class),
    PREFS("PREFERENCES", R.drawable.ic_menu_prefs, PrefsActivity.class),
    ABOUT("ABOUT", R.drawable.ic_menu_about, AboutActivity.class);

    public final String label;
    public final int icon;
    public final Class activityClass;

    DrawerItem(String label, int icon, Class activityClass) {
        this.label = label;
        this.icon = icon;
        this.activityClass = activityClass;
    }
}
