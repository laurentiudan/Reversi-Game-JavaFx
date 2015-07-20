/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversi;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

// class defnition for reversi game
public class Reversi extends Application {
	// overridden init method
	public void init() {
            sp_mainlayout   = new StackPane();
            rc_reversi      = new ReversiControl();
            sp_mainlayout.getChildren().add(rc_reversi);
                    
        }
	// overridden start method
	public void start(Stage primaryStage) {
            primaryStage.setTitle("Game Reversi");
            primaryStage.setScene(new Scene(sp_mainlayout, 800, 800));
            primaryStage.show();
	}
	
	// overridden stop method
	public void stop() {
		
	}
	
	// entry point into our program for launching our javafx applicaton
	public static void main(String[] args) {
            launch(args);
	}
	
	// private fields for a stack pane and a reversi control
	private StackPane sp_mainlayout;
	private ReversiControl rc_reversi;
	
}

// class definition for a custom reversi control
class ReversiControl extends Control {
	// constructor for the class
	public ReversiControl() {
            setSkin(new ReversiControlSkin(this));
            rb_board = new ReversiBoard();
            getChildren().add(rb_board);
            
            setOnMouseClicked(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent mouse){
                    rb_board.placePiece(mouse.getX(), mouse.getY());

                }
            });
            setOnKeyPressed(new EventHandler<KeyEvent>(){
                public void handle(KeyEvent key){
                    if(key.getCode() == KeyCode.SPACE){
                        rb_board.resetGame();
                        System.out.println("Game Reseted!");
                    }    
                }
            });
	}
	
	// overridden version of the resize method
	@Override
	public void resize(double width, double height) {
            super.resize(width, height);
            rb_board.resize(width, height);
	}
	
	// private fields of a reversi board
	ReversiBoard rb_board;
}

// class definition for a skin for the reversi control
// NOTE: to keep JavaFX happy we dont use the skin here
class ReversiControlSkin extends SkinBase<ReversiControl> {
	// default constructor for the class
	public ReversiControlSkin(ReversiControl rc) {
		super(rc);
	}
}

// class definition for the reversi board
class ReversiBoard extends Pane {
	// default constructor for the class
	public ReversiBoard() {
            render          = new ReversiPiece[8][8];
            horizontal      = new Line[8];
            vertical        = new Line[8];
            horizontal_t    = new Translate[8];
            vertical_t      = new Translate[8];
            surrounding     = new int[3][3];
            can_reverse     = new boolean[3][3];
            
            this.initialiseLinesBackground();
            this.initialiseRender();
            this.resetGame();
             
	}
	
	// public method that will try to place a piece in the given x,y coordinate
	public void placePiece(final double x, final double y) {
            int cx = (int)(x / cell_width);
            int cy = (int)(y / cell_height);
            
            determineSurrounding(cx, cy);
            
            if(in_play == true && getPiece(cx, cy) == 0 && adjacentOpposingPiece() == true 
                    && determineReverse(cx, cy)){
                
                if(current_player == 1){
                    placeAndReverse(cx, cy);
                    swapPlayers();
                    System.out.println("Player\'s turn: "+current_player);
                }
                else{
                    placeAndReverse(cx, cy);
                    swapPlayers();
                    System.out.println("Player\'s turn: "+current_player);
                }
            updateScores();
            System.out.println("Score: ");
            System.out.println("Player 1: "+player1_score);
            System.out.println("Player 2: "+player2_score);
            determineEndGame();
            System.out.println("\n");
            
             determineSurrounding(cx, cy);
          
            }
           
            //----------------------------------------------
                          
            //-----------------------------------------------
            
            determineEndGame();
            determineWinner();
            
	}
	
	// overridden version of the resize method to give the board the correct size
	@Override
	public void resize(double width, double height) {
            super.resize(width, height);
            
            cell_width = width / 8.0;
            cell_height = height / 8.0;
            
            background.setWidth(width);
            background.setHeight(height);
            
           this.horizontalResizeRelocate(width);
           this.verticalResizeRelocate(height);
           this.pieceResizeRelocate();
	}
	
