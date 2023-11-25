import java.util.Scanner;
import java.util.Random;
import java.util.Arrays;

public class Match3{
		final static Scanner sc = new Scanner(System.in);
		final static Random rand = new Random();
		final static char[] colors = new char[]{'R', 'G', 'B'};
		final static String[] colorNames = new String[]{"red", "green", "blue"};
		final static int colorCount = colors.length;
		final static int scoreMultiplier = 5; //effects the game difficulty
		
		static int[][] board;
		static int[][] boardBuffer;
		static int[] scores = new int[colorCount];
		static int boardVertical;
		static int boardHorizontal;
		static int[] blankColumns = new int[2]; //min, max
		static int moveCount = 0;
		static int gameMode = -1;
			//Mode 0: collect 200 points in 15 moves
			//Mode 1: collect 100 points for each color in 20 moves
		static int[] boardSize = new int[2];
	
	public static void main(String[] args){
		String input;
		int[] inputCell = new int[2];
		int[] lastPos = new int[2];
		boolean gameRunning = true;
		int entityBuffer;
		boolean repeat = false;
		int gameStatBuffer;
		
		//starting messages
		System.out.println("Welcome to the game!");
		System.out.println("Enter 's' to start the game or 'q' to quit:");
		
		//starting inputs
		while(true){
			input = sc.next();
			
			if(input.equals("s")){ //start the game
				break;
			}
			else if(input.equals("q")){ //quit the game
				return;
			}
		}
		
		//inputs
		GatherBoardSize();
		while(boardSize[0] < colorCount || boardSize[1] < colorCount){
			GatherBoardSize();
		}
		System.out.println("Good luck!");
		
		board = new int[boardSize[0]][boardSize[1]];
		boardBuffer = new int[boardSize[0]][boardSize[1]];
		boardVertical = board.length;
		boardHorizontal = board[0].length;
		
		ShuffleBoard();
		ReShuffleIfNoMatch(); //reshuffle the board if there is no possible match
		
		//choose game mode
		gameMode = rand.nextInt(2);
		if(gameMode == 0){
			moveCount = 15; //set the move count
			System.out.println("You have to collect 200 points in 15 moves!");
		}
		else if(gameMode == 1){
			moveCount = 20; //set the move count
			System.out.println("You have to collect 100 points for each color in 20 moves!");
		}
		
		PrintBoard();
		
		//game loop
		while(gameRunning){
			//inputs
			System.out.println("Enter the cell:");
			inputCell[0] = sc.nextInt(); 
			inputCell[1] = sc.nextInt(); 
			
			blankColumns[0] = boardHorizontal;
			blankColumns[1] = 0;
			
			//invalid input
			if(inputCell[0] <= 0 || inputCell[0] > board.length || inputCell[1] <= 0 || inputCell[1] > board[0].length)
				continue;
			
			System.out.println("Enter the direction:");
			input = sc.next();
			
			//base cell
			inputCell[0]--;
			inputCell[1]--;
			lastPos[0] = inputCell[0];
			lastPos[1] = inputCell[1];
			
			//calculate the direction
			switch(input){
				case "up":
					lastPos[0] -= 1;
					break;
				case "down":
					lastPos[0] += 1;
					break;
				case "left":
					lastPos[1] -= 1;
					break;
				case "right":
					lastPos[1] += 1;
					break;
				default: //invalid input
					continue;
			}
			
			//invalid move			
			if(lastPos[0] < 0 || lastPos[0] > board.length - 1 || lastPos[1] < 0 || lastPos[1] > board[0].length - 1){
				System.out.println("Invalid move!");
				PrintBoard();
				continue;	
			}
			
			//swap the cells
			entityBuffer = board[inputCell[0]][inputCell[1]];
			board[inputCell[0]][inputCell[1]] = board[lastPos[0]][lastPos[1]];
			board[lastPos[0]][lastPos[1]] = entityBuffer;
			
			repeat = CheckMatchReplace();
			
			//check, if there is no match reswap to the old position
			if(!repeat){
				entityBuffer = board[inputCell[0]][inputCell[1]];
				board[inputCell[0]][inputCell[1]] = board[lastPos[0]][lastPos[1]];
				board[lastPos[0]][lastPos[1]] = entityBuffer;
			}
			//print the board and the score if the move is valid, else ask for inputs again
			else{
				while(CheckMatchReplace()){} //if the repeat variable equals to true, repeat the method till it returns false because there may be another matches after we moved cells down
				repeat = false;
				moveCount--;
				
				//check if game ends
				gameStatBuffer = CheckIfGameEnded();
				if(gameStatBuffer == 1){
					PrintScore();
					System.out.println("Unfortunately, you lost the game.");
					gameRunning = false;
					break;
				}
				else if(gameStatBuffer == 2){
					PrintScore();
					System.out.println("Congratulations, you won the game!");
					gameRunning = false;
					break;
				}
				
				//PrintBoard(); //FOR DEBUGGING PURPOSES!!!!
				PrintScore();
				GenerateNewCells(); 	//generates new cells to the just blanked cells
				ReShuffleIfNoMatch();	//reshuffle the board if there is no possible match
				PrintBoard();
			}
		}
		
		sc.close();
	}
	
