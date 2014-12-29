package com.gmail.tylercap4.jumpsum;

import java.util.Collections;
import java.util.LinkedList;

import com.google.android.gms.games.Games;

import android.widget.FrameLayout;

public class JumpSumLevel2 extends JumpSum
{    
	private static final String HIGH_SCORE_KEY  = "HIGH_SCORE_2";
	private static final String GAME_VALUES_KEY = "GAME_VALUES_2";
	
	private static final int rows = 7;
	private static final int columns = 6;
	
	@Override
	protected void setCorrectContentView(){
        setContentView(R.layout.jump_sum_level2);		
	}
	
    @Override
    protected void initBoardAndWidgets(){
    	widget_ids = new int[rows][columns];
    	initWidgetIds();
        gameboard = new int[rows][columns];
        widgets = new IndexedButton[rows][columns];
    }
	
	@Override
	protected String getGameValsKey(){
		return GAME_VALUES_KEY;
	}
	
	@Override
	protected String getHighScoreKey(){
		return HIGH_SCORE_KEY;
	}
    
	@Override
    protected void showLeaderboard(){
    	// show the google play leaderboard
    	startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
    	        			   getString(R.string.leaderboard_hard_id)), REQUEST_LEADERBOARD);
    }

	@Override
	protected void updateAdditionalAchievements(int score) {
		Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_hard_id), score);		
		
		if( score >= 60 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_60plus_l2_id));
		}
		if( score >= 80 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_80plus_l2_id));
		}
		if( score >= 90 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_90plus_l2_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_90plus20_id), 1);
		}
		if( score >= 95 ){
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_95plus100_id), 1);
		}
		if( score == 100 ){
			Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_l2_id));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect5_id), 1);
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_perfect20_id), 1);
		}
	}
    
    @Override
    protected String getGameAsString(){
    	// adjust for new board
    	// save the game currently in progress
    	StringBuilder game_string = new StringBuilder();
    	for(int row = 0; row < rows; row++ ){    		
    		for( int column = 0; column < columns; column++ ){
    			int value = gameboard[row][column];
    			
    			game_string.append(value);
    			if( column < (columns - 1) ){
    				game_string.append(',');
    			}
    		}
    		if( row < (rows - 1) ){
				game_string.append(';');
			}
    	}
    	
    	return game_string.toString();
    }
    
    
    @Override
    protected boolean checkGameOver(){    	
    	for(int row = 0; row < rows; row++ ){    		
    		for( int column = 0; column < columns; column++ ){
    			if( getEligibleDropTargets(row, column).size() > 0 ){
    				return false;
    			}
    		}
    	}
    	
    	return true;
    }

    @Override
    protected int getScore(){
    	int score = 0;
    	
    	for(int row = 0; row < rows; row++ ){    		
    		for( int column = 0; column < columns; column++ ){
    			score = Math.max(gameboard[row][column], score);
    		}
    	}
    	
    	return score;
    }

    @Override
    protected LinkedList<IndexedButton> getEligibleDropTargets( int row, int column ){
    	LinkedList<IndexedButton> eligible = new LinkedList<IndexedButton>();
    	
    	synchronized( JumpSumLevel2.this ){
	    	if( gameboard[row][column] <= 0 ){
	    		// can't move a blank piece
	    		return eligible;
	    	}
	    	
	    	// check for the new board layout
	    	// must check that there is a value in between the two as well
	    	if( row + 2 < rows && (gameboard[row + 2][column] == -1) && (gameboard[row + 1][column] > 0) ){
	    		eligible.add( widgets[row + 2][column] );
	    	}
	    	if( row - 2 >= 0 && (gameboard[row - 2][column] == -1) && (gameboard[row - 1][column] > 0) ){
	    		eligible.add( widgets[row - 2][column] );
	    	}
	    	if( column + 2 < columns && (gameboard[row][column + 2] == -1) && (gameboard[row][column + 1] > 0) ){
	    		eligible.add( widgets[row][column + 2] );
	    	}
	    	if( column - 2 >= 0 && (gameboard[row][column - 2] == -1) && (gameboard[row][column - 1] > 0) ){
	    		eligible.add( widgets[row][column - 2] );
	    	}
    	}
    	
    	return eligible;
    }

    @Override
    protected void doNewGame(){
    	synchronized( JumpSumLevel2.this ){
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
	    	
	    	// layout new board style
	    	// now fill the table from the list
	    	for(int row = 0; row < rows; row++ ){    		
	    		for( int column = 0; column < columns; column++ ){
	    			int val;
	    			if( (column == 5 && row % 2 == 0) || 
	    			    (column == 0 && row % 2 == 1) )
	    			{
	    				val = -2;
	    			}
	    			else{
	    				val = list.pollLast().getValue();
		    			
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
    
    private void initWidgetIds(){
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
    }	
}