	// public method for resetting the game
	public void resetGame() {
            this.resetRenders();
            render[3][3].setPiece(1);
            render[4][4].setPiece(1);
            render[3][4].setPiece(2);
            render[4][3].setPiece(2);
            
            in_play = true;
            current_player = 2;
            opposing  = 1;
            player1_score = 2;
            player2_score = 2;
            this.pieceResizeRelocate();
	}
	
	// private method that will reset the renders
	private void resetRenders() {
            for(int i = 0; i<8; i++){
                for(int j = 0; j<8; j++){
                    render[i][j].setPiece(0);
                }
            }
	}
	
	// private method that will initialise the background and the lines
	private void initialiseLinesBackground() {
            background = new Rectangle();
            background.setFill(Color.CYAN);
            background.setStroke(Color.YELLOW);
            background.setStrokeWidth(5);
            getChildren().add(background);

            for(int i = 0; i<8; i++){
                horizontal[i] = new Line();
                horizontal_t[i] = new Translate();
                horizontal[i].getTransforms().add(horizontal_t[i]);
                horizontal[i].setStartX(0); horizontal[i].setStartY(0);
                horizontal[i].setEndY(0);
                getChildren().add(horizontal[i]);
                
                vertical[i] = new Line();
                vertical_t[i] = new Translate();
                vertical[i].getTransforms().add(vertical_t[i]); 
                vertical[i].setStartX(0); vertical[i].setEndX(0);
                vertical[i].setStartY(0);
                
                getChildren().add(vertical[i]); 
            }
	}
	
	// private method for resizing and relocating the horizontal lines
	private void horizontalResizeRelocate(final double width) {
            for(int i = 0; i<8; i++){
                horizontal[i].setEndX(width);
                horizontal_t[i].setY(i * cell_height);
            }
	}
	
	// private method for resizing and relocating the vertical lines
	private void verticalResizeRelocate(final double height) {
            for(int i = 0; i<8; i++){
                vertical[i].setEndY(height);
                vertical_t[i].setX(i * cell_width);
            }
	}
	
	// private method for swapping the players
	private void swapPlayers() {
            int swapPlayer = current_player;
            current_player = opposing;
            opposing = swapPlayer;
	}
	
	// private method for updating the player scores
	private void updateScores() {
            player1_score = 0;
            player2_score = 0;
            for(int i = 0; i< 8; i++){
                for(int j = 0; j<8; j++){
                    if(render[i][j].getPiece() == 1)
                        player1_score++;
                    else if(render[i][j].getPiece() == 2)
                        player2_score++;
                }
            }

	}
	// private method for resizing and relocating all the pieces
	private void pieceResizeRelocate() {
            for(int i = 0; i<8; i++){
                for(int j = 0; j<8; j++){
                    	render[i][j].resize(cell_width, cell_height);
                    	render[i][j].relocate(i * cell_width,j * cell_height); 	
                }
            }
        }
	// private method for determining which pieces surround x,y will update the
	// surrounding array to reflect this
	private void determineSurrounding(final int x, final int y) {

            surrounding[0][0] = getPiece(x-1, y-1);
            surrounding[0][1] = getPiece(x, y-1);
            surrounding[0][2] = getPiece(x+1, y-1);
            surrounding[1][0] = getPiece(x-1, y);
            surrounding[1][1] = -1;
            surrounding[1][2] = getPiece(x+1, y);
            surrounding[2][0] = getPiece(x-1, y+1);
            surrounding[2][1] = getPiece(x, y+1);
            surrounding[2][2] = getPiece(x+1, y+1);
   
	}	
	// private method for determining i*f a reverse can be made will update the can_reverse
	// array to reflect the answers will return true if a single reverse is found
	private boolean determineReverse(final int x, final int y) {
            // NOTE: this is to keep the compiler happy until you get to this part
      // /*
            boolean check = false;
            for(int i = -1; i < 2; i++){    //i in range [-1,1] for dx
                for(int j = -1; j<2; j++){  //j in range [-1,1] for dy
                   
                    if(current_player == 1 && surrounding[i+1][j+1] == 2){
                        int a = x+j;
                        int b = y+i;
                       
                            check = isReverseChain(a, b, j, i, 1);
                    }
                    else if(current_player == 2 && surrounding[i+1][j+1] == 1){
                        int a = j+x;
                        int b = i+y;

                            check = isReverseChain(a, b, j, i, 2);
                    }
                    else{
                        check = false;
                    }
                    if(check)
                        can_reverse[i+1][j+1]=true;
                    else
                        can_reverse[i+1][j+1]=false;
                }
            }
           check = false;
           for(int i =0; i<3; i++){
               for(int j =0; j<3; j++){
                   if(can_reverse[i][j] == true)
                       check = true;
               } 
           }    
           return check;
	}
	
