package game.controllers.examples;

import game.controllers.GhostController;
import game.core.Game.DM;
import game.core.Game;
import game.core.GameView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MyGhosts implements GhostController
{
	private static final int BLINKY = 0;
	private static final int PINKY = 1;
	private static final int INKY = 3;
	private static final int CLYDE = 2;
	
	// debugging switch
	private boolean Debugging = false;
	public MyGhosts(boolean debugging)
	{
		Debugging = debugging;
	}
	
	// ghost mode state machine
	private enum MODE {
		CHASE,
		SCATTER,
		FRIGHTENED
	}
	
	private class ModeStateMachine {
		private final int updateSpec[][] = {
				{7, 20, 7, 20, 5, 20, 5, Integer.MAX_VALUE},
				{7, 20, 7, 20, 5, 1033, 0, Integer.MAX_VALUE},
				{5, 20, 5, 20, 5, 1037, 0, Integer.MAX_VALUE}};
		private Stack<Integer> m_nextSpecChangeStack;
		
		private MODE m_mode;
		private long m_changeTimer;
		private long m_lastRecordedTime;
		public boolean reverse = false;
		
		public ModeStateMachine(Game game) {
			m_mode = MODE.SCATTER;
			
			// build updateSpec for state changes based on level
			m_nextSpecChangeStack = new Stack<>();
			int specLevel = 0;
			if (game.getCurLevel() > 0 && game.getCurLevel() < 4)
				specLevel = 1;
			else if (game.getCurLevel() >= 4)
				specLevel = 2;
			for (int i = updateSpec[specLevel].length - 1; i >= 0; --i)
				m_nextSpecChangeStack.push(new Integer(updateSpec[specLevel][i]));
			m_changeTimer = m_nextSpecChangeStack.pop() * 1000L;
			
			m_lastRecordedTime = System.currentTimeMillis();
		}
		
		public void updateState(Game game) {
			// unset reverse flag
			if (reverse) reverse = false;
			
			// measure elapsed time from last state update call
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - m_lastRecordedTime;
			
			// determine if any ghosts are frightened (in frightened mode)
			boolean frightened = false;
			for (int i = 0; i < 4; ++i)
				if (game.isEdible(i))
					frightened = true;
			
			// if not in frightened mode, decrement timer
			if (frightened == false)
				m_changeTimer -= (m_changeTimer == Integer.MAX_VALUE * 1000L) ? 0 : elapsedTime;
			m_lastRecordedTime = currentTime;
			
			// execute a mode change if needed
			if (m_changeTimer <= 0) {
				reverse = true;
				m_changeTimer = m_nextSpecChangeStack.pop() * 1000L;
				switch(m_mode) {
				case CHASE:
					m_mode = MODE.SCATTER;
					break;
				case SCATTER:
					m_mode = MODE.CHASE;
					break;
				}
			}
		}
		
		public MODE mode() { return m_mode; }
	}
	
	// controller logic variables
	private ModeStateMachine m_modeStateMachine;
	private GameMap m_gameMap;
	private int currentLevel = -1;
	private static int[] elroy1Spec =
		{20, 30, 40, 40, 40, 50, 50, 50, 60, 60, 60, 80, 80, 80, 100, 100, 100, 100, 120, 120, 120};
	private int elroy1Threshold(int level) {
		if (level > 20)
			level = 20;
		return elroy1Spec[level];
	}
	
	public int[] getActions(Game game, long timeDue)
	{
		// initialize internal game map and per-level counters
		if (currentLevel != game.getCurLevel()) {
			currentLevel = game.getCurLevel();
			m_gameMap = new GameMap(game);
			m_modeStateMachine = new ModeStateMachine(game);
		}
		
		// update the mode state machine
		m_modeStateMachine.updateState(game);
		
		// assign ghost targets
		int[] directions = new int[Game.NUM_GHOSTS];
		for (int i = 0; i < directions.length; ++i) {
			int destination = 0;
			switch (m_modeStateMachine.mode()) {
			case CHASE:
				destination = m_gameMap.getChaseDestination(game, i);
				break;
			case SCATTER:
				// speedy elroy scatter logic depends on number of dots left and clyde's being outside of the lair
				boolean elroy =
						(game.getNumActivePills() < elroy1Threshold(currentLevel)) ? true : false;
				if (game.getLairTime(CLYDE) > 0) elroy = false;
				destination = m_gameMap.getScatterTarget(game, i, elroy);
				break;
			case FRIGHTENED:
				destination = game.getCurGhostLoc(i);
				break;
			}
			if (game.isEdible(i))
				destination = game.getCurGhostLoc(i);
			
			// only deliver a direction for ghosts in need of a decision
			if (game.ghostRequiresAction(i)) {
				int action = game.getNextGhostDir(i, destination, true, Game.DM.PATH);
				directions[i] = m_modeStateMachine.reverse ? game.getReverse(action) : action;
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
				
		return directions;
	}
	
	// the internal game map logic
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
		private Tile m_upperLeft = null;
		private Tile m_upperRight = null;
		private Tile m_lowerLeft = null;
		private Tile m_lowerRight = null;
		private static final int TILE_GAP = 4;
		
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
					if (x < lastx + TILE_GAP) continue;
					// otherwise, it is, create a new tile and update current column
					else {
						m_tiles.add(new Tile(i, x, y));
						lastx = x;
					}
				} else {
					// we must be at least 4 pixels away to be significant
					if (y < lasty + TILE_GAP) continue;
					// create new tile, and update row and column (since we could be in an arbitrary column)
					else {
						m_tiles.add(new Tile(i, x, y));
						lasty = y;
						lastx = x;
					}
				}
			}
		}
		
		public int getChaseDestination(Game game, int index) {
			switch(index) {
			case BLINKY:
				// target pac-man
				return game.getCurPacManLoc();
			case PINKY:
				// get the location 4 spaces ahead of pac-man in the direction he is pointing
				int loc = game.getCurPacManLoc();
				int locx = game.getX(loc);
				int locy = game.getY(loc);
				switch (game.getCurPacManDir()) {
				case Game.LEFT:
					locx -= 4 * TILE_GAP;
					break;
				case Game.RIGHT:
					locx += 4 * TILE_GAP;
					break;
				case Game.UP:
					locx -= 4 * TILE_GAP;
					locy -= 4 * TILE_GAP;
					break;
				case Game.DOWN:
					locy += 4 * TILE_GAP;
					break;
				}
				return nearestIndex(locx, locy);
			case CLYDE:
				// scatter if too close else attack pac-man directly
				double distance = game.getEuclideanDistance(game.getCurPacManLoc(), game.getCurGhostLoc(CLYDE));
				if (distance > 8 * TILE_GAP)
					return game.getCurPacManLoc();
				else
					return clydeCorner().index;
			case INKY:
				// find the location 2 spaces ahead of pac-man in the direction he is pointing
				int head = game.getCurPacManLoc();
				int headx = game.getX(head);
				int heady = game.getY(head);
				switch(game.getCurPacManDir()) {
				case Game.LEFT:
					headx -= 2 * TILE_GAP;
					break;
				case Game.RIGHT:
					headx += 2 * TILE_GAP;
					break;
				case Game.UP:
					heady -= 2 * TILE_GAP;
					headx -= 2 * TILE_GAP;
					break;
				case Game.DOWN:
					heady += 2 * TILE_GAP;
					break;
				}
				// double the vector from Blinky to that location and target that spot
				int tail = game.getCurGhostLoc(BLINKY);
				int tailx = game.getX(tail);
				int taily = game.getY(tail);
				headx += (headx - tailx);
				heady += (heady - taily);
				return nearestIndex(headx, heady);
			default:
				return 0;
			}
		}
		public int getScatterTarget(Game game, int index, boolean elroy) {
			switch (index) {
			case BLINKY:
				if (elroy) return game.getCurPacManLoc();
				else return blinkyCorner().index;
			case PINKY:
				return pinkyCorner().index;
			case CLYDE:
				return clydeCorner().index;
			case INKY:
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
		private int nearestIndex(int x, int y) {
			// find the nearest tile to a coordinate brute force O(n) euclidean distance sqrt(pow, pow)
			Tile nearestTile = m_tiles.get(0);
			double distance = Double.MAX_VALUE;
			for (Tile tile : m_tiles) {
				double testDistance = Math.sqrt(Math.pow(x - tile.x, 2) + Math.pow(y - tile.y, 2)); 
				if (testDistance < distance) {
					distance = testDistance;
					nearestTile = tile;
				}
			}
			return nearestTile.index;
		}
	}
}

