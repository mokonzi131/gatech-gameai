import java.util.*;

public class MyBot {
	private static void defend(PlanetWars pw) {
		// TODO
	}
	
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
	
	// Make the best investment possible (greedy)
	private static void invest(PlanetWars pw) {
		// TODO change this logic...
		// // TODO if (surplus) capture
		// // TODO value = cost + power + distance (risk)
		// // TODO fortify frontier according to values
	}
	
	public static class Snapshot {
		public int[] planets;
		public int[] ownership;
		
		public Snapshot(int size) {
			planets = new int[size];
			ownership = new int[size];
		}
	}
	
	public static class Node {
		public final int totalPlanets;
		public final int[] growthRates;
		public List<Snapshot> timeline;
		
		public Node(PlanetWars pw) {
			totalPlanets = pw.NumPlanets();
			
			growthRates = new int[totalPlanets];
			for (int i = 0; i < totalPlanets; ++i)
				growthRates[i] = pw.GetPlanet(i).GrowthRate();
			
			timeline = new ArrayList<>();
			Snapshot snapshot = new Snapshot(totalPlanets);
			for (int i = 0; i < totalPlanets; ++i) {
				Planet planet = pw.GetPlanet(i);
				snapshot.planets[i] = planet.NumShips();
				int owner = planet.Owner();
				snapshot.ownership[i] = (owner == 2) ? -1 : owner;
			}
			timeline.add(snapshot);
			
			List<Fleet> fleets = pw.Fleets();
		}
	}
	
    public static void DoTurn(PlanetWars pw) {
    	// setup analysis Node
    	Node node = new Node(pw);
    	
    	// perform an investment
    	invest(pw);
    	
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

