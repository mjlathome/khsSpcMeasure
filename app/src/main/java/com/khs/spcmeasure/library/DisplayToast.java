package com.khs.spcmeasure.library;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by mlees on 4/8/2016.
 * see: http://stackoverflow.com/questions/5346980/intentservice-wont-show-toast
 */
public class DisplayToast implements Runnable{
    private final Context mContext;
    private String mText;

    public DisplayToast(Context mContext, String text){
        this.mContext = mContext;
        mText = text;
    }

    public void run(){
        Toast.makeText(mContext, mText, Toast.LENGTH_LONG).show();
    }
}
