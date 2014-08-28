package namasphere.yogamoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.VideoView;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

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

    private final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

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
        animationsLayout.setColumnCount(2);
        phrasesLayout.setColumnCount(3);
        symbolsLayout.setColumnCount(3);

        makeToast(String.valueOf(allNames.length));

        allLayout.setRowCount(allNames.length + 1);
        asanaLayout.setRowCount(asanaNames.length + 1);
        animationsLayout.setRowCount(animationsNames.length + 2);
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
            final Thread toSend = new Thread(new EmojiSender((ImageView)v));
            toSend.setPriority(Thread.MAX_PRIORITY);
            toSend.start();
        }
    };

    private class EmojiSender implements Runnable {
        private final Bitmap theImage;

        public EmojiSender(final ImageView theIV) {
            this.theImage = ((BitmapDrawable) theIV.getDrawable()).getBitmap();
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            final long start = System.currentTimeMillis();

            try {
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

        int counter = 0;

        for(int i = 0; i < asanaNames.length; i++, counter++) {
            new EmojiAdder(ASANA_KEY, i, counter).execute(asanaNames[i]);
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

            InputStream theIS = null;

            try {
                theIS = theAssets.open("emojis/" + fileName[0]);
                return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(theIS),
                      SIZE, SIZE, false);
            }
            catch (Exception e) {
                log(e.toString());
                e.printStackTrace();
            }
            finally {
                if(theIS != null) {
                    try {
                        theIS.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(final Bitmap theBitmap) {
            if(theBitmap == null) {
                log("Return");
                return;
            }

            final ImageView theImage = new ImageView(theC);
            theImage.setImageBitmap(theBitmap);
            setImageParams(theImage);
            allLayout.addView(theImage);

            log("ADDED IMAGE IN MS:\t" + (System.currentTimeMillis() - startTime));

            final ImageView theImage1 = new ImageView(theC);
            theImage1.setImageBitmap(theBitmap);
            setImageParams(theImage1);

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
    }

    private void setImageParams(final ImageView theImage) {
        theImage.setMinimumHeight(imageHeight);
        theImage.setMinimumWidth(imageWidth);
        theImage.setPadding(SIDE_MARGIN, 0, 0, SIDE_MARGIN * 2);
        theImage.setOnClickListener(SendEmojiListener);
        theImage.setCropToPadding(true);
        theImage.setMaxHeight(imageHeight);
        theImage.setMaxWidth(imageWidth);
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
            return rootInflater;
        }

        @Override
        public void setMenuVisibility(final boolean visible) {
            super.setMenuVisibility(visible);
            if (visible) {
                final Intent toSomewhere = new Intent(getActivity(), AllGifs.class);
                toSomewhere.putExtra("fileNames", animationsNames);
                startActivity(toSomewhere);
            }
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