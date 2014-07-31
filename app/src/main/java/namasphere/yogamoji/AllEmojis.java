package namasphere.yogamoji;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.LinkedList;

/** Written by Ryan D'souza
 * Shows all Emojis
 */

public class AllEmojis extends EmojiList {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final String[] fileNames = getFileNames();
        Arrays.sort(fileNames);
        View rootInflater = inflater.inflate(R.layout.symbols_emojis, container, false);
        theC  = getActivity().getApplicationContext();

        theLayout = (LinearLayout) rootInflater.findViewById(R.id.emojisLL);
        super.setLayout(theLayout);

        for(int i = 0; i < fileNames.length; i++)
            new EmojiAdder(fileNames[i]).execute(fileNames[i]);

        return rootInflater;
    }

    private final String[] getFileNames() {
        final LinkedList<String> theList = new LinkedList<String>();
        theList.addAll(Arrays.asList(super.getEmojiNamesList(super.ASANA)));
        theList.addAll(Arrays.asList(super.getEmojiNamesList(super.LOGOSBACKGROUNDS)));
        theList.addAll(Arrays.asList(super.getEmojiNamesList(super.PHRASES)));
        theList.addAll(Arrays.asList(super.getEmojiNamesList(super.SYMBOLS)));

        return theList.toArray(new String[theList.size()]);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
