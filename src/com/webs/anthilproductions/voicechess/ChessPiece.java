// licensed under the Apache 2.0 License. See AndroidManifest.xml for further details
package com.webs.anthilproductions.voicechess;
// Code written by: Anton Gilgur
// ChessPiece class to have fields a normal Button does not

import com.webs.anthilproductions.voicechess.R;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.widget.*;
import android.graphics.*;

public class ChessPiece extends TextView
{
	private int color; // holds color of the piece
	private String enPassant; // if pawn can enPassant in which direction
	private String enPassantDirection; // direction of enPassant
	private int enPassantColor; // color of piece taken
	private boolean inCheck; // if king is in check
	private boolean canCastle; // if king/rook can castle
	private int row = 0, col = 0; // buttons don't change, only text does, so this works fine

	public ChessPiece(Context context, String s, int row, int col)
	{
		super(context);
		super.setText(s);
		super.setGravity(Gravity.CENTER); // centered text
		
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {setFont("port");} // portrait font
		else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {setFont("land");} // landscape font
		
		enPassant = "none";
		enPassantDirection = "none";
		enPassantColor = Color.MAGENTA;
		inCheck = false;
		canCastle = false; // all buttons to false from beginning
		color = Color.MAGENTA;
		this.row = row;
		this.col = col;		
	}
	
	public ChessPiece(Context context, String s)
	{
		super(context);
		super.setText(s);
		super.setGravity(Gravity.CENTER); // centered text
		
		if(s.equals("Undo") || s.equals("Speak Move")) {super.setBackgroundResource(android.R.drawable.btn_default);} // make these look like buttons
		if(s.equals("a") || s.equals("b") || s.equals("c") || s.equals("d") || s.equals("e") || s.equals("f") || s.equals("g") || s.equals("h") ||
			s.equals("1") || s.equals("2") || s.equals("3") || s.equals("4") || s.equals("5") || s.equals("6") || s.equals("7") || s.equals("8"))
			{super.setBackgroundResource(R.drawable.rectangle_outline);} // give these an outline
		
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {setFont("port");} // portrait font
		else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {setFont("land");} // landscape font
		
	}
	
	public String getText()
	{
		if(super.getText().length() == 0 || super.getText().length() > 1) {return super.getText().toString();}
		else
		{
			switch(super.getText().toString().charAt(0))
			{
			case '\u265A': return "K"; // king
			case '\u265B': return "Q"; // queen
			case '\u265C': return "R"; // rook
			case '\u265D': return "B"; // bishop
			case '\u265E': return "N"; // knight
			case '\u265F': return "P"; // pawn
			default: return super.getText().toString();
			}
		}
	}
	
	public void setText(String s)
	{
		if(s.length() == 0 || s.length() > 1) {super.setText(s);}
		else
		{
			switch(s.charAt(0))
			{
			case 'K': super.setText("\u265A"); break;
			case 'Q': super.setText("\u265B"); break;
			case 'R': super.setText("\u265C"); break;
			case 'B': super.setText("\u265D"); break;
			case 'N': super.setText("\u265E"); break;
			case 'P': super.setText("\u265F"); break;
			default: super.setText(s);
			}
		}
	}

	public int getColor() // return color
	{
		return color;
	}

	public void setColor(int c) // change color
	{
		color = c;
		super.setTextColor(c);
	}

	public String getEnPassant() // return enPassant
	{
		return enPassant;
	}

	public void setEnPassant(String rightLeft) // change enPassant
	{
		enPassant = rightLeft;
	}
	
	public String getEnPassantDirection() // return enPassantDirection
	{
		return enPassantDirection;
	}
	
	public void setEnPassantDirection(String s) // change enPassantDirection
	{
		enPassantDirection = s;
	}
	
	public int getEnPassantColor() // return enPassantColor
	{
		return enPassantColor;
	}
	
	public void setEnPassantColor(int c) // change enPassantColor
	{
		enPassantColor = c;
	}

	public boolean getInCheck() // return inCheck
	{
		return inCheck;
	}

	public void setInCheck(boolean trueFalse) // change inCheck
	{
		inCheck = trueFalse;
	}
	
	public boolean getCanCastle() // return canCastle
	{
		return canCastle;
	}

	public void setCanCastle(boolean trueFalse) // change canCastle
	{
		canCastle = trueFalse;
	}
	
	public int getRow() // return row
	{
		return row;
	}

	public void setRow(int c) // change row
	{
		row = c;
	}
	
	public int getCol() // return col
	{
		return col;
	}

	public void setCol(int c) // change col
	{
		col = c;
	}
	
	public void setFont(String orientation)
	{
		String buttonText = getText();
		// largest font sizes in portrait/landscape
		if((buttonText.equals("Speak Move") || buttonText.equals("Recognizer not present")) && orientation.equals("port")) {super.setTextSize(15);} // voice recog port
		else if((buttonText.equals("Speak Move") || buttonText.equals("Recognizer not present")) && orientation.equals("land")) {super.setTextSize(12);} // voice recog land
		else if(buttonText.equals("Undo") && orientation.equals("port")) {super.setTextSize(20);} // port undo
		else if(buttonText.equals("Undo") && orientation.equals("land")) {super.setTextSize(12);} // land undo
		else if(orientation.equals("port") && (buttonText.equals("a") || buttonText.equals("b") || buttonText.equals("c") || buttonText.equals("d") || buttonText.equals("e") || buttonText.equals("f") || buttonText.equals("g") || buttonText.equals("h") ||
		   buttonText.equals("1") || buttonText.equals("2") || buttonText.equals("3") || buttonText.equals("4") || buttonText.equals("5") || buttonText.equals("6") || buttonText.equals("7") || buttonText.equals("8"))) 
			{super.setTextSize(24);} // portrait row/col #s
		else if(orientation.equals("land") && (buttonText.equals("a") || buttonText.equals("b") || buttonText.equals("c") || buttonText.equals("d") || buttonText.equals("e") || buttonText.equals("f") || buttonText.equals("g") || buttonText.equals("h") ||
				   buttonText.equals("1") || buttonText.equals("2") || buttonText.equals("3") || buttonText.equals("4") || buttonText.equals("5") || buttonText.equals("6") || buttonText.equals("7") || buttonText.equals("8"))) 
					{super.setTextSize(13);} // portrait row/col #s
		else if(orientation.equals("port")) {super.setTextSize(40);} // portrait chess pieces
		else if(orientation.equals("land")) {super.setTextSize(22);} // landscape chess pieces
	}

}
// Anton Gilgur