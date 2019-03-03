package com.triplec.triway.common;

/**
 * Connect one selected place to its neighbors, and calculate the weight
 * @author Qingran Li
 */
public class SelectedPlace {
	/*----- Constant for distance calculation -----*/
	private static final int EARTH_RADIUS = 6378137;
	
	/* Private fields */
	private TriPlace p;
	private double[] weights;
	
	/**
	 * Constructor
	 * Initialize weights to empty arrays
	 * 
	 * @param place				Current place
	 * @param numOfNeighbor		The number of choices of next stop from current place
	 */
	public SelectedPlace(TriPlace place, int numOfNeighbor) {
		p = place;
		weights = new double[numOfNeighbor];
	}
	
	/**
	 * Add neighbor to array and calculate the weight of route from current place to neighbor
	 * 
	 * @param neighbor		The next stop from current place
	 * @param index			The index of neighbor in array
	 */
	public void setNeighbor(TriPlace neighbor, int index) {
		setWeight(neighbor, index);
	}
	
	/**
	 * Get current place
	 * 
	 * @return Current place
	 */
	public TriPlace getPlace() {
		return p;
	}
	
	/**
	 * Get weight of route from current place to neighbor at given index
	 * 
	 * @param index		The index of neighbor in array
	 * @return			Weight
	 */
	public double getWeight(int index) {
		return weights[index];
	}

	
	/**
	 * Set weight of route from current place to neighbor with the formula:
	 * 			weight = distance
	 * Might add more factors later
	 * 
	 * @param neighbor	The next stop from current place
	 * @param index		The index of neighbor in array
	 */
	private void setWeight(TriPlace neighbor, int index) {
		weights[index] = getDistance(neighbor);
	}
	
	/**
	 * Calculate the distance between current place and given neighbor
	 * using haversine formula
	 *
	 * @param neighbor	The next stop from current place
	 * @return			Distance in meters
	 */
	private double getDistance(TriPlace neighbor) {
		// get latitude and longitude of current place and neighbor
		double lat1 = radius(p.getLatitude());
        double lon1 = radius(p.getLongitude());
        double lat2 = radius(neighbor.getLatitude());
        double lon2 = radius(neighbor.getLongitude());
        
        // get distance in latitude and longitude
        double vLon = Math.abs(lon1 - lon2);
        double vLat = Math.abs(lat1 - lat2);
        
        // calculate diatance with haversine formula
        double h = HaverSin(vLat) + Math.cos(lat1) * Math.cos(lat2) * HaverSin(vLon);
        double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
        
        return distance;
	}
	
	/**
	 * Helper method
	 * Convert angle to radius with the formula:
	 * 			r = d * PI / 180
	 * 
	 * @param angle	Given angle
	 * @return		Radius
	 */
	private double radius(double angle) {
		return angle * Math.PI / 180;
	}
	
	/**
	 * Helper method
	 * Calculate haversine of given angle with the formula:
	 * 			hav(theta) = sin(theta / 2)^2
	 * 
	 * @param theta	The given angle
	 * @return		Haversine
	 */
	public static double HaverSin(double theta) {
        double v = Math.sin(theta / 2);
        return v * v;
    }
}
