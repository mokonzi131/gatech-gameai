package game.core;

/*
 * This class is to replay games that were recorded using Replay. The only differences are:
 * 1. Ghost reversals are removed
 * 2. Directions are not checked (since they are necessarily valid)
 * This class should only be used in conjunction with stored directions, not to play the game itself.
 */
public final class _RG_ extends _G_
{	
	//Updates the locations of the ghosts without reversals
	protected void updateGhosts(int[] directions,boolean reverse)
	{
		super.updateGhosts(directions,false);
	}
	
	public int checkGhostDir(int whichGhost,int direction)
	{
		return direction;
	}
	
	public int checkPacManDir(int direction)
	{
		return direction;		
	}
}