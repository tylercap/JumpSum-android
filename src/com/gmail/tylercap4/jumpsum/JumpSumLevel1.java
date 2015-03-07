package com.gmail.tylercap4.jumpsum;

import java.util.Collections;
import java.util.LinkedList;

import com.google.android.gms.games.Games;

import android.widget.FrameLayout;

public class JumpSumLevel1 extends JumpSum7x5
{    
	private static final String HIGH_SCORE_KEY  = "HIGH_SCORE";
	private static final String GAME_VALUES_KEY = "GAME_VALUES";	
	
	@Override
	protected String getGameValsKey(){ return GAME_VALUES_KEY; }	
	@Override
	protected String getHighScoreKey(){ return HIGH_SCORE_KEY; }
	@Override
	protected int getLevelNumber(){ return 1; }
    
	@Override
    protected void showLeaderboard(){
    	// show the google play leaderboard    	
    	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
    	        			   getString(R.string.leaderboard_id)), REQUEST_LEADERBOARD);
    }
	
	@Override
	protected void updateAdditionalAchievements( int score ){
		Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_id), score);
		
		if( score >= 60 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_60plus_id));
		}
		if( score >= 80 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_80plus_id));
		}
		if( score >= 90 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_90plus_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus20_id), 1);
		}
		if( score >= 95 ){
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_95plus100_id), 1);
		}
		if( score == 100 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect20_id), 1);
		}
	}

    @Override
    protected void doNewGame(){
    	synchronized( JumpSumLevel1.this ){
	    	LinkedList<ValueSortable> list = new LinkedList<ValueSortable>();
	    	
	    	// will places 10 1's, 10 2's, 10 3's, and 4 10's randomly in the board
	    	// also has 1 open space (represented by -1)
	    	for( int val = 1; val <= 3; val++ ){
		    	for( int i = 0; i < 10; i++ ){
		    		ValueSortable vs = new ValueSortable(val);
		    		list.add(vs);
		    	}
	    	}
	    	for( int i = 0; i < 4; i++ ){
	    		ValueSortable vs = new ValueSortable(10);
	    		list.add(vs);
	    	}
	    	list.add(new ValueSortable(-1));
	    	
	    	Collections.sort(list);
	    	
	    	// now fill the table from the list
	    	for(int row = 0; row < getRows(); row++ ){    		
	    		for( int column = 0; column < getColumns(); column++ ){
	    			int val = list.pollLast().getValue();
	    			gameboard[row][column] = val;
	    			
	    			FrameLayout view = (FrameLayout)findViewById(widget_ids[row][column]);
	    			IndexedButton button = new IndexedButton(this, row, column);
	    			setUpButton(button, val);
	            	
	    			if( view.getChildCount() > 0 )
	    				view.removeAllViews();
	            	view.addView(button);
	            	widgets[row][column] = button;
	    		}
	    	}
    	}
    }
}
