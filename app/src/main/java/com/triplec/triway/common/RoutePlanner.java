package com.triplec.triway.common;

import java.util.List;



/**
 * Plan a route with min cost for a given list of places
 */
public class RoutePlanner {
	/* input list of places */
	private List<TriPlace> list;
	/* selected places and relative weight */
	private SelectedPlace[] mplaces;
	private int[] index;
	/* result */
	private TriPlan plan;
	private TriPlace[] result;
	private double minCost = Integer.MAX_VALUE;

	/**
	 * Initialize list of places
	 *
	 * @param alist The given list of selected places
	 */
	public RoutePlanner(List<TriPlace> alist) {
		list = alist;
	}


	/**
	 * Get the route with minimum cost (shortest distance for now)
	 *
	 * @return reordered array list of places
	 */
	public TriPlan planRoute() {
		// return empty array list if input list is empty
		int num = list.size();
		if(num == 0) {
			TriPlan.TriPlanBuilder myBuilder = new TriPlan.TriPlanBuilder();
			return myBuilder.buildPlan();
		}

		// initialize selected places, index array, and result
		mplaces = new SelectedPlace[num];
		index = new int[num];
		result = new TriPlace[num];

		// set up places with weight and index array
		for(int i = 0; i < num; i++) {
			index[i] = i;
			mplaces[i] = new SelectedPlace(list.get(i), num);
			// set weight
			for(int j = 0; j < num; j++) {
				mplaces[i].setNeighbor(list.get(j), j);
			}
		}

		// iterate through all permutations, get best route with minCost
		permute(index, 0);

		// build the resulting triplan
		TriPlan.TriPlanBuilder myBuilder = new TriPlan.TriPlanBuilder();
		for(int i = 0; i < num; i++) {
			myBuilder.addPlace(result[i]);
		}
		return myBuilder.buildPlan();
	}

	/**
	 * Iterate through all permutations, update route and minCost
	 *
	 * @param index		Array of index of neighbors
	 * @param start		The start index to permute
	 */
	private void permute(int[] index, int start){
		int num = list.size();

		// base case
		if (start == num - 1){
			// check the cost of current route
			double currCost = getRouteCost(index);

			// update minCost and route
			if(currCost < minCost) {
				minCost = currCost;
				for(int i = 0; i < num; i++) {
					result[i] = list.get(index[i]);
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
	private double getRouteCost(int[] index) {
		int num = list.size();
		double cost = 0;
		// add weights
		for(int i = 0; i < num - 1; i++) {
			cost += mplaces[index[i]].getWeight(index[i + 1]);
		}
		return cost;
	}
}