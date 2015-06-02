package com.b2msolutions.reynaexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.b2msolutions.reyna.Header;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.services.StoreService;

import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends ActionBarActivity {

    Button ui_storeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.ui_storeButton = (Button) findViewById(R.id.store_message_button);
        StoreService.setLogLevel(Log.VERBOSE);

        this.ui_storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = GetUrl();
                if(url == null || url.isEmpty()){
                    Toast toast = Toast.makeText(MainActivity.this, R.string.emptyUrl, Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    StoreAndSend(url);
                }
            }
        });
    }

    private String GetUrl() {
        EditText editText = (EditText)findViewById(R.id.urlEdit);
        return editText.getText().toString();
    }

    private void StoreAndSend(String url) {
        try {
            Header[] headers = new Header[]{
                    new Header("Content-Type", "application/json"),
            };

            Message message = new Message(new URI(url), "", headers);
            StoreService.start(MainActivity.this, message);
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
