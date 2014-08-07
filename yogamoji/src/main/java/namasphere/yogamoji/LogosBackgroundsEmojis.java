package namasphere.yogamoji;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public class LogosBackgroundsEmojis extends EmojiList {

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
            aLayout.setWeightSum(3);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final String[] fileNames = super.getEmojiNamesList(super.LOGOSBACKGROUNDS);

        View rootInflater = inflater.inflate(R.layout.logos_backgrounds_emojis, container, false);
        theC  = getActivity().getApplicationContext();

        theLayout = (LinearLayout) rootInflater.findViewById(R.id.emojisLL);
        super.setLayout(theLayout);

        for(int i = 0; i < fileNames.length; i++)
            new EmojiAdder(fileNames[i]).execute(fileNames[i]);

        return rootInflater;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}