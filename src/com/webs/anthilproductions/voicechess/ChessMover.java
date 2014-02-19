// licensed under the Apache 2.0 License. See AndroidManifest.xml for further details
package com.webs.anthilproductions.voicechess;
// ChessMover class written by: Anton Gilgur

// imports for graphics
import android.graphics.Color;

public class ChessMover  
{	
	
	// checks if piece can move
	public boolean canMove(ChessPiece currentButton, ChessPiece previousButton, ChessPiece[][] buttonList) // tests if previousButton can move to position of currentButton
	{	
		// cannot move to same color piece
		if(previousButton.getColor() == currentButton.getColor()) return false;
		if(previousButton.getText() == "") return false; // an empty button can't move
		
		// initialize rows and cols
		int pButtonRow = previousButton.getRow(), pButtonCol = previousButton.getCol(); 
		int cButtonRow = currentButton.getRow(), cButtonCol = currentButton.getCol();		
		
		// java 6 can only use chars, not Strings
		switch((char) previousButton.getText().charAt(0)) // switch based on text of previousButton 
		{
			case 'P': return pawnCanMove(previousButton, currentButton, pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // pawn	
			case 'R': return rookCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // rook
			case 'N': return knightCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // knight
			case 'B': return bishopCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // bishop
			case 'Q': return queenCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // queen
			case 'K': return kingCanMove(previousButton, currentButton, pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList);	// king		
		} // end of switch
		return false;

	} // end of canMove
	
	// see if a pawn can move
	private boolean pawnCanMove(ChessPiece previousButton, ChessPiece currentButton, int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		//black
		if(pButtonRow == 1 && previousButton.getColor() == Color.BLACK) // if the pawn is in the first row and black
		{	
			if((currentButton.getText() != "") && 
				((pButtonCol + 1) == cButtonCol || (pButtonCol - 1) == cButtonCol) &&
				((pButtonRow + 1) == cButtonRow)) // if there is another piece diagonal
				{return true;}
			if(currentButton.getText() == "") // if not diagonal cannot take piece
			{
				if(pButtonCol != cButtonCol) // must be in same column
					{return false;}
				if((pButtonRow + 1) == cButtonRow || (pButtonRow + 2) == cButtonRow) // if the other button is one/two row(s) down
				{
     				if(pButtonRow + 2 == cButtonRow)
					{	 
                        if(buttonList[pButtonRow+1][pButtonCol].getText() != "") // if double move and piece in between 
                        	{return false;}                                		 	
                        if(cButtonCol + 1 <= 7)
							{buttonList[cButtonRow][cButtonCol+1].setEnPassant("left");}
						if(cButtonCol - 1 >= 0)
							{buttonList[cButtonRow][cButtonCol-1].setEnPassant("right");}
					} // pawn has double moved
					return true;
				}
			}
		}
		
		else if(previousButton.getColor() == Color.BLACK) // if black
		{
			// enPassant
			if(currentButton.getText() == "" && cButtonRow == 5 
			   && ((pButtonCol + 1 == cButtonCol && previousButton.getEnPassant().equals("right")) || (pButtonCol - 1 == cButtonCol && previousButton.getEnPassant().equals("left")))
			   && buttonList[pButtonRow][cButtonCol].getText() == "P")
			{
				 buttonList[pButtonRow][cButtonCol].setText("");   // since piece is not taken, its text + color are not changed in other method
				 buttonList[pButtonRow][cButtonCol].setColor(Color.MAGENTA);
				 if(pButtonCol + 1 == cButtonCol) {previousButton.setEnPassantDirection("right");}
				 else if (pButtonCol - 1 == cButtonCol) {previousButton.setEnPassantDirection("left");} // enPassant done
				 previousButton.setEnPassantColor(Color.WHITE); // white is color that is taken
				 return true;
			}// end enPassant

			if((currentButton.getText() != "") &&
				((pButtonCol + 1) == cButtonCol || (pButtonCol - 1) == cButtonCol) &&
				((pButtonRow + 1) == cButtonRow)) // if there is another piece diagonal
				{return true;}
			if(currentButton.getText() == "") // if not diagonal cannot take piece
			{
				if(pButtonCol != cButtonCol) // must be in same column if not diagonal
					{return false;}
				if((pButtonRow + 1) == cButtonRow) // other button can only be one row below
					{return true;}
			}
		}

		//white
		else if(pButtonRow == 6 && previousButton.getColor() == Color.WHITE) // if the pawn is in the sixth row and white
		{
			if((currentButton.getText() != "") &&
				((pButtonCol + 1) == cButtonCol || (pButtonCol - 1) == cButtonCol) &&
				((pButtonRow - 1) == cButtonRow)) // if there is another piece diagonal
				{return true;}
			if(currentButton.getText() == "") // if not diagonal then cannot take piece
			{
				if(pButtonCol != cButtonCol) // must be in same column
					{return false;}
				if((pButtonRow - 1) == cButtonRow || (pButtonRow - 2) == cButtonRow) // if the other button is one/two row(s) up
				{	
                    if(pButtonRow - 2 == cButtonRow)
					{
                        if(buttonList[pButtonRow - 1][pButtonCol].getText() != "") // if double move and piece in between 
                    		{return false;}   
                        if(cButtonCol + 1 <= 7)
							{buttonList[cButtonRow][cButtonCol+1].setEnPassant("left");}
						if(cButtonCol - 1 >= 0)
							{buttonList[cButtonRow][cButtonCol-1].setEnPassant("right");}		
					} // pawn has double moved
					return true;
				}
			}
		}
		
		else if(previousButton.getColor() == Color.WHITE)// if white
		{
			// enPassant
			if(currentButton.getText() == "" && cButtonRow == 2 
			   && ((pButtonCol + 1 == cButtonCol && previousButton.getEnPassant().equals("right")) || (pButtonCol - 1 == cButtonCol && previousButton.getEnPassant().equals("left")))
			   && buttonList[pButtonRow][cButtonCol].getText() == "P")
			{
				 buttonList[pButtonRow][cButtonCol].setText("");   // since piece is not taken, its text + color are not changed in other method
				 buttonList[pButtonRow][cButtonCol].setColor(Color.MAGENTA);
				 if(pButtonCol + 1 == cButtonCol) {previousButton.setEnPassantDirection("right");}
				 else if (pButtonCol - 1 == cButtonCol) {previousButton.setEnPassantDirection("left");} // enPassant done
				 previousButton.setEnPassantColor(Color.BLACK); // black is color that is taken
				 return true;
			} // end enPassant

			if((currentButton.getText() != "") && 
				((pButtonCol + 1) == cButtonCol || (pButtonCol - 1) == cButtonCol) &&
				((pButtonRow - 1) == cButtonRow)) // if there is another piece diagonal
				{return true;}
			
			if(currentButton.getText() == "") // if not diagonal cannot take piece
			{
				if(pButtonCol != cButtonCol) // must be in same column
					{return false;}
				if((pButtonRow - 1) == cButtonRow) // other button can only be one row above
					{return true;}
			}
			
		}
		return false; // false if true has yet to be returned
	}
	// see if a rook can move
	private boolean rookCanMove(int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		// no diagonal/etc movement
		if(pButtonRow != cButtonRow && pButtonCol != cButtonCol)
			{return false;}
						
		// below col
        if(pButtonCol > cButtonCol && pButtonRow == cButtonRow) // same row, diff col
        {
            for(int i = pButtonCol-1; i > cButtonCol; i--) // if button in cButtonCol has text, can take it
            {
                if(buttonList[pButtonRow][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                {return false;}
            }
        }
            
        // above col
        else if(pButtonCol < cButtonCol && pButtonRow == cButtonRow) // same row, diff col
        {
            for(int i = pButtonCol+1; i < cButtonCol; i++) // if button in cButtonCol has text, can take it
            {
                if(buttonList[pButtonRow][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                    {return false;}
            }
        }
            
        // below row
        else if(pButtonRow > cButtonRow && pButtonCol == cButtonCol) // same col, diff row
        {
            for(int i = pButtonRow-1; i > cButtonRow; i--) // if button in cButtonRow has text, can take it
            {
                if(buttonList[i][pButtonCol].getText() != "") // if there is a piece between the two, it cannot just go through it
                        {return false;}
            }
        }
            
        // above row
        else if(pButtonRow < cButtonRow && pButtonCol == cButtonCol) // same col, diff row
        {
            for(int i = pButtonRow+1; i < cButtonRow; i++) // if button in cButtonRow has text, can take it
            {
                if(buttonList[i][pButtonCol].getText() != "") // if there is a piece between the two, it cannot just go through it
                        {return false;}
            }
        }
    
        // if none are false, then it can move
        return true;
	}
	// see if a knight can move
	private boolean knightCanMove(int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		// if vertically aligned L
        if(((pButtonCol + 1 == cButtonCol) || (pButtonCol - 1 == cButtonCol))
            && ((pButtonRow + 2 == cButtonRow) || (pButtonRow - 2 == cButtonRow)))
            {return true;}
                
        // horizontally aligned L
        else if(((pButtonRow + 1 == cButtonRow) || (pButtonRow - 1 == cButtonRow))
                && ((pButtonCol + 2 == cButtonCol) || (pButtonCol - 2 == cButtonCol)))
                {return true;}
      
        // if none possible, then move is false
        return false;
	}
	// see if a bishop can move
	private boolean bishopCanMove(int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		//cannot be in same row or column
		if(pButtonRow == cButtonRow || pButtonCol == cButtonCol) return false;
		if(Math.abs(pButtonCol - cButtonCol) != Math.abs(pButtonRow - cButtonRow)) {return false;} // if not diagonal

        // left-up diagonal
        if(pButtonCol > cButtonCol && pButtonRow > cButtonRow) 
        {   
            int j = pButtonRow - 1;
            for(int i = pButtonCol-1; i > cButtonCol; i--) 
            {
                if(buttonList[j][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                    {return false;}
                if(j > cButtonRow)
                    {j -= 1;}
            }
        }
            
        // left-down diagonal
        else if(pButtonCol > cButtonCol && pButtonRow < cButtonRow)
        {
            int j = pButtonRow + 1;
            for(int i = pButtonCol-1; i > cButtonCol; i--)
            {
                if(buttonList[j][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                    {return false;}
                if(j < cButtonRow)
                    {j += 1;}
            }
        }
            
        // right-up diagonal
        else if(pButtonCol < cButtonCol && pButtonRow > cButtonRow) 
        {
            int j = pButtonRow - 1;
            for(int i = pButtonCol+1; i < cButtonCol; i++) 
            {
                if(buttonList[j][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                    {return false;}
                if(j > cButtonRow)
                    {j -= 1;}
            }
        }
            
        // right-down diagonal
        else if(pButtonCol < cButtonCol && pButtonRow < cButtonRow) 
        {   
            int j = pButtonRow + 1;
            for(int i = pButtonCol+1; i < cButtonCol; i++)
            {
                if(buttonList[j][i].getText() != "") // if there is a piece between the two, it cannot just go through it
                    {return false;}
                if(j < cButtonRow)
                    {j += 1;}
            }

        }
        // if no falses, then it can move
        return true;
	}
	// see if a queen can move
	private boolean queenCanMove(int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		// queen can move as rook or bishop
		return rookCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList) || bishopCanMove(pButtonRow, pButtonCol, cButtonRow, cButtonCol, buttonList); // if can't move as either, then false
	}
	// see if a king can move
	private boolean kingCanMove(ChessPiece previousButton, ChessPiece currentButton, int pButtonRow, int pButtonCol, int cButtonRow, int cButtonCol, ChessPiece[][] buttonList)
	{
		if(((pButtonRow + 1 == cButtonRow || pButtonRow - 1 == cButtonRow) && // one row up/down 
		        (pButtonCol == cButtonCol || 
		         pButtonCol + 1 == cButtonCol || pButtonCol - 1 == cButtonCol)) || // same col or one left/right
		        ((pButtonCol + 1 == cButtonCol || pButtonCol - 1 == cButtonCol) && // one col left/right
		         (pButtonRow == cButtonRow ||
		         pButtonRow + 1 == cButtonRow || pButtonRow - 1 == cButtonRow)) || // same row or one up/down
		         (!previousButton.getInCheck() && previousButton.getCanCastle() && pButtonRow == cButtonRow  && // check if king can castle
		         ((pButtonCol + 2 == cButtonCol && buttonList[pButtonRow][pButtonCol+3].getCanCastle() && 
		         buttonList[pButtonRow][pButtonCol+1].getText() == "" && buttonList[pButtonRow][pButtonCol+2].getText() == "") || 
		        (pButtonCol - 2 == cButtonCol  && buttonList[pButtonRow][pButtonCol-4].getCanCastle() && 
		         buttonList[pButtonRow][pButtonCol-1].getText() == "" && buttonList[pButtonRow][pButtonCol-2].getText() == "" && buttonList[pButtonRow][pButtonCol-3].getText() == ""))))
		    {
				String oldText = currentButton.getText(); // hold text of currentButton
				int oldColor = currentButton.getColor(); // hold color of currentButton
				String newText = previousButton.getText(); // hold text of previousButton
				int newColor = previousButton.getColor(); // hold color of previousButton
				
				currentButton.setText(previousButton.getText());
				currentButton.setColor(previousButton.getColor());  //change the text/color to see if the king can theoretically move there
				previousButton.setText("");
				previousButton.setColor(Color.MAGENTA); // temporarily remove the text/color of this button
				
				for(int i = 0; i < buttonList.length; i++) 
				{ 
					for(int j = 0; j < buttonList[i].length; j++) 
					{ 
						// if ChessPiece is the opposite color of the king 
						if(buttonList[i][j].getText() != "" && buttonList[i][j].getColor() != previousButton.getColor())
						{
							int r = currentButton.getRow();
							int c = currentButton.getCol();
							if((buttonList[i][j].getText() == "K" && (r == i && (c == j+1 || c == j-1) || (c == j && (r == i+1 || r == i-1)) || ((r == i+1 || r == i-1) && (c == j+1 || c == j-1)))) // two kings cannot be adjacent to each other
								|| canMove(currentButton, buttonList[i][j], buttonList)) // if canMove to the position where king wants to move, king cannot move there (king cannot move into check)
								
							{
								currentButton.setText(oldText); // set it back to its previous text and color and return false since a piece can move there
								currentButton.setColor(oldColor);
								previousButton.setText(newText);
								previousButton.setColor(newColor);
								return false;
							} // end if 2
							//iterate through otherwise
						} // end if 1
					} // end for 2
				} // end for 1
				currentButton.setText(oldText); // set it back to its previous text and color and king should be able to move there
				currentButton.setColor(oldColor);
				previousButton.setText(newText);
				previousButton.setColor(newColor);
				
				// if right castle
				if(!previousButton.getInCheck() && previousButton.getCanCastle() && pButtonRow == cButtonRow  && pButtonCol + 2 == cButtonCol &&
				   buttonList[pButtonRow][pButtonCol+3].getCanCastle() && buttonList[pButtonRow][pButtonCol+1].getText() == "" && buttonList[pButtonRow][pButtonCol+2].getText() == "")
				{
					buttonList[pButtonRow][pButtonCol+1].setText(buttonList[pButtonRow][pButtonCol+3].getText());
					buttonList[pButtonRow][pButtonCol+1].setColor(buttonList[pButtonRow][pButtonCol+3].getColor());
					buttonList[pButtonRow][pButtonCol+3].setText("");
					buttonList[pButtonRow][pButtonCol+3].setColor(Color.MAGENTA);
				}
				// if left castle
				else if(!previousButton.getInCheck() && previousButton.getCanCastle() && pButtonRow == cButtonRow && pButtonCol - 2 == cButtonCol  &&
						buttonList[pButtonRow][pButtonCol-4].getCanCastle() && buttonList[pButtonRow][pButtonCol-1].getText() == "" &&
						buttonList[pButtonRow][pButtonCol-2].getText() == "" && buttonList[pButtonRow][pButtonCol-3].getText() == "")
				{
					buttonList[pButtonRow][pButtonCol-1].setText(buttonList[pButtonRow][pButtonCol-4].getText());
					buttonList[pButtonRow][pButtonCol-1].setColor(buttonList[pButtonRow][pButtonCol-4].getColor());
					buttonList[pButtonRow][pButtonCol-4].setText("");
					buttonList[pButtonRow][pButtonCol-4].setColor(Color.MAGENTA);
				}
				return true;
            } // end main if
			else return false; // otherwise false
	}
	
	public ChessPiece[] getCanMoveArray(ChessPiece currentButton, ChessPiece[][] buttonList)
	{
		// row and col of currentButton
		int row = currentButton.getRow(), col = currentButton.getCol();
		
		// array
		ChessPiece[] array;
		switch((char) currentButton.getText().charAt(0)) // switch to determine max amount of elements
		{
			case 'P': array = new ChessPiece[4]; break; // pawn can move to one of 4 different spaces max
			case 'R': array = new ChessPiece[14]; break; // rook can move to one of 14 different spaces max
			case 'B': array = new ChessPiece[13]; break; // bishop can move to one of 13 different spaces max
			case 'N': array = new ChessPiece[8]; break; // knight can move to one of 8 different spaces max
			case 'Q': array = new ChessPiece[27]; break; // queen can move to one of 27 different spaces max
			case 'K': array = new ChessPiece[10]; break; // king can move to one of 10 different spaces max
			default: array = new ChessPiece[1]; break; // default 1..should not be reached though
		}
		int counter = 0; // holds number of places
		
		// switch based off text of button
		switch((char) currentButton.getText().charAt(0))
		{
			//pawn
			case 'P':
				
				// black
				if(currentButton.getColor() == Color.BLACK)
				{
					if(row + 1 < 8 && canMove(buttonList[row+1][col], currentButton, buttonList)) {array[counter] = buttonList[row+1][col]; counter++;} // down 1
					if(row + 2 < 8 && canMove(buttonList[row+2][col], currentButton, buttonList)) {array[counter] = buttonList[row+2][col]; counter++;} // down 2						
					if(row + 1 < 8 && col + 1 < 8 && canMove(buttonList[row+1][col+1], currentButton, buttonList)) {array[counter] = buttonList[row+1][col+1]; counter++;} // right diag
					if(row + 1 < 8 && col - 1 > -1 && canMove(buttonList[row+1][col-1], currentButton, buttonList)) {array[counter] = buttonList[row+1][col-1]; counter++;} // left diag
				}
				// white
				else if(currentButton.getColor() == Color.WHITE)
				{
					if(row - 1 > -1 && canMove(buttonList[row-1][col], currentButton, buttonList)) {array[counter] = buttonList[row-1][col]; counter++;} // up 1
					if(row - 2 > -1 && canMove(buttonList[row-2][col], currentButton, buttonList)) {array[counter] = buttonList[row-2][col]; counter++;} // up 2
					if(row - 1 > -1 && col + 1 < 8 && canMove(buttonList[row-1][col+1], currentButton, buttonList)) {array[counter] = buttonList[row-1][col+1]; counter++;} // right diag
					if(row - 1 > -1 && col - 1 > -1 && canMove(buttonList[row-1][col-1], currentButton, buttonList)) {array[counter] = buttonList[row-1][col-1]; counter++;} // left diag
				}
				return array;
			// rook	
			case 'R':
			
				for(int i = col + 1; i < 8; i++) {if(canMove(buttonList[row][i], currentButton, buttonList)) {array[counter] = buttonList[row][i]; counter++;}} // right
				for(int i = col - 1; i > -1; i--) {if(canMove(buttonList[row][i], currentButton, buttonList)) {array[counter] = buttonList[row][i]; counter++;}} // left
				for(int i = row + 1; i < 8; i++) {if(canMove(buttonList[i][col], currentButton, buttonList)) {array[counter] = buttonList[i][col]; counter++;}} // down
				for(int i = row - 1; i > -1; i--) {if(canMove(buttonList[i][col], currentButton, buttonList)) {array[counter] = buttonList[i][col]; counter++;}} // up
				
				return array;
			// bishop
			case 'B':
				
				for(int i = row + 1; i < 8; i++) {if(col + Math.abs(row - i) < 8 && canMove(buttonList[i][col + Math.abs(row - i)], currentButton, buttonList)) {array[counter] = buttonList[i][col + Math.abs(row - i)]; counter++;}} // right-down
				for(int i = row + 1; i < 8; i++) {if(col - Math.abs(row - i) > -1 && canMove(buttonList[i][col - Math.abs(row - i)], currentButton, buttonList)) {array[counter] = buttonList[i][col - Math.abs(row - i)]; counter++;}} // left-down
				for(int i = row - 1; i > -1; i--) {if(col + Math.abs(row - i) < 8 && canMove(buttonList[i][col + Math.abs(row - i)], currentButton, buttonList)) {array[counter] = buttonList[i][col + Math.abs(row - i)]; counter++;}} // right-up
				for(int i = row - 1; i > -1; i--) {if(col - Math.abs(row - i) > -1 && canMove(buttonList[i][col - Math.abs(row - i)], currentButton, buttonList)) {array[counter] = buttonList[i][col - Math.abs(row - i)]; counter++;}} // left-up
				
				return array;
			// knight
			case 'N':
				
				// vertically aligned 
				if(row - 2 > -1 && col + 1 < 8 && canMove(buttonList[row-2][col+1], currentButton, buttonList)) {array[counter] = buttonList[row-2][col+1]; counter++;} // up-right
				if(row - 2 > -1 && col - 1 > -1 && canMove(buttonList[row-2][col-1], currentButton, buttonList)) {array[counter] = buttonList[row-2][col-1]; counter++;} // up-left
				if(row + 2 < 8 && col + 1 < 8 && canMove(buttonList[row+2][col+1], currentButton, buttonList)) {array[counter] = buttonList[row+2][col+1]; counter++;} // down-right
				if(row + 2 < 8 && col - 1 > -1 && canMove(buttonList[row+2][col-1], currentButton, buttonList)) {array[counter] = buttonList[row+2][col-1]; counter++;} // down-left
				// horizontally aligned
				if(col - 2 > -1 && row + 1 < 8 && canMove(buttonList[row+1][col-2], currentButton, buttonList)) {array[counter] = buttonList[row+1][col-2]; counter++;} // left-down
				if(col - 2 > -1 && row - 1 > -1 && canMove(buttonList[row-1][col-2], currentButton, buttonList)) {array[counter] = buttonList[row-1][col-2]; counter++;} // left-up
				if(col + 2 < 8 && row + 1 < 8 && canMove(buttonList[row+1][col+2], currentButton, buttonList)) {array[counter] = buttonList[row+1][col+2]; counter++;} // right-down
				if(col + 2 < 8 && row - 1 > -1 && canMove(buttonList[row-1][col+2], currentButton, buttonList)) {array[counter] = buttonList[row-1][col+2]; counter++;} // right-up
				
				return array;
			// queen
			case 'Q':
				// as rook
				currentButton.setText("R");
				ChessPiece[] rookArray = getCanMoveArray(currentButton, buttonList);
				for(int i = 0; i < rookArray.length; i++) 
				{
					try {rookArray[i].getText();} catch(NullPointerException e) {i = rookArray.length; continue;} // if null pointer, then skip to end of array
					array[counter] = rookArray[i]; 
					counter++;
				}
				// as bishop
				currentButton.setText("B");
				ChessPiece[] bishopArray = getCanMoveArray(currentButton, buttonList);
				for(int i = 0; i < bishopArray.length; i++) 
				{
					try {bishopArray[i].getText();} catch(NullPointerException e) {i = bishopArray.length; continue;} // if null pointer then skip to end of array
					array[counter] = bishopArray[i]; 
					counter++;
				}
				
				currentButton.setText("Q"); // back to queen
				return array;
			// king
			case 'K':
				
				if(row - 1 > -1 && canMove(buttonList[row-1][col], currentButton, buttonList)) {array[counter] = buttonList[row-1][col]; counter++;} // up
				if(row + 1 < 8 && canMove(buttonList[row+1][col], currentButton, buttonList)) {array[counter] = buttonList[row+1][col]; counter++;} // down
				if(col + 1 < 8 && canMove(buttonList[row][col+1], currentButton, buttonList)) {array[counter] = buttonList[row][col+1]; counter++;} // right
				if(col - 1 > -1 && canMove(buttonList[row][col-1], currentButton, buttonList)) {array[counter] = buttonList[row][col-1]; counter++;} // left
				if(row - 1 > -1 && col + 1 < 8 && canMove(buttonList[row-1][col+1], currentButton, buttonList)) {array[counter] = buttonList[row-1][col+1]; counter++;} // up-right
				if(row - 1 > -1 && col - 1 > -1 && canMove(buttonList[row-1][col-1], currentButton, buttonList)) {array[counter] = buttonList[row-1][col-1]; counter++;} // up-left
				if(row + 1 < 8 && col + 1 < 8 && canMove(buttonList[row+1][col+1], currentButton, buttonList)) {array[counter] = buttonList[row+1][col+1]; counter++;} // down-right
				if(row + 1 < 8 && col - 1 < 8 && canMove(buttonList[row+1][col-1], currentButton, buttonList)) {array[counter] = buttonList[row+1][col-1]; counter++;} // down-left
				if(col + 2 < 8 && canMove(buttonList[row][col+2], currentButton, buttonList)) {array[counter] = buttonList[row][col+2]; counter++;} // right castle
				if(col - 2 < 8 && canMove(buttonList[row][col-2], currentButton, buttonList)) {array[counter] = buttonList[row][col-2]; counter++;} // left castle
				
				return array;
				
			default:
				return array; // return the array if it hasn't already (which it should have)
				
		} // end switch
	} // end getArray
	
} // Anton Gilgur