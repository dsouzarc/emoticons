package namasphere.yogamoji;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class YogaMojiHome extends FragmentActivity {

    private static final String ALL = "All";
    private static final String ASANA = "Asana";
    private static final String ANIMATIONS = "Animations";
    private static final String PHRASES = "Phrases";
    private static final String SYMBOLS = "Symbols";

    private static final int SIZE = 200;

    private ActionBar theActionBar;
    private ViewPager theViewPager;
    private Context theC;
    private AssetManager theAssets;
    private Resources theResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_moji_home);

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        theC = getApplicationContext();
        theAssets = theC.getAssets();
        theResources = theC.getResources();

        theActionBar = getActionBar();
        theActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;

        theViewPager = (ViewPager) findViewById(R.id.theViewPager);

        final FragmentManager theManager = getSupportFragmentManager();

        //listener for pageChange
        final ViewPager.SimpleOnPageChangeListener thePageListener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                theActionBar.setSelectedNavigationItem(position);
            }
        };

        //Set the page listener to the view listener
        theViewPager.setOnPageChangeListener(thePageListener);

        //Create FragmentPageAdapter
        final TheFragmentPagerAdapter fragmentPagerAdapter = new TheFragmentPagerAdapter(theManager, theC, width, height);

        theViewPager.setAdapter(fragmentPagerAdapter);
        theActionBar.setDisplayShowTitleEnabled(true);

        //Create the tabs
        Tab theTab = theActionBar.newTab().setText(ALL).setTabListener(tabListener);
        theTab.setCustomView(getTab(ALL));
        theActionBar.addTab(theTab, 0);

        theTab = theActionBar.newTab().setText(ASANA).setTabListener(tabListener);
        theTab.setCustomView(getTab(ASANA));
        theActionBar.addTab(theTab, 1);

        theTab = theActionBar.newTab().setText(ANIMATIONS).setTabListener(tabListener);
        theTab.setCustomView(getTab(ANIMATIONS));
        theActionBar.addTab(theTab, 2);

        //new AddTabTask(ALL, 0).execute();
        //new AddTabTask(ANIMATIONS, 1).execute();
        //new AddTabTask(ASANA, 2).execute();
        new AddTabTask(PHRASES, 3).execute();
        new AddTabTask(SYMBOLS, 4).execute();
    }

    //Tab listener
    private final ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        @Override
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        }
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            theViewPager.setCurrentItem(tab.getPosition());

            switch(tab.getPosition()) {
                case 0:
                    theActionBar.setTitle("All Yogamojis!");
                    break;
                case 1:
                    theActionBar.setTitle("Asana Yogamojis!");
                    break;
                case 2:
                    theActionBar.setTitle("Yogamoji Animations!");
                    break;
                case 3:
                    theActionBar.setTitle("Yogamoji Phrases!");
                    break;
                case 4:
                    theActionBar.setTitle("Yogamoji Symbols!");
                    break;
                default:
                    theActionBar.setTitle("Yogamojis!");
                    break;
            }
        }

        @Override
        public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
        }
    };

    private View getTab(final String type) {
        final View theView = View.inflate(getApplicationContext(), R.layout.generic_activity_tab, null);
        final ImageView theImage = (ImageView) theView.findViewById(R.id.icon);
        final TextView theText = (TextView) theView.findViewById(R.id.title);

        theText.setText(type);

        if(type.equals(ALL)) {
            theImage.setImageBitmap(getDrawable("icons/all.png"));
        }
        else if(type.equals(ANIMATIONS)) {
            theText.setText("GIFS");
            theImage.setImageBitmap(getDrawable("icons/animations.png"));
        }
        else if(type.equals(ASANA)) {
            theImage.setImageBitmap(getDrawable("icons/asana.png"));
        }
        else if(type.equals(PHRASES)) {
            theText.setTextSize(18);
            theImage.setImageBitmap(getDrawable("icons/phrases.png"));
        }
        else if(type.equals(SYMBOLS)) {
            theText.setTextSize(18);
            theImage.setImageBitmap(getDrawable("icons/symbols.png"));
        }
        else {
            theImage.setImageDrawable(null);
        }
        return theView;
    }

    private class AddTabTask extends AsyncTask<Void, Void, Tab> {
        private final String fileName;
        private final int position;

        public AddTabTask(final String fileName, final int position) {
            this.fileName = fileName;
            this.position = position;
        }

        @Override
        public Tab doInBackground(Void... params) {
            final Tab theTab = theActionBar.newTab().setText(fileName).setTabListener(tabListener);
            theTab.setCustomView(getTab(fileName));
            return theTab;
        }
        @Override
        public void onPostExecute(final Tab theTab) {
            theActionBar.addTab(theTab, position);
        }
    }
    public Bitmap getDrawable(final String assetFileName) {
        try {
            return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(
                    theAssets.open(assetFileName)), SIZE, SIZE, false);
        }
        catch(Exception e) {
            e.printStackTrace();
            log(e.toString());
            return null;
        }
    }

    public void log(final String message) {
        Log.e("com.NamaSphere.yogamoji", message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.yoga_moji_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}