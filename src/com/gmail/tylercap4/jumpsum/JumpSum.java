package com.gmail.tylercap4.jumpsum;

import java.util.LinkedList;
import java.util.StringTokenizer;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.gmail.tylercap4.jumpsum.basegameutils.BaseGameUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;


public abstract class JumpSum extends Activity implements ConnectionCallbacks, OnConnectionFailedListener 
{	
	protected static final String 	TAG = "JumpSum";
	private static final String 	HIGH_SCORE_KEY = "HIGH_SCORE";
	private static final String 	GAME_VALUES_KEY = "GAME_VALUES";
	private static final String 	SIGNED_IN_KEY = "SIGNED_IN";
	private static int RC_SIGN_IN = 9001;
	private static int REQUEST_LEADERBOARD = 8099;
	
	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient;

	private boolean mResolvingConnectionFailure = false;
	private boolean mSignInClicked = false;

	private   int					text_color;
	private   int 					high_score;
	protected int [][]				widget_ids;
	protected int [][] 				gameboard;
	protected IndexedButton [][] 	widgets;
	private   boolean				game_over;
	private   boolean				current_drag;
	
	private UiLifecycleHelper uiHelper;
    
	protected abstract void							setCorrectContentView();
    protected abstract void 						initBoardAndWidgets();
    protected abstract void 						doNewGame();
    protected abstract String 						getGameAsString();
    protected abstract int 							getScore();
    protected abstract boolean 						checkGameOver();
    protected abstract LinkedList<IndexedButton> 	getEligibleDropTargets( int row, int column );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCorrectContentView();
        
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        
        this.text_color = getResources().getColor(R.color.button_border);
        
