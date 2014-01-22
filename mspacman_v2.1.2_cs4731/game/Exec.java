package game;

import game.controllers.Human;
import game.controllers.PacManController;
import game.controllers.GhostController;
import game.controllers.examples.AttractRepelGhosts;
import game.controllers.examples.Legacy;
import game.controllers.examples.Legacy2TheReckoning;
import game.controllers.examples.NearestPillPacMan;
import game.controllers.examples.NearestPillPacManVS;
import game.controllers.examples.RandomGhosts;
import game.controllers.examples.RandomNonRevPacMan;
import game.controllers.examples.RandomPacMan;
import game.controllers.examples.MyGhosts;
import game.core.G;
import game.core.GameView;
import game.core._G_;
import game.core.Replay;
import game.core._RG_;

/*
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Exec
{	
	//Several options are listed - simply remove comments to use the option you want
	public static void main(String[] args)
	{
		Exec exec=new Exec();
		
		//this can be used for numerical testing (non-visual, no delays)
//		exec.runExperiment(new RandomPacMan(),new AttractRepelGhosts(true),100);
		
		//run game without time limits (un-comment if required)
//		exec.runGame(new RandomPacMan(),new RandomGhosts(),true,G.DELAY);
		
		//run game with time limits (un-comment if required)
//		exec.runGameTimed(new Human(),new AttractRepelGhosts(true),true);
		//run game with time limits. Here NearestPillPacManVS is chosen to illustrate how to use graphics for debugging/information purposes 
		exec.runGameTimed(new NearestPillPacManVS(),new MyGhosts(true),true);
		
		//this allows you to record a game and replay it later. This could be very useful when
		//running many games in non-visual mode - one can then pick out those that appear irregular
		//and replay them in visual mode to see what is happening.
//		exec.runGameTimedAndRecorded(new Human(),new AttractRepelGhosts(false),true,"human-v-Legacy2.txt");
//		exec.replayGame("human-v-Legacy2.txt");
	}
	
    protected int pacDir;
    protected int[] ghostDirs;
    protected _G_ game;
    protected PacMan pacMan;
    protected Ghosts ghosts;
    protected boolean pacmanPlayed,ghostsPlayed;
   
    /*
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     */
    public void runExperiment(PacManController pacManController,GhostController ghostController,int trials)
    {
    	double avgScore=0;
    	
		game=new _G_();
		
		for(int i=0;i<trials;i++)
		{
			game.newGame();
			
			while(!game.gameOver())
			{
				long due=System.currentTimeMillis()+G.DELAY;
		        game.advanceGame(pacManController.getAction(game.copy(),due),ghostController.getActions(game.copy(),due));
			}
			
			avgScore+=game.getScore();
			System.out.println(game.getScore());
		}
		
		System.out.println(avgScore/trials);
    }
    
    /*
     * Run game without time limit. Very good for testing as game progresses as soon as the controllers
     * return their action(s). Can be played with and without visual display of game states. The delay
     * is purely for visual purposes (as otherwise the game could be too fast if controllers compute quickly. 
     * For testing, this can be set to 0 for fasted game play.
     */
	public void runGame(PacManController pacManController,GhostController ghostController,boolean visual,int delay)
	{
		game=new _G_();
		game.newGame();

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
			long due=System.currentTimeMillis()+G.DELAY;
	        game.advanceGame(pacManController.getAction(game.copy(),due),ghostController.getActions(game.copy(),due));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
	}
	
    /*
     * Run game with time limit. This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     */
	public void runGameTimed(PacManController pacManController,GhostController ghostController,boolean visual)
	{
		game=new _G_();
		game.newGame();
		pacMan=new PacMan(pacManController);
		ghosts=new Ghosts(ghostController);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof Human)
				gv.getFrame().addKeyListener((Human)pacManController);
		}		
		
		while(!game.gameOver())
		{
			pacMan.alert();
			ghosts.alert();

			try
			{
				Thread.sleep(G.DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacDir,ghostDirs);	        
	        
	        if(visual)
	        	gv.repaint();
		}
		
		pacMan.kill();
		ghosts.kill();
	}
	
	/*
	 * Runs a game and records all directions taken by all controllers - the data may then be used to replay any game saved using
	 * replayGame(-).
	 */
	public void runGameTimedAndRecorded(PacManController pacManController,GhostController ghostController,boolean visual,String fileName)
	{
		StringBuilder history=new StringBuilder();
		int lastLevel=0;
		boolean firstWrite=false;	//this makes sure the content of any existing files is overwritten
		
		game=new _G_();
		game.newGame();
		pacMan=new PacMan(pacManController);
		ghosts=new Ghosts(ghostController);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof Human)
				gv.getFrame().addKeyListener((Human)pacManController);
		}		
		
		while(!game.gameOver())
		{
			pacMan.alert();
			ghosts.alert();

			try
			{
				Thread.sleep(G.DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        int[] actionsTaken=game.advanceGame(pacDir,ghostDirs);	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        history=addActionsToString(history,actionsTaken,game.getCurLevel()==lastLevel);
        	
	        //saves actions after every level
        	if(game.getCurLevel()!=lastLevel)
        	{
        		Replay.saveActions(history.toString(),fileName,firstWrite);
        		lastLevel=game.getCurLevel();
        		firstWrite=true;
        		history=new StringBuilder();
        	}	   
		}
		
		//save the final actions
		Replay.saveActions(history.toString(),fileName,firstWrite);
		
		pacMan.kill();
		ghosts.kill();
	}
	
	/*
	 * This is used to replay a recorded game. The controllers are given by the class Replay which may
	 * also be used to load the actions from file.
	 */
	public void replayGame(String fileName)
	{
		_RG_ game=new _RG_();
		game.newGame();

		Replay replay=new Replay(fileName);
		PacManController pacManController=replay.getPacMan();
		GhostController ghostController=replay.getGhosts();
		
		GameView gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getAction(game.copy(),0),ghostController.getActions(game.copy(),0));
	        
	        gv.repaint();
	        
	        try{Thread.sleep(G.DELAY);}catch(Exception e){}
		}
	}
	
    private StringBuilder addActionsToString(StringBuilder history,int[] actionsTaken,boolean newLine)
    {
    	history.append((game.getTotalTime()-1)+"\t"+actionsTaken[0]+"\t");

        for (int i=0;i<G.NUM_GHOSTS;i++)
        	history.append(actionsTaken[i+1]+"\t");

        if(newLine)
        	history.append("\n");
        
        return history;
    }
    	
	//sets the latest direction to take for each game step (if controller replies in time)
	public void setGhostDirs(int[] ghostDirs)
	{
		this.ghostDirs=ghostDirs;
		this.ghostsPlayed=true;
	}
	
	//sets the latest direction to take for each game step (if controller replies in time)
	public void setPacDir(int pacDir)
	{
		this.pacDir=pacDir;
		this.pacmanPlayed=true;
	}
	
	/*
	 * Wraps the controller in a thread for the timed execution. This class then updates the
	 * directions for Exec to parse to the game.
	 */
	public class PacMan extends Thread 
	{
	    private PacManController pacMan;
	    private boolean alive;

	    public PacMan(PacManController pacMan) 
	    {
	        this.pacMan=pacMan;
	        alive=true;
	        start();
	    }

	    public synchronized void kill() 
	    {
	        alive=false;
	        notify();
	    }
	    
	    public synchronized void alert()
	    {
	        notify();
	    }

	    public synchronized void run() 
	    {
	        while(alive) 
	        {
	        	try 
	        	{
	        		synchronized(this)
	        		{
	        			wait();
	                }
	                
	        		setPacDir(pacMan.getAction(game.copy(),System.currentTimeMillis()+G.DELAY));
	            } 
	        	catch(InterruptedException e) 
	        	{
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	/*
	 * Wraps the controller in a thread for the timed execution. This class then updates the
	 * directions for Exec to parse to the game.
	 */
	public class Ghosts extends Thread 
	{
		private GhostController ghosts;
	    private boolean alive;

	    public Ghosts(GhostController ghosts) 
	    {	    	
	    	this.ghosts=ghosts;
	        alive=true;
	        start();
	    }

	    public synchronized void kill() 
	    {
	        alive=false;
	        notify();
	    }

	    public synchronized void alert() 
	    {
	        notify();
	    }
	    
	    public synchronized void run() 
	    {
	        while(alive) 
	        {
	        	try 
	        	{
	        		synchronized(this)
	        		{
	        			wait();
	                }

	        		setGhostDirs(ghosts.getActions(game.copy(),System.currentTimeMillis()+G.DELAY));
	            } 
	        	catch(InterruptedException e) 
	        	{
	                e.printStackTrace();
	            }
	        }
	    }
	}
}