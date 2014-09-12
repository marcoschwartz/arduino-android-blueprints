package com.arduinoandroid.arduinonfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class NFCActivity extends Activity {

    //Declaring the variable for mStatusText as a TextView
    private TextView mStatusText;

    //Getting the name for Log Tags
    private final String DEBUG_TAG = NFCActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
    }

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

                mStatusText = (TextView) findViewById(R.id.nfcTextStatus);

                mStatusText.setText(message);

            } catch (Exception e) {

                Log.e(DEBUG_TAG, "Error retrieving beam message.", e);

            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nfc, menu);
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
