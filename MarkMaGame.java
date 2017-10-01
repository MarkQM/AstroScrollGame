import java.awt.event.KeyEvent;
import java.util.Random;

public class MarkMaGame
{
  
  // set to false to use your code
  private static final boolean DEMO = false;           
  public MattGame dg;
  
  // Game window should be wider than tall:   H_DIM < W_DIM   
  // (more effectively using space)
  private static final int H_DIM = 6;   // # of cells vertically by default: height of game
  private static final int W_DIM = 12;  // # of cells horizontally by default: width of game
  private static final int U_ROW = 0;
  public Random random = new Random();
  
  private Grid grid;
  private int userRow;
  private int userCol;
  private int msElapsed;
  private int energyRemaining;
  private int livesRemaining;
  private int timesHit;
  private int timesMiss;
  private int speedMultiplier = 3;
  private double timeMultiplier = 0.0;
  
  
  private int pauseTime = 100;
  private boolean isPaused = false;
  
  public MarkMaGame()
  {

    init(H_DIM, W_DIM, U_ROW);
  }
  
  public MarkMaGame(int hdim, int wdim, int uRow)
  {
    init(hdim, wdim, uRow);

  }
  
  private void init(int hdim, int wdim, int uRow) {  
 //   grid = new Grid(hdim, wdim, Color.BLACK, Color.BLUE); 
    grid = new Grid("MarkMaMap.gif", hdim, wdim);
    
    //other Grid constructor of interest: 
    //    comment the line above; uncomment the one below 
    //You can adjust colors by creating Color objects: look at the Grid constructors
    //grid = new Grid(hdim, wdim, Color.MAGENTA);   
    ///////////////////////////////////////////////
    userRow = uRow;
    userCol = 0;
    msElapsed = 0;
    energyRemaining = 100;
    livesRemaining = 4;
    timesHit = 0;
    updateTitle();
    grid.setImage(new Location(userRow, 0), "MarkMaUser.gif");
    
  }
  
  private void calculateMultiplier(){
    if (timeMultiplier < 4){
    timeMultiplier = 1.4*(msElapsed)/(pauseTime*1000.0);
    }
  }
  
  private void calculateEnergy(){
    if (energyRemaining > 100) {
      energyRemaining = 100;
    }
    else if(energyRemaining < 1) {
      livesRemaining--;
      if(livesRemaining > 0)
        energyRemaining = 100;
    }
    energyRemaining-=1;
  }
  
  
  public void play()
  {
   while (!isGameOver())
   {
       grid.pause(pauseTime);
       handleKeyPress();
       if(!isPaused){
         handleProjectiles();
         if (msElapsed % (speedMultiplier * pauseTime) == 0)
         {
           scrollLeft();
           populateRightEdge();
           calculateEnergy();
         }
         updateTitle();
         msElapsed += pauseTime;
         calculateMultiplier();
       }
    }
  }
      
  
  
  
  public void handleKeyPress()
  {
    int key = grid.checkLastKeyPressed();
    //use Java constant names for key presses
    //http://docs.oracle.com/javase/7/docs/api/constant-values.html#java.awt.event.KeyEvent.VK_DOWN
    
    if (key == KeyEvent.VK_Q)
      System.exit(0);
    
    else if(key == KeyEvent.VK_P) {
      if(isPaused){
        isPaused = false;
        System.out.println("UNPAUSE");
      }
      else{
        isPaused = true;
        System.out.println("PAUSE");
      }
    }
      
    if(!isPaused){
      
     if(key == KeyEvent.VK_UP) {
      if(userRow != 0) {
        userRow--;
        handleCollision(new Location(userRow, userCol));
        grid.setImage(new Location(userRow, userCol), "MarkMaUser.gif");
        grid.setImage(new Location(userRow+1, userCol), null);
        
      }
    }
    
    else if(key == KeyEvent.VK_DOWN) {
      if(userRow != grid.getNumRows()-1) {
        userRow++;
        handleCollision(new Location(userRow, userCol));
        grid.setImage(new Location(userRow, userCol), "MarkMaUser.gif");
        grid.setImage(new Location(userRow-1, userCol), null);
      }
    }
    
    else if(key == KeyEvent.VK_LEFT) {
      if(userCol != 0) {
        userCol--;
        handleCollision(new Location(userRow, userCol));
        grid.setImage(new Location(userRow, userCol), "MarkMaUser.gif");
        grid.setImage(new Location(userRow, userCol+1), null);
      }
    }
    
    else if(key == KeyEvent.VK_RIGHT) {
      if(userCol != grid.getNumCols()-2) {
        userCol++;
        handleCollision(new Location(userRow, userCol));
        grid.setImage(new Location(userRow, userCol), "MarkMaUser.gif");
        grid.setImage(new Location(userRow, userCol-1), null);
      }
    }
    
    else if(key == KeyEvent.VK_SPACE) {
     fireMissile();
     energyRemaining/=1.5;
      
    }
    
    else if(key == KeyEvent.VK_COMMA) {
      speedMultiplier++;
    }
    
    else if(key == KeyEvent.VK_PERIOD) {
       if(speedMultiplier > 1)
         speedMultiplier--;   
    }
    
    else if (key == KeyEvent.VK_T) 
    {
      boolean interval = (msElapsed % (speedMultiplier * pauseTime) == 0);
      System.out.println("pauseTime " + pauseTime + " msElapsed reset " + msElapsed 
                        + " interval " + interval + " timeMultiplier " + timeMultiplier + " speedMultiplier " +speedMultiplier);
    }
    }

  }
  
