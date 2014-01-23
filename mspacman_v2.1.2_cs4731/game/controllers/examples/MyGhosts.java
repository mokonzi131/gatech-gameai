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
	
	// ghost mode state machine
	private ModeStateMachine m_modeStateMachine;
	
	private enum MODE {
		CHASE,
		SCATTER,
		FRIGHTENED
	}
	
	private class ModeStateMachine {
		private MODE m_mode;
		private Game m_game;
		
		private int counter = 0;
		
		public ModeStateMachine(Game game) {
			m_mode = MODE.SCATTER;
			m_game = game;
		}
		
		public void updateState() {
			// TODO - implement real timings
			// TODO - implement pause on frightened mode
			// TODO - reverse between scatter and chase
//			++counter;
//			if (counter >= 50) {
//				counter = 0;
//				switch(m_mode) {
//				case CHASE:
//					m_mode = MODE.SCATTER;
//					break;
//				case SCATTER:
//					m_mode = MODE.FRIGHTENED;
//					break;
//				case FRIGHTENED:
//					m_mode = MODE.CHASE;
//					break;
//				}
//			}
		}
		
		public MODE mode() { return m_mode; }
	}
	
	/// TEMP
	private int index = 0;
	int hold = 0;
	int oldloc = 0;
	/// END TEMP
	
	private int currentLevel = -1;
	
	public int[] getActions(Game game, long timeDue)
	{
		// initialize internal game map and per-level counters
		if (currentLevel != game.getCurLevel()) {
			currentLevel = game.getCurLevel();
			m_gameMap = new GameMap(game);
			m_modeStateMachine = new ModeStateMachine(game);
		}
		if (Debugging) {
			Color color = Color.DARK_GRAY;
			for (Tile tile : m_gameMap.tiles()) {
				GameView.addPoints(game, color, tile.index);
			}
		}
		
		// update the mode state machine
		m_modeStateMachine.updateState();
		if (Debugging)
			System.out.println(m_modeStateMachine.mode());
		
		// assign ghost targets
		int[] directions = new int[Game.NUM_GHOSTS];
		for (int i = 0; i < directions.length; ++i) {
			int destination = 0;
			switch (m_modeStateMachine.mode()) {
			case CHASE:
				destination = game.getCurPacManLoc();
				break;
			case SCATTER:
				destination = m_gameMap.getScatterTarget(i);
				break;
			case FRIGHTENED:
				break;
			}
			
			if (game.ghostRequiresAction(i)) {
				directions[i] = game.getNextGhostDir(i, destination, true, Game.DM.PATH);
			}
			
			// print debugging lines
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
				GameView.addLines(game, color, game.getCurGhostLoc(i), destination);
				GameView.addPoints(game, color, destination);
			}
		}
		
//		DM[] dms = Game.DM.values();
//		for(int i=0;i<directions.length;i++) {
			// the ai - pacman's current location
//			int myComputedGhostDestination = (i == 0) ? m_gameMap.tiles().get(index).index : 0; //game.getCurPacManLoc();
//			System.out.println("NODE 1: " + game.getX(1) + " - " + game.getY(1));
//			++hold;
//			if (hold > 50) {
//				hold = 0;
//				index += 1;
//			}
//			if (index >= m_gameMap.tiles().size())
//				index = 0;
//		}
				
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
		private Game m_game;
		private List<Tile> m_tiles;
		private Tile m_upperLeft = null;
		private Tile m_upperRight = null;
		private Tile m_lowerLeft = null;
		private Tile m_lowerRight = null;
		
		public GameMap(Game game) {
			m_game = game;
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
		public int getChaseDestination(int index) {
			switch(index) {
			case 0:
				return m_game.getCurPacManLoc();
			case 1:
				// TODO pinky
			case 2:
				// TODO clyde
			case 3:
				// TODO inky
			default:
				return 0;
			}
		}
		public int getScatterTarget(int index) {
			switch (index) {
			case 0:
				return blinkyCorner().index;
			case 1:
				return pinkyCorner().index;
			case 2:
				return clydeCorner().index;
			case 3:
				return inkyCorner().index;
			default:
					return 0;
			}
		}
		private Tile pinkyCorner() {
			if (m_upperLeft == null)
				m_upperLeft = m_tiles.get(0);
			
			return m_upperLeft;
		}
		private Tile blinkyCorner() {
			if (m_upperRight == null) {
				m_upperRight = m_tiles.get(0);
				for (int i = 1; i < m_tiles.size(); ++i) {
					if (m_tiles.get(i).y > m_upperRight.y) break;
					else m_upperRight = m_tiles.get(i);
				}
			}
			return m_upperRight;
		}
		private Tile inkyCorner() {
			if (m_lowerRight == null)
				m_lowerRight = m_tiles.get(m_tiles.size() - 1);
			
			return m_lowerRight;
		}
		private Tile clydeCorner	() {
			if (m_lowerLeft == null) {
				m_lowerLeft = m_tiles.get(m_tiles.size() - 1);
				for (int i = m_tiles.size() - 1 - 1; i >= 0; --i) {
					if (m_tiles.get(i).y < m_lowerLeft.y) break;
					else m_lowerLeft = m_tiles.get(i);
				}
			}
			return m_lowerLeft;
		}
	}
}

