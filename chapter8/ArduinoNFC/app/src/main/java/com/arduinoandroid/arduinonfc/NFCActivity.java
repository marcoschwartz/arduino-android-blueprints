package com.arduinoandroid.arduinonfc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.Charset;


public class NFCActivity extends Activity {

    //Declaring the User Interface Variables for mStatusText as a TextView
    private TextView mStatusText;
    private TextView messageToBeam;
    private Button switchOn;
    private Button switchOff;

    //Initializing the NFC Adapater for sending messages
    NfcAdapter mNfcAdapter;
    private static final int BEAM_BEAMED = 0x1001;
    public static final String MIMETYPE = "application/com.arduinoandroid.arduinonfc";

    //Keys for Opening and Closing the Relay
    String open_key = "oWnHV6uXre";
    String close_key = "C19HNuqNU4";

    //Getting the name for Log Tags
    private final String TAG = NFCActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        mStatusText = (TextView) findViewById(R.id.nfcTextStatus);
        messageToBeam = (TextView) findViewById(R.id.messageToBeam);
        switchOn = (Button) findViewById(R.id.switchOnBtn);
        switchOff = (Button) findViewById(R.id.switchOffBtn);

        // Adding OnClick Listeners to the Buttons
        switchOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToBeam.setText(open_key);
            }
        });

        switchOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToBeam.setText(close_key);
            }
        });

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            mStatusText.setText("NFC is not available on this device.");
        }

        // Register to create and NDEF message when another device is in range
        mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                //the variable message is from the EditText field
                String message = messageToBeam.getText().toString();
                String text = (message);
                byte[] mime = MIMETYPE.getBytes(Charset.forName("US-ASCII"));
                NdefRecord mimeMessage = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA, mime, new byte[0], text
                        .getBytes());
                NdefMessage msg = new NdefMessage(
                        new NdefRecord[]{
                                mimeMessage,
                                NdefRecord
                                        .createApplicationRecord("com.arduinoandroid.arduinonfc")});
                return msg;
            }
        }, this);

        // And handle the send status
        mNfcAdapter.setOnNdefPushCompleteCallback(
                new NfcAdapter.OnNdefPushCompleteCallback() {

                    @Override
                    public void onNdefPushComplete(NfcEvent event) {
                        mHandler.obtainMessage(BEAM_BEAMED).sendToTarget();
                    }
                }, this);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BEAM_BEAMED:
                    mStatusText.setText("Your message has been beamed");
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Did we receive an NDEF message?

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            try {
                Parcelable[] rawMsgs = intent
                        .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                // we created the message, so we know the format
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                NdefRecord[] records = msg.getRecords();
                byte[] firstPayload = records[0].getPayload();
                String message = new String(firstPayload);
                mStatusText.setText(message);
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving beam message.", e);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // handle singleTop so we don't launch a number of instances...
        setIntent(intent);
    }
}