	private static int CheckIfGameEnded(){ //0 not ended, 1 lost, 2 won
		if(gameMode == 0){
			if(moveCount <= 0) //lost
				return 1;
				
			if(CalculateTotalScore() >= 200)
				return 2;
		}
		else if(gameMode == 1){
			if(moveCount <= 0) //lost
				return 1;
			
			int count = 0;
			for(int i : scores)
				if(i >= 100)
					count++;
				
			if(count == scores.length)
				return 2;
		}
		
		return 0;
	}
	
	private static void ReShuffleIfNoMatch(){ //if no moves are possible, reshuffle the board
		while(!IsMovePossible()){
			//PrintBoard(); //FOR DEBUGGING PURPOSES!!!!
			ShuffleBoard();
		}
	}
	
	private static boolean IsMovePossible(){ //checks if there is any possible match left in the board
		//horizontal check
		for(int i = 0; i<boardVertical; i++){
			for(int j = 0; j<boardHorizontal-1; j++){
				if(board[i][j] == board[i][j+1]){ //-- patterns
					if(j-2>=0 && board[i][j] == board[i][j-2] || boardHorizontal>j+3 && board[i][j] == board[i][j+3] ||
					   j-1>=0 && i-1>=0 && board[i][j] == board[i-1][j-1] || boardHorizontal>j+2 && i-1>=0 && board[i][j] == board[i-1][j+2] ||
					   j-1>=0 && boardVertical>i+1 && board[i][j] == board[i+1][j-1] || boardHorizontal>j+2 && boardVertical>i+1 && board[i][j] == board[i+1][j+2])
						return true;
				}
				
				if(j+2 < boardHorizontal && board[i][j] == board[i][j+2]){ //-.- patterns
					if(i-1>=0 && board[i][j] == board[i-1][j+1] || boardVertical>i+1 && board[i][j] == board[i+1][j+1])
						return true;
				}
			}
		}
		
		//vertical check
		for(int i = 0; i<boardHorizontal; i++){
			for(int j = 0; j<boardVertical-1; j++){
				if(board[j][i] == board[j+1][i]){ //downward -- patterns
					if(j-2>=0 && board[j][i] == board[j-2][i] || boardVertical>j+3 && board[j][i] == board[j+3][i] ||
					   j-1>=0 && i-1>=0 && board[j][i] == board[j-1][i-1] || boardVertical>j+2 && i-1>=0 && board[j][i] == board[j+2][i-1] ||
					   j-1>=0 && boardHorizontal>i+1 && board[j][i] == board[j-1][i+1] || boardVertical>j+2 && boardHorizontal>i+1 && board[j][i] == board[j+2][i+1])
						return true;
				}
				
				if(j+2 < boardVertical && board[j][i] == board[j+2][i]){ //downward -.- patterns
					if(i-1>=0 && board[j][i] == board[j+1][i-1] || boardHorizontal>i+1 && board[j][i] == board[j+1][i+1])
						return true;
				}
			}
		}
		
		return false;
	}
	
	private static void GatherBoardSize(){ //recieves the boardSize from user
		System.out.println("Enter the size of the matrix:");
		boardSize[0] = sc.nextInt();
		boardSize[1] = sc.nextInt();	
	}

