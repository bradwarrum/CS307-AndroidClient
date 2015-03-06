package com.example.android.virtualpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class BarcodeActivity extends Activity implements OnClickListener {

    private Button button;
    private TextView format;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        button = (Button) findViewById(R.id.scan_button);
        format = (TextView) findViewById(R.id.scan_format);
        content = (TextView) findViewById(R.id.scan_content);

        button.setOnClickListener(this);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.scan_button) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.initiateScan();
        }
    }

    @Override
    public void onActivityResult(int request, int result, Intent intent) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(request, result, intent);

        if (intentResult != null) {
            String scanFormat = intentResult.getFormatName();
            String scanContent = intentResult.getContents();
            format.setText("Format: " + scanFormat);
            content.setText("Content: " + scanContent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No data.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
