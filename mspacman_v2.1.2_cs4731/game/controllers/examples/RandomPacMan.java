package game.controllers.examples;

import game.controllers.PacManController;
import game.core.G;
import game.core.Game;

public final class RandomPacMan implements PacManController
{
	public int getAction(Game game,long timeDue)
	{
		int[] directions=game.getPossiblePacManDirs(true);		//set flag as true to include reversals		
		return directions[G.rnd.nextInt(directions.length)];
	}
}