package com.khs.spcmeasure.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.service.SimpleCodeService;
import com.khs.spcmeasure.tasks.CheckVersionTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "LoginActivity";

    private static final String TAG_LDAP_AUTH  = "ldapAuth";
    private static final String TAG_CAN_ACCESS = "canAccess";

    private static String url = "http://thor.kmx.cosma.com/spc/spc_measure_login.php";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "mark:test1234"
    };

    private final int MIN_PSWD_LEN = 8;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.txtUsername);
        // mUsernameView.setText(SecurityUtils.getUsername(this));

        // don't need auto complete at present
        // populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.edtPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mBtnSignInButton = (Button) findViewById(R.id.btnSignIn);
        mBtnSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    // handle back out
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        postLogin(false);

        super.onBackPressed();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= MIN_PSWD_LEN;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            JSONObject jAuth = new JSONObject();

//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mUsername)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            try {
                // build json for the authentication
                jAuth.put("user_name", mUsername);
                jAuth.put("password", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONParser jParser = new JSONParser();

            // get JSON from URL
            JSONObject json = jParser.getJSONFromUrl(url, jAuth.toString());

            // TODO: register the new account here.
            return json;
        }

        @Override
        protected void onPostExecute(final JSONObject json) {
            boolean ldapAuth = false;
            boolean canAccess = false;

            Log.d(TAG, "processResponse: json = " + json.toString());

            try {
                // unpack success flag and message
                ldapAuth  = Boolean.valueOf(json.getBoolean(TAG_LDAP_AUTH));
                canAccess = Boolean.valueOf(json.getBoolean(TAG_CAN_ACCESS));

            } catch (JSONException e) {
                e.printStackTrace();
                ldapAuth = false;
                canAccess = false;
            }

            mAuthTask = null;
            showProgress(false);

            Log.d(TAG, "OnPostExecute: ldapAuth = " + ldapAuth);

            Log.d(TAG, "OnPostExecute: 1 Lock = " + SecurityUtils.getIsLoggedIn(LoginActivity.this) + "; App = " + SecurityUtils.getIsLoggedIn(LoginActivity.this));
            SecurityUtils.setUsername(LoginActivity.this, mUsername);   // store username
            SecurityUtils.setIsLoggedIn(LoginActivity.this, ldapAuth);  // store logged in state
            SecurityUtils.setCanMeasure(LoginActivity.this, canAccess); // store can measure
            Log.d(TAG, "OnPostExecute: 2 Lock = " + SecurityUtils.getIsLoggedIn(LoginActivity.this) + "; App = " + SecurityUtils.getIsLoggedIn(LoginActivity.this));

            if (ldapAuth) {
                postLogin(true);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    // handle post login actions based up success parameter
    private void postLogin(boolean ok) {
        Log.d(TAG, "postLogin: ok = " + ok);

        // check successful login
        if (ok) {
            // check version
            new CheckVersionTask(getApplicationContext()).execute();

            // import Action Cause Simple Codes
            SimpleCodeService.startActionImport(getApplicationContext(), SimpleCodeService.TYPE_ACTION_CAUSE);

            // import Gauge Audit Simple Codes
            SimpleCodeService.startActionImport(getApplicationContext(), SimpleCodeService.TYPE_GAUGE_AUDIT);
        }

        returnResult(ok);

    }   // postLogin

    // return result to calling activity
    private void returnResult(boolean ok) {
        Log.d(TAG, "returnResult: ok = " + ok);

//        Intent returnIntent = new Intent();
//        if (ok) {
//            LoginActivity.this.setResult(RESULT_OK, returnIntent);
//        } else {
//            LoginActivity.this.setResult(RESULT_CANCELED, returnIntent);
//        }
        finish();
    }
}

