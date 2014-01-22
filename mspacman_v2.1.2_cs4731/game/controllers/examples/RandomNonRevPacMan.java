package game.controllers.examples;

import game.controllers.PacManController;
import game.core.G;
import game.core.Game;

public final class RandomNonRevPacMan implements PacManController
{	
	public int getAction(Game game,long timeDue)
	{			
		int[] directions=game.getPossiblePacManDirs(false);		//set flag as false to prevent reversals	
		return directions[G.rnd.nextInt(directions.length)];		
	}
}