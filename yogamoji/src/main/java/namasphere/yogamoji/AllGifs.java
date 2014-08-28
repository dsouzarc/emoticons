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
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
            new Thread(new SendAnimation(theGifView.getGifName())).start();
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
                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.gif"));
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                emailIntent.setType("image/gif");
                startActivity(Intent.createChooser(emailIntent, "Send Animation"));
            }
            catch(Exception e) {
                e.printStackTrace();
                log("Error sending: " + e.toString());
            }
        }
    };

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
