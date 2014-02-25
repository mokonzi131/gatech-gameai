import java.util.*;

public class MyBot {
	private static void attack(PlanetWars pw) {
		// (1) If we currently have two fleets in flight, just do nothing.
    	int numFleets;
    	boolean attackMode = false;
    	if (pw.NumShips(1) >= pw.NumShips(2)) {
    	    numFleets = 1;
    	} else {
    	    numFleets = 3;
    	}
    	if (pw.MyFleets().size() >= numFleets) {
    	    return;
    	}
		
		// (2) Find my strongest planet.
		Planet source = null;
		double sourceScore = Double.MIN_VALUE;
		for (Planet p : pw.MyPlanets()) {
			double score = (double)p.NumShips() / (1 + p.GrowthRate());
		    if (score > sourceScore) {
			sourceScore = score;
			source = p;
		    }
		}
		
		// (3) Find the weakest enemy or neutral planet.
		Planet dest = null;
		double destScore = Double.MIN_VALUE;
		for (Planet p : pw.NotMyPlanets()) {
			double score = (double)(1 + p.GrowthRate()) / p.NumShips();
		    if (score > destScore) {
			destScore = score;
			dest = p;
		    }
		}
		
		// (4) Send half the ships from my strongest planet to the weakest
		// planet that I do not own.
		if (source != null && dest != null) {
		    int numShips = source.NumShips() / 2;
		    pw.IssueOrder(source, dest, numShips);
		}
	}
	
	public static class Investment {
		public int source;
		public int destination;
		public int force;
		
		public int timeToReturn;
	}
	
	// Make the best investment possible (greedy)
	//  where investment value is calculated by quickest return on investment
	// This is not optimal, but a decent heuristic to start with
	private static void invest(PlanetWars pw, Node node) {
		Investment bestInvestment = null;
		for (Planet source : pw.MyPlanets()) {
			for (Planet destination : pw.NotMyPlanets()) {
				int distance = pw.Distance(source.PlanetID(), destination.PlanetID());
				Snapshot snapshot = node.timeline.get(distance);
			}
		}
	}
	
	public static class Snapshot {
		public int[] planets;
		public int[] ownership;
		
		public Snapshot(int size) {
			planets = new int[size];
			ownership = new int[size];
		}
		
		private Snapshot(Snapshot other) {
			int size = other.planets.length;
			planets = new int[size];
			ownership = new int[size];
			
			for (int i = 0; i < size; ++i) {
				planets[i] = other.planets[i];
				ownership[i] = other.ownership[i];
			}
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
			Snapshot current = new Snapshot(totalPlanets);
			for (int i = 0; i < totalPlanets; ++i) {
				Planet planet = pw.GetPlanet(i);
				current.planets[i] = planet.NumShips();
				current.ownership[i] = planet.Owner();
			}
			timeline.add(current);
			
			// create remaining snapshots using knowledge of current fleets
			List<Fleet> fleets = pw.Fleets();
			while (fleets.size() > 0) {
				// create a new snapshot for next timestep
				Snapshot next = current.clone();
				
				// update planets according to growth
				for (int i = 0; i < totalPlanets; ++i) {
					if (next.ownership[i] != 0)
						next.planets[i] += growthRates[i];
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
					forces[i][next.ownership[i]] = next.planets[i];
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
					next.planets[i] = remaining;
					if (remaining > 0)
						next.ownership[i] = maxLocation;
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
			
			while (timeline.size() < 30) {
				// create a new snapshot for next timestep
				Snapshot next = current.clone();
				
				// update planets according to growth
				for (int i = 0; i < totalPlanets; ++i) {
					if (next.ownership[i] != 0)
						next.planets[i] += growthRates[i];
				}
				
				// add snapshot and update current pointer
				timeline.add(next);
				current = next;
			}
		}
	}
	
    public static void DoTurn(PlanetWars pw) {
    	// setup analysis Node
    	Node node = new Node(pw);
    	
    	// perform an investment
    	invest(pw, node);
    	
    	// TODO make further investments according to strength, advantage, or desperation
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

