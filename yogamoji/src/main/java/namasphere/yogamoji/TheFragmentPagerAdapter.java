package namasphere.yogamoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;

public class TheFragmentPagerAdapter extends FragmentStatePagerAdapter {

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
    private static final int PAGE_COUNT = 4;
    private static final int PADDING = 16;

    private static final LinearLayout.LayoutParams gifLayoutParam =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

    private static final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
    private static final  GridLayout.LayoutParams imageParams = new GridLayout.LayoutParams();

    private final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    private final Context theC;
    private final AssetManager theAssets;

    private final String[] allNames, asanaNames, animationsNames, phrasesNames, symbolsNames;
    private final GridLayout asanaLayout, animationsLayout, phrasesLayout, symbolsLayout;

    private final int width, height, imageWidth, imageHeight, SIDE_MARGIN;
    private final int animationWidth, animationSIDE_MARGIN;

    public TheFragmentPagerAdapter(final FragmentManager fm, final Context theC, final int width, final int height) {
        super(fm);
        this.theC = theC;
        theAssets = theC.getAssets();
        this.width = width;
        this.height = height;
        this.SIDE_MARGIN = (this.width - (3 * SIZE))/5;
        this.imageWidth = (this.width - (3 * SIZE))/3;
        this.animationWidth = (this.width - (2 * SIZE))/4;
        this.animationSIDE_MARGIN = (this.width - (2 * SIZE))/2;
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

        asanaLayout = new GridLayout(theC);
        animationsLayout = new GridLayout(theC);
        phrasesLayout = new GridLayout(theC);
        symbolsLayout = new GridLayout(theC);

        asanaLayout.setLayoutParams(gridParams);
        animationsLayout.setLayoutParams(gridParams);
        phrasesLayout.setLayoutParams(gridParams);
        symbolsLayout.setLayoutParams(gridParams);

        asanaLayout.setColumnCount(3);
        animationsLayout.setColumnCount(2);
        phrasesLayout.setColumnCount(3);
        symbolsLayout.setColumnCount(3);

        asanaLayout.setRowCount(asanaNames.length + 1);
        animationsLayout.setRowCount(animationsNames.length + 2);
        phrasesLayout.setRowCount(phrasesNames.length + 1);
        symbolsLayout.setRowCount(symbolsNames.length + 1);

        getAllDrawables();
        log("WIDTH: " + imageWidth);
        log("HEIGHT: " + imageHeight);
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
                final File imageFile = new File(path, "Yogamoji!" + ".png");
                final FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                theImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutPutStream);
                fileOutPutStream.flush();
                fileOutPutStream.close();

