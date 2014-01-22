package game.controllers.examples;

import game.controllers.GhostController;
import game.core.G;
import game.core.Game;

public final class RandomGhosts implements GhostController
{	
	public int[] getActions(Game game,long timeDue)
	{	
		int[] directions=new int[Game.NUM_GHOSTS];
		
		//Chooses a random LEGAL action if required. Could be much simpler by simply returning
		//any random number of all of the ghosts
		for(int i=0;i<directions.length;i++)
			if(game.ghostRequiresAction(i))
			{			
				int[] possibleDirs=game.getPossibleGhostDirs(i);			
				directions[i]=possibleDirs[G.rnd.nextInt(possibleDirs.length)];
			}
		
		return directions;
	}
}