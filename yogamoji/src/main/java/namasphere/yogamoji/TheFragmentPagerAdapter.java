package namasphere.yogamoji;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import java.util.Map;
import android.view.ViewParent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import java.io.InputStream;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.Collections;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.content.Context;
import java.util.concurrent.ConcurrentHashMap;
import android.content.res.AssetManager;


public class TheFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final String ALL_KEY = "all";
    public static final String ASANA_KEY = "asana";
    public static final String LOGOSBACKGROUNDS_KEY = "logos";
    public static final String PHRASES_KEY = "phrases";
    public static final String SYMBOLS_KEY = "symbols";

    public static final String ASANA = "asana.txt";
    public static final String LOGOSBACKGROUNDS = "logos_and_backgrounds.txt";
    public static final String PHRASES = "phrases.txt";
    public static final String SYMBOLS = "symbols.txt";

    private static final int SIZE = 200;

    final int PAGE_COUNT = 5;

    private final HashMap<String, Bitmap[]> theImages = new HashMap<String, Bitmap[]>();
    private final HashMap<ImageView, Integer> theViews = new HashMap<ImageView, Integer>();

    private final Context theC;
    private final AssetManager theAssets;

    private final String[] allNames, asanaNames, logosNames, phrasesNames, symbolsNames;

    private final LinearLayout allLayout, asanaLayout, logosLayout, phrasesLayout, symbolsLayout;

    public TheFragmentPagerAdapter(final FragmentManager fm, final Context theC) {
        super(fm);
        this.theC = theC;
        theAssets = theC.getAssets();

        asanaNames = getEmojiNamesList(ASANA);
        logosNames = getEmojiNamesList(LOGOSBACKGROUNDS);
        phrasesNames = getEmojiNamesList(PHRASES);
        symbolsNames = getEmojiNamesList(SYMBOLS);
        allNames = getAllNames();

        allLayout = new LinearLayout(theC);
        asanaLayout = new LinearLayout(theC);
        logosLayout = new LinearLayout(theC);
        phrasesLayout = new LinearLayout(theC);
        symbolsLayout = new LinearLayout(theC);

        allLayout.setOrientation(LinearLayout.VERTICAL);
        asanaLayout.setOrientation(LinearLayout.VERTICAL);
        logosLayout.setOrientation(LinearLayout.VERTICAL);
        phrasesLayout.setOrientation(LinearLayout.VERTICAL);
        symbolsLayout.setOrientation(LinearLayout.VERTICAL);

        getAllDrawables();
    }

    private void getAllDrawables() {
        theImages.put(ALL_KEY, new Bitmap[allNames.length]);
        theImages.put(ASANA_KEY, new Bitmap[asanaNames.length]);
        theImages.put(LOGOSBACKGROUNDS_KEY, new Bitmap[logosNames.length]);
        theImages.put(PHRASES_KEY, new Bitmap[phrasesNames.length]);
        theImages.put(SYMBOLS_KEY, new Bitmap[symbolsNames.length]);

        int counter = 0;

        for(int i = 0; i < asanaNames.length; i++, counter++) {
            new EmojiAdder(ASANA_KEY, i, counter).execute(asanaNames[i]);
        }

        for(int i = 0; i < logosNames.length; i++, counter++) {
            new EmojiAdder(LOGOSBACKGROUNDS_KEY, i, counter).execute(logosNames[i]);
        }

        for(int i = 0; i < phrasesNames.length; i++, counter++) {
            new EmojiAdder(PHRASES_KEY, i, counter).execute(phrasesNames[i]);
        }

        for(int i = 0; i < symbolsNames.length; i++, counter++) {
            new EmojiAdder(SYMBOLS_KEY, i, counter).execute(symbolsNames[i]);
        }

    }

    /** For adding emojis to the layout
     * Given file name, gets Bitmap, adds it t layout
     */
    protected class EmojiAdder extends AsyncTask<String, Void, Bitmap> {
        private long startTime;
        private final String tag_name;
        private final int arrayElement, counter;

        public EmojiAdder(final String tag_name, final int arrayElement, final int counter) {
            this.tag_name = tag_name;
            this.arrayElement = arrayElement;
            this.counter = counter;
        }

        @Override
        public Bitmap doInBackground(final String... fileName) {
            startTime = System.currentTimeMillis();
            try {
                return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(theAssets.open("emojis/" + fileName[0])),
                        SIZE, SIZE, false);
            }
            catch (Exception e) {
                log(e.toString());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onPostExecute(final Bitmap theBitmap) {
            if(theBitmap == null) {
                log("Return");
                return;
            }

            theImages.get(tag_name)[arrayElement] = theBitmap;
            theImages.get(ALL_KEY)[counter] = theBitmap;

            final ImageView theImage = new ImageView(theC);
            theImage.setImageBitmap(theBitmap);
            theViews.put(theImage, counter);
            allLayout.addView(theImage);


            log("ADDED IMAGE IN MS:\t" + (System.currentTimeMillis() - startTime));
            final long newImageView = System.currentTimeMillis();
            if(tag_name.equals(ASANA_KEY)) {
                final ImageView theImage1 = new ImageView(theC);
                theImage1.setImageBitmap(theBitmap);
                asanaLayout.addView(theImage1);
            }
            log("NEW IMAGE VIEW TIME:\t" + (System.currentTimeMillis() - newImageView));
        }
    };

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

    int asanaFirst = 0;

    public class AsanaEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            asanaFirst++;
            final View rootInflater = inflater.inflate(R.layout.asana_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);

            if(asanaFirst > 1) {
                container.removeAllViews();
                theScroll.removeAllViews();
                asanaLayout.removeAllViews();
            }

            final Context theC = getActivity().getApplicationContext();

            final Bitmap[] thePhotos = theImages.get(ASANA_KEY);
            for(Bitmap thePhoto : thePhotos) {
                final ImageView theV = new ImageView(theC);
                theV.setImageBitmap(thePhoto);
                asanaLayout.addView(theV);
            }

            theScroll.addView(asanaLayout);
            return rootInflater;
        }
    }

    public class AllEmojis extends EmojiList {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.all_emojis, container, false);

            final ScrollView theView = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            final Context theC = getActivity().getApplicationContext();

            theView.removeAllViews();
            allLayout.removeAllViews();

            final Bitmap[] allImages = theImages.get(ALL_KEY);
            for(Bitmap theM : allImages) {
                ImageView aV = new ImageView(theC);
                aV.setImageBitmap(theM);
                allLayout.addView(aV);
            }

            theView.addView(allLayout);

            return rootInflater;
        }
    }

    public class SymbolsEmojis extends EmojiList {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.symbols_emojis, container, false);
            return rootInflater;
        }

    }

    public class PhrasesEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.phrases_emojis, container, false);
            return rootInflater;
        }
    }

    public class LogosBackgroundsEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.logos_backgrounds_emojis, container, false);
            return rootInflater;
        }
    }

    protected String[] getEmojiNamesList(final String fileName) {
        try {

            final LinkedList<String> theFileNames = new LinkedList<String>();
            InputStreamReader theISR =
                    new InputStreamReader(theAssets.open(fileName));
            BufferedReader theReader = new BufferedReader(theISR);

            while(theReader.ready())
                theFileNames.add(theReader.readLine());

            return theFileNames.toArray(new String[theFileNames.size()]);
        }

        catch (Exception e) {
            e.printStackTrace();
            return new String[]{e.toString()};
        }
    }

    private String[] getAllNames() {
        final LinkedList<String> theNames = new LinkedList<String>();
        theNames.addAll(Arrays.asList(asanaNames));
        theNames.addAll(Arrays.asList(logosNames));
        theNames.addAll(Arrays.asList(phrasesNames));
        theNames.addAll(Arrays.asList(symbolsNames));
        return theNames.toArray(new String[theNames.size()]);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public void log(final String message) {
        Log.e("com.NamaSphere.yogamoji", message);
    }
}