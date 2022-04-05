package com.wEternityReadyTV.pack;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WebViewFragment extends Fragment{

    private MaterialToolbar toolbar;
    private ViewPager2 pager2;
    private TabLayout tabLayout;
    private FragmentPagerAdapter adapter;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private final String[] webUrls = new String[3];
    private final String[] titles = new String[3];

    public WebViewFragment() {

    }

    public static WebViewFragment newInstance(){
        return new WebViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titles[0] = getString(R.string.Tv);
        titles[1] = getString(R.string.On_Demand);
        titles[2] =getString(R.string.Search);

        webUrls[0] = getString(R.string.Tv_link);
        webUrls[1] = getString(R.string.On_demand_link);
        webUrls[2] = getString(R.string.Search_link);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adapter = new FragmentPagerAdapter(this, webUrls);
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabs);
        pager2 = view.findViewById(R.id.viewpager2);
        toolbar = view.findViewById(R.id.toolbar);
        //((MainActivity) requireActivity()).setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int pos = preferences.getInt(ThemeModel.COLOR_KEY, 0);
        ThemeModel model = ThemeModel.get(pos);

        pager2.setNestedScrollingEnabled(true);
        pager2.setAdapter(adapter);
        pager2.setOffscreenPageLimit(2);
        pager2.setUserInputEnabled(false);//disables user swiping functions
        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                toolbar.setTitle( titles[position] );
                if (position != 0)
                    ((WebViewContainer) adapter.getFragAt(0, getChildFragmentManager())).pauseWebView();
                else if(position == 0)
                    ((WebViewContainer) adapter.getFragAt(0, getChildFragmentManager())).resumeWebView();
                else if(position != 1)
                    ((WebViewContainer) adapter.getFragAt(1, getChildFragmentManager())).pauseWebView();
                else if (position == 1)
                    ((WebViewContainer) adapter.getFragAt(1, getChildFragmentManager())).resumeWebView();
            }
        });

        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, pager2, (tabs, position) -> tabs.setText(titles[position]) );
        mediator.attach();

        toolbar.setBackgroundColor(model.TOOLBAR_COLOR);
        tabLayout.setBackgroundColor(model.TAB_LAYOUT_BACKGROUND);

        toolbar.setTitle( titles[pager2.getCurrentItem()] );
        listener = (sharedPreferences, key) -> {
            if (key.equals(ThemeModel.COLOR_KEY)) {
                int index = sharedPreferences.getInt(key, 0);
                ThemeModel themeModel = ThemeModel.get(index);
                toolbar.setBackgroundColor(themeModel.TOOLBAR_COLOR);
                tabLayout.setBackgroundColor(themeModel.TAB_LAYOUT_BACKGROUND);
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener((menuItem) -> {
            switch (menuItem.getItemId()){
                case R.id.menu_settings:
                    DialogFragment dialogFragment = new ThemeDialogFragment();
                    dialogFragment.show(getParentFragmentManager(), "Theme Dialog");
                    break;
                case R.id.menu_share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Download Rapture Ready at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    break;
                case R.id.menu_exit:
                    requireActivity().finish();
                    break;
                case R.id.menu_about:
                    startActivity(new Intent(requireActivity(), About.class));
                    break;
                case R.id.menu_refresh:
                    Fragment fragment = adapter.getFragAt(pager2.getCurrentItem(), getChildFragmentManager());
                    if (fragment instanceof WebViewContainer)
                        ((WebViewContainer) fragment).tryReload();
                    break;
                case R.id.menu_rate:
                    Uri uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
                    Intent rateIntent = new Intent(Intent.ACTION_VIEW, uri);
                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
                    try {
                        startActivity(rateIntent);
                    }
                    catch (ActivityNotFoundException e){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                    }
                    break;
                default: break;
            }
            return true;
        });
    }
}