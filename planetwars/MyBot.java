import java.util.*;

public class MyBot {
	private static final int LOOKAHEAD = 30;
	
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
		
		boolean done = false;
		for (Planet source : pw.MyPlanets()) {
			for (Planet destination : pw.NotMyPlanets()) {
				if (System.currentTimeMillis() - time > 950) {
					done = true;
					break;
				}
				int sid = source.PlanetID();
				int did = destination.PlanetID();
				int distance = pw.Distance(sid, did);
//				Snapshot snapshot = node.timeline.get(distance);
				Snapshot snapshot = node.timeline.get(0);
				
				// make sure we have enough forces to invest
				int sacrifice = snapshot.data[0][did] + 1; // we need at least 1 more than they will have
				if (snapshot.data[0][sid] < sacrifice) continue;
				
				// compute time it will take to net zero on investment
				//  using makeup time by solving equation: 1 + [growth_rate][time] = [sacrifice]
				int makeupTime = (sacrifice - 1);// / node.growthRates[did];
				int netTime = distance + makeupTime;
				
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
	
	public static class Snapshot {
		public static final int DATASETS = 2;
		public static final int MAX_PLANETS = 31;
		
		// data[0] = planets
		// data[1] = ownership
		public int[][] data = new int[DATASETS][MAX_PLANETS];
		
		public Snapshot() {}
				
		private Snapshot(Snapshot other) {
			for (int i = 0; i < DATASETS; ++i)
				for (int j = 0; j < MAX_PLANETS; ++j)
					data[i][j] = other.data[i][j];
		}
		
		public Snapshot clone() {
			return new Snapshot(this);
		}
	}
	
	public static class Node {
		public int totalPlanets;
		public int[] growthRates;
		public List<Snapshot> timeline;
		
		
		public Node(PlanetWars pw) {
			totalPlanets = pw.NumPlanets();
			
			// keep track of growth rates
			growthRates = new int[totalPlanets];
			for (int i = 0; i < totalPlanets; ++i)
				growthRates[i] = pw.GetPlanet(i).GrowthRate();
			
			// create first snapshot in timeline representing current game state
			timeline = new ArrayList<>();
			Snapshot current = new Snapshot();
			for (int i = 0; i < totalPlanets; ++i) {
				Planet planet = pw.GetPlanet(i);
				current.data[0][i] = planet.NumShips();
				current.data[1][i] = planet.Owner();
			}
			timeline.add(current);
			
			// create remaining snapshots using knowledge of current fleets
			List<Fleet> fleets = pw.Fleets();
			while (fleets.size() > 0) {
				// create a new snapshot for next timestep
				Snapshot next = current.clone();
				
				// update planets according to growth
				for (int i = 0; i < totalPlanets; ++i) {
					if (next.data[1][i] != 0)
						next.data[0][i] += growthRates[i];
				}
				
				// advance fleets to next timestep
				for (Fleet fleet : fleets)
					fleet.TimeStep();
				
				// compute fleet arrivals
				int[][] forces = new int[totalPlanets][3];
				for (int i = 0; i < totalPlanets; ++i)
					for (int j = 0; j < 3; ++j)
						forces[i][j] = 0;
				for (int i = 0; i < totalPlanets; ++i) {
					forces[i][next.data[1][i]] = next.data[0][i];
				}
				for (Fleet fleet : fleets) {
					if (fleet.TurnsRemaining() == 0) {
						forces[fleet.DestinationPlanet()][fleet.Owner()] += fleet.NumShips();
					}
				}
				for (int i = 0; i < totalPlanets; ++i) {
					int maxLocation = 0;
					for (int j = 1; j < 3; ++j)
						if (forces[i][j] > forces[i][maxLocation])
							maxLocation = j;
					int losses = 0;
					for (int j = 0; j < 3; ++j)
						if (j != maxLocation && forces[i][j] > losses)
							losses = forces[i][j];
					int remaining = forces[i][maxLocation] - losses;
					next.data[1][i] = remaining;
					if (remaining > 0)
						next.data[1][i] = maxLocation;
				}
				
				// formulate list of remaining fleets
				List<Fleet> remainingFleets = new ArrayList<>();
				for (Fleet fleet : fleets) {
					if (fleet.TurnsRemaining() > 0)
						remainingFleets.add(fleet);
				}
				fleets = remainingFleets;
				
				// add Snapshot and update current pointer
				timeline.add(next);
				current = next;
			}
			
			while (timeline.size() < LOOKAHEAD) {
				// create a new snapshot for next timestep
				Snapshot next = current.clone();
				
				// update planets according to growth
				for (int i = 0; i < totalPlanets; ++i) {
					if (next.data[1][i] != 0)
						next.data[0][i] += growthRates[i];
				}
				
				// add snapshot and update current pointer
				timeline.add(next);
				current = next;
			}
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

