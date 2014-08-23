package namasphere.yogamoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

//TODO: Each fragment gets its own GridLayout, global variable, basically replace with LinearLayout
//TODO: LayoutParams are also global variables

public class TheFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final String ALL_KEY = "all";
    public static final String ASANA_KEY = "asana";
    public static final String ANIMATIONS_KEY = "animations";
    public static final String PHRASES_KEY = "phrases";
    public static final String SYMBOLS_KEY = "symbols";

    public static final String ASANA = "asana.txt";
    public static final String ANIMATIONS = "animations.txt";
    public static final String PHRASES = "phrases.txt";
    public static final String SYMBOLS = "symbols.txt";

    private final int SIZE = 250;
    private static final int PAGE_COUNT = 5;
    private final int SIDE_MARGIN;

    private static final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
    private static final  GridLayout.LayoutParams imageParams = new GridLayout.LayoutParams();

    private final HashMap<String, Bitmap[]> theImages = new HashMap<String, Bitmap[]>();
    private final HashMap<ImageView, Integer> theViews = new HashMap<ImageView, Integer>();

    private final Context theC;
    private final AssetManager theAssets;

    private final String[] allNames, asanaNames, animationsNames, phrasesNames, symbolsNames;
    private final GridLayout allLayout, asanaLayout, animationsLayout, phrasesLayout, symbolsLayout;

    private final int width, height, imageWidth, imageHeight;

    public TheFragmentPagerAdapter(final FragmentManager fm, final Context theC, final int width, final int height) {
        super(fm);
        this.theC = theC;
        theAssets = theC.getAssets();
        this.width = width;
        this.height = height;
        this.SIDE_MARGIN = (this.width - (3 * SIZE))/5;
        this.imageWidth = (this.width - (3 * SIZE))/3;

        //this.SIDE_MARGIN = 35; //(int) (width * 0.1);
        //this.imageWidth = (int) (0.1 * (width - (SIDE_MARGIN)));
        this.imageHeight = imageWidth;
        log("WiDTH\t" + imageWidth + " HEIGhT\t" + imageHeight);

        gridParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        gridParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageParams.height = LayoutParams.WRAP_CONTENT;
        imageParams.width = LayoutParams.WRAP_CONTENT;

        asanaNames = getEmojiNamesList(ASANA);
        animationsNames = getEmojiNamesList(ANIMATIONS);
        phrasesNames = getEmojiNamesList(PHRASES);
        symbolsNames = getEmojiNamesList(SYMBOLS);
        allNames = getAllNames();

        allLayout = new GridLayout(theC);
        asanaLayout = new GridLayout(theC);
        animationsLayout = new GridLayout(theC);
        phrasesLayout = new GridLayout(theC);
        symbolsLayout = new GridLayout(theC);

        allLayout.setLayoutParams(gridParams);
        asanaLayout.setLayoutParams(gridParams);
        animationsLayout.setLayoutParams(gridParams);
        phrasesLayout.setLayoutParams(gridParams);
        symbolsLayout.setLayoutParams(gridParams);

        allLayout.setColumnCount(3);
        asanaLayout.setColumnCount(3);
        animationsLayout.setColumnCount(3);
        phrasesLayout.setColumnCount(3);
        symbolsLayout.setColumnCount(3);

        makeToast(String.valueOf(allNames.length));

        allLayout.setRowCount(allNames.length + 1);
        asanaLayout.setRowCount(asanaNames.length + 1);
        animationsLayout.setRowCount(animationsNames.length + 1);
        phrasesLayout.setRowCount(phrasesNames.length + 1);
        symbolsLayout.setRowCount(symbolsNames.length + 1);

        getAllDrawables();
    }

    /** On click listener for sending a Yogamoji */
    private final OnClickListener SendEmojiListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!(v instanceof ImageView)) {
                log("Returning...");
                return;
            }
            makeToast("Loading...");
            final Thread toSend = new Thread(new EmojiSender(theViews.get(v)));
            toSend.setPriority(Thread.MAX_PRIORITY);
            toSend.start();
        }
    };

    private class EmojiSender implements Runnable {
        private final int counter;

        public EmojiSender(final int counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            final long start = System.currentTimeMillis();
            final Bitmap theImage = theImages.get(ALL_KEY)[counter];

            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(path, "Yogamoji!" + ".png");
                FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                theImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutPutStream);
                fileOutPutStream.flush();
                fileOutPutStream.close();

                final Intent sendEmoji = new Intent(Intent.ACTION_SEND);
                sendEmoji.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendEmoji.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFile.getAbsolutePath()));
                sendEmoji.setType("image/png");

                final Intent theSender = Intent.createChooser(sendEmoji, "Send Yogamoji using ");
                theSender.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                theC.startActivity(theSender);
            } catch (Exception e) {
                e.printStackTrace();
                log(e.toString());
            }

            log("Sent image in: " + (System.currentTimeMillis() - start));
        }
    };


    private void getAllDrawables() {
        theImages.put(ALL_KEY, new Bitmap[allNames.length]);
        theImages.put(ASANA_KEY, new Bitmap[asanaNames.length]);
        theImages.put(ANIMATIONS_KEY, new Bitmap[animationsNames.length]);
        theImages.put(PHRASES_KEY, new Bitmap[phrasesNames.length]);
        theImages.put(SYMBOLS_KEY, new Bitmap[symbolsNames.length]);

        int counter = 0;

        for(int i = 0; i < asanaNames.length; i++, counter++) {
            new EmojiAdder(ASANA_KEY, i, counter).execute(asanaNames[i]);
        }

        for(int i = 0; i < animationsNames.length; i++) {

        for(int i = 0; i < phrasesNames.length; i++, counter++) {
            new EmojiAdder(PHRASES_KEY, i, counter).execute(phrasesNames[i]);
        }

        for(int i = 0; i < symbolsNames.length; i++, counter++) {
            new EmojiAdder(SYMBOLS_KEY, i, counter).execute(symbolsNames[i]);
        }
    }

    protected class AnimationAdder extends AsyncTask<String, Void, ShowGifView> {

        @Override
        protected ShowGifView doInBackground(String... params) {
            try {
                return new ShowGifView(theC, theAssets.open("gifs/" + params[0]));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public void onPostExecute(final ShowGifView theGif) {
            if (theGif == null) {
                log("Return");
                return;
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
            setImageParams(theImage);
            theViews.put(theImage, counter);
            allLayout.addView(theImage);

            log("ADDED IMAGE IN MS:\t" + (System.currentTimeMillis() - startTime));

            final ImageView theImage1 = new ImageView(theC);
            theImage1.setImageBitmap(theBitmap);
            setImageParams(theImage1);
            theViews.put(theImage1, counter);

            if(tag_name.equals(ASANA_KEY)) {
                asanaLayout.addView(theImage1);
            }
            else if(tag_name.equals(PHRASES_KEY)) {
                phrasesLayout.addView(theImage1);
            }
            else if(tag_name.equals(SYMBOLS_KEY)) {
                symbolsLayout.addView(theImage1);
            }
        }
    };

    private void setImageParams(final View theImage) {
        theImage.setMinimumHeight(imageHeight);
        theImage.setMinimumWidth(imageWidth);
        theImage.setPadding(SIDE_MARGIN, 0, 0, SIDE_MARGIN * 2);
        theImage.setOnClickListener(SendEmojiListener);

        if(theImage instanceof ImageView) {
            final ImageView image = (ImageView) theImage;
            image.setCropToPadding(true);
            image.setMaxHeight(imageHeight);
            image.setMaxWidth(imageWidth);
        }
    }

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
                final AnimationEmojis theAnimations = new AnimationEmojis();
                data.putInt("current_page", tabSelected + 1);
                theAnimations.setArguments(data);
                return theAnimations;

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

    public class AnimationEmojis extends EmojiList {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootInflater = inflater.inflate(R.layout.animations_emojis, container, false);
            final ScrollView theScroll = (ScrollView) rootInflater.findViewById(R.id.theScrollView);
            removeParent(animationsLayout);
            try {
                animationsLayout.addView(new ShowGifView(getActivity().getApplicationContext(),
                        getActivity().getAssets().open("gifs/Awake.gif")));
            }
            catch (Exception e) {
                makeToast(e.toString());
            }
            theScroll.addView(animationsLayout);
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

    private void makeToast(final String theMessage) {
        final Toast theToast = Toast.makeText(theC, theMessage, Toast.LENGTH_LONG);
        theToast.setGravity(Gravity.CENTER, 0, 0);
        theToast.show();
    }

    private String[] getAllNames() {
        final LinkedList<String> theNames = new LinkedList<String>();
        theNames.addAll(Arrays.asList(asanaNames));
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