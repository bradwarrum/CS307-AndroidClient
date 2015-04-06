package com.example.android.virtualpantry;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;


public class LoginRegisterActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_register, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private AutoCompleteTextView mEmail;
        private EditText mPrimaryPassword;
        private EditText mConfirmPassword;
        private Button mLoginButton;
        private Button mRegsiterButton;
        private Button mSwitchButton;
        private TextView mHeader;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login_register, container, false);

            //grab all the handles
            mEmail = (AutoCompleteTextView) rootView.findViewById(R.id.LoginRegEmail);
            mPrimaryPassword = (EditText) rootView.findViewById(R.id.LoginPasswordMain);
            mConfirmPassword = (EditText) rootView.findViewById(R.id.LoginPasswordConfirm);
            mLoginButton = (Button) rootView.findViewById(R.id.LoginButton);
            mRegsiterButton = (Button) rootView.findViewById(R.id.RegisterButton);
            mSwitchButton = (Button) rootView.findViewById(R.id.LoginRegSwitchModeButton);
            mHeader = (TextView) rootView.findViewById(R.id.LoginRegHeader);

            //setup click listeners
            mSwitchButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    switchModes();
                }
            });
            mLoginButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    login();
                }
            });
            mRegsiterButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    register();
                }
            });
            return rootView;
        }

        private void switchModes(){
            //in login mode
            if(mLoginButton.getVisibility() == View.VISIBLE){
                //login mode
                mLoginButton.setVisibility(View.GONE);
                mRegsiterButton.setVisibility(View.VISIBLE);
                mSwitchButton.setText(R.string.LoginRegisterSwitchButtonText_Register);
                mHeader.setText(R.string.RegiserHeader);
            } else {
                //register mode
                mLoginButton.setVisibility(View.VISIBLE);
                mRegsiterButton.setVisibility(View.GONE);
                mSwitchButton.setText(R.string.LoginRegisterSwitchButtonText_Login);
                mHeader.setText(R.string.LoginHeader);
            }
        }

        private void login(){

        }

        private void register(){

        }

        public class LoginTask extends AsyncTask<Void, Void, Integer> {

            private static final String LOG_TAG = "LoginTask";
            private final String mEmail;
            private final String mPassword;
            private Request request;

            LoginTask(String email, String password){
                mEmail = email;
                mPassword = password;
                String encryptedPassword = NetworkUtility.sha256(mPassword);
                request = new Request(
                        NetworkUtility.LOGIN_FILE_PATH,
                        Request.POST,
                        new JSONModels.LoginReqJSON(mEmail, encryptedPassword));
            }

            @Override
            protected Integer doInBackground(Void... params) {
                if(request.openConnection()){
                    request.execute();
                    return request.getResponseCode();
                } else {
                    //request failed
                    Log.e(LOG_TAG, "Failed to login due to network error");
                    return Request.ERR_REQUEST_FAILED;
                }

            }

            //todo: post execute options
            @Override
            protected void onPostExecute(Integer result) {

            }
        }
    }
}
