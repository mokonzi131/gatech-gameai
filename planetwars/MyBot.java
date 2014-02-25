import java.util.*;

public class MyBot {
	public static class Investment {
		public int source;
		public int destination;
		public int force;
		
		public int timeToReturn;
	}
	
	// Make the best investment possible (greedy)
	//  where investment value is calculated by quickest return on investment
	// This is not optimal, but a decent heuristic to start with
	private static void invest(PlanetWars pw, Node node, long time) {
		Investment best = null;
		List<Fleet> fleets = pw.MyFleets();
		
		boolean done = false;
		for (Planet source : pw.MyPlanets()) {
			for (Planet destination : pw.NotMyPlanets()) {
				// make sure we don't go over-time
				if (System.currentTimeMillis() - time > 950) {
					done = true;
					break;
				}
				
				int sid = source.PlanetID();
				int did = destination.PlanetID();
				for (Fleet fleet : fleets)
					if (fleet.DestinationPlanet() == did) continue;
				
//				int distance = Math.min(pw.Distance(sid, did), Node.LOOKAHEAD);
				int distance = pw.Distance(sid, did);
				
				// make sure we have enough forces to invest
				int sacrifice = node.timeline[0][0][did] + 1;
				if (node.timeline[0][0][sid] < sacrifice) continue;
//				int sacrifice = node.timeline[distance][0][did] + 1; // we need at least 1 more than they will have
//				if (node.timeline[distance][0][sid] < sacrifice) continue;
				
				// compute time it will take to net zero on investment
				//  using makeup time by solving equation: 1 + [growth_rate][time] = [sacrifice]
//				int makeupTime = node.timeline[distance][0][did] / node.growthRates[did];
				int netTime = distance;// + makeupTime;
				
				// figure out new best investment
				if (best == null || netTime < best.timeToReturn) {
					best = new Investment();
					best.source = sid;
					best.destination = did;
					best.force = sacrifice;
					best.timeToReturn = netTime;
				}
			}
			if (done) break;
		}
		
		if (best != null)
			pw.IssueOrder(best.source, best.destination, best.force);
	}
	
	public static class Node {
		private static final int LOOKAHEAD = 5;
		public static final int DATASETS = 2;
		public static final int MAX_PLANETS = 31;
		private static final int TEAMS = 3;
		// timeline[x] = snapshot "x" timesteps ahead of current iteration
		// timeline[x][0] = number of ships
		// timeline[x][1] = ownership
		// timeline[x][y][z] = specific planet
		
		public int totalPlanets;
		public int[] growthRates = new int[MAX_PLANETS];
		public int[][][] timeline = new int[LOOKAHEAD][DATASETS][MAX_PLANETS];
		private int[][] forces = new int[MAX_PLANETS][TEAMS];
		
		public Node(PlanetWars pw) {
			// find total planets in the current map, assuming this is never > 30
			totalPlanets = pw.NumPlanets();
			
			// keep track of growth rates
			for (int i = 0; i < totalPlanets; ++i)
				growthRates[i] = pw.GetPlanet(i).GrowthRate();
			
			// create first snapshot in timeline representing current game state
			int index = 0;
			for (int i = 0; i < totalPlanets; ++i) {
				Planet planet = pw.GetPlanet(i);
				timeline[index][0][i] = planet.NumShips();
				timeline[index][1][i] = planet.Owner();
			}
			
			// create remaining snapshots using knowledge of current fleets
//			List<Fleet> fleets = pw.Fleets();
//			while (fleets.size() > 0) {
				// point to next timestep and copy values from prev timestep
//				++index;
//				for (int i = 0; i < DATASETS; ++i)
//					for (int j = 0; j < MAX_PLANETS; ++j)
//						timeline[index][i][j] = timeline[index-1][i][j];
				
				// update planets according to growth, only grow non-neutral planets
//				for (int i = 0; i < totalPlanets; ++i) {
//					if (timeline[index][1][i] != 0)
//						timeline[index][0][i] += growthRates[i];
//				}
				
				// advance fleets to next timestep
//				for (Fleet fleet : fleets)
//					fleet.TimeStep();
				
//				// to compute fleet arrivals, first clear forces array
//				for (int i = 0; i < totalPlanets; ++i)
//					for (int j = 0; j < TEAMS; ++j)
//						forces[i][j] = 0;
//				
//				// note current planet occupations
//				for (int i = 0; i < totalPlanets; ++i)
//					forces[i][timeline[index][1][i]] += timeline[index][0][i];
//				
				// note all fleets that are currently arriving, eliminate them from new list of fleets
//				List<Fleet> remainingFleets = new ArrayList<>();
//				for (Fleet fleet : fleets) {
//					if (fleet.TurnsRemaining() == 0)
//						forces[fleet.DestinationPlanet()][fleet.Owner()] += fleet.NumShips();
//					else
//						remainingFleets.add(fleet);
//				}
//				fleets = remainingFleets;
				
//				// assign new occupant according to battle rules
//				for (int i = 0; i < totalPlanets; ++i) {
//					int maxLocation = 0;
//					for (int j = 1; j < TEAMS; ++j)
//						if (forces[i][j] > forces[i][maxLocation])
//							maxLocation = j;
//					int losses = 0;
//					for (int j = 0; j < TEAMS; ++j)
//						if (j != maxLocation && forces[i][j] > losses)
//							losses = forces[i][j];
//					int remaining = forces[i][maxLocation] - losses;
//					timeline[index][0][i] = remaining;
//					if (remaining > 0)
//						timeline[index][1][i] = maxLocation;
//				}
//			}
			
			// extend the rest of the timeline out to lookahead limit
//			while (index < LOOKAHEAD) {
//				++index;
//				for (int i = 0; i < DATASETS; ++i)
//					for (int j = 0; j < totalPlanets; ++j)
//						timeline[index][i][j] = timeline[index-1][i][j];
//				
//				for (int i = 0; i < totalPlanets; ++i) {
//					if (timeline[index][1][i] != 0)
//						timeline[index][0][i] += growthRates[i];
//				}
//			}
		}
	}
	
    public static void DoTurn(PlanetWars pw) {
    	long time = System.currentTimeMillis();
    	if (!pw.IsAlive(1) || pw.MyPlanets().size() <= 0) return;
    	
    	// setup analysis Node
    	Node node = new Node(pw);
    	
    	// perform an investment
    	invest(pw, node, time);
    }

    /* LEAVE MAIN METHOD ALONE */
    public static void main(String[] args) {
	String line = "";
	String message = "";
	int c;
	try {
	    while ((c = System.in.read()) >= 0) {
		switch (c) {
		case '\n':
		    if (line.equals("go")) {
			PlanetWars pw = new PlanetWars(message);
			DoTurn(pw);
		        pw.FinishTurn();
			message = "";
		    } else {
			message += line + "\n";
		    }
		    line = "";
		    break;
		default:
		    line += (char)c;
		    break;
		}
	    }
	} catch (Exception e) {
	    // Owned.
	}
    }
}

