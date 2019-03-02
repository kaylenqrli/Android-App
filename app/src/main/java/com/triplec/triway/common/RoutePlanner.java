package com.triplec.triway.common;

import java.util.ArrayList;

/**
 * Take an array list of places and give a route with minimum cost
 * @author Qingran Li
 */
public class RoutePlanner {
	/* input list of places */
	private static ArrayList<TriPlace> list;
	private static int num;
	
	/* Store places and relative weight */
	private static SelectedPlace[] mplaces;
	private static int[] index;
	
	/* Store result */
	private static Plan plan;
	private static ArrayList<TriPlace> result;
	private static double minCost = Integer.MAX_VALUE;
	
	/**
	 * Set list and get number of places
	 * 
	 * @param alist The given list of selected places
	 */
	public static void setRoutePlanner(ArrayList<TriPlace> alist) {
		list = alist;
		num = list.size();
	}
	
	/**
	 * Get the route with minimum cost (shortest distance for now)
	 * Must call setRoutePlanner() with a nonempty array list before calling this function
	 * 
	 * @return array list of places with the order such that the route has minimum cost
	 */
	public static Plan planRoute() {
		// return empty array list if input list is empty
		if(num == 0) {
			return new Plan();
		}
		
		// initialize selected places and index array
		mplaces = new SelectedPlace[num];
		index = new int[num];
		
		// set up places with weight and index array
		for(int i = 0; i < num; i++) {
			index[i] = i;
			mplaces[i] = new SelectedPlace(list.get(i), num);
			// set weight
			for(int j = 0; j < num; j++) {
				mplaces[i].setNeighbor(list.get(j), j);
			}
		}
		
		// initialize the result array list
		plan = new Plan();
		result = new ArrayList<TriPlace>(num);
		
		// iterate through all permutations, get best route with minCost
		permute(index, 0);
		for(TriPlace p: result){
			plan.addPlace(p);
		}
		
		return plan;
	}
	
	/**
	 * Iterate through all permutations, update route and minCost
	 * 
	 * @param index		Array of index of neighbors
	 * @param start		The start index to permute
	 */
	private static void permute(int[] index, int start){
		// base case
		if (start == num - 1){
            // check the cost of current route
			double currCost = getRouteCost(index);
			
			// update minCost and route
			if(currCost < minCost) {
				minCost = currCost;
				result.clear();
				for(int i = 0; i < num; i++) {
					result.add(mplaces[index[i]].getPlace());
				}
			}
        }
        
		// recursion
        for(int i = start; i < num; i++){
            swap(index, i, start);
            permute(index, start + 1);
            swap(index, start, i);
        }
        
    }
	
	/**
	 * Swap start and end index
	 * 
	 * @param index		Array of index of neighbors
	 * @param start		Start index to swap
	 * @param end		End index to swap
	 */
	private static void swap(int[] index, int start, int end) {
		int tmp = index[start];
		index[start] = index[end];
		index[end] = tmp;
	}
	
	/**
	 * Get cost of current route
	 * 
	 * @param index		Array of index of neighbors
	 * @return			Cost of current route
	 */
	private static double getRouteCost(int[] index) {
		double cost = 0;
		
		// add weights
		for(int i = 0; i < num - 1; i++) {
			cost += mplaces[index[i]].getWeight(index[i + 1]);
		}
		
		return cost;
	}
}
