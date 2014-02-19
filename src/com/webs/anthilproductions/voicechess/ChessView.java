// licensed under the Apache 2.0 License. See AndroidManifest.xml for further details
package com.webs.anthilproductions.voicechess;
// ChessView class written by: Anton Gilgur
// Voice Recognition written by: Sahil Gupta

// imports for graphics
import java.util.ArrayList;
import java.util.List;

import com.webs.anthilproductions.voicechess.R;

import android.os.Bundle;
import android.graphics.*;
import android.speech.RecognizerIntent;
import android.view.*;
import android.widget.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;

public class ChessView extends Activity implements View.OnClickListener
{
	// ---- constants to hold row and column values ----
	public static final int ROWS = 8, COLUMNS = 8;
	
	public ChessMover chessMover;

	private TableLayout tableLayout; 
	private TableRow tableRows[]; // layout and rows to hold buttons in a table
	private ChessPiece[][] buttonList; // holds all references in 2D array so assigning cells is possible
	private ChessPiece[] columnList; // hold column numbers
	private ChessPiece[] rowList; // hold row numbers
	
	private boolean pieceClicked; // records if a piece has been clicked
	private ChessPiece buttonClicked; // records which piece was clicked
	private int turn; // which color's turn it is
	private TextView notificationBox; // writes notifications for the user/indicates whose turn it is
	
	private ChessPiece king; // holds king
	
	private Object[] previousMove; // holds turn and buttonList of the last move (for undo or save state)
	private ChessPiece[][] initialList; // holds first list in case of new game/restart
	
	// -- voice recognition variables -- //
	private LinearLayout linear; // holds the buttons and text views
	private ChessPiece speakButton; // button that handles voice recognition
	private ChessPiece undoButton; // undoes previous move
	private TextView tv; // holds input from voice recog
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private String[] moves; // two places person is moving from and to
	
	// --- onCreate ---
	public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // lock orientation to portrait. Landscape is usually hard to see due to less vertical space
        setContentView(R.layout.activity_main); // set content
        tableLayout = (TableLayout) findViewById(R.id.tableLayout); // set layout
        tableRows = new TableRow[9]; // 9 rows (one for column numbers)
        linear = new LinearLayout(this);
        rowList = new ChessPiece[8]; // hold row numbers
        
        // -- voice recognition --
     	moves = new String[2];
     	moves[0] = "x0"; moves[1] = "x0"; // input if speech is not recognized     		
		
        // --- set buttons ---
        makeButtons();
		
		//create chessMover
        chessMover = new ChessMover();

		// --- white goes first ---
		turn = Color.WHITE;
		notificationBox.setBackgroundColor(turn); notificationBox.setTextColor(Color.BLACK); // white background to signal white's turn
		
