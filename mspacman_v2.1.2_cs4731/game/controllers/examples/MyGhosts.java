package game.controllers.examples;

import game.controllers.GhostController;
import game.core.Game.DM;
import game.core.Game;
import game.core.GameView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MyGhosts implements GhostController
{
	// debugging switch
	private boolean Debugging = false;
	public MyGhosts(boolean debugging)
	{
		Debugging = debugging;
	}
	
	// enemy attack mode state machine
	private enum MODE {
		CHASE,
		SCATTER,
		FRIGHTENED
	}
	private MODE mode = MODE.SCATTER;
	private int modeTimer = 0;
	
	private int index = 0;
	int hold = 0;
	int oldloc = 0;
	
	private int currentLevel = -1;
	public int[] getActions(Game game, long timeDue)
	{
		// initialize internal game map and per-level counters
		if (currentLevel != game.getCurLevel()) {
			currentLevel = game.getCurLevel();
			m_gameMap = new GameMap(game);
		}
		
		// update the mode state machine
		
		// assign ghost targets
		
		System.out.println(m_gameMap.tiles().size());
		int myloc = game.getCurPacManLoc();
		if (myloc != oldloc) {
			oldloc = myloc;
//			System.out.println(myloc);
		}
		int[] neighbors = game.getPacManNeighbours();
		for (int i = 0; i < 4; ++i) {
			if (neighbors[i] == -1)
				neighbors[i] = 0;
		}
//		if (hold == 0)
//			System.out.println(myloc + "(" + game.getX(myloc) + "," + game.getY(myloc) + ")");
//			System.out.println(myloc + ": " + neighbors[0] + ", " + neighbors[1] + ", " + neighbors[2] + ", " + neighbors[3]);
		
		int[] directions=new int[Game.NUM_GHOSTS];
		DM[] dms = Game.DM.values();
		
		for(int i=0;i<directions.length;i++) {
			// the ai - pacman's current location
			int myComputedGhostDestination = (i == 0) ? m_gameMap.tiles().get(index).index : 0; //game.getCurPacManLoc();
//			System.out.println("NODE 1: " + game.getX(1) + " - " + game.getY(1));
			++hold;
			if (hold > 50) {
				hold = 0;
				index += 1;
			}
			if (index >= m_gameMap.tiles().size())
				index = 0;
			
			// set moves according to pathfinding logic
			if(game.ghostRequiresAction(i))	{
				directions[i] = game.getNextGhostDir(i, myComputedGhostDestination, true, Game.DM.PATH);
			}
			
			// print debugging lines
			if (Debugging) {
				Color color = Color.DARK_GRAY;
				for (Tile tile : m_gameMap.tiles()) {
					GameView.addPoints(game, color, tile.index);
				}
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
				GameView.addLines(game, color, game.getCurGhostLoc(i), myComputedGhostDestination);
				GameView.addPoints(game, color, myComputedGhostDestination);
			}
		}
				
		return directions;
	}
	
	// the internal game map logic
	private GameMap m_gameMap;
	
	private class Tile {
		public int index;
		public int x;
		public int y;
		
		public Tile(int _index, int _x, int _y) {
			index = _index;
			x = _x;
			y = _y;
		}
	}
	
	private class GameMap {		
		private List<Tile> m_tiles;
		
		public GameMap(Game game) {
			m_tiles = new ArrayList<>();
			
			// assume the first node is a valid tile (experimentation suggests that this is always the case)
			Tile tile = new Tile(0, game.getX(0), game.getY(0));
			m_tiles.add(tile);
			int lastx = tile.x;
			int lasty = tile.y;
			
			// for all the rest of the nodes, create tiles as needed
			for (int i = 1; i < game.getNumberOfNodes(); ++i) {
				int x = game.getX(i);
				int y = game.getY(i);
				
				// new node is in current row
				if (y == lasty) {
					// if we aren't at least 4 pixels away, this is not a significant change
					if (x < lastx + 4) continue;
					// otherwise, it is, create a new tile and update current column
					else {
						m_tiles.add(new Tile(i, x, y));
						lastx = x;
					}
				} else {
					// we must be at least 4 pixels away to be significant
					if (y < lasty + 4) continue;
					// create new tile, and update row and column (since we could be in an arbitrary column)
					else {
						m_tiles.add(new Tile(i, x, y));
						lasty = y;
						lastx = x;
					}
				}
			}
		}
		
		public List<Tile> tiles() { return m_tiles; }
	}
}

