package com.gmail.tylercap4.jumpsum;

import com.gmail.tylercap4.jumpsum.basegameutils.BaseGameUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity implements ConnectionCallbacks, OnConnectionFailedListener
{
	private static final String SIGNED_IN_KEY = "SIGNED_IN";
	private static int RC_SIGN_IN = 9001;
	
	/* Client used to interact with Google APIs. */
	protected GoogleApiClient mGoogleApiClient;

	private boolean mResolvingConnectionFailure = false;
	private boolean mSignInClicked = false;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        final View signIn = findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (!mGoogleApiClient.isConnecting()) {
            		mSignInClicked = true;
                    mGoogleApiClient.connect();
            	}
            }
        });
        
        final Button signOut = (Button) findViewById(R.id.sign_out_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mGoogleApiClient.isConnected()) {
            		mSignInClicked = false;
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.clearDefaultAccountAndReconnect();

                    // show sign-in button, hide the sign-out button
                    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.sign_out_layout).setVisibility(View.GONE);
                }
            }
        });        
        
        final View level1 = findViewById(R.id.level1_button);
        level1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel1.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level2 = findViewById(R.id.level2_button);
        level2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel2.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level3 = findViewById(R.id.level3_button);
        level3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel3.class );
            	MainMenu.this.startActivity( i );
            }
        }); 
        
        final View level4 = findViewById(R.id.level4_button);
        level4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel4.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level5 = findViewById(R.id.level5_button);
        level5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel5.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level6 = findViewById(R.id.level6_button);
        level6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel6.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level7 = findViewById(R.id.level7_button);
        level7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel7.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level8 = findViewById(R.id.level8_button);
        level8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel8.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level9 = findViewById(R.id.level9_button);
        level9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel9.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final View level10 = findViewById(R.id.level10_button);
        level10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent( MainMenu.this, JumpSumLevel10.class );
            	MainMenu.this.startActivity( i );
            }
        });
        
        final Button howTo = (Button) findViewById(R.id.howToButton);
        howTo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHowTo();
            }
        });
	}

    private void showHowTo(){
    	// show a new page explaining how to play the game
    	Intent how_to = new Intent( this, HowToPage.class );
    	startActivity( how_to );
    }
	    
    @Override
    protected void onStart(){
    	super.onStart();
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean signed_in = prefs.getBoolean(SIGNED_IN_KEY, false);
    	if( signed_in ){
    		mGoogleApiClient.connect();
    	}
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor prefs_editor = prefs.edit();
    	
    	if (mGoogleApiClient.isConnected()) {
    		mGoogleApiClient.disconnect();
        	prefs_editor.putBoolean(SIGNED_IN_KEY, true);
    	}
    	else{
    		prefs_editor.putBoolean(SIGNED_IN_KEY, false);
    	}
    	
    	prefs_editor.commit();
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
    	mSignInClicked = false;
	      
    	findViewById(R.id.sign_in_button).setVisibility(View.GONE);
	    
	    final View signOutLayout = findViewById(R.id.sign_out_layout);
	    final TextView user_view = (TextView) findViewById(R.id.current_user);
	    
	    // show email for user signed in
	    String username = Plus.AccountApi.getAccountName(mGoogleApiClient);
	    if( username != null )
	    	user_view.setText(username);
	    
	    signOutLayout.setVisibility(View.VISIBLE);
    }
    
    public void onConnectionSuspended(int cause) {
    	mGoogleApiClient.connect();
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.sign_in_failed))) 
            {
                mResolvingConnectionFailure = false;
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                    requestCode, resultCode, R.string.sign_in_failed);
            }
        }
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
        
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
        	findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    	    
    	    final View signOutLayout = findViewById(R.id.sign_out_layout);
    	    final TextView user_view = (TextView) findViewById(R.id.current_user);
    	    
    	    // show email for user signed in
    	    String username = Plus.AccountApi.getAccountName(mGoogleApiClient);
    	    if( username != null )
    	    	user_view.setText(username);
    	    
    	    signOutLayout.setVisibility(View.VISIBLE);
        }
        else{
        	findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    	    findViewById(R.id.sign_out_layout).setVisibility(View.GONE);
        }
    }
}
