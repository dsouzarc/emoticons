package namasphere.yogamoji;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.FragmentTransaction;

//TODO: Add Contextual menu for selecting multiple Yogamojis

public class YogaMojiHome extends FragmentActivity {

    private ActionBar theActionBar;
    private ViewPager theViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_moji_home);

        theActionBar = getActionBar();
        theActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        theViewPager = (ViewPager) findViewById(R.id.theViewPager);

        FragmentManager theManager = getSupportFragmentManager();

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
        final TheFragmentPagerAdapter fragmentPagerAdapter = new TheFragmentPagerAdapter(theManager);

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
                        theActionBar.setTitle("Yogamoji Logos and Backgrounds!");
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

        theActionBar.addTab(tab);
        tab = theActionBar.newTab().setText("Asana").setTabListener(tabListener);
        theActionBar.addTab(tab);
        tab = theActionBar.newTab().setText("Logos & Backgrounds").setTabListener(tabListener);
        theActionBar.addTab(tab);
        tab = theActionBar.newTab().setText("Phrases").setTabListener(tabListener);
        theActionBar.addTab(tab);
        tab = theActionBar.newTab().setText("Symbols").setTabListener(tabListener);
        theActionBar.addTab(tab);
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
