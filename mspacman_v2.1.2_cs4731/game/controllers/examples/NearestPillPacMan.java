package game.controllers.examples;

import game.controllers.PacManController;
import game.core.G;
import game.core.Game;

public class NearestPillPacMan implements PacManController
{	
	public int getAction(Game game,long timeDue)
	{	
		int current=game.getCurPacManLoc();
		
		//get all active pills
		int[] activePills=game.getPillIndicesActive();
		
		//get all active power pills
		int[] activePowerPills=game.getPowerPillIndicesActive();
		
		//create a target array that includes all ACTIVE pills and power pills
		int[] targetsArray=new int[activePills.length+activePowerPills.length];
		
		for(int i=0;i<activePills.length;i++)
			targetsArray[i]=activePills[i];
		
		for(int i=0;i<activePowerPills.length;i++)
			targetsArray[activePills.length+i]=activePowerPills[i];		
		
		//return the next direction once the closest target has been identified
		return game.getNextPacManDir(game.getTarget(current,targetsArray,true,G.DM.PATH),true,Game.DM.PATH);	
	}
}