		// create initialList
		initialList = new ChessPiece[8][8];
		save(initialList); // save text/color of initial pieces...initialList is now not an alias of previousMove[1] (other way around)
		
	} // end onCreate
	
	// create options menu and items
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "Undo");
		menu.add(0, 2, 0, "New Game");
		menu.add(0, 3, 0, "Directions");
		menu.add(0, 4, 0, "Quit");
		
		return result;
	}
	// handle selected items
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case 1: // undo
				restore(); // go back to previous move
				return true;
			case 2: // new game
				newGame();
				return true;
			case 3: // directions
				// ------ directions dialog is a long string --------- //
				dialog(
					"Modes of input:\n" +
					"\t•Touch\n" +
					"\t\t-Tap piece to move\n" +
					"\t\t-Tap square to move to\n" +
					"\t•Speech\n" +
					"\t\t-Tap \"Speak Move\"\n" +
					"\t\t-Say start location\n" +
					"\t\t-Say \"move\"\n" +
					"\t\t-Say end location\n" +
					"\n" +
					"Speech examples\n" +
					"\t•\"e2 move e4\"\n" +
					"\t•\"b8 move c6\"\n" +
					"\n" +
					"Speech tips\n" +
					"\t•Have an internet connection (all phones before 4.1)\n" +
					"\t•Enunciate each letter/number\n" +
					"\t•Don't blend letters/numbers together\n" +
					"\t•Speak close to phone microphone\n" +
					"\n" +
					"Notification Box\n" +
					"\t•Bottom left corner\n" +
					"\t•Background color shows which side to move\n" +
					"\t•\"I\" indicates invalid move\n" +
					"\t•\"C\" indicates check\n"
					);
				return true;
			case 4: // quit
				ChessView.this.finish();
				return true;
		}
		return false;		
	}
	
	private void newGame()
	{
		previousMove[0] = Color.WHITE; 
		previousMove[1] = initialList;
		previousMove[2] = "";
		king = buttonList[0][4]; // first king is white king
		restore(); // go back to beginning
	}
	
	private void submitMoves()
	{
		int[] configMoves = getConfiguredMoves();
		for(int i = 0; i < configMoves.length; i++)
		{
			if(configMoves[i] == -1) {notificationBox.setText("I"); return;} // if input is invalid/not recognized, show invalid move
		}
		// simulate click
		onClick(buttonList[configMoves[0]][configMoves[1]]);
		onClick(buttonList[configMoves[2]][configMoves[3]]);
	}
	
	// handle config changes
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		// change font size based on orientation		
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) 
		{
			speakButton.setFont("land");
			undoButton.setFont("land");
			for(int i = 0; i < columnList.length; i++) {columnList[i].setFont("land");}
			for(int i = 0; i < rowList.length; i++) {rowList[i].setFont("land");}
			for(int i = 0; i < buttonList.length; i++) 
			{ 
				for(int j = 0; j < buttonList[i].length; j++) 
				{ 
					buttonList[i][j].setFont("land");
				} 
			} // end of fors
		} // end if
		else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) 
		{
			speakButton.setFont("port");
			undoButton.setFont("port");
			for(int i = 0; i < columnList.length; i++) {columnList[i].setFont("port");}
			for(int i = 0; i < rowList.length; i++) {rowList[i].setFont("port");}
			for(int i = 0; i < buttonList.length; i++) 
			{ 
				for(int j = 0; j < buttonList[i].length; j++) 
				{ 
					buttonList[i][j].setFont("port");
				} 
			} // end of fors
		} // end if
	}
	
	// save necessary objects
	private void save(ChessPiece[][] newList)
	{
		if(previousMove == null) {previousMove = new Object[3];}
		previousMove[0] = turn; // turn is an int so not aliases
		
		// set text/color of previous list
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				newList[i][j] = new ChessPiece(this, "", i, j); //initialize them
				// set state of saved buttons (buttonList = buttonList does not update graphics and creates aliases)
				newList[i][j].setText(buttonList[i][j].getText());
				newList[i][j].setColor(buttonList[i][j].getColor());
				newList[i][j].setEnPassant(buttonList[i][j].getEnPassant());
				newList[i][j].setEnPassantColor(buttonList[i][j].getEnPassantColor());
				newList[i][j].setEnPassantDirection(buttonList[i][j].getEnPassantDirection());
				newList[i][j].setInCheck(buttonList[i][j].getInCheck());
				newList[i][j].setCanCastle(buttonList[i][j].getCanCastle());
			} 
		}
		previousMove[1] = newList; // newList is used so that whatever it is an alias of...will not be an alias of previousMove[1] (basically so that initialList does not change)
		previousMove[2] = notificationBox.getText();
	}
	
	// restore necessary objects 
	private void restore()
	{
		if (previousMove != null)  // can only restore if it has been initialized already
	    {
	        turn = ((Integer) previousMove[0]).intValue(); // set Turn
	        notificationBox.setText((String) previousMove[2]);
	        for(int i = 0; i < buttonList.length; i++) 
			{ 
				for(int j = 0; j < buttonList[i].length; j++) 
				{ 
					// set state of saved buttons (buttonList = buttonList does not update graphics and will make them aliases)
					buttonList[i][j].setText(((ChessPiece[][]) previousMove[1])[i][j].getText());
					buttonList[i][j].setColor(((ChessPiece[][]) previousMove[1])[i][j].getColor());
					buttonList[i][j].setEnPassant(((ChessPiece[][]) previousMove[1])[i][j].getEnPassant());
					buttonList[i][j].setEnPassantColor(((ChessPiece[][]) previousMove[1])[i][j].getEnPassantColor());
					buttonList[i][j].setEnPassantDirection(((ChessPiece[][]) previousMove[1])[i][j].getEnPassantDirection());
					buttonList[i][j].setInCheck(((ChessPiece[][]) previousMove[1])[i][j].getInCheck());
					buttonList[i][j].setCanCastle(((ChessPiece[][]) previousMove[1])[i][j].getCanCastle());
				} 
			}
	        
	        //set king and notificationBox colors
	        if(turn == Color.WHITE) {locateKing(Color.BLACK); notificationBox.setBackgroundColor(Color.WHITE); notificationBox.setTextColor(Color.BLACK); }
	        else if(turn == Color.BLACK) {locateKing(Color.WHITE); notificationBox.setBackgroundColor(Color.BLACK); notificationBox.setTextColor(Color.WHITE);} //locateKing finds opposite color king
	    } // end if
	}

	// --- makeButtons helper ---
	private void makeButtons()
	{
		// -- buttonList --
		buttonList = new ChessPiece[ROWS][COLUMNS]; // 8 rows with 8 buttons each 
		tableLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
		TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
		TableRow.LayoutParams cellParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
		
		// voice recognition
		speakButton = new ChessPiece(this, "Speak Move");
		undoButton = new ChessPiece(this, "Undo");
     	tv = new TextView(this);
     	tv.setTextColor(Color.WHITE);
     	tv.setTextSize(18);
     	tv.setGravity(Gravity.CENTER); // centered font
     	tv.setText("Voice input written here");
     	// Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
        	speakButton.setOnClickListener(this);
        	undoButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            undoButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {speakButton.setFont("land");}
            else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {speakButton.setFont("port");}
        }
		linear.addView(speakButton, cellParams);
		linear.addView(tv, cellParams);
		linear.addView(undoButton, cellParams);
		tableLayout.addView(linear, rowParams);
		
		for(int i = 0; i < 9; i++) 
		{ 
			tableRows[i] = new TableRow(this);
			if(i < 8)
			{				
				for(int j = 0; j < 9; j++) 
				{
					if(j == 0)
					{
						rowList[i] = new ChessPiece(this, "" + (8-i));
						// gets rid of rectangle outline //rowList[i].setBackgroundColor(Color.rgb(0,128,128)); // same as teal background color of app
						rowList[i].setTextColor(Color.WHITE); // white to compliment the teal
						rowList[i].setPadding(11, 0, 11, 0); // so #s do not touch edges
						tableRows[i].addView(rowList[i], cellParams); // add a not listened chessPiece to hold row number
					}
					else
					{
						buttonList[i][j-1] = new ChessPiece(this, "", i, j-1);  // set all blank buttons
						tableRows[i].addView(buttonList[i][j-1], cellParams);
						buttonList[i][j-1].setOnClickListener(this); // register buttons
					}
				}
			}
			else
			{
				// add not listened column letters
				columnList = new ChessPiece[8];
				notificationBox = new TextView(this);
				notificationBox.setTextSize(20);
				notificationBox.setText(""); //first column is row numbers, so this would otherwise be blank // multiple spaces so that row numbers have more "padding"
				tableRows[i].addView(notificationBox, cellParams);
				
				columnList[0] = new ChessPiece(this, "a");
				columnList[1] = new ChessPiece(this, "b");
				columnList[2] = new ChessPiece(this, "c");
				columnList[3] = new ChessPiece(this, "d");
				columnList[4] = new ChessPiece(this, "e");
				columnList[5] = new ChessPiece(this, "f");
				columnList[6] = new ChessPiece(this, "g");
				columnList[7] = new ChessPiece(this, "h");
				for(int clNum = 0; clNum < columnList.length; clNum++)
				{
					columnList[clNum].setPadding(0, 8, 0, 5); // so #s do not touch edges
					// gets rid of rectangle outline //columnList[clNum].setBackgroundColor(Color.rgb(0,128,128)); // same as teal background color of app
					columnList[clNum].setTextColor(Color.WHITE);
					tableRows[i].addView(columnList[clNum], cellParams);
				}
			}
			tableLayout.addView(tableRows[i], rowParams);
		}
		
		// -- Set colors of table: light brown (white) and dark brown (black)
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				if((i%2 == 0) && (j%2 == 0))
					//buttonList[i][j].setBackgroundColor(Color.rgb(240, 200, 190)); // light brown color
					buttonList[i][j].setBackgroundColor(Color.rgb(215, 180, 125));
				else if(j%2 == 1 && i%2 == 1)
					//buttonList[i][j].setBackgroundColor(Color.rgb(240, 200, 190)); // light brown color
					buttonList[i][j].setBackgroundColor(Color.rgb(215, 180, 125));
				else
					buttonList[i][j].setBackgroundColor(Color.rgb(139, 69, 19)); // dark brown
					//buttonList[i][j].setBackgroundColor(Color.rgb(72,31,3));
			} 
		}

		// -- Pawns --
		for(int i = 0; i < 16; i++) 
		{
			if(i < 8)
			{
				buttonList[1][i].setText("P"); // row 1 is all pawns
				buttonList[1][i].setColor(Color.BLACK); // row 1 is black
			}
			else if(i < 16)
			{
				buttonList[6][i%8].setText("P"); // row 6 is all pawns
				buttonList[6][i%8].setColor(Color.WHITE); // row 6 is white
			}
		}

		// -- Rooks --
		buttonList[0][0].setText("R"); // rook in 0,0
		buttonList[0][7].setText("R"); // rook in 0,7
			buttonList[0][0].setColor(Color.BLACK); 
			buttonList[0][7].setColor(Color.BLACK); // rooks are black
			buttonList[0][0].setCanCastle(true); 
			buttonList[0][7].setCanCastle(true); // rooks can castle at first
		buttonList[7][0].setText("R"); // rook in 7,0
		buttonList[7][7].setText("R"); // rook in 7,7
			buttonList[7][0].setColor(Color.WHITE); 
			buttonList[7][7].setColor(Color.WHITE); // rooks are white
			buttonList[7][0].setCanCastle(true); 
			buttonList[7][7].setCanCastle(true); // rooks can castle at first

		// -- Knights --
		buttonList[0][1].setText("N"); // knight in 0,1
		buttonList[0][6].setText("N"); // knight in 0,6
			buttonList[0][1].setColor(Color.BLACK);
			buttonList[0][6].setColor(Color.BLACK); // knights are black
		buttonList[7][1].setText("N"); // knight in 7,1
		buttonList[7][6].setText("N"); // knight in 7,6
			buttonList[7][1].setColor(Color.WHITE);
			buttonList[7][6].setColor(Color.WHITE); // knights are white

		// -- Bishops --
		buttonList[0][2].setText("B"); // bishop in 0,2
		buttonList[0][5].setText("B"); // bishop in 0,5
			buttonList[0][2].setColor(Color.BLACK);
			buttonList[0][5].setColor(Color.BLACK); // bishops are black
		buttonList[7][2].setText("B"); // bishop in 7,2
		buttonList[7][5].setText("B"); // bishop in 7,5
			buttonList[7][2].setColor(Color.WHITE);
			buttonList[7][5].setColor(Color.WHITE); // bishops are white

		// -- Queens --
		buttonList[0][3].setText("Q"); // queen in 0,3
			buttonList[0][3].setColor(Color.BLACK); // queen is black
		buttonList[7][3].setText("Q"); // queen in 7,3
			buttonList[7][3].setColor(Color.WHITE); // queen is white

		// -- Kings --
		buttonList[0][4].setText("K"); // king in 0,4
			buttonList[0][4].setColor(Color.BLACK); // king is black
			buttonList[0][4].setCanCastle(true); // king can castle at first
			king = buttonList[0][4]; // first king is white king
		buttonList[7][4].setText("K"); // king in 7,4
			buttonList[7][4].setColor(Color.WHITE); // king is white
			buttonList[7][4].setCanCastle(true); // king can castle at first

	} // end of makeButtons


	// --- onClick ---
	public void onClick(View v)
	{
		if(((ChessPiece) v).getText().equals("Speak Move"))
		{
			startVoiceRecognitionActivity(); // this now auto-submits
			// submitMoves() is not here due to it not being modal
		}
		else if(((ChessPiece) v).getText().equals("Undo"))
		{
			restore();
		}
		//if no button has been clicked yet and the current one is a piece
		else if(!(((ChessPiece) v).getText() == "") && (!pieceClicked) && (((ChessPiece) v).getColor() == turn))
		{
			pieceClicked = true; // a piece has been clicked
			buttonClicked = (ChessPiece) v; // button recorded
			save(new ChessPiece[8][8]); // save state in case of undo
		}
		
		// if same color is clicked, switch control to that color
		else if(pieceClicked && ((ChessPiece) v).getColor() == buttonClicked.getColor())
		{
			buttonClicked = (ChessPiece) v;
		}
		
		// cannot take king and show a dialog box if tried to take
		else if(pieceClicked && ((ChessPiece) v).getColor() != buttonClicked.getColor() && ((ChessPiece) v).getText() == "K")
		{
			//dialog("Cannot take King");
			notificationBox.setText("I");
			return; 
		}
						
		// if a button has been clicked and it can move to the next clicked button
		else if(pieceClicked && canPieceMove((ChessPiece) v, buttonClicked) && chessMover.canMove((ChessPiece) v, buttonClicked, buttonList))
		{
			notificationBox.setText("");
			// promotion (dialog is modal so it will occur after text is changed)
			if((((ChessPiece) v).getRow() == 7 || ((ChessPiece) v).getRow() == 0) && buttonClicked.getText() == "P")
				{promotion((ChessPiece) v);}
			
			clearEnPassant(); // pieces cannot enPassant

			((ChessPiece) v).setText(buttonClicked.getText()); // set button's text to the text of the previously clicked one
			((ChessPiece) v).setColor(buttonClicked.getColor()); // set button's color to the color of the previously clicked one			
			
			buttonClicked.setText(""); // change previously clicked button's text to null
			buttonClicked.setColor(Color.MAGENTA); // any color other than black or white
			buttonClicked.setCanCastle(false); // cannot castle after making first move
			pieceClicked = false; // no piece has been clicked anymore
			
			//locate opposite color king
			locateKing(turn);
		
			check();
			if(king.getInCheck()) {/*dialog("Check");*/ notificationBox.setText("C");}
			
			if(checkmate()) {dialog("Checkmate"); newGame(); return;} // see if king is in check or mate and if it is in mate, end the game and show a dialog box
			if(stalemate()) {dialog("Stalemate"); newGame(); return;} // if stalemate, end game and show dialog box
			
			clearEnPassantDirections(); // piece has not enPassant-ed
			
			if(turn == Color.WHITE) {turn = Color.BLACK; notificationBox.setBackgroundColor(Color.BLACK); notificationBox.setTextColor(Color.WHITE);}
			else if(turn == Color.BLACK) {turn = Color.WHITE; notificationBox.setBackgroundColor(Color.WHITE); notificationBox.setTextColor(Color.BLACK);} // other person's turn now
		
		}
		// other moves are invalid
		else{notificationBox.setText("I");}
		
		// this would waste too much processing power for almost no reason...not ideal on a mobile device
		// if the king is in check and user tries to move a piece without realizing
		//else if(pieceClicked && buttonClicked.getColor() == turn && buttonClicked.getText() != "K" && king.getInCheck() && !canPieceMove(buttonClicked)) 
			//{dialog("King is in or would be in Check"); notificationBox.setText("I");}

	} // end of onClick
	
	// clears all enPassant so no piece can enPassant
	private void clearEnPassant()
	{
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				if(buttonList[i][j].getColor() == buttonClicked.getColor())
					{buttonList[i][j].setEnPassant("none");}
			} 
		} 	
	}
	
	// clears all enPassant directions to show that no piece has enPassant-ed
	private void clearEnPassantDirections()
	{
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				buttonList[i][j].setEnPassantDirection("none");
				buttonList[i][j].setEnPassantColor(Color.MAGENTA);
			} 
		} 	
	}
	
	private void check()
	{
		king.setInCheck(false); // not in check unless found to be in check below
		
		//see if king is in check
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				if(buttonList[i][j].getText() != "" && buttonList[i][j].getColor() != king.getColor() && chessMover.canMove(king, buttonList[i][j], buttonList)) 
				{
					king.setInCheck(true);
				} // end if 1				 						
			} // end for 2
		} // end for 1
	} // end of check
	
	private boolean checkmate()
	{
		if(!king.getInCheck()) return false; // if the king is not in check then it is false
		int kRow = king.getRow(), kCol = king.getCol();
		
		// 8 spots king can move to
		// check if spots are on board/king can move to them
		// true if the spot is not on the board or king will be in check there
		boolean checkLeftUp = !(kRow - 1 > -1 && kCol - 1 > -1 && chessMover.canMove(buttonList[kRow-1][kCol-1], king, buttonList));
		boolean checkUp = !(kRow - 1 > -1 &&  chessMover.canMove(buttonList[kRow-1][kCol], king, buttonList));
		boolean checkRightUp = !(kRow - 1 > -1 && kCol + 1 < 8 &&  chessMover.canMove(buttonList[kRow-1][kCol+1], king, buttonList));
		boolean checkRight = !(kCol + 1 < 8 &&  chessMover.canMove(buttonList[kRow][kCol+1], king, buttonList));
		boolean checkRightDown = !(kCol + 1 < 8 && kRow + 1 < 8 &&  chessMover.canMove(buttonList[kRow+1][kCol+1], king, buttonList));
		boolean checkDown = !(kRow + 1 < 8 &&  chessMover.canMove(buttonList[kRow+1][kCol], king, buttonList));
		boolean checkLeftDown = !(kRow + 1 < 8 && kCol - 1 > -1 &&  chessMover.canMove(buttonList[kRow+1][kCol-1], king, buttonList));
		boolean checkLeft = !(kCol - 1 > -1 &&  chessMover.canMove(buttonList[kRow][kCol-1], king, buttonList));

		// true if king cannot move anywhere and nothing can block it, false if it can or something can block the check
		return canAnythingMove() && checkLeftUp && checkUp && checkRightUp && checkRight && checkRightDown && checkDown && checkLeftDown && checkLeft;
	} // end checkmate
	
	private boolean stalemate()
	{
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{
				if(buttonList[i][j].getText() != "" && buttonList[i][j].getColor() != turn) // same color as turn
				{
					if(buttonList[i][j].getInCheck()) return false; // if king is in check, then it is not a stalemate
					ChessPiece[] array = chessMover.getCanMoveArray(buttonList[i][j], buttonList); // array to hold where that button can move
					try	{array[0].getText();} catch(NullPointerException e) {continue;} // if the 0th element is null then skip to the next iteration
					
					ChessPiece clickedButton = buttonList[i][j];
					if(clickedButton.getEnPassantDirection().equals("right")) //right enPassant
					{
						buttonList[clickedButton.getRow()][clickedButton.getCol() + 1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol() + 1].setColor(clickedButton.getEnPassantColor()); 
						clickedButton.setEnPassantDirection("none"); clickedButton.setEnPassantColor(Color.MAGENTA);
					} 
					else if(clickedButton.getEnPassantDirection().equals("left")) //left enPassant
					{
						buttonList[clickedButton.getRow()][clickedButton.getCol() - 1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol() - 1].setColor(clickedButton.getEnPassantColor()); 
						clickedButton.setEnPassantDirection("none"); clickedButton.setEnPassantColor(Color.MAGENTA);
					} 
					
					if(array.length > 0) {return false;} // if the array length is greater than 0 (it can move somewhere), then it is not a stalemate
				} // end if 1
			} // end for 2
		} // end for 1
		return true; // if there has been no false returned so far, then it it is a stalemate
	} // end stalemate
	
	// find if previous piece can move to current place and not put king into check in doing so
	private boolean canPieceMove(ChessPiece currentButton, ChessPiece buttonClicked)
	{
		String oldText = "";
		int oldColor = Color.MAGENTA; // hold old text and color
		String newText = buttonClicked.getText();
		int newColor = buttonClicked.getColor(); // hold new text and color
		if(newText == "K") {return true;}
		
		// if the king is not in check anymore, piece can move, otherwise it cannot
		return checkArray(buttonClicked, currentButton, oldText, oldColor, newText, newColor, false); 	
	}
	
	// find if anything can move if king is in check
	private boolean canAnythingMove()
	{		
		String oldText = "", newText = "";
		int oldColor = Color.MAGENTA, newColor = Color.MAGENTA; // hold old/new text and color
		
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{
				if(buttonList[i][j].getText() != "" && buttonList[i][j].getColor() == king.getColor() && buttonList[i][j].getText() != "K") // if same color and not king
				{
					newText = buttonList[i][j].getText();
					newColor = buttonList[i][j].getColor(); // set text + color
					
					// if king is not in checkmate
					if(!checkArray(buttonList[i][j], null, oldText, oldColor, newText, newColor, true)) return false;
						
				} // end if 2				 						
			} // end for 2
		} // end for 1
		return true; // true if no false has been returned
	}
	
	private boolean checkArray(ChessPiece clickedButton, ChessPiece currentButton, String oldText, int oldColor, String newText, int newColor, boolean checkmate)
	{
		ChessPiece[] array = chessMover.getCanMoveArray(clickedButton, buttonList); // array to hold where that button can move
		for(int k = 0; k < array.length; k++) // iterate through array
		{
			// set the old text...if the array element is null (since the method gives a max amount of elements which may not be filled) then end the loop because there are no more elements
			try	{oldText = array[k].getText(); } catch(NullPointerException e) {k = array.length; continue;}
			//although this would save processing power, this screws up enPassant... //if(currentButton != null && currentButton != buttonList[array[k].getRow()][array[k].getCol()]) {continue;} // if the button is not the same, then try the next one
			oldColor = array[k].getColor(); // set old color
			
			buttonList[array[k].getRow()][array[k].getCol()].setText(newText); // move the piece to the place array specifies
			buttonList[array[k].getRow()][array[k].getCol()].setColor(newColor); // change the color to piece's color
			clickedButton.setText("");
			clickedButton.setColor(Color.MAGENTA); // remove current button before checking
			
			// right - if pawn can enPassant and did
			if(clickedButton.getEnPassantDirection().equals("right") && 
				(array[k].getRow() == clickedButton.getRow()+1 || array[k].getRow() == clickedButton.getRow()-1) && array[k].getCol() == clickedButton.getCol() + 1) 
			{buttonList[clickedButton.getRow()][clickedButton.getCol()+1].setText(""); buttonList[clickedButton.getRow()][clickedButton.getCol()+1].setColor(Color.MAGENTA);}
			else if(clickedButton.getEnPassantDirection().equals("right")) // pawn did not enPassant
			{buttonList[clickedButton.getRow()][clickedButton.getCol()+1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol()+1].setColor(clickedButton.getEnPassantColor());} 
			
			// left - if pawn can enPassant and did
			else if(clickedButton.getEnPassantDirection().equals("left") && 
					(array[k].getRow() == clickedButton.getRow()+1 || array[k].getRow() == clickedButton.getRow()-1) && array[k].getCol() == clickedButton.getCol() - 1) 
			{buttonList[clickedButton.getRow()][clickedButton.getCol()-1].setText(""); buttonList[clickedButton.getRow()][clickedButton.getCol()-1].setColor(Color.MAGENTA);}
			else if(clickedButton.getEnPassantDirection().equals("left") ) // pawn did not enPassant
			{buttonList[clickedButton.getRow()][clickedButton.getCol()-1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol()-1].setColor(clickedButton.getEnPassantColor());} 
			
			check(); // see if the king is still in check
			
			buttonList[array[k].getRow()][array[k].getCol()].setText(oldText); 
			buttonList[array[k].getRow()][array[k].getCol()].setColor(oldColor);
			
			if(clickedButton.getEnPassantDirection().equals("right") && 
				(array[k].getRow() == clickedButton.getRow()+1 || array[k].getRow() == clickedButton.getRow()-1) && array[k].getCol() == clickedButton.getCol() + 1) //right enPassant
			{
				buttonList[clickedButton.getRow()][clickedButton.getCol() + 1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol() + 1].setColor(clickedButton.getEnPassantColor()); 
				clickedButton.setEnPassantDirection("none"); clickedButton.setEnPassantColor(Color.MAGENTA);
			} 
			else if(clickedButton.getEnPassantDirection().equals("left") && 
					(array[k].getRow() == clickedButton.getRow()+1 || array[k].getRow() == clickedButton.getRow()-1) && array[k].getCol() == clickedButton.getCol() - 1) //left enPassant
			{
				buttonList[clickedButton.getRow()][clickedButton.getCol() - 1].setText("P"); buttonList[clickedButton.getRow()][clickedButton.getCol() - 1].setColor(clickedButton.getEnPassantColor()); 
				clickedButton.setEnPassantDirection("none"); clickedButton.setEnPassantColor(Color.MAGENTA);
			} 
			
			clickedButton.setText(newText);
			clickedButton.setColor(newColor);
			
			if(checkmate) 
			{
				// if the king is not in check, it is not in checkmate
				if(!king.getInCheck()) 
				{
					king.setInCheck(true); 
					return false; // king is still in check even if not checkmate because check() sets it to false  
				}
			}
			else
			{
				// if the king is not in check anymore, piece can move
				if(!king.getInCheck() && currentButton == buttonList[array[k].getRow()][array[k].getCol()])
				{
					return true;
				} 
			}
		} // end for
		
		if(checkmate) {return true;} // hasn't returned false, then true
		else {return false;} // hasn't returned true, then false
	}
	
	private void locateKing(int c)
	{
		for(int i = 0; i < buttonList.length; i++) 
		{ 
			for(int j = 0; j < buttonList[i].length; j++) 
			{ 
				if(buttonList[i][j].getColor() != c && buttonList[i][j].getText() == "K") 
				{
					king = buttonList[i][j];
				}						
			} 
		} // end king location
	}
	
	private void dialog(final String s)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(s)
			   .setCancelable(false)
			   .setNeutralButton("Okay", new DialogInterface.OnClickListener()
			   { public void onClick(DialogInterface dialog, int id) 
			   		{/*if(s.equals("Checkmate") || s.equals("Stalemate")) ChessView.this.finish();
			   		 else*/ dialog.dismiss();}});
		AlertDialog alert = builder.create();
		alert.show();			   
	}
	
	private void promotion(final ChessPiece previousButton)
	{
		final CharSequence[] possiblePieces = { "\u265B", "\u265E", "\u265C", "\u265D" }; // Q, N, R, B - order based off what users choose most
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Promotion");
		builder.setSingleChoiceItems(possiblePieces, -1, new DialogInterface.OnClickListener() 
			{ public void onClick(DialogInterface dialog, int piece) 
			  {
				previousButton.setText(possiblePieces[piece]);
				dialog.dismiss();
				
				// do checks again because dialog is not modal**
				if(turn == Color.WHITE) turn = Color.BLACK;
				else if(turn == Color.BLACK) turn = Color.WHITE; // first person's turn again
				
				//locate opposite color king
				locateKing(turn);
			
				check();
				if(king.getInCheck()) {dialog("Check");}
				
				if(checkmate()) {dialog("Checkmate");} // see if king is in check or mate and if it is in mate, end the game and show a dialog box
				if(stalemate()) {dialog("Stalemate");} // if stalemate, end game and show dialog box
				
				if(turn == Color.WHITE) turn = Color.BLACK;
				else if(turn == Color.BLACK) turn = Color.WHITE; // other person's turn now
			}});
		AlertDialog alert = builder.create();
		alert.show();
	} // end promotion	

	
	// ---------------------------------------- Everything from here on is Voice Recognition ------------------------------------------------- //
	// Written mainly by: Sahil Gupta
	
	
	//Fire an intent to start the speech recognition activity.
    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "e.g.  b1  'move'  c3");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
  
    //Handle the results from the recognition activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {    	
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            //System.out.println(matches);
            //process raw input
            moves=processInput(matches);
            tv.setText(getMoves()); // set input to text of tv
            submitMoves(); // voice recog auto-submits
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    // return string of moves to ask user if this is right input
    public String getMoves()
    {
    	if(moves[0].equals("x0") && moves[1].equals("x0")) {return "?" + " " + "\u2192" + " " + "?";} // invalid/unidentified moves
    	else if(moves[0].equals("x0")) {return "?" + " " + "\u2192" + " " + moves[1];} // moves 0 invalid
    	else if(moves[1].equals("x0")) {return moves[0] + " " + "\u2192" + " " + "?";} // moves 1 invalid
    	return moves[0] + " " + "\u2192" + " " + moves[1];
    }
    
    // return the to and from places in row/col (ints) format
    public int[] getConfiguredMoves()
    {
    	return configureMoves(moves);
    }
    
    // change strings to 4 ints of row/col, row/col
    private int[] configureMoves(String[] moves)
    {
    	int[] configuredMoves = new int[4];
    	
    	// first row
    	switch(moves[0].charAt(1)) // second char (number) is actually the row
    	{
    		case '8': configuredMoves[0] = 0; break; // 8 is row 0
    		case '7': configuredMoves[0] = 1; break; // 7 is row 1
    		case '6': configuredMoves[0] = 2; break; // 6 is row 2
    		case '5': configuredMoves[0] = 3; break; // 5 is row 3
    		case '4': configuredMoves[0] = 4; break; // 4 is row 4
    		case '3': configuredMoves[0] = 5; break; // 3 is row 5
    		case '2': configuredMoves[0] = 6; break; // 2 is row 6
    		case '1': configuredMoves[0] = 7; break; // 1 is row 7
    		case '0': configuredMoves[0] = -1; break; // 0 = invalid input, -1 = flag for calling method
    	}
    	
    	// first col
    	switch(moves[0].charAt(0)) // first char (letter) is actually the col
    	{
    		case 'a': configuredMoves[1] = 0; break; // a is col 0
    		case 'b': configuredMoves[1] = 1; break; // b is col 1
    		case 'c': configuredMoves[1] = 2; break; // c is col 2
    		case 'd': configuredMoves[1] = 3; break; // d is col 3
    		case 'e': configuredMoves[1] = 4; break; // e is col 4
    		case 'f': configuredMoves[1] = 5; break; // f is col 5
    		case 'g': configuredMoves[1] = 6; break; // g is col 6
    		case 'h': configuredMoves[1] = 7; break; // h is col 7
    		case 'x': configuredMoves[1] = -1; break; // x = invalid input, -1 = flag for calling method
    	}
    	
    	// second row
    	switch(moves[1].charAt(1)) // second char (number) is actually the row
    	{
    		case '8': configuredMoves[2] = 0; break; // 8 is row 0
    		case '7': configuredMoves[2] = 1; break; // 7 is row 1
    		case '6': configuredMoves[2] = 2; break; // 6 is row 2
    		case '5': configuredMoves[2] = 3; break; // 5 is row 3
    		case '4': configuredMoves[2] = 4; break; // 4 is row 4
    		case '3': configuredMoves[2] = 5; break; // 3 is row 5
    		case '2': configuredMoves[2] = 6; break; // 2 is row 6
    		case '1': configuredMoves[2] = 7; break; // 1 is row 7
    		case '0': configuredMoves[2] = -1; break; // 0 = invalid input, -1 = flag for calling method
    	}
    	
    	// second col
    	switch(moves[1].charAt(0)) // first char (letter) is actually the col
    	{
    		case 'a': configuredMoves[3] = 0; break; // a is col 0
    		case 'b': configuredMoves[3] = 1; break; // b is col 1
    		case 'c': configuredMoves[3] = 2; break; // c is col 2
    		case 'd': configuredMoves[3] = 3; break; // d is col 3
    		case 'e': configuredMoves[3] = 4; break; // e is col 4
    		case 'f': configuredMoves[3] = 5; break; // f is col 5
    		case 'g': configuredMoves[3] = 6; break; // g is col 6
    		case 'h': configuredMoves[3] = 7; break; // h is col 7
    		case 'x': configuredMoves[3] = -1; break; // x = invalid input, -1 = flag for calling method
    	}
    	
    	// return moves in row/col format as ints
    	return configuredMoves;
    }
    
  //returns start location and end location
    //spoken delimiter is "move"
  //returns start location and end location
    //spoken delimiter is "move"
    private String[] processInput(ArrayList<String> data){
    	String[] results=new String[2];
    	//holds each string in data, but each string is cut off at delimiter "move"
    	ArrayList<String> dataBeforeMove=new ArrayList<String>();
    	ArrayList<String> dataAfterMove=new ArrayList<String>();
    	int indexOfMove=0;
    	int spaceAfterMove=0;
    	
    	
    	
    	//make all strings lowercase
    	for(int i=0;i<data.size();i++)
    	{
    		data.set(i, data.get(i).toLowerCase());
    	}
    	    	
    	
    	
    	for(String s:data){
    		//"move" might be interpreted as "moves","moved","movie","mood"
    		indexOfMove=Math.max(s.indexOf("mov"),s.indexOf("mood"));
    		//as long as "move" or some variation is found in the string
    		if(indexOfMove>2){
    			//add text before "move"
    			dataBeforeMove.add(s.substring(0, indexOfMove-1));
    			
    			//add text after "move"
    			spaceAfterMove=s.indexOf(" ", indexOfMove);
    			if(spaceAfterMove!=-1){
    				dataAfterMove.add(s.substring(spaceAfterMove + 1 , s.length()));
    			}
    		}
    		//if input is bad
    		else{
    			results[0]="x0";
    			results[1]="x0";
    			return results;
    		}
    	}
    	
    	//System.out.println(dataBeforeMove);
    	//System.out.println(dataAfterMove);
    	
    	results[0]=processEachInput(dataBeforeMove);
    	results[1]=processEachInput(dataAfterMove);   	
    	
    	return results;
    }
    	


    // string output is "(mostFrequentLetter)(mostFrequentNumber)"
 	// if there is a bad input, return "x0"
 	private String processEachInput(ArrayList<String> data){
 		//store chars and frequencies
 		//a,b,c,d,e,f,g,h
 		//ascii of 'a' is 97
 		//ascii of '1' is 49

 		int[] charFreqs={0,0,0,0,0,0,0,0};   	
 		int[] numFreqs= {0,0,0,0,0,0,0,0};
 		char mostFreqChar;
 		char mostFreqNum;
 		char firstLetter;
 		char notFirstLetter;


 		//parse each character in each string for most freq char and number
 		for(int i=0;i<data.size();i++){ 
 			firstLetter=data.get(i).charAt(0);			
 			
 			//if first word is "at " || "and " || "have " || "ask " || "after " || "s ", then increase freq of 'f'
 			if(data.get(i).indexOf("at ")==0 || data.get(i).indexOf("and ")==0 || data.get(i).indexOf("have ")==0 || data.get(i).indexOf("ask ")==0 || data.get(i).indexOf("after ")==0 || data.get(i).indexOf("s ")==0){
 				charFreqs['f'-'a']++;    				
 			}
 			//if first word is "age ", then increase freq of 'h'
 			else if(data.get(i).indexOf("age ")==0){
 				charFreqs['h'-'a']++;
 			}
 			//if first word is "8 " || "hey ", then increase freq of 'a'
 			else if(data.get(i).indexOf("8 ")==0 || data.get(i).indexOf("hey ")==0){
 				charFreqs['a'-'a']++;
 			}
 			//if first word is "see " || "sea ", then increase freq of 'c'
 			else if(data.get(i).indexOf("see ")==0 || data.get(i).indexOf("sea ")==0){
 				charFreqs['c'-'a']++;
 			}
 			//if first word is "you " || "the " || "he ", then increase freq of 'e'
 			else if(data.get(i).indexOf("you ")==0 || data.get(i).indexOf("the ")==0 || data.get(i).indexOf("he ")==0){
 				charFreqs['e'-'a']++;
 			}
 			//if char is between 'a' and 'h'			
 			else if(firstLetter>96 && firstLetter<105){
 				charFreqs[firstLetter-'a']++;    	    			
 			}
 			
 			//if second word is "want"/"wanna", then increase freq of '1'
     		if(data.get(i).indexOf(" wan")>0){
     			numFreqs['1'-'1']++;
     		}
     		//if second word is "to"/"too", then increase freq of '2'
     		else if(data.get(i).indexOf(" to")>0){
     			numFreqs['2'-'1']++;
     		}
     		//if first word is "before" || second word is "for", then increase freq of '4'
     		else if(data.get(i).indexOf("before")==0 || data.get(i).indexOf(" for")>0){
     			numFreqs['4'-'1']++;
     		}
     		//if char is between '1' and '8' && not the first char
 			else{
 				for(int j=1;j<data.get(i).length();j++){
 					notFirstLetter=data.get(i).charAt(j);
 					if(notFirstLetter>48 && notFirstLetter<57){
 						numFreqs[notFirstLetter-'1']++;
 					}
 				}
 			}


 		}

 		mostFreqChar='x';
 		int maxFreq=0;
 		for(int i=0;i<charFreqs.length;i++){
 			if(charFreqs[i]>maxFreq){
 				mostFreqChar=(char) ('a'+i);
 				maxFreq=charFreqs[i];
 			}    		
 		}

 		mostFreqNum='0';
 		maxFreq=0;
 		for(int i=0;i<numFreqs.length;i++){
 			if(numFreqs[i]>maxFreq){
 				mostFreqNum=(char) ('1'+i);
 				maxFreq=numFreqs[i];
 			}    		
 		}

 		//if there is a bad letter/number, return "x0"
 		if(mostFreqChar=='x' || mostFreqNum=='0'){
 			return "x0";
 		}   	    	

 		return ""+mostFreqChar+mostFreqNum;
 	}
	
}// end of class

// Anton Gilgur and Sahil Gupta