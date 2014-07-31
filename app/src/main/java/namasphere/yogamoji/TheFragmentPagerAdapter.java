package namasphere.yogamoji;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class TheFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 5;

    public TheFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //Invoked when page is requested to be made
    @Override
    public Fragment getItem(int tabSelected) {
        Bundle data = new Bundle();
        
        switch(tabSelected) {

            case 0:
                final AllEmojis allE = new AllEmojis();
                data.putInt("current_page", tabSelected + 1);
                allE.setArguments(data);
                return allE;

            case 1:
                final AsanaEmojis theAE = new AsanaEmojis();
                data.putInt("current_page", tabSelected+1);
                theAE.setArguments(data);
                return theAE;

            case 2:
                final LogosBackgroundsEmojis theLogos = new LogosBackgroundsEmojis();
                data.putInt("current_page", tabSelected + 1);
                theLogos.setArguments(data);
                return theLogos;

            case 3:
                PhrasesEmojis thePhrases = new PhrasesEmojis();
                data.putInt("current_page", tabSelected+1);
                thePhrases.setArguments(data);
                return thePhrases;

            case 4:
                SymbolsEmojis theSymbols = new SymbolsEmojis();
                data.putInt("current_page", tabSelected + 1);
                theSymbols.setArguments(data);
                return theSymbols;
        }

        return null;
    }

    /** Returns the number of pages */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public void log(final String message) {
        Log.e("com.NamaSphere.yogamoji", message);
    }

}