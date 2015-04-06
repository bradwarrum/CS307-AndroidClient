package com.example.android.virtualpantry;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
                    .add(R.id.container, new LoginRegFragment())
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
    public static class LoginRegFragment extends Fragment {

        //UI handles
        private AutoCompleteTextView mEmail;
        private EditText mPrimaryPassword;
        private EditText mConfirmPassword;
        private Button mLoginButton;
        private Button mRegisterButton;
        private Button mSwitchButton;
        private TextView mHeader;
        private LinearLayout mRegisterFields;
        private EditText mFirstName;
        private EditText mLastName;
        private TextView mStatusText;

        //async tasks
        private LoginTask mLoginTask = null;
        private RegisterTask mRegisterTask = null;

        public LoginRegFragment() {
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
            mRegisterButton = (Button) rootView.findViewById(R.id.RegisterButton);
            mSwitchButton = (Button) rootView.findViewById(R.id.LoginRegSwitchModeButton);
            mHeader = (TextView) rootView.findViewById(R.id.LoginRegHeader);
            mRegisterFields = (LinearLayout) rootView.findViewById(R.id.RegisterFields);
            mFirstName = (EditText) rootView.findViewById(R.id.RegisterFirstName);
            mLastName = (EditText) rootView.findViewById(R.id.RegisterLastName);
            mStatusText = (TextView) rootView.findViewById(R.id.LoginPageStatusText);

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
            mRegisterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    register();
                }
            });
            return rootView;
        }

        private void switchModes(){
            //in login mode
            if(mLoginButton.getVisibility() == View.VISIBLE){
                //login mode to register
                mLoginButton.setVisibility(View.GONE);
                mRegisterFields.setVisibility(View.VISIBLE);
                mSwitchButton.setText(R.string.LoginRegisterSwitchButtonText_Login);
                mHeader.setText(R.string.RegisterHeader);
            } else {
                //register mode to login
                mLoginButton.setVisibility(View.VISIBLE);
                mRegisterFields.setVisibility(View.GONE);
                mSwitchButton.setText(R.string.LoginRegisterSwitchButtonText_Register);
                mHeader.setText(R.string.LoginHeader);
            }
        }

        private void login(){
            //make sure we are not running any tasks in the background
            if(mLoginTask != null | mRegisterTask != null){
                return;
            }

            //reset field errors
            mEmail.setError(null);
            mPrimaryPassword.setError(null);

            //get values
            String email = mEmail.getText().toString();
            String password = mPrimaryPassword.getText().toString();

            boolean cancelLogin = false;
            View focusView = null;

            //check password
            if(TextUtils.isEmpty(password) | !isPasswordValid(password)){
                mPrimaryPassword.setError(getString(R.string.InvalidPasswordString));
                focusView = mPrimaryPassword;
                cancelLogin = true;
            }

            //check email
            if(TextUtils.isEmpty(email)){
                mEmail.setError(getString(R.string.EmptyEmail));
                focusView = mEmail;
                cancelLogin = true;
            } else if(!isEmailValid(email)){
                mEmail.setError(getString(R.string.InvalidEmail));
                focusView = mEmail;
                cancelLogin = true;
            }

            if(cancelLogin){
                focusView.requestFocus();
            } else {
                mStatusText.setVisibility(View.VISIBLE);
                mStatusText.setText(getString(R.string.AttemptLogin));
                mLoginTask = new LoginTask(email, password);
                mLoginTask.execute((Void) null);
            }

        }

        private void register(){
            //make sure we are not running any tasks in the background
            if(mLoginTask != null | mRegisterTask != null){
                return;
            }

            //reset field errors
            mEmail.setError(null);
            mPrimaryPassword.setError(null);
            mConfirmPassword.setError(null);

            //get values
            String email = mEmail.getText().toString();
            String password = mPrimaryPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();
            String firstName = mFirstName.getText().toString();
            String lastName = mLastName.getText().toString();

            boolean cancelRegister = false;
            View focusView = null;

            if(TextUtils.isEmpty(firstName)){
                cancelRegister = true;
                mFirstName.setError(getString(R.string.EmptyNameError));
                focusView = mFirstName;
            }

            if(TextUtils.isEmpty(lastName)){
                cancelRegister = true;
                mLastName.setError(getString(R.string.EmptyNameError));
                focusView = mLastName;
            }

            //check password
            if(TextUtils.isEmpty(password) | !isPasswordValid(password)){
                mPrimaryPassword.setError(getString(R.string.InvalidPasswordString));
                focusView = mPrimaryPassword;
                cancelRegister = true;
            }

            if(TextUtils.isEmpty(confirmPassword) | !password.equals(confirmPassword)){
                mConfirmPassword.setError(getString(R.string.PasswordsDoNotMatch));
                focusView = mConfirmPassword;
                cancelRegister = true;
            }

            //check email
            if(TextUtils.isEmpty(email)){
                mEmail.setError(getString(R.string.EmptyEmail));
                focusView = mEmail;
                cancelRegister = true;
            } else if(!isEmailValid(email)){
                mEmail.setError(getString(R.string.InvalidEmail));
                focusView = mEmail;
                cancelRegister = true;
            }

            if(cancelRegister){
                focusView.requestFocus();
            } else {
                mStatusText.setVisibility(View.VISIBLE);
                mStatusText.setText(getString(R.string.AttemptRegister));
                mRegisterTask = new RegisterTask(email, password, firstName, lastName);
                mRegisterTask.execute((Void) null);
            }
        }

        private boolean isEmailValid(String email){
            if(!email.contains("@"))
                return false;
            if(email.charAt(0) == '@')
                return false;
            if(email.charAt(email.length()-1) == '@')
                return false;
            return true;
        }

        private boolean isPasswordValid(String password){
            return true;
        }

        private void requestFailed(){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(R.string.LoginRegRequestFailed);
        }

        private void invalidPassword(){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.InvalidPasswordString));
            mPrimaryPassword.setError(getString(R.string.InvalidPasswordString));
            View focusView = mPrimaryPassword;
            focusView.requestFocus();
        }

        private void invalidUsername(){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.InvalidEmail));
            mEmail.setError(getString(R.string.InvalidEmail));
            View focusView = mEmail;
            focusView.requestFocus();
        }

        private void malformedInput(String input){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.MalformedInput) + "\n" + input);
        }

        private void emailTaken(){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(R.string.EmailTaken);
            mEmail.setError(getString(R.string.EmailTaken));
            View focusView = mEmail;
            focusView.requestFocus();
        }

        private void loginSuccessful(String response){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(R.string.LoginSuccess);
        }

        private void registerSuccessful(String response){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.RegisterSuccess));
        }

        private void unknownRequestError(int code, String response){
            mStatusText.setVisibility(View.VISIBLE);
            mStatusText.setText(getString(R.string.UnknownRequestError) + code + "\n" + response);
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

            @Override
            protected void onPostExecute(Integer result) {
                mLoginTask = null;
                if(result == 200){
                    loginSuccessful(request.getResponse());
                } else {
                    int errorCode = JSONModels.gson.fromJson(request.getResponse(), JSONModels.ErrorResponseJSON.class).errorCode;
                    switch (errorCode) {
                    case Request.ERR_REQUEST_FAILED:
                        requestFailed();
                        break;
                    case Request.ERR_INVALID_PASSWORD:
                        invalidPassword();
                        break;
                    case Request.ERR_USER_NOT_FOUND:
                        invalidUsername();
                        break;
                    case Request.ERR_INVALID_PAYLOAD:
                        malformedInput(request.getSendJSON());
                        break;
                    default:
                        unknownRequestError(request.getResponseCode(), request.getResponse());
                }
                }
            }
        }

        public class RegisterTask extends AsyncTask<Void, Void, Integer> {

            private static final String LOG_TAG = "RegisterTask";
            private final String mEmail;
            private final String mPassword;
            private final String mFirstName;
            private final String mLastName;
            private Request request;

            RegisterTask(String email, String password, String firstName, String lastName){
                mEmail = email;
                mPassword = password;
                mFirstName = firstName;
                mLastName = lastName;
                String encryptedPassword = NetworkUtility.sha256(mPassword);
                request = new Request(
                        NetworkUtility.REGISTER_FILE_PATH,
                        Request.POST,
                        new JSONModels.RegisterReqJSON(mEmail, encryptedPassword, mFirstName, mLastName));
            }

            @Override
            protected Integer doInBackground(Void... params) {
                if(request.openConnection()){
                    request.execute();
                    return request.getResponseCode();
                } else {
                    //request failed
                    Log.e(LOG_TAG, "Failed to Register due to network error");
                    return Request.ERR_REQUEST_FAILED;
                }

            }

            @Override
            protected void onPostExecute(Integer result) {
                mLoginTask = null;

                if(result == 201){
                    registerSuccessful(request.getResponse());
                } else {
                    Log.e(LOG_TAG, "Response code was: " + result);
                    Log.e(LOG_TAG, "Response was: " + request.getResponse());
                    int errorCode = JSONModels.gson.fromJson(request.getResponse(), JSONModels.ErrorResponseJSON.class).errorCode;
                    switch (errorCode) {
                        case Request.ERR_EMAIL_TAKEN:
                            emailTaken();
                            break;
                        case Request.ERR_INVALID_PAYLOAD:
                            malformedInput(request.getSendJSON());
                            break;
                        default:
                            unknownRequestError(request.getResponseCode(), request.getResponse());
                    }
                }
            }
        }
    }
}
