package namasphere.yogamoji;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AsanaEmojis extends EmojiList {
    private LinearLayout theLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final String[] fileNames = super.getEmojiNamesList(super.ASANA);

        View rootInflater = inflater.inflate(R.layout.asana_emojis, container, false);
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
