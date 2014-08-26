package namasphere.yogamoji;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class YogaMojiHome extends FragmentActivity {

    private static final int SIZE = 200;

    private ActionBar theActionBar;
    private ViewPager theViewPager;
    private static Context theC;
    private AssetManager theAssets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_moji_home);

        theC = getApplicationContext();
        theAssets = theC.getAssets();

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

        //Tab listener
        final ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabReselected(Tab arg0, android.app.FragmentTransaction arg1) {

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
        //Create the tabs
        Tab tab = theActionBar.newTab().setText("All").setTabListener(tabListener);
        View theView = View.inflate(getApplicationContext(), R.layout.tab_customview, null);
        tab.setCustomView(theView);
        theActionBar.addTab(tab);

        tab = theActionBar.newTab().setText("Asana").setTabListener(tabListener);

        theActionBar.addTab(tab);

        tab = theActionBar.newTab().setText("Animations").setTabListener(tabListener);
        theActionBar.addTab(tab);

        tab = theActionBar.newTab().setText("Phrases").setTabListener(tabListener);
        theActionBar.addTab(tab);

        tab = theActionBar.newTab().setText("Symbols").setTabListener(tabListener);
        theActionBar.addTab(tab);
    }

    public Drawable getDrawable(final String assetFileName) {
        try {
            return new BitmapDrawable(getApplicationContext().getResources(),
                    Bitmap.createScaledBitmap(BitmapFactory.decodeStream
                                    (theAssets.open(assetFileName)), SIZE, SIZE, false));
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
