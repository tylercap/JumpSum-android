package com.gmail.tylercap4.jumpsum;

import java.util.Collections;
import java.util.LinkedList;

import com.google.android.gms.games.Games;

import android.widget.FrameLayout;

public class JumpSumLevel5 extends JumpSum
{    
	private static final String HIGH_SCORE_KEY  = "HIGH_SCORE_5";
	private static final String GAME_VALUES_KEY = "GAME_VALUES_5";	
	private static final int ROWS = 8;
	private static final int COLUMNS = 6;
	
	@Override
	protected void setCorrectContentView(){
        setContentView(R.layout.jump_sum_8x6);
	}
	
	@Override
	protected int getRows(){ return ROWS; }
	@Override
	protected int getColumns(){ return COLUMNS; }
	@Override
	protected String getGameValsKey(){ return GAME_VALUES_KEY; }	
	@Override
	protected String getHighScoreKey(){ return HIGH_SCORE_KEY; }
    
	@Override
    protected void showLeaderboard(){
    	// show the google play leaderboard
    	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
    	        			   getString(R.string.leaderboard_l5_id)), REQUEST_LEADERBOARD);
    }
	
	@Override
	protected void updateAdditionalAchievements( int score ){
		Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_l5_id), score);		
    	
		if( score >= 60 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_60plus_l5_id));
		}
		if( score >= 80 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_80plus_l5_id));
		}
		if( score >= 90 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_90plus_l5_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus20_id), 1);
		}
		if( score >= 95 ){
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_95plus100_id), 1);
		}
		if( score == 100 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_l5_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect20_id), 1);
		}
	}
	
    @Override
    protected void doNewGame(){
    	synchronized( JumpSumLevel5.this ){
	    	LinkedList<ValueSortable> list = new LinkedList<ValueSortable>();
	    	
	    	// will places 20 1's, 12 2's, 12 3's, 2 5's and 1 10 randomly in the board
	    	// also has 1 open space (represented by -1)
	    	for( int i = 0; i < 20; i++ ){
	    		ValueSortable vs = new ValueSortable(1);
	    		list.add(vs);
	    	}
	    	for( int val = 2; val <= 3; val++ ){
		    	for( int i = 0; i < 12; i++ ){
		    		ValueSortable vs = new ValueSortable(val);
		    		list.add(vs);
		    	}
	    	}
	    	for( int i = 0; i < 2; i++ ){
	    		ValueSortable vs = new ValueSortable(5);
	    		list.add(vs);
	    	}
	    	list.add(new ValueSortable(10));
	    	list.add(new ValueSortable(-1));
	    	
	    	Collections.sort(list);
	    	
	    	// now fill the table from the list
	    	for(int row = 0; row < ROWS; row++ ){    		
	    		for( int column = 0; column < COLUMNS; column++ ){
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
    
    @Override
    protected void initWidgetIds(){
    	widget_ids[0][0] = R.id.widgetr0c0;
    	widget_ids[0][1] = R.id.widgetr0c1;
    	widget_ids[0][2] = R.id.widgetr0c2;
    	widget_ids[0][3] = R.id.widgetr0c3;
    	widget_ids[0][4] = R.id.widgetr0c4;
    	widget_ids[0][5] = R.id.widgetr0c5;

    	widget_ids[1][0] = R.id.widgetr1c0;
    	widget_ids[1][1] = R.id.widgetr1c1;
    	widget_ids[1][2] = R.id.widgetr1c2;
    	widget_ids[1][3] = R.id.widgetr1c3;
    	widget_ids[1][4] = R.id.widgetr1c4;
    	widget_ids[1][5] = R.id.widgetr1c5;
    	
    	widget_ids[2][0] = R.id.widgetr2c0;
    	widget_ids[2][1] = R.id.widgetr2c1;
    	widget_ids[2][2] = R.id.widgetr2c2;
    	widget_ids[2][3] = R.id.widgetr2c3;
    	widget_ids[2][4] = R.id.widgetr2c4;
    	widget_ids[2][5] = R.id.widgetr2c5;

    	widget_ids[3][0] = R.id.widgetr3c0;
    	widget_ids[3][1] = R.id.widgetr3c1;
    	widget_ids[3][2] = R.id.widgetr3c2;
    	widget_ids[3][3] = R.id.widgetr3c3;
    	widget_ids[3][4] = R.id.widgetr3c4;
    	widget_ids[3][5] = R.id.widgetr3c5;
    	
    	widget_ids[4][0] = R.id.widgetr4c0;
    	widget_ids[4][1] = R.id.widgetr4c1;
    	widget_ids[4][2] = R.id.widgetr4c2;
    	widget_ids[4][3] = R.id.widgetr4c3;
    	widget_ids[4][4] = R.id.widgetr4c4;
    	widget_ids[4][5] = R.id.widgetr4c5;

    	widget_ids[5][0] = R.id.widgetr5c0;
    	widget_ids[5][1] = R.id.widgetr5c1;
    	widget_ids[5][2] = R.id.widgetr5c2;
    	widget_ids[5][3] = R.id.widgetr5c3;
    	widget_ids[5][4] = R.id.widgetr5c4;
    	widget_ids[5][5] = R.id.widgetr5c5;
    	
    	widget_ids[6][0] = R.id.widgetr6c0;
    	widget_ids[6][1] = R.id.widgetr6c1;
    	widget_ids[6][2] = R.id.widgetr6c2;
    	widget_ids[6][3] = R.id.widgetr6c3;
    	widget_ids[6][4] = R.id.widgetr6c4;
    	widget_ids[6][5] = R.id.widgetr6c5;
    	
    	widget_ids[7][0] = R.id.widgetr7c0;
    	widget_ids[7][1] = R.id.widgetr7c1;
    	widget_ids[7][2] = R.id.widgetr7c2;
    	widget_ids[7][3] = R.id.widgetr7c3;
    	widget_ids[7][4] = R.id.widgetr7c4;
    	widget_ids[7][5] = R.id.widgetr7c5;
    }	
}