	private static void GenerateNewCells(){ //generates new cells to the blank ones
		int[] colorsBuffer = new int[colorCount];
		int colorLengthBuffer;
		//System.out.println(blankColumns[0] + " -AAA- " + blankColumns[1]); //FOR DEBUGGING PURPOSES!!!!
			
		for(int i = boardVertical-1; i>=0; i--){
			for(int j = blankColumns[0]; j<=blankColumns[1]; j++){ //loop through the only empty columns to save time
				if(board[i][j] == -1){ //if the cell is an empty one
					colorLengthBuffer = 0;
					
					if(boardVertical > i + 2 && board[i+2][j] == board[i+1][j])//if up 2 cells are same color exclude that one from appearing in randomizer
						colorsBuffer[colorLengthBuffer++] = board[i+1][j]; //append the excluded color to an array

					//if left 2 cells and up 2 cells are paired with same colors or left 1 and right 1 cell are same colors but left and up colors are not same
					if((j - 2 >= 0 && board[i][j-2] == board[i][j-1] || (j-1>=0 && boardHorizontal>j+1) && board[i][j-1] == board[i][j+1]) && 
					  !(colorLengthBuffer >= 1 && colorsBuffer[colorLengthBuffer - 1] == board[i][j-1])){
						colorsBuffer[colorLengthBuffer++] = board[i][j-1];
					}
					//right 2 cells
					
					if(colorLengthBuffer > 0) //if there is an excluded color call the picker method
						board[i][j] = ColorPickerExcept(Arrays.copyOfRange(colorsBuffer, 0, colorLengthBuffer));
					else //if there is not any excluded color pick a random color normally
						board[i][j] = rand.nextInt(colorCount);
						
				}
			}
		}
	}
	
	private static int CalculateTotalScore(){ //returns the total score in the scores array
		int totalScore = 0;
		for(int i : scores) //calculate the total score
			totalScore += i;
			
		return totalScore;
	}
	
	private static void PrintScore(){ //Print the score message
			switch(gameMode){
				case 0: 	//Mode 0: collect 200 points in 15 moves
					System.out.println("You have collected " + CalculateTotalScore() + " points and you have " + moveCount + " moves left!");
					break;
					
				case 1:		//Mode 1: collect 100 points for each color in 20 moves
					String output = "You have collected ";
					for(int i = 0; i<colorCount; i++){ //calculate the total score
						output += Colors.GetColor(i) + scores[i] + Colors.RESET + " points for " + colorNames[i];
						if(i + 1 != colorCount)
							output += ", ";
					}
					output += " and you have " + moveCount + " moves left!";
				
					System.out.println(output);
					break;
			}
	}

	private static void MoveCellsDown(int minColumn, int maxColumn){ //move's the cells to the bottom 
		int lastIndex = -1;
		
		for(int i = minColumn; i<=maxColumn; i++){ //loop through the whole board
			for(int j = boardVertical-1; j>=0; j--){
				if(lastIndex == -1){
					if(board[j][i] == -1){
						lastIndex = j;
					}
				}
				else{
					if(board[j][i] != -1){
						board[lastIndex--][i] = board[j][i];
						board[j][i] = -1;	
					}
				}
			}
			
			lastIndex = -1;
		}
	}
	
	private static boolean CheckMatchReplace(){ //checks the board for match and generates new colors if there is any, returns 1 if there is any match else -1
		int counter;
		boolean match = false;
		int minColumn = boardHorizontal; //maxla
		int maxColumn = 0;
		
		//horizontal check
		for(int i = 0; i<boardVertical; i++){ //loop through the whole board
			counter = 1;
		
			for(int j = 1; j<boardHorizontal; j++){
				if(board[i][j-1] == board[i][j] && board[i][j] != -1)
					counter++;
				else	
					counter = 1;
				
				if(counter == 3){
					match = true;
					scores[board[i][j]] += 3 * scoreMultiplier; //horizontal scores
					
					if(maxColumn < j)
						maxColumn = j;
					if(j-2 < minColumn)
						minColumn = j-2;
					
					boardBuffer[i][j-2] = 1;
					boardBuffer[i][j-1] = 1;
					boardBuffer[i][j] = 1;
				}
				else if(counter > 3){
					scores[board[i][j]] += scoreMultiplier; //horizontal scores
					
					if(maxColumn < j)
						maxColumn = j;
					
					boardBuffer[i][j] = 1;
				}
			}
		}
		
		//vertical check
		for(int i = 0; i<boardHorizontal; i++){ //loop through the whole board
			counter = 1;
		
			for(int j = 1; j<boardVertical; j++){
				if(board[j-1][i] == board[j][i] && board[j][i] != -1)
					counter++;
				else	
					counter = 1;
				
				if(counter == 3){
					match = true;
					scores[board[j][i]] += 3 * scoreMultiplier; //vertical scores
					
					if(maxColumn < i)
						maxColumn = i;
					if(i < minColumn)
						minColumn = i;
				
					boardBuffer[j-2][i] = 1;
					boardBuffer[j-1][i] = 1;
					boardBuffer[j][i] = 1;
				}
				else if(counter > 3){
					scores[board[j][i]] += scoreMultiplier; //vertical scores
					boardBuffer[j][i] = 1;
				}
			}
		}
		
		//clear
		if(match){
			//update the blankColumns array
			if(minColumn < blankColumns[0])
				blankColumns[0] = minColumn;
			if(blankColumns[1] < maxColumn)
				blankColumns[1] = maxColumn;
			
			ClearCells();
			MoveCellsDown(minColumn, maxColumn);
			return true;
		}
		
		return false;
	}
	
