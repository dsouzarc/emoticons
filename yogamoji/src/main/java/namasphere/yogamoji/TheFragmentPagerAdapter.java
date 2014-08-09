package namasphere.yogamoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


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
    private static final int PAGE_COUNT = 5;

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

    /** On click listener for sending a Yogamoji */
    private final OnClickListener SendEmojiListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            log("Clicked");
            if(!(v instanceof ImageView)) {
                log("Returning...");
                return;
            }

            makeToast("loading...");
            final long start = System.currentTimeMillis();
            final int counter = theViews.get(v);
            log("Found map: " + (System.currentTimeMillis() - start));
            new EmojiSender(theC).execute(theViews.get(v));
        }
    };

    private void makeToast(final String message) {
        Toast.makeText(theC, message,Toast.LENGTH_SHORT).show();
    }

    /** For sending Yogamojis
     * Using filename, re-reads entire Image
     * Writes it temporarily, sends it
     */
    protected final class EmojiSender extends AsyncTask<Integer, Void, Uri> {
        private int theCounter;
        private final Context theContext;

        public EmojiSender(final Context theC) {
            this.theContext = theC;
        }

        @Override
        public Uri doInBackground(final Integer... theCounters) {
            this.theCounter = theCounters[0];
            final long find = System.currentTimeMillis();
            final Bitmap theImage = theImages.get(ALL_KEY)[theCounter];
            log("Found in: " + (System.currentTimeMillis() - find));

            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(path, "Yogamoji!" + ".png");
                FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);

                theImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutPutStream);
                fileOutPutStream.flush();
                fileOutPutStream.close();

                log("Total\t" + (System.currentTimeMillis() - find));

                return Uri.parse("file://" + imageFile.getAbsolutePath());
            }
            catch (Exception e) {
                e.printStackTrace();
                log(e.toString());
                return null;
            }
        }

        @Override
        public void onPostExecute(final Uri theUri) {
            if(theUri == null)
                return;
            try {
                final long s = System.currentTimeMillis();

                final Intent sendEmoji = new Intent(Intent.ACTION_SEND);
                sendEmoji.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendEmoji.putExtra(Intent.EXTRA_STREAM, theUri);
                sendEmoji.setType("image/png");
                final Intent theSender = Intent.createChooser(sendEmoji, "Send Yogamoji using ");
                theSender.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                theContext.startActivity(theSender);

                log("Intent: " + (System.currentTimeMillis() - s));
            }

            catch(Exception e) {
                e.printStackTrace();
                log(e.toString());
            }
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
            theImage.setOnClickListener(SendEmojiListener);
            allLayout.addView(theImage);

            log("ADDED IMAGE IN MS:\t" + (System.currentTimeMillis() - startTime));

            final ImageView theImage1 = new ImageView(theC);
            theImage1.setImageBitmap(theBitmap);
            theImage1.setOnClickListener(SendEmojiListener);

            if(tag_name.equals(ASANA_KEY)) {
                asanaLayout.addView(theImage1);
            }
            else if(tag_name.equals(LOGOSBACKGROUNDS_KEY)) {
                logosLayout.addView(theImage1);
            }
            else if(tag_name.equals(PHRASES_KEY)) {
                phrasesLayout.addView(theImage1);
            }
            else if(tag_name.equals(SYMBOLS_KEY)) {
                symbolsLayout.addView(theImage1);
            }
        }
    };

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

    public class AllEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.all_emojis, container, false);
            final ScrollView theView = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(allLayout);
            theView.addView(allLayout);
            return rootInflater;
        }
    }

    public class AsanaEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.asana_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(asanaLayout);
            theScroll.addView(asanaLayout);
            return rootInflater;
        }
    }

    public class LogosBackgroundsEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.logos_backgrounds_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(logosLayout);
            theScroll.addView(logosLayout);
            return rootInflater;
        }
    }

    public class PhrasesEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.phrases_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(phrasesLayout);
            theScroll.addView(phrasesLayout);
            return rootInflater;
        }
    }

    public class SymbolsEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(namasphere.yogamoji.R.layout.symbols_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(symbolsLayout);
            theScroll.addView(symbolsLayout);
            return rootInflater;
        }
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

    protected String[] getEmojiNamesList(final String fileName) {
        try {
            final LinkedList<String> theFileNames = new LinkedList<String>();
            final InputStreamReader theISR = new InputStreamReader(theAssets.open(fileName));
            final BufferedReader theReader = new BufferedReader(theISR);

            while(theReader.ready()) {
                theFileNames.add(theReader.readLine());
            }

            return theFileNames.toArray(new String[theFileNames.size()]);
        }

        catch (Exception e) {
            e.printStackTrace();
            return new String[]{e.toString()};
        }
    }

    private void removeParent(final View theView) {
        if(theView == null) {
            return;
        }

        final ViewGroup theParent = (ViewGroup) theView.getParent();
        if(theParent != null) {
            theParent.removeAllViewsInLayout();
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