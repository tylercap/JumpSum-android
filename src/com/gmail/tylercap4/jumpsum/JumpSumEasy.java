package com.gmail.tylercap4.jumpsum;

import java.util.Collections;
import java.util.LinkedList;

import android.widget.FrameLayout;

public class JumpSumEasy extends JumpSum
{    
	@Override
	protected void setCorrectContentView(){
        setContentView(R.layout.jump_sum_easy);		
	}
	
    @Override
    protected void initBoardAndWidgets(){
    	widget_ids = new int[7][5];
    	initWidgetIds();
        gameboard = new int[7][5];
        widgets = new IndexedButton[7][5];
    }
    
    @Override
    protected String getGameAsString(){
    	// save the game currently in progress
    	StringBuilder game_string = new StringBuilder();
    	for(int row = 0; row < 7; row++ ){    		
    		for( int column = 0; column < 5; column++ ){
    			int value = gameboard[row][column];
    			
    			game_string.append(value);
    			if( column < 4 ){
    				game_string.append(',');
    			}
    		}
    		if( row < 6 ){
				game_string.append(';');
			}
    	}
    	
    	return game_string.toString();
    }
    
    
    @Override
    protected boolean checkGameOver(){    	
    	for(int row = 0; row < 7; row++ ){    		
    		for( int column = 0; column < 5; column++ ){
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
    	
    	for(int row = 0; row < 7; row++ ){    		
    		for( int column = 0; column < 5; column++ ){
    			score = Math.max(gameboard[row][column], score);
    		}
    	}
    	
    	return score;
    }

    @Override
    protected LinkedList<IndexedButton> getEligibleDropTargets( int row, int column ){
    	LinkedList<IndexedButton> eligible = new LinkedList<IndexedButton>();
    	
    	synchronized( JumpSumEasy.this ){
	    	if( gameboard[row][column] <= 0 ){
	    		// can't move a blank piece
	    		return eligible;
	    	}
	    	
	    	// must check that there is a value in between the two as well
	    	if( row + 2 < 7 && (gameboard[row + 2][column] < 0) && (gameboard[row + 1][column] > 0) ){
	    		eligible.add( widgets[row + 2][column] );
	    	}
	    	if( row - 2 >= 0 && (gameboard[row - 2][column] < 0) && (gameboard[row - 1][column] > 0) ){
	    		eligible.add( widgets[row - 2][column] );
	    	}
	    	if( column + 2 < 5 && (gameboard[row][column + 2] < 0) && (gameboard[row][column + 1] > 0) ){
	    		eligible.add( widgets[row][column + 2] );
	    	}
	    	if( column - 2 >= 0 && (gameboard[row][column - 2] < 0) && (gameboard[row][column - 1] > 0) ){
	    		eligible.add( widgets[row][column - 2] );
	    	}
    	}
    	
    	return eligible;
    }

    @Override
    protected void doNewGame(){
    	synchronized( JumpSumEasy.this ){
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
	    	for(int row = 0; row < 7; row++ ){    		
	    		for( int column = 0; column < 5; column++ ){
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
    
    private void initWidgetIds(){
    	widget_ids[0][0] = R.id.widgetr0c0;
    	widget_ids[0][1] = R.id.widgetr0c1;
    	widget_ids[0][2] = R.id.widgetr0c2;
    	widget_ids[0][3] = R.id.widgetr0c3;
    	widget_ids[0][4] = R.id.widgetr0c4;
    	
    	widget_ids[1][0] = R.id.widgetr1c0;
    	widget_ids[1][1] = R.id.widgetr1c1;
    	widget_ids[1][2] = R.id.widgetr1c2;
    	widget_ids[1][3] = R.id.widgetr1c3;
    	widget_ids[1][4] = R.id.widgetr1c4;
    	
    	widget_ids[2][0] = R.id.widgetr2c0;
    	widget_ids[2][1] = R.id.widgetr2c1;
    	widget_ids[2][2] = R.id.widgetr2c2;
    	widget_ids[2][3] = R.id.widgetr2c3;
    	widget_ids[2][4] = R.id.widgetr2c4;
    	
    	widget_ids[3][0] = R.id.widgetr3c0;
    	widget_ids[3][1] = R.id.widgetr3c1;
    	widget_ids[3][2] = R.id.widgetr3c2;
    	widget_ids[3][3] = R.id.widgetr3c3;
    	widget_ids[3][4] = R.id.widgetr3c4;
    	
    	widget_ids[4][0] = R.id.widgetr4c0;
    	widget_ids[4][1] = R.id.widgetr4c1;
    	widget_ids[4][2] = R.id.widgetr4c2;
    	widget_ids[4][3] = R.id.widgetr4c3;
    	widget_ids[4][4] = R.id.widgetr4c4;
    	
    	widget_ids[5][0] = R.id.widgetr5c0;
    	widget_ids[5][1] = R.id.widgetr5c1;
    	widget_ids[5][2] = R.id.widgetr5c2;
    	widget_ids[5][3] = R.id.widgetr5c3;
    	widget_ids[5][4] = R.id.widgetr5c4;
    	
    	widget_ids[6][0] = R.id.widgetr6c0;
    	widget_ids[6][1] = R.id.widgetr6c1;
    	widget_ids[6][2] = R.id.widgetr6c2;
    	widget_ids[6][3] = R.id.widgetr6c3;
    	widget_ids[6][4] = R.id.widgetr6c4;
    }
}