        gameboard = null;
        widgets = null;
        widget_ids = null;
        high_score = -1;
        
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
            	    findViewById(R.id.leaderboard_button).setVisibility(View.GONE);
                }
            }
        });
        
        final Button newGame = (Button) findViewById(R.id.newGameButton);
        newGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newGame();
            }
        });
        
        final Button howTo = (Button) findViewById(R.id.howToButton);
        howTo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHowTo();
            }
        });
        
        final Button leaderboard = (Button) findViewById(R.id.leaderboard_button);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showLeaderboard();
            }
        });
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
    protected void onStop(){
    	super.onStop();
    	
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
	    findViewById(R.id.leaderboard_button).setVisibility(View.VISIBLE);
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
        else{
	        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	            @Override
	            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	                Log.e(TAG, String.format("Post Error: %s", error.toString()));
	            }
	
	            @Override
	            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	                Log.i(TAG, "Post Success!");
	            }
	        });
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    
    @Override
    protected void onPause(){
    	super.onPause();
        uiHelper.onPause();
        
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.commit();
    	
    	super.onPause();
    	
    	if( game_over ){
    		// Erase any saved games.  If we finish on a saved game, a new game should be loaded on launch.
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        	SharedPreferences.Editor prefs_editor = prefs.edit();
        	prefs_editor.remove(GAME_VALUES_KEY);
        	prefs_editor.commit();
    	}
    	else{
    		// save the game currently in progress.
    		String game_as_string = getGameAsString();
    		
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        	SharedPreferences.Editor prefs_editor = prefs.edit();
        	prefs_editor.putString(GAME_VALUES_KEY, game_as_string);
        	prefs_editor.commit();
    	}
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
        uiHelper.onResume();
        
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
        	findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    	    
    	    final View signOutLayout = findViewById(R.id.sign_out_layout);
    	    final TextView user_view = (TextView) findViewById(R.id.current_user);
    	    
    	    // show email for user signed in
    	    String username = Plus.AccountApi.getAccountName(mGoogleApiClient);
    	    if( username != null )
    	    	user_view.setText(username);
    	    
    	    signOutLayout.setVisibility(View.VISIBLE);
    	    findViewById(R.id.leaderboard_button).setVisibility(View.VISIBLE);
        }
        else{
        	findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
    	    findViewById(R.id.sign_out_layout).setVisibility(View.GONE);
    	    findViewById(R.id.leaderboard_button).setVisibility(View.GONE);
        }
        
    	if( gameboard == null ){
	        initBoardAndWidgets();
	        
	        // check for game currently in progress
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	String game_string = prefs.getString(GAME_VALUES_KEY, null);
	        
	        if( game_string != null ){
	        	// load the values into our board
	        	loadGame( game_string );
	        }
	        else{
	        	newGame();
	        }
    	}
    	else if( game_over ){
    		newGame();
    	}

        updateHighScore(getHighScore());
    	int score = getScore();
    	updateScore(score);
    	updateHighScore(score);
    	this.current_drag = false;
    }
    
    private void showLeaderboard(){
    	// show the google play leaderboard
    	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
    	        			   getString(R.string.leaderboard_id)), REQUEST_LEADERBOARD);
    }

    private void showHowTo(){
    	// show a new page explaining how to play the game
    	Intent how_to = new Intent( this, HowToPage.class );
    	startActivity( how_to );
    }
    
    private void loadGame( String game_as_string ){
    	// load the game previously in progress
    	synchronized( JumpSum.this ){
	    	StringTokenizer st = new StringTokenizer(game_as_string, ";");
	    	
	    	int row = 0;
	    	while( st.hasMoreTokens() ){
	    		int column = 0;
	    		String row_string = st.nextToken();
	    		
	    		StringTokenizer row_st = new StringTokenizer(row_string, ",");
	    		while( row_st.hasMoreTokens() ){
	    			int value = Integer.parseInt(row_st.nextToken());
	    			
	    			gameboard[row][column] = value;
	    			
	    			FrameLayout view = (FrameLayout)findViewById(widget_ids[row][column]);
	    			IndexedButton button = new IndexedButton(this, row, column);
	    			setUpButton(button, value);
	            	
	    			if( view.getChildCount() > 0 )
	    				view.removeAllViews();
	            	view.addView(button);
	            	widgets[row][column] = button;
	    			
	    			column++;
	    		}
	        	
	        	row++;
	    	}
    	}
    }
    
    private void newGame(){
    	doNewGame();
    	this.game_over = false;
    }
    
    private void gameOver( boolean new_high, int score ){    	
    	this.game_over = true;
    	
    	updateAchievements( score );
    	
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
    	
    	GameOverDialog dialog = new GameOverDialog( new_high, score );
    	dialog.show(ft, "dialog");
    }
    
    private void updateAchievements( int score ){
    	// update achievements and leader board
    	if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
    		Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_id), score);
    		
    		Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_10000g_id), 1);
    		Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_10000p_id), score);
    		
    		if( score > 60 ){
    			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_60plus_id));
    		}
    		if( score > 80 ){
    			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_80plus_id));
    		}
    		if( score > 90 ){
    			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_90plus_id));
    			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus5_id), 1);
    			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus20_id), 1);
    		}
    		if( score > 95 ){
    			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_95plus100_id), 1);
    		}
    		if( score == 100 ){
    			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_id));
    			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect5_id), 1);
    			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect20_id), 1);
    		}
    	}
    }
    
    private void jumpedTile( IndexedButton start_tile, IndexedButton end_tile ){
    	// the end_tile must be empty tile
    	// the jumped_tile will be the tile in between the start_tile and end_tile
    	// the value at the start_tile will be added to the value at the jumped_tile and the sum will be placed in the end_tile
    	// the start_tile and jumped_tile will become empty tiles
    	synchronized( JumpSum.this ){
	    	int start_row = start_tile.getRow();
	    	int start_column = start_tile.getColumn();
	    	int end_row = end_tile.getRow();
	    	int end_column = end_tile.getColumn();
	    	
	    	int jump_row = (start_row + end_row) / 2;
	    	int jump_column = (start_column + end_column) / 2;
	    	IndexedButton jumped_tile = widgets[jump_row][jump_column];
	    	
	    	int start_val = gameboard[start_row][start_column];
	    	int jump_val = gameboard[jump_row][jump_column];
	    	
	    	int end_val = start_val + jump_val;
	    	gameboard[end_row][end_column] = end_val;
	    	end_tile.setText(String.valueOf(end_val));
	    	
	    	gameboard[start_row][start_column] = -1;
	    	gameboard[jump_row][jump_column] = -1;
	    	start_tile.setText("");
	    	jumped_tile.setText("");
	    	
	    	int score = getScore();
	    	updateScore(score);

	    	new CheckGameOverTask().execute(score);
    	}
    }
    
    private int getHighScore(){
    	// get the high score from the memory if it exists
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	return prefs.getInt(HIGH_SCORE_KEY, 0);
    }
    
    private boolean updateHighScore(int score){
    	if( score > this.high_score ){
			this.high_score = score;
	    	TextView textView = (TextView)findViewById(R.id.highScoreText);
	    	textView.setText(R.string.high_score);
	    	textView.append(String.valueOf(score));
	    	
	    	// write the new high score to memory
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	SharedPreferences.Editor prefs_editor = prefs.edit();
	    	prefs_editor.putInt(HIGH_SCORE_KEY, score);
	    	prefs_editor.commit();
	    	
	    	return true;
		}
    	
    	return false;
    }
    
    private void updateScore(int score){
    	TextView textView = (TextView)findViewById(R.id.currentScoreText);
    	textView.setText(R.string.current_score);
    	textView.append(String.valueOf(score));
    }
    
    protected void setUpButton( IndexedButton button, int value ){
    	button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT, 1.0f));
		button.setOnTouchListener(new TileTouchListener());
		button.setOnDragListener(new TileDragListener());
		button.setBackgroundResource(R.drawable.custom_button);
		button.setTextColor(this.text_color);
		if( value > 0 ){
			button.setText(String.valueOf(value));
		}
    }
    
    private final class TileTouchListener implements OnTouchListener 
    {
        @Override
	    public boolean onTouch(View view, MotionEvent motionEvent) {
        	if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        		synchronized( JumpSum.this ){
        			if( !JumpSum.this.current_drag ){
	        			JumpSum.this.current_drag = true;
				        ClipData data = ClipData.newPlainText("", "");
				        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				        view.startDrag(data, shadowBuilder, view, 0);
				        view.setVisibility(View.INVISIBLE);
				        
				        IndexedButton button = (IndexedButton)view;
				        for( Button eligible:getEligibleDropTargets(button.getRow(), button.getColumn()) ){
				        	eligible.setBackgroundResource(R.drawable.eligible_drop);
				        }	
        			}
        		}
		        return true;
		    } 
        	if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
        		view.performClick();
        		return true;
        	}
        	else {
		    	return false;
		    }
	    }
	} 
    
    private final class TileDragListener implements OnDragListener 
    {
        @Override
        public boolean onDrag(View v, DragEvent event) {
	        int action = event.getAction();
            View dragged_view = (View) event.getLocalState();
            IndexedButton dragged_button = (IndexedButton)dragged_view;
            int row = dragged_button.getRow();
            int column = dragged_button.getColumn();
            
	        switch (action) {
		        case DragEvent.ACTION_DRAG_STARTED:
		            // do nothing
		            break;
		        case DragEvent.ACTION_DRAG_ENTERED:
		        	// View entered_view = (View) v;
		            break;
		        case DragEvent.ACTION_DRAG_EXITED:
		            break;
		        case DragEvent.ACTION_DROP:
		            View dropped_view = (View) v;
		            synchronized( JumpSum.this ){
			        	dragged_view.setVisibility(View.VISIBLE);
			        	
			        	for( IndexedButton eligible:getEligibleDropTargets(row, column) ){
			        		eligible.setBackgroundResource(R.drawable.custom_button);
				        	
				        	if( eligible.equals(dropped_view) ){
				        		jumpedTile(dragged_button, eligible);
				        	}
				        }
			        	JumpSum.this.current_drag = false;
		            }
		            break;
		        case DragEvent.ACTION_DRAG_ENDED:
		        	synchronized( JumpSum.this ){
			        	dragged_view.setVisibility(View.VISIBLE);
			        	
			        	for( Button eligible:getEligibleDropTargets(row, column) ){
				        	eligible.setBackgroundResource(R.drawable.custom_button);
				        }
			        	JumpSum.this.current_drag = false;
		        	}
		        	break;
		        default:
		            break;
	        }
	        return true;
        }
    } 
    
    protected static class ValueSortable implements Comparable<ValueSortable>
    {
    	private final int value;
    	private final int sortValue;
    	
    	protected ValueSortable(int value){
    		this.value = value;
    		this.sortValue = (int) (Math.random() * 100);
    	}
    	
    	public int compareTo(ValueSortable comp){    		
    		return this.sortValue - comp.sortValue;
    	}
    	
    	protected int getValue(){
    		return this.value;
    	}
    }
    
    private class CheckGameOverTask extends AsyncTask<Integer, Integer, Integer> 
    {	

    	@Override
    	protected Integer doInBackground(Integer... params) {
        	boolean over = checkGameOver();
        	
        	Integer score = (Integer)params[0];
        	if( over ){
        		return score;
        	}
        	
        	return -1;
    	}
    	
    	@Override
    	public void onPostExecute(Integer score) {
    		if( score > 0 ){
    			boolean new_high = updateHighScore(score);
    			gameOver( new_high, score );
    		}
    	}

    }
    
    private class GameOverDialog extends DialogFragment 
    {
    	private final boolean new_high;
    	private final int score;
    	
    	
    	public GameOverDialog( boolean new_high, int score ){
    		super();
    		
    		this.new_high = new_high;
    		this.score = score;
    	}
    	
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getGameOverMessage())
                   .setPositiveButton(/* "New Game"*/R.string.new_game, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           newGame();
                       }
                   })
                   .setNegativeButton(/*"Dismiss"*/R.string.post_score, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           postScoreToFacebook();
                       }
                   });
            // Create the AlertDialog object and return it
            AlertDialog dialog = builder.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            
            return dialog;
        }
        
        private String getGameOverMessage(){
        	if( new_high ){
        		return "Game Over\nScore: " + score + "\nNew High Score!";
        	}
        	else{
        		return "Game Over\nScore: " + score;
        	}
        }
        
        private void postScoreToFacebook(){    		
        	// post to Facebook
        	if (FacebookDialog.canPresentShareDialog(getApplicationContext(), 
                    FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) 
        	{			    
        		String message = "I Scored " + score + " in Jump Sum";
        		
			    FacebookDialog.ShareDialogBuilder builder = new FacebookDialog.ShareDialogBuilder(JumpSum.this)
			            .setLink("http://play.google.com/store/apps/details?id=com.gmail.tylercap4.jumpsum")
			            .setCaption(message)			            
			            .setDescription(message);

		        if (builder.canPresent()) {
		        	uiHelper.trackPendingDialogCall(builder.build().present());
		        }
        	}
        	else
        	{
//        		Bundle params = new Bundle();
//        	    params.putString("caption", message);
//        	    params.putString("description", message);
//        	    params.putString("link", "http://play.google.com/store/apps/details?id=com.gmail.tylercap4.jumpsum");
//
//        	    WebDialog feedDialog = (
//        	            new WebDialog.FeedDialogBuilder(getActivity(),
//        	                    Session.getActiveSession(),
//        	                    params)).build();
//        	    feedDialog.show();
        	    
        		StringBuilder message = new StringBuilder("Unable to post to Facebook.");
        		message.append("\nFacebook App not installed, or");
        		message.append("\nsharing is not supported.");
        		Toast.makeText(JumpSum.this, message.toString(), Toast.LENGTH_LONG).show();
        	}
        }
    }
}