	private static void ClearCells(){ //clears cells with checking
		for(int i = 0; i<boardVertical; i++){
			for(int j = 0; j<boardHorizontal; j++){
				if(boardBuffer[i][j] != 0){
					boardBuffer[i][j] = 0;
					board[i][j] = -1; 		  //delete the char
				}
			}
		}
	}
	
	private static void PrintBoard(){ //Print the board with colors
		System.out.println();
		
		for(int i = 0; i<boardVertical; i++){ //loop through the whole board
			for(int j = 0; j<boardHorizontal; j++){
				System.out.print("|" + Colors.GetColor(board[i][j]) + colors[board[i][j]] + Colors.RESET); //print the board with colors
			}
			
			System.out.println("|");
		}
	}
	
	private static void ShuffleBoard(){ //Shuffle the board
		int[] colorsBuffer = new int[colorCount];
		int colorLengthBuffer;
	
		for(int i = 0; i<boardVertical; i++){ //loop through the whole board
			for(int j = 0; j<boardHorizontal; j++){
				colorLengthBuffer = 0;
				
				if(i - 2 >= 0 && board[i-2][j] == board[i-1][j])//if up 2 cells are same color exclude that one from appearing in randomizer
					colorsBuffer[colorLengthBuffer++] = board[i-1][j]; //append the excluded color to an array

				//if left 2 cells and up 2 cells are paired with same colors but left and up colors are not same
				if(j - 2 >= 0 && board[i][j-2] == board[i][j-1] && !(colorLengthBuffer >= 1 && colorsBuffer[colorLengthBuffer - 1] == board[i][j-1])){
					colorsBuffer[colorLengthBuffer++] = board[i][j-1];
				}
				
				if(colorLengthBuffer > 0) //if there is an excluded color call the picker method
					board[i][j] = ColorPickerExcept(Arrays.copyOfRange(colorsBuffer, 0, colorLengthBuffer));
				else //if there is not any excluded color pick a random color normally
					board[i][j] = rand.nextInt(colorCount);
			}
		}
	}
	
	private static int ColorPickerExcept(int[] vals){ //choose random colors except specified ones
		int[] colorsBuffer = new int[colorCount - vals.length];
		int num = 0;
		boolean exceptionBuffer;
		
		for(int i = 0; i<colorCount; i++){ //loop through excluded colors and find the available ones
			exceptionBuffer = false;
			
			for(int j = 0; j<vals.length; j++){
				if(i == vals[j]){ //if a color matches with an excluded one break the loop and continue on upper loop
					exceptionBuffer = true;
					break;
				}
			}
			
			if(!exceptionBuffer) //if there is no match add the available color to an array
				colorsBuffer[num++] = i;
		}
		return colorsBuffer[rand.nextInt(colorsBuffer.length)];
	}
}

class Colors{ //provides colors
	private Colors(){}
	
	static String RED = "\u001B[31m";
	static String GREEN = "\u001B[32m";
	static String BLUE = "\u001B[36m"; //cyan
	static String RESET = "\u001B[0m";
	
	static String GetColor(int val){ //return color according to it's char value
		switch(val){
			case 0: //R
				return RED;
			case 1: //G
				return GREEN;
			case 2: //B
				return BLUE;
			default:
				return RESET;
		}
	}
}