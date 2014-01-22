package game.controllers.examples;

import game.controllers.GhostController;
import game.core.Game.DM;
import game.core.Game;
import game.core.GameView;
import java.awt.Color;

public class MyGhosts implements GhostController
{
	private boolean Debugging = false;
	
	public MyGhosts(boolean debugging)
	{
		Debugging = debugging;
	}
	
	
	public int[] getActions(Game game,long timeDue)
	{
		int[] directions=new int[Game.NUM_GHOSTS];
		DM[] dms=Game.DM.values();
		
		for(int i=0;i<directions.length;i++) {
			if(game.ghostRequiresAction(i))	{
				directions[i]=game.getNextGhostDir(i,game.getCurPacManLoc(),true,Game.DM.PATH);
			}
			if (Debugging) {
				Color color = Color.GRAY;
				if (i == 0) {
					color = Color.RED;
				}
				else if (i == 1) {
					color = Color.PINK;
				}
				else if (i == 2) {
					color = Color.ORANGE;
				}
				else {
					color = Color.BLUE;
				}
				GameView.addLines(game, color,game.getCurGhostLoc(i), game.getCurPacManLoc());
			}
		}
				
		return directions;
	}
}

