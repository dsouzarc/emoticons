package namasphere.yogamoji;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AllGifs extends Activity {

    private static final int PADDING = 16;

    private Context theC;
    private String[] fileNames;
    private AssetManager theAssets;
    private LinearLayout theLayout;

    private static final LinearLayout.LayoutParams gifLayoutParam =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

    final LinearLayout.LayoutParams gifLinearLayoutParam =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_gifs);
        initializeVariables();
    }

    private void initializeVariables() {
        this.theC = getApplicationContext();
        this.theAssets = getAssets();

        this.theLayout = new LinearLayout(theC);
        this.theLayout.setOrientation(LinearLayout.VERTICAL);
        this.theLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        final Bundle fromPrevious = getIntent().getExtras();
        fileNames = fromPrevious.getStringArray("fileNames");

        final ScrollView theSV = (ScrollView) findViewById(R.id.theScrollView);
        theSV.addView(theLayout);

        gifLinearLayoutParam.weight = 2;
        gifLayoutParam.weight = 1;

        for(int i = 0; i < fileNames.length - 1; i++) {
            final LinearLayout tempLayout = new LinearLayout(theC);
            tempLayout.setLayoutParams(gifLinearLayoutParam);
            tempLayout.setOrientation(LinearLayout.HORIZONTAL);
            theLayout.addView(tempLayout);
            new AnimationAdder(tempLayout).execute(fileNames[i]);
            new AnimationAdder(tempLayout).execute(fileNames[++i]);
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
                startActivity(Intent.createChooser(emailIntent, "Send Animation"));
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

        private final LinearLayout theLayout;
        public AnimationAdder(LinearLayout tl) {
            this.theLayout = tl;
        }

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
            theGif.setMinimumHeight(500);
            theGif.setMinimumWidth(500);
            theGif.setPadding(PADDING, PADDING, PADDING, 0);
            theGif.setOnClickListener(startAnimationListener);
            theGif.setOnLongClickListener(sendAnimationListener);
            theGif.setLayoutParams(gifLayoutParam);
            theLayout.addView(theGif);
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

    private void makeToast(final String theMessage) {
        final Toast theToast = Toast.makeText(theC, theMessage, Toast.LENGTH_LONG);
        theToast.setGravity(Gravity.CENTER, 0, 0);
        theToast.show();
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
