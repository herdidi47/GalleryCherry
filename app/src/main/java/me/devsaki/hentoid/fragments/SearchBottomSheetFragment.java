package me.devsaki.hentoid.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import me.devsaki.hentoid.R;
import me.devsaki.hentoid.database.domains.Attribute;
import me.devsaki.hentoid.enums.AttributeType;
import me.devsaki.hentoid.util.Helper;
import me.devsaki.hentoid.util.IllegalTags;
import me.devsaki.hentoid.util.Preferences;
import me.devsaki.hentoid.viewmodels.SearchViewModel;
import timber.log.Timber;

import static java.lang.String.format;
import static me.devsaki.hentoid.abstracts.DownloadsFragment.MODE_LIBRARY;
import static me.devsaki.hentoid.abstracts.DownloadsFragment.MODE_MIKAN;
import static me.devsaki.hentoid.activities.SearchActivity.TAGFILTER_ACTIVE;
import static me.devsaki.hentoid.activities.SearchActivity.TAGFILTER_INACTIVE;
import static me.devsaki.hentoid.activities.SearchActivity.TAGFILTER_SELECTED;

public class SearchBottomSheetFragment extends BottomSheetDialogFragment {
    // Panel that displays the "waiting for metadata info" visuals
    private View tagWaitPanel;
    // Image that displays current metadata type title (e.g. "Character search")
    private TextView tagWaitTitle;
    // Image that displays current metadata type icon (e.g. face icon for character)
    private ImageView tagWaitImage;
    // Image that displays metadata search message (e.g. loading up / too many results / no result)
    private TextView tagWaitMessage;
    // Search bar
    private SearchView tagSearchView;
    // Container where all available attributes are loaded
    private ViewGroup attributeMosaic;

    // Attributes sort order
    private int attributesSortOrder = Preferences.getAttributesSortOrder();

    // Mode : show library or show Mikan search
    private int mode;

    private List<AttributeType> attributeTypes = new ArrayList<>();
    private AttributeType mainAttr;

    private SearchViewModel model;


    // ======== UTIL OBJECTS
    // Handler for text searches; needs to be there to be cancelable upon new key press
    private final Handler searchHandler = new Handler();


    // ======== CONSTANTS
    protected static final int MAX_ATTRIBUTES_DISPLAYED = 40;

    private static final String KEY_ATTRIBUTE_TYPES = "attributeTypes";

    public static void show(FragmentManager fragmentManager, int mode, AttributeType[] types) {
        ArrayList<Integer> selectedTypes = new ArrayList<>();
        for (AttributeType type : types) selectedTypes.add(type.getCode());

        Bundle bundle = new Bundle();
        bundle.putInt("mode", mode);
        bundle.putIntegerArrayList(KEY_ATTRIBUTE_TYPES, selectedTypes);

        SearchBottomSheetFragment searchBottomSheetFragment = new SearchBottomSheetFragment();
        searchBottomSheetFragment.setArguments(bundle);
        searchBottomSheetFragment.setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        searchBottomSheetFragment.show(fragmentManager, null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            mode = bundle.getInt("mode", -1);
            List<Integer> attrTypesList = bundle.getIntegerArrayList(KEY_ATTRIBUTE_TYPES);
            if (-1 == mode || null == attrTypesList || attrTypesList.isEmpty()) {
                Timber.d("Initialization failed");
                return;
            }

            for (Integer i : attrTypesList) attributeTypes.add(AttributeType.searchByCode(i));
            mainAttr = attributeTypes.get(0);

            model = ViewModelProviders.of(requireActivity()).get(SearchViewModel.class);
            model.setMode(mode);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.include_search_filter_category, container, false);

        tagWaitPanel = view.findViewById(R.id.tag_wait_panel);
        tagWaitImage = view.findViewById(R.id.tag_wait_image);
        tagWaitMessage = view.findViewById(R.id.tag_wait_description);
        tagWaitTitle = view.findViewById(R.id.tag_wait_title);
        attributeMosaic = view.findViewById(R.id.tag_suggestion);

        tagSearchView = view.findViewById(R.id.tag_filter);
        tagSearchView.setSearchableInfo(getSearchableInfo(requireActivity())); // Associate searchable configuration with the SearchView
        tagSearchView.setQueryHint("Search " + mainAttr.name().toLowerCase());
        tagSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (MODE_MIKAN == mode && mainAttr.equals(AttributeType.TAG) && IllegalTags.isIllegal(s)) {
                    Snackbar.make(view, R.string.masterdata_illegal_tag, Snackbar.LENGTH_LONG).show();
                } else if (!s.isEmpty()) {
                    submitAttributeSearchQuery(attributeTypes, s);
                }
                tagSearchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (MODE_MIKAN == mode && mainAttr.equals(AttributeType.TAG) && IllegalTags.isIllegal(s)) {
                    Snackbar.make(view, R.string.masterdata_illegal_tag, Snackbar.LENGTH_LONG).show();
                    searchHandler.removeCallbacksAndMessages(null);
                } else /*if (!s.isEmpty())*/ {
                    submitAttributeSearchQuery(attributeTypes, s, 1000);
                }

                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        searchMasterData(attributeTypes, "");
        // Update attribute mosaic buttons state according to available metadata
        model.getAvailableAttributes(attributeTypes).observe(this, this::updateAttributeMosaic);
    }