	// private method for determining if a reverse can be made from a position (x,y) for
	// a player piece in the given direction (dx,dy) returns true if possible
	// assumes that the first piece has already been checked
	private boolean isReverseChain(final int x, final int y, final int dx, final int dy, final int player) {
		// NOTE: this is to keep the compiler happy until you get to this part

            if(player == 1){
               if(getPiece(x+dx, y+dy) == -1 || getPiece(dx+x,dy+y) == 0){
                   return false;
               }
               else if(getPiece(x+dx,y+dy) == 1)
                    return true;
               else if(getPiece(x+dx, y+dy) == 2)
                   isReverseChain(x+dx, y+dy, dx, dy, 1);
              
           }
           else if(player == 2){
               if(getPiece(x+dx,y+dy) == -1 || getPiece(dx+x,dy+y) == 0){
                   return false;
               }
               else if(getPiece(x+dx, y+dy) == 2)
                   return true; 
               else if(getPiece(x+dx, y+dy) == 1)
                   isReverseChain(x+dx, y+dy, dx, dy, 2);         
           }  
         return isReverseChain(dx+x, dy+y, dx, dy, player);   
	}
	
	// private method for determining if any of the surrounding pieces are an opposing
	// piece. if a single one exists then return true otherwise false
	private boolean adjacentOpposingPiece() {
            // NOTE: this is to keep the compiler happy until you get to this part
            boolean check = false;
            for(int i = 0; i < 3; i++){
                for(int j = 0; j<3; j++){
                    if(current_player == 1 && surrounding[i][j] == 2)
                        check = true;
                    else if(current_player == 2 && surrounding[i][j] == 1){
                        check = true;
                    }
                }
            } 
            return check;
	}
	
	// private method for placing a piece and reversing pieces
	private void placeAndReverse(final int x, final int y) {
          //  determineReverse(x, y);
            if(getPiece(x, y) ==0)
                 render[x][y].setPiece(current_player);
            
              for(int i = -1; i<2; i++){
                  for(int j = -1; j<2; j++){
                      if(can_reverse[i+1][j+1]==true){
                          reverseChain(x+j, y+i, j, i);
                      }
                  }
              }
	}
	// private method to reverse a chain
	private void reverseChain(final int x, final int y, final int dx, final int dy) {
            render[x][y].setPiece(current_player);
            if(current_player == 1 && render[x+dx][y+dy].getPiece()==2){
                render[x+dx][y+dy].swapPiece();
                reverseChain(x+dx, y+dy, dx, dy);
            }
            else if(current_player == 2 && render[x+dx][y+dy].getPiece()==1){
                render[x+dx][y+dy].swapPiece();
                reverseChain(x+dx, y+dy, dx, dy);
            }
   
	}

	// private method for getting a piece on the board. this will return the board
	// value unless we access an index that doesnt exist. this is to make the code
	// for determing reverse chains much easier
	private int getPiece(final int x, final int y) {
		// NOTE: this is to keep the compiler happy until you get to this point
            try{
                return render[x][y].getPiece();    
            }
            catch(Exception e){return -1;}
	}
	
	// private method that will determine if the end of the game has been reached
	private void determineEndGame() {
         
                if((player1_score + player2_score) == 64 ){
                    in_play = false;
                }
                else if(player1_score == 0 || player2_score == 0){
                    in_play = false;
                }
                else if( !canMove()){
                    swapPlayers();
                    if(!canMove())
                        in_play = false;
                    else
                        in_play = true;
                }
    
	}
	
