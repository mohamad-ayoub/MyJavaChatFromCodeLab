package com.example.myjavachatfromcodelab;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;

public class SendButtonObserver implements TextWatcher {
    private ImageView sendButton;

    public SendButtonObserver(ImageView sendButton) {
        this.sendButton = sendButton;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length()>0) {
            sendButton.setEnabled(true);
            sendButton.setImageResource(R.drawable.outline_send_24);
        } else {
            sendButton.setEnabled(false);
            sendButton.setImageResource(R.drawable.outline_send_gray_24);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
