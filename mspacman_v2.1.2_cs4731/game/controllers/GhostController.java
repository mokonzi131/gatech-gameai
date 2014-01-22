package game.controllers;

import game.core.Game;

/*
 * Interface that Ghosts controllers must implement. The only method that is
 * required is getActions(-), which returns the direction to be taken: 
 * Up - Right - Down - Left -> 0 - 1 - 2 - 3
 * Any other number is considered to be a lack of action (Neutral). 
 */
public interface GhostController
{
	public int[] getActions(Game game,long timeDue);
}