    private void submitAttributeSearchQuery(List<AttributeType> a, String s) {
        submitAttributeSearchQuery(a, s, 0);
    }

    private void submitAttributeSearchQuery(List<AttributeType> a, final String s, long delay) {
        searchHandler.removeCallbacksAndMessages(null);
        searchHandler.postDelayed(() -> searchMasterData(a, s), delay);
    }

    /**
     * Loads the attributes corresponding to the given AttributeType, filtered with the given string
     *
     * @param a Attribute Type whose attributes to retrieve
     * @param s Filter to apply to the attributes name (only retrieve attributes with name like %s%)
     */
    private void searchMasterData(List<AttributeType> a, final String s) {
        tagWaitImage.setImageResource(a.get(0).getIcon());
        tagWaitTitle.setText(format("%s search", Helper.capitalizeString(a.get(0).name())));
        tagWaitMessage.setText(R.string.downloads_loading);

        // Set blinking animation
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(750);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tagWaitMessage.startAnimation(anim);

        tagWaitPanel.setVisibility(View.VISIBLE);

        model.searchAttributes(a, s).observe(this, this::onAttributesReady);
    }

    private void onAttributesReady(SearchViewModel.AttributeSearchResult results) {
        if (!results.success)
        {
            onAttributesFailed(results.message);
            return;
        }

        attributeMosaic.removeAllViews();

        tagWaitMessage.clearAnimation();

        if (results.attributes.isEmpty()) {
            tagWaitMessage.setText(R.string.masterdata_no_result);
        } else if (results.attributes.size() > MAX_ATTRIBUTES_DISPLAYED) {
            String searchQuery = tagSearchView.getQuery().toString();

            String errMsg = (0 == searchQuery.length()) ? getString(R.string.masterdata_too_many_results_noquery) : getString(R.string.masterdata_too_many_results_query);
            tagWaitMessage.setText(errMsg.replace("%1", searchQuery));
        } else {
            // Sort items according to prefs
            Comparator<Attribute> comparator;
            switch (attributesSortOrder) {
                case Preferences.Constant.PREF_ORDER_ATTRIBUTES_ALPHABETIC:
                    comparator = Attribute.NAME_COMPARATOR;
                    break;
                default:
                    comparator = Attribute.COUNT_COMPARATOR;
            }
            Attribute[] attrs = results.attributes.toArray(new Attribute[0]); // Well, yes, since results.sort(comparator) requires API 24...
            Arrays.sort(attrs, comparator);

            // Display buttons
            for (Attribute attr : attrs) {
                addChoiceChip(attributeMosaic, attr);
            }
        }
    }