                final Intent sendEmoji = new Intent(Intent.ACTION_SEND);
                sendEmoji.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendEmoji.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFile.getAbsolutePath()));
                sendEmoji.setType("image/png");

                final Intent theSender = Intent.createChooser(sendEmoji, "Send Yoga Moji using ");
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
        final Thread getImages1 = new Thread(new GetImages(0));
        getImages1.start();
        final Thread getImages2 = new Thread(new GetImages(1));
        getImages2.start();
        final Thread getImages3 = new Thread(new GetImages(2));
        getImages3.start();

        for(int i = 0; i < animationsNames.length; i++) {
            new AnimationAdder().execute(animationsNames[i]);
        }
    }

    private class GetImages implements Runnable {
        private final BitmapFactory.Options o = new BitmapFactory.Options();
        private final BitmapFactory.Options o2 = new BitmapFactory.Options();

        private final int startAt;

        public GetImages(final int startAt) {
            this.startAt = startAt;
        }

        @Override
        public void run() {
            for(int i = startAt; i < allNames.length; i+= 3) {
                try {
                    new AddToDisplay(i, getBitmap(allNames[i])).execute();
                }
                catch (Exception e) {
                    log(e.toString());
                    e.printStackTrace();
                }
            }
        }

        private Bitmap getBitmap(final String fileName) {

            try {
                return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(theAssets.open("emojis/" + fileName)),
                        SIZE, SIZE, false);
            }
            catch (Exception e) {
                log("Error at getBitmap" + e.toString());
                e.printStackTrace();
                return null;
            }

            /*try {
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(theAssets.open("emojis/" + fileName), null, o);
                //Find the correct scale value. It should be the power of 2.
                final int REQUIRED_SIZE = 70;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale++;
                }

                //decode with inSampleSize
                o2.inSampleSize = scale;
                return BitmapFactory.decodeStream(theAssets.open("emojis/" + fileName), null, o2);
            }
            catch (Exception e) {
                log("HERE ERROR: " + e.toString());
                e.printStackTrace();
                return null;
            }*/
        }
    }

    private final OnClickListener startAnimationListener = new OnClickListener(){
        @Override
        public void onClick(View v) {
            final ShowGifView theGifView = (ShowGifView) v;
            theGifView.startAnimation();
        }
    };

    private final OnLongClickListener sendAnimationListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            makeToast("Loading");
            final ShowGifView theGifView = (ShowGifView) v;
            new Thread(new SendAnimation(theGifView.getGifName())).start();
            return false;
        }
    };

    private class SendAnimation implements Runnable {
        private final String gifName;
        public SendAnimation(final String gifName) {
            this.gifName = gifName;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = theAssets.open("gifs/" + gifName);
                    out = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "image.gif"));
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                    e.printStackTrace();
                }

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                final Uri uri =
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.gif"));
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                emailIntent.setType("image/gif");
                theC.startActivity(Intent.createChooser(emailIntent, "Send Animation Using"));
            }
            catch(Exception e) {
                e.printStackTrace();
                log("Error sending: " + e.toString());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    protected class AnimationAdder extends AsyncTask<String, Void, ShowGifView> {
        @Override
        protected ShowGifView doInBackground(String... params) {
            InputStream theIS = null;

            try {
                theIS = theAssets.open("gifs/" + params[0]);
                return new ShowGifView(theC, theIS, params[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (theIS != null) {
                        theIS.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log("Problem: " + params[0]);

            return null;
        }

        @Override
        public void onPostExecute(final ShowGifView theGif) {
            if (theGif == null) {
                log("Return");
                return;
            }
            theGif.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            theGif.setAdjustViewBounds(true);
            theGif.setMinimumHeight(imageWidth);
            theGif.setMinimumWidth(imageWidth);
            theGif.setPadding(PADDING, PADDING, animationSIDE_MARGIN, 0);
            theGif.setOnClickListener(startAnimationListener);
            theGif.setOnLongClickListener(sendAnimationListener);
            theGif.setLayoutParams(gifLayoutParam);
            animationsLayout.addView(theGif);
        }
    }

    private class AddToDisplay extends AsyncTask<Void, Void, ImageView> {
        private final int counter;
        private final Bitmap theBM;

        public AddToDisplay(final int counter, final Bitmap theBM) {
            this.counter = counter;
            this.theBM = theBM;
        }

        @Override
        public ImageView doInBackground(Void... params) {

            if(theBM == null) {
                log("Bitmap NULL");
                return null;
            }
            final ImageView theImage = new ImageView(theC);
            theImage.setImageBitmap(theBM);
            setImageParams(theImage);
            return theImage;
        }

        @Override
        public void onPostExecute(final ImageView theView) {
            if(theView == null) {
                return;
            }
            if(counter < asanaNames.length) {
                asanaLayout.addView(theView);
            }
            else if(counter < (phrasesNames.length + asanaNames.length) && counter >= asanaNames.length) {
                phrasesLayout.addView(theView);
            }
            else if(counter >= (phrasesNames.length + asanaNames.length)) {
                symbolsLayout.addView(theView);
            }
        }
    }

    private void setImageParams(final ImageView theImage) {
        theImage.setPadding(SIDE_MARGIN, 0, 0, SIDE_MARGIN * 2);
        theImage.setOnClickListener(SendEmojiListener);
        theImage.setCropToPadding(true);
        theImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        theImage.setMinimumHeight(imageHeight);
        theImage.setMinimumWidth(imageWidth);
        theImage.setMaxHeight(imageHeight);
        theImage.setMaxWidth(imageWidth);

        try {
            theImage.getLayoutParams().height = imageHeight;
            theImage.getLayoutParams().width = imageWidth;
        }
        catch (Exception e) {
        }
    }

    @Override
    public Fragment getItem(int tabSelected) {
        Bundle data = new Bundle();
        
        switch(tabSelected) {
            case 0:
                final AsanaEmojis theAE = new AsanaEmojis();
                data.putInt("current_page", tabSelected+1);
                theAE.setArguments(data);
                return theAE;

            case 1:
                final AnimationEmojis theAnimations = new AnimationEmojis();
                data.putInt("current_page", tabSelected + 1);
                theAnimations.setArguments(data);
                return theAnimations;

            case 2:
                PhrasesEmojis thePhrases = new PhrasesEmojis();
                data.putInt("current_page", tabSelected+1);
                thePhrases.setArguments(data);
                return thePhrases;

            case 3:
                SymbolsEmojis theSymbols = new SymbolsEmojis();
                data.putInt("current_page", tabSelected + 1);
                theSymbols.setArguments(data);
                return theSymbols;
        }

        return null;
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
            theScroll.addView(animationsLayout);

            return rootInflater;
        }

        @Override
        public void setMenuVisibility(final boolean visible) {
            super.setMenuVisibility(visible);
            if (visible) {
                //viewAnimations(getActivity());
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