  public void populateRightEdge()
  {
    int avoidPerRow = 0;
    int getPerRow = 0;
    for(int row=0; row < grid.getNumRows(); row++) {
      double rando = random.nextGaussian();
      
      if (rando > (1.6-0.4*timeMultiplier)  && avoidPerRow < grid.getNumRows()/2) {
        grid.setImage(new Location(row, grid.getNumCols()-1), "MarkMaAvoid.gif");
        avoidPerRow++;
      }
      
      else if(rando < (-1.4+0.2*timeMultiplier) && getPerRow < grid.getNumRows()/3){
        grid.setImage(new Location(row, grid.getNumCols()-1), "MarkMaGet.png");
        getPerRow++;
      }
    }
      
  }
  
  public void scrollLeft()
  {
    for(int col=0; col < grid.getNumCols(); col++) {
      for(int row=0; row < grid.getNumRows(); row++) {
        String image = grid.getImage(new Location(row, col));
        if (image == "MarkMaAvoid.gif" || image == "MarkMaGet.png") {
          if (col == 0) {
            grid.setImage(new Location(row, col), null);
          }
          else if(grid.getImage(new Location(row, col-1)) != null && grid.getImage(new Location(row, col-1)).equals("MarkMaUser.gif")){
            handleCollision(new Location(row, col));
          }
          
          else if(grid.getImage(new Location(row, col-1)) != null && 
                  (grid.getImage(new Location(row, col-1)).equals("MarkMaBomb.gif") || grid.getImage(new Location(row, col-1)).equals("MarkMaGet.png"))) {
            handleHit(new Location(row, col));
          }
          
          else {
            grid.setImage(new Location(row, col-1), image);
            grid.setImage(new Location(row, col), null);
          }
        }
        else if(image == "MarkMaExplode.gif" || image == "MarkMaMinus.png") {
          grid.setImage(new Location(row, col), null);
        }
      }
    }
  }
  
  
  public void handleProjectiles()
  {
    for(int col=grid.getNumCols()-1; col > 0; col--) {
      for(int row=0; row < grid.getNumRows(); row++) {
        String image = grid.getImage(new Location(row, col));
        if (image == "MarkMaBomb.gif") {
          if(col == grid.getNumCols()-1) {
            grid.setImage(new Location(row, col),  null);
          }
          else if(grid.getImage(new Location(row, col+1)) != null && 
                  (grid.getImage(new Location(row, col+1)).equals("MarkMaAvoid.gif") || grid.getImage(new Location(row, col+1)).equals("MarkMaGet.png"))) {
            handleHit(new Location(row, col));
          }
          else {
            grid.setImage(new Location(row, col+1), image);
            grid.setImage(new Location(row, col), null);
          }
        }
      }
    }
  }

  
  public void handleHit(Location loc){
    if(grid.getImage(loc).equals("MarkMaAvoid.gif")) {
      grid.setImage(loc, "MarkMaExplode.gif");
      timesHit++;
    }
    
    else if(grid.getImage(loc).equals("MarkMaGet.png")) {
      grid.setImage(loc, "MarkMaMinus.png");
      timesMiss++;
    }
      
  }
  
  public void fireMissile()
  {
  grid.setImage(new Location(userRow, userCol+1), "MarkMaBomb.gif");
  }
    
    
  
  public void handleCollision(Location loc)
  {
    if(grid.getImage(loc) == null) {
      return;
    }
     if(grid.getImage(loc).equals("MarkMaAvoid.gif")) {
       livesRemaining--;
//       System.out.println("Avoid " + timesAvoid);
    }
    else if(grid.getImage(loc).equals("MarkMaGet.png")) {
       energyRemaining+=15;
//       System.out.println("Get " + timesGet);
    }
    grid.setImage(loc, null);
  }
  
  public int getScore()
  {
    return (msElapsed/20 + timesHit*150 - timesMiss*50);
  }
  
  public void updateTitle()
  {
    grid.setTitle("INTERSTELLA 121:  " + getScore() + "  LIVES: " + (livesRemaining) + " ENERGY: " + energyRemaining + "%" + "  TIME: " + msElapsed);
  }
  
  public boolean isGameOver()
  {
    if(livesRemaining <= 0) {
      System.out.println("Game Over!  Your final score is: " + getScore());
      return true;
    }
    
     else if(msElapsed > 300000) {
      System.out.println("Congratulations!  You win!  Your final score is: " + (getScore()+12100));
      return true;
    }
      
    return false;
  }
  
  public static void test()
  {
    if (DEMO) {       // reference game: 
                      //   - play and observe first the mechanism of the demo to understand the basic game 
                      //   - go back to the demo anytime you don't know what your next step is
                      //     or details about it are not concrete
                      //         figure out according to the game play 
                      //         (the sequence of display and action) how the functionality
                      //         you are implementing next is supposed to operate
                      // It's critical to have a plan for each piece of code: follow, understand
                      // and study the assignment description details; and explore the basic game. 
                      // You should always know what you are doing (your current, small goal) before
                      // implementing that piece or talk to us. 

      System.out.println("Running the demo: DEMO=" + DEMO);
      //default constructor   (4 by 10)
      MattGame game = new MattGame();
      //other constructor: client adjusts game window size   TRY IT
      // MattGame game = new MattGame(10, 20, 0);
      game.play();
    
    } else {
      System.out.println("Running student game: DEMO=" + DEMO);
      // !DEMO   -> your code should execute those lines when you are
      // implementing your game
      
      //test 1: with parameterless constructor
      MarkMaGame game = new MarkMaGame();
      
      //test 2: with constructor specifying grid size    IT SHOULD ALSO WORK as long as height < width
      //MarkMaGame game = new MarkMaGame(10, 20, 5);
      
      game.play();
    }
  }
  
  public static void main(String[] args)
  {
    test();
  }
}