    private void onAttributesFailed(String message) {
        Timber.w(message);
        Snackbar.make(Objects.requireNonNull(getView()), message, Snackbar.LENGTH_SHORT).show(); // TODO: 9/11/2018 consider retry button if applicable
        tagWaitPanel.setVisibility(View.GONE);
    }

    private void addChoiceChip(ViewGroup parent, Attribute attribute) {
        String label = format("%s %s", attribute.getName(), attribute.getCount() > 0 ? "(" + attribute.getCount() + ")" : "");

        TextView chip = (TextView) getLayoutInflater().inflate(R.layout.item_chip_choice, parent, false);
        chip.setText(label);
        chip.setTag(attribute);
        chip.setOnClickListener(this::toggleSearchFilter);

        parent.addView(chip);
    }

    /**
     * Applies to the edges and text of the given Button the color corresponding to the given state
     * TODO - use a StateListDrawable resource for this
     *
     * @param b        Button to be updated
     * @param tagState Tag state whose color has to be applied
     */
    private void colorChip(View b, int tagState) {
        int color = ResourcesCompat.getColor(getResources(), R.color.red_item, null);
        if (TAGFILTER_SELECTED == tagState) {
            color = ResourcesCompat.getColor(getResources(), R.color.red_accent, null);
        } else if (TAGFILTER_INACTIVE == tagState) {
            color = Color.DKGRAY;
        }
        b.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Handler for Attribute button click
     *
     * @param button Button that has been clicked on
     */
    private void toggleSearchFilter(View button) {
        Attribute a = (Attribute) button.getTag();

        if (null == model.getSelectedAttributes().getValue() || !model.getSelectedAttributes().getValue().contains(a)) { // Add selected tag
            colorChip(button, TAGFILTER_SELECTED);
            model.selectAttribute(attributeTypes, a);
        } else { // Remove selected tag
            colorChip(button, TAGFILTER_ACTIVE);
            model.unselectAttribute(attributeTypes, a);
        }
    }

    /**
     * Refresh attributes list according to selected attributes
     * NB : available in library mode only because Mikan does not provide enough data for it
     */
    private void updateAttributeMosaic(SearchViewModel.AttributeSearchResult availableAttrs) {
        if (MODE_LIBRARY == mode) {
            tagWaitPanel.setVisibility(View.GONE);

            // Refresh displayed tag buttons
            boolean found, selected;
            String label = "";
            for (int i = 0; i < attributeMosaic.getChildCount(); i++) {
                TextView button = (TextView) attributeMosaic.getChildAt(i);
                Attribute displayedAttr = (Attribute) button.getTag();
                if (displayedAttr != null) {
                    found = false;
                    for (Attribute attr : availableAttrs.attributes)
                        if (attr.getId().equals(displayedAttr.getId())) {
                            found = true;
                            label = attr.getName() + " (" + attr.getCount() + ")";
                            break;
                        }
                    if (!found) {
                        label = displayedAttr.getName() + " (0)";
                    }

                    selected = false;
                    if (model.getSelectedAttributes() != null) {
                        List<Attribute> selectedAttributes = model.getSelectedAttributes().getValue();
                        if (selectedAttributes != null)
                            for (Attribute attr : selectedAttributes)
                                if (attr.getId().equals(displayedAttr.getId())) {
                                    selected = true;
                                    break;
                                }
                        button.setEnabled(selected || found);
                        colorChip(button, selected ? TAGFILTER_SELECTED : found ? TAGFILTER_ACTIVE : TAGFILTER_INACTIVE);
                        button.setText(label);
                    }
                }
            }
        }
    }

    /**
     * Utility method
     *
     * @param activity the activity to get the SearchableInfo from
     */
    private static SearchableInfo getSearchableInfo(Activity activity) {
        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        if(searchManager == null) throw new RuntimeException();
        return searchManager.getSearchableInfo(activity.getComponentName());
    }
}
