package com.gmail.tylercap4.jumpsum;

import java.util.Collections;
import java.util.LinkedList;

import com.google.android.gms.games.Games;

import android.widget.FrameLayout;

public class JumpSumLevel10 extends JumpSum7x7
{    
	private static final String HIGH_SCORE_KEY  = "HIGH_SCORE_10";
	private static final String GAME_VALUES_KEY = "GAME_VALUES_10";
	
	@Override
	protected String getGameValsKey(){ return GAME_VALUES_KEY; }	
	@Override
	protected String getHighScoreKey(){ return HIGH_SCORE_KEY; }
    
	@Override
    protected void showLeaderboard(){
    	// show the google play leaderboard
    	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
    	        			   getString(R.string.leaderboard_l10_id)), REQUEST_LEADERBOARD);
    }

	@Override
	protected void updateAdditionalAchievements(int score) {
		Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_l10_id), score);		
		
		if( score >= 60 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_60plus_l10_id));
		}
		if( score >= 80 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_80plus_l10_id));
		}
		if( score >= 90 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_90plus_l10_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus20_id), 1);
		}
		if( score >= 95 ){
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_95plus100_id), 1);
		}
		if( score == 100 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_l10_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect20_id), 1);
		}
	}

    @Override
    protected void doNewGame(){
    	synchronized( JumpSumLevel10.this ){
	    	LinkedList<ValueSortable> list = new LinkedList<ValueSortable>();
	    	
	    	// will places 12 1's, 12 2's, 12 3's, and 4 7's randomly in the board
	    	// also has 1 open space (represented by -1)
	    	for( int val = 1; val <= 3; val++ ){
		    	for( int i = 0; i < 12; i++ ){
		    		ValueSortable vs = new ValueSortable(val);
		    		list.add(vs);
		    	}
	    	}
	    	for( int i = 0; i < 4; i++ ){
	    		ValueSortable vs = new ValueSortable(7);
	    		list.add(vs);
	    	}
	    	list.add(new ValueSortable(-1));
	    	
	    	Collections.sort(list);
	    	
	    	// layout new board style
	    	// now fill the table from the list
	    	for(int row = 0; row < getRows(); row++ ){    		
	    		for( int column = 0; column < getColumns(); column++ ){
	    			int val;
	    			if( (column < 2 || column > 4) && (row == 3) ||
	    				(row < 2 || row > 4) && (column == 3) )
	    			{
	    				val = -2;
	    			}
	    			else{
	    				val = list.pollLast().getValue();
	    				// the open space must start in the corner of one of the 5 '3x3' boxes
	    				if( val == -1 ){
	    					boolean ok_space = false;
	    					if( row == 0 && ( column == 0 || column == 6 ) ){
	    						ok_space = true;
	    					}
	    					else if( row == 2 && ( column == 0 || column == 6 || column == 2 || column == 4 ) ){
	    						ok_space = true;
	    					}
	    					else if( row == 4 && ( column == 0 || column == 6 || column == 2 || column == 4 ) ){
	    						ok_space = true;
	    					} 
	    					else if( row == 6 && ( column == 0 || column == 6 ) ){
	    						ok_space = true;
	    					}
	    					
	    					if( !ok_space ){
	    						// get a new value for this tile and put the -1 back into the list
	    						val = list.pollLast().getValue();
	    						
	    						list.add(new ValueSortable(-1));
	    				    	
	    				    	Collections.sort(list);
	    					}
	    				}
		    			
		    			FrameLayout view = (FrameLayout)findViewById(widget_ids[row][column]);
		    			IndexedButton button = new IndexedButton(this, row, column);
		    			setUpButton(button, val);
		            	
		    			if( view.getChildCount() > 0 )
		    				view.removeAllViews();
		            	view.addView(button);
		            	widgets[row][column] = button;
	    			}
	    			
	    			gameboard[row][column] = val;
	    		}
	    	}
    	}
    }	
}