package namasphere.yogamoji;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.widget.Button;
import android.support.v4.app.FragmentActivity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.Arrays;
import java.util.LinkedList;

import android.util.Log;
import android.net.Uri;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ScrollView;

import java.io.InputStream;

public class AllGifs extends Activity {

    private Context theC;
    private GridLayout theGrid;
    private String[] fileNames;
    private AssetManager theAssets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_gifs);

        initializeVariables();
    }

    private void initializeVariables() {
        this.theC = getApplicationContext();
        this.theGrid = new GridLayout(theC);
        this.theAssets = getAssets();

        final GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
        gridParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        gridParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        theGrid.setLayoutParams(gridParams);
        theGrid.setColumnCount(2);

        final Bundle fromPrevious = getIntent().getExtras();
        fileNames = fromPrevious.getStringArray("fileNames");

        final ScrollView theSV = (ScrollView) findViewById(R.id.theScrollView);
        theSV.addView(theGrid);

        for(String fileName : fileNames) {
            new AnimationAdder().execute(fileName);
        }
    }

    private final OnClickListener sendGifListener = new OnClickListener(){

        @Override
        public void onClick(View v) {
            final ShowGifView theGifView = (ShowGifView) v;

            try {

                final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File imageFile = new File(path, "Yogamoji!" + ".gif");
                FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                fileOutPutStream.write(ShowGifView.streamToBytes((theGifView.getGifInputStream())));
                fileOutPutStream.flush();
                fileOutPutStream.close();

                /*final Intent sendEmoji = new Intent(Intent.ACTION_SEND);
                sendEmoji.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendEmoji.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFile.getAbsolutePath()));
                sendEmoji.setType("image/png");

                final Intent theSender = Intent.createChooser(sendEmoji, "Send Yogamoji using ");
                theSender.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/


                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
                sendIntent.putExtra("address", "1213123123");
                sendIntent.putExtra("sms_body", "if you are sending text");

                Uri uri = Uri.fromFile(imageFile);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType("video/*");
                startActivity(sendIntent);
            }
            catch(Exception e) {
                e.printStackTrace();
                log("Error sending: " + e.toString());
            }

        }
    };

    protected class AnimationAdder extends AsyncTask<String, Void, ShowGifView> {
        @Override
        protected ShowGifView doInBackground(String... params) {

            InputStream theIS = null;

            try {
                theIS = theAssets.open("gifs/" + params[0]);
                return new ShowGifView(theC, theIS);
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
            theGif.setMaxHeight(100);
            theGif.setMaxWidth(100);
            theGif.setOnClickListener(sendGifListener);
            theGrid.addView(theGif);
        }
    }

    public void log(final String message) {
        Log.e("com.NamaSphere.yogamoji", message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.all_gifs, menu);
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
