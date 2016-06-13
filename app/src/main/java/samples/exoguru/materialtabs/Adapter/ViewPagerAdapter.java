package samples.exoguru.materialtabs.Adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import samples.exoguru.materialtabs.countOrderFragment;
import samples.exoguru.materialtabs.labelCreateFragment;
import samples.exoguru.materialtabs.poFragment;
import samples.exoguru.materialtabs.priceHistoryFragment;
import samples.exoguru.materialtabs.reciveOrderFragment;
import samples.exoguru.materialtabs.stockHistoryFragment;
import samples.exoguru.materialtabs.viewFile;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                priceHistoryFragment tab5 = new priceHistoryFragment();
                return  tab5;
            case 1:
                stockHistoryFragment tab1 = new stockHistoryFragment();
                return tab1;
            case 2:
                poFragment tab2 = new poFragment();
                return tab2;
            case 3:
                countOrderFragment tab3 = new countOrderFragment();
                return tab3;
            case 4:
                reciveOrderFragment tab4 = new reciveOrderFragment();
                return tab4;
            case 5:
                labelCreateFragment tab6 = new labelCreateFragment();
                return tab6;
            case 6:
                viewFile view = new viewFile();
                return view;
            default:stockHistoryFragment tabdef = new stockHistoryFragment();
                return tabdef;
        }
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}