	// private method to determine if a player has a move available
	private boolean canMove() {
            // NOTE: this is to keep the compiler happy until you get to this part
            boolean check = false;
            for(int i = 0; i<8; i++){
                for(int j = 0; j<8; j++){
                    
                    if(getPiece(i, j) == 0){
                        determineSurrounding(j, i);
                        if(current_player == 1 && determineReverse(j, i) && !check )
                            check = true;
                        else if(current_player == 2 && determineReverse(j, i) && !check)
                            check = true;
                    }    
                }
            }
          return check;
	}
	
	// private method that determines who won the game
	private void determineWinner() {
            
           if(!in_play){
                if(player1_score > player2_score)
                    System.out.println("White won the game.");
                else if(player2_score > player1_score)
                    System.out.println("Black won the game.");
                else 
                    System.out.println("Draw game.");
                
                System.out.println("The game has ended.");
           }
                    
        }
	
	// private method that will initialise everything in the render array
	private void initialiseRender() {
            for(int i = 0; i< 8; i++){
                for(int j = 0; j<8; j++){
                    render[i][j] = new ReversiPiece(0);
                    getChildren().add(render[i][j]);
                }
            }
	}
	
	
	// private fields that make the reversi board work
	
	// rectangle that makes the background of the board
	private Rectangle background;
	// arrays for the lines that makeup the horizontal and vertical grid lines
	private Line[] horizontal;
	private Line[] vertical;
	// arrays holding translate objects for the horizontal and vertical grid lines
	private Translate[] horizontal_t;
	private Translate[] vertical_t;
	// arrays for the internal representation of the board and the pieces that are
	// in place
	private ReversiPiece[][] render;
	// the current player who is playing and who is his opposition
	private int current_player;
	private int opposing;
	// is the game currently in play
	private boolean in_play;
	// current scores of player 1 and player 2
	private int player1_score;
	private int player2_score;
	// the width and height of a cell in the board
	private double cell_width;
	private double cell_height;
	// 3x3 array that holds the pieces that surround a given piece
	private int[][] surrounding;
	// 3x3 array that determines if a reverse can be made in any direction
	private boolean[][] can_reverse;
}

// class definition for a reversi piece
class ReversiPiece extends Group {
	// default constructor for the class
	public ReversiPiece(int player) {
          
            this.player = player;
            piece = new Ellipse();
            piece.setStroke(Color.YELLOW);
            t = new Translate();
            piece.getTransforms().add(t);
            
           setVisible(false);
            if(player == 1){
                piece.setFill(Color.WHITE);
                piece.setVisible(true);
                piece.setStroke(Color.BLUE);
            }
            else if(player == 2){
                piece.setFill(Color.BLACK);
                piece.setVisible(true);
            }
           
            getChildren().add(piece);
	}
	
	// overridden version of the resize method to give the piece the correct size
	@Override
	public void resize(double width, double height) {
            super.resize(width, height); 
           
            piece.setRadiusX(width / 2.0);
            piece.setRadiusY(height / 2.0);
            piece.setCenterX(width / 2.0);
            piece.setCenterY(height / 2.0);  
           
	}
	   
	// overridden version of the relocate method to position the piece correctly
	@Override
	public void relocate(double x, double y) {
            super.relocate(x, y);
            t.setX(x); t.setY(y);
	}
	
	// public method that will swap the colour and type of this piece
	public void swapPiece() {
            if(player == 1){
                piece.setVisible(true);
                piece.setFill(Color.BLACK);
                this.setPiece(2);
                
               
            }
            else if(player == 2){
                piece.setVisible(true);
                piece.setFill(Color.WHITE);
                this.setPiece(1);
               
            }
            
	}
	
	// method that will set the piece type
	public void setPiece(final int type) {
            player = type;
            setVisible(true);
            switch (player) {
                    case 1: {
                    piece.setFill(Color.WHITE);
                    break;
                    }
                    case 2: {
                        piece.setFill(Color.BLACK);
                        break;
                    }
                case 0: {
                    setVisible(false);
                    break;
                    }
                }

	}
	
	// returns the type of this piece
	public int getPiece() {
           return player;
	}
	
	// private fields
	private int player;		// the player that this piece belongs to
	private Ellipse piece;	// ellipse representing the player's piece
	private Translate t;	// translation for the player piece
}