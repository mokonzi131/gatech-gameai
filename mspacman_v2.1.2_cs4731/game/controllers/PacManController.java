package game.controllers;

import game.core.Game;

/*
 * Interface that Ms Pac-Man controllers must implement. The only method that is
 * required is getAction(-), which returns the direction to be taken: 
 * Up - Right - Down - Left -> 0 - 1 - 2 - 3
 * Any other number is considered to be a lack of action (Neutral). 
 */
public interface PacManController
{
	public int getAction(Game game,long timeDue);
}