package namasphere.yogamoji;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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

public abstract class EmojiList extends Fragment {

    public static final String ASANA = "asana.txt";
    public static final String LOGOSBACKGROUNDS = "logos_and_backgrounds.txt";
    public static final String PHRASES = "phrases.txt";
    public static final String SYMBOLS = "symbols.txt";

    protected static final int SIZE = 200;

    protected Context theC;
    protected AssetManager theAssets;
    protected LinearLayout theLayout;

    /** Returns a String array
     * of all the names of the Emojis in that textfile
     */
    protected String[] getEmojiNamesList(final String fileName) {
        theC = getActivity().getApplicationContext();
        theAssets = theC.getAssets();

        try {
            //LinkedLists are best for adding unknown amounts of data
            final LinkedList<String> theFileNames = new LinkedList<String>();
            InputStreamReader theISR =
                    new InputStreamReader(getActivity().getAssets().open(fileName));
            BufferedReader theReader = new BufferedReader(theISR);

            while(theReader.ready())
                theFileNames.add(theReader.readLine());

            return theFileNames.toArray(new String[theFileNames.size()]);
        }

        catch (Exception e) { e.printStackTrace(); return new String[]{e.toString()}; }
    }

    /** For sending Yogamojis
     * Using filename, re-reads entire Image
     * Writes it temporarily, sends it
     */
    protected final class EmojiSender extends AsyncTask<String, Void, Uri> {
        private String fileName;

        @Override
        public Uri doInBackground(final String... fileNames) {
            this.fileName = fileNames[0];

            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(path, "Yogamoji!" + ".png");
                FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                BitmapFactory.decodeStream(theAssets.open("emojis/" + fileName)).
                        compress(Bitmap.CompressFormat.PNG, 100, fileOutPutStream);

                fileOutPutStream.flush();
                fileOutPutStream.close();

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
                final Intent sendEmoji = new Intent(Intent.ACTION_SEND);
                sendEmoji.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendEmoji.putExtra(Intent.EXTRA_STREAM, theUri);
                sendEmoji.setType("image/png");
                startActivity(Intent.createChooser(sendEmoji, "Send Yogamoji using "));
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
        private String emojiName;
        private String emojiFileName;

        public EmojiAdder(String emojiName) {
            this.emojiFileName = emojiName;
            this.emojiName = reformatEmojiName(emojiName);
        }

        @Override
        public Bitmap doInBackground(String... fileName) {
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
            if(theBitmap == null)
                return;

            final LinearLayout aLayout = new LinearLayout(theC);
            aLayout.setOrientation(LinearLayout.HORIZONTAL);
            aLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            aLayout.setWeightSum(1);

            final TextView emojiName = new TextView(theC);
            emojiName.setText(this.emojiName);
            emojiName.setTextSize(25);
            emojiName.setPadding(30, 10, 0, 0);
            emojiName.setGravity(Gravity.CENTER_VERTICAL);
            emojiName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            emojiName.setTextColor(Color.parseColor("#ff0099cc"));

            final SendEmoji theListener = new SendEmoji(emojiFileName);
            emojiName.setOnClickListener(theListener);

            final ImageView theImage = new ImageView(getActivity().getApplicationContext());
            theImage.setImageBitmap(theBitmap);
            theImage.setOnClickListener(theListener);
            aLayout.addView(theImage);
            aLayout.addView(emojiName);
            aLayout.setPadding(0, 40, 0, 0);

            theLayout.addView(aLayout);
            log("ADDED IMAGE IN MS:\t" + (System.currentTimeMillis() - startTime));
        }
    };

    /** On click listener, starts
     * EmojiSender asynctask
     */
    protected final class SendEmoji implements View.OnClickListener {
        private String fileName;
        public SendEmoji(final String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void onClick(View theView) {
            new EmojiSender().execute(fileName);
            makeToast("Sending " + reformatEmojiName(fileName) + " Yogamoji");
        }
    }

    /** Returns String of formatted emojiname
     */
    protected final static String reformatEmojiName(String emojiName) {
        emojiName = emojiName.toLowerCase();
        emojiName = emojiName.replace("_", " ");
        emojiName = emojiName.replace(".png", "");
        emojiName = emojiName.replace(".jpg", "");

        final char[] chars = emojiName.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }
    protected final void setLayout(final LinearLayout theLayout) {
        this.theLayout = theLayout;
    }

    protected final void makeToast(final String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    protected final void log(final String message) {
        Log.e("com.namasphere.yogamoji", message);
    }
}