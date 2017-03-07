package CAPRESMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Heuristic1 {
	// Class Variable
		// Single solution instance
		private class Solution{
			List<Integer> path;
			HashMap<String, List<Double>> similarityScore;
			HashMap<String, List<Double>> cost;
			List<Integer> nodeVisit;
			HashMap<String, Integer> cumEachTypeItemPicked;
			List<List<Integer>> edges;
			double cumSimScore;
			int cumCost;
			Solution(List<Integer> p, 
					HashMap<String, List<Double>> sS, 
					HashMap<String, List<Double>> c, 
					List<Integer> nV,
					HashMap<String, Integer> cETIP,
					List<List<Integer>> e,
					double cSS, int cC){
				path = new ArrayList<Integer>();
				for(int v: p) path.add(v);
				
				similarityScore = new HashMap<String, List<Double>>();
				for(String s: sS.keySet()) similarityScore.put(s, sS.get(s));
				
				cost = new HashMap<String, List<Double>>();
				for(String cst: c.keySet()) cost.put(cst, c.get(cst));
				
				nodeVisit = new ArrayList<Integer>();
				for(int n: nV) nodeVisit.add(n);
				
				cumEachTypeItemPicked = new HashMap<String, Integer>();
				for(String cE: cETIP.keySet()) cumEachTypeItemPicked.put(cE, cETIP.get(cE));
				
				edges = new ArrayList<List<Integer>>();
				for(List<Integer> ed: e) edges.add(ed);
				
				cumSimScore = cSS;
				cumCost = cC;
			}
		}
		
		// Solution containing on the vertices (first integer is the vertex and second integer is the time index)
		private HashMap<List<Integer>, List<Solution>> solOnVertex;
		
		// Solution for end vertex
		private List<Solution> finalSolution;

		// Constructor
		Heuristic1(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
				List<Integer> vertexToVisit, int permNum) {
			//Initial Solution
			Solution initSol = new Solution(
					Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
					new HashMap<String, List<Double>>(),
					new HashMap<String, List<Double>>(),
					new ArrayList<Integer>(),
					new HashMap<String, Integer>(),
					new ArrayList<List<Integer>>(),
					0.0, 0);
			solOnVertex = new HashMap<List<Integer>, List<Solution>>();
			solOnVertex.put(Arrays.asList(0, roadNetworkObj.getVertexLocation(user.getStartDest())), Arrays.asList(initSol));
			// Iterate over all time steps and nodes
			for(int time = 0; time <= user.getTimeConstraint(); time++){
				for(int onVertex: userG.vertexList()){
					 int fromVertexIndex = 0;
					 List<Integer> fromVertexList = new ArrayList<Integer>();
					 List<Integer> timeToLook = new ArrayList<Integer>();
					 for(int fromVertex: userG.edgeList().get(onVertex)){
						if(time - userG.edgeWeightList().get(onVertex).get(fromVertexIndex) >= 0 &&
							time + roadNetworkObj.getShortestPath(
									onVertex, roadNetworkObj.getVertexLocation(user.getEndDest())) 
									<= user.getTimeConstraint() &&
							solOnVertex.containsKey(
									Arrays.asList(
											time - userG.edgeWeightList().get(onVertex).get(fromVertexIndex), 
											fromVertex))){
							 fromVertexList.add(fromVertex);
							 timeToLook.add(time - userG.edgeWeightList().get(onVertex).get(fromVertexIndex));
						}	 
						fromVertexIndex ++;
					 }
					 if(fromVertexList.size() != 0)
						// If the vertex has items to pick
						if(userI.getItemValues().get(onVertex).size() != 0)
							if(permNum <= 2)
								computeScoreItemBeforeSimScore(onVertex, fromVertexList, timeToLook, user, userI, vertexToVisit, time, permNum);
							else
								computeScoreSimScoreBeforeItem(onVertex, fromVertexList, timeToLook, user, userI, vertexToVisit, time, permNum);

						// If the vertex has no item to pick
						else
							compSolNoItemInVertex(onVertex, fromVertexList, timeToLook, user, vertexToVisit, time, permNum);
				}
			}
			
			//Get the final solution
			finalSolution = new ArrayList<Solution>();
			List<Solution> allFinalSolution = new ArrayList<Solution>();
			for(int time = 0; time < user.getTimeConstraint(); time ++){
				if(solOnVertex.containsKey(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest())))){
					for(Solution S: solOnVertex.get(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest())))){
						allFinalSolution.add(S);
					}
				}
			}
			allFinalSolution = sortOnPerm(allFinalSolution, permNum, user);
			for(int index = 0; index < Math.min(allFinalSolution.size(), user.getNumRecommendation()); index++){
				finalSolution.add(allFinalSolution.get(index));
			}
		}

		// Comparator method 1
		private int compareItemPicked(HashMap<String, Integer> o1, HashMap<String, Integer> o2, UserClass user){
			double score1 = 0.0;
			for(String str: user.getNumItems().keySet())
				if(o1.containsKey(str))
					score1 += (double) o1.get(str) / (double) user.getNumItems().get(str);
			double score2 = 0.0;
			for(String str: user.getNumItems().keySet())
				if(o2.containsKey(str))
					score2 += (double) o2.get(str) / (double) user.getNumItems().get(str);
			return score1 > score2 ? - 1:
				   score1 < score2 ? 1:
				   0;
		}
		
		// Comparator method 2
		private int compareNumNodeVisit(List<Integer> o1, List<Integer> o2){
			return o1.size() > o2.size() ? -1:
				   o1.size() < o2.size() ? 1:
				   0;
		}
		
		// Comparator method 3
		private int compareSimScore(double o1, double o2){
			return o1 > o2 ? -1:
				   o1 < o2 ? 1:
				   0;
		}

		// Compute solution based on permutation if the vertex does not have any item to collected
		private void compSolNoItemInVertex(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				 List<Integer> vertexToVisit, int currentTime, int permNum){
			List<Solution> newSolutions = new ArrayList<Solution>();
			int index = 0;
			for(int fVertex: fromVertex){
				for(Solution S: solOnVertex.get(Arrays.asList(timeToLook.get(index),fVertex))){
					Solution solToAdd = new Solution(
							S.path, 
							S.similarityScore,
							S.cost, 
							S.nodeVisit, 
							S.cumEachTypeItemPicked,
							S.edges,
							S.cumSimScore, 
							S.cumCost);
					if(solToAdd.edges.contains(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex))) continue;
					solToAdd.edges.add(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex));
					solToAdd.path.add(onVertex);
					if(vertexToVisit.contains(onVertex) && !solToAdd.nodeVisit.contains(onVertex)){
						solToAdd.nodeVisit.add(onVertex);
					}
					newSolutions.add(solToAdd);
				}
				index ++;
			}
			// sort the new solutions
			newSolutions = sortOnPerm(newSolutions, permNum, user);
			List<Solution> solForVertex = new ArrayList<Solution>();
			for(index = 0; index < Math.min(user.getNumRecommendation(), newSolutions.size()); index++){
				solForVertex.add(newSolutions.get(index));
			}
			solOnVertex.put(Arrays.asList(currentTime, onVertex), solForVertex);
		}
		
		// Sort the solutions based on ordering in the permutation
		private List<Solution> sortOnPerm(List<Solution> newSolutions, int permNum, UserClass user) {
			if (permNum == 0)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return itemComparison != 0 ? itemComparison:
							   nodeComparison != 0 ? nodeComparison:
							   simScoreComparison;
					}
				});
			else if (permNum == 1)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return itemComparison != 0 ? itemComparison:
							   simScoreComparison != 0 ? simScoreComparison:
							   nodeComparison;
					}
				});
			else if (permNum == 2)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return nodeComparison != 0 ? nodeComparison:
							   itemComparison != 0 ? itemComparison:
							   simScoreComparison;
					}
				});
			else if (permNum == 3)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return nodeComparison != 0 ? nodeComparison:
							   simScoreComparison != 0 ? simScoreComparison:
							   itemComparison;
					}
				});
			else if (permNum == 4)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return simScoreComparison != 0 ? simScoreComparison:
							   nodeComparison != 0 ? nodeComparison:
							   itemComparison;
					}
				});
			else if (permNum == 5)
				Collections.sort(newSolutions, new Comparator<Solution>(){
					@Override
					public int compare(Solution o1, Solution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return simScoreComparison != 0 ? simScoreComparison:
							   itemComparison != 0 ? itemComparison:
							   nodeComparison;
					}
				});
			return newSolutions;
		}

		// Sort cost in non increasing ratio and order itemName, cost and simScore accordingly
		private void fourListSortOnRatio(List<String> itemName, List<Double> cost, List<Double> simScore, List<Double> ratio) {
			for(int i = 1; i < ratio.size(); i++){
				int j = i;
				while(ratio.get(j-1) < ratio.get(j)){
					double dummyR = ratio.get(j);
					ratio.set(j, ratio.get(j-1));
					ratio.set(j-1, dummyR);
					double dummy1 = cost.get(j);
					cost.set(j, cost.get(j-1));
					cost.set(j-1, dummy1);
					double dummy2 = simScore.get(j);
					simScore.set(j, simScore.get(j-1));
					simScore.set(j-1, dummy2);
					String dummy3 = itemName.get(j);
					itemName.set(j, itemName.get(j-1));
					itemName.set(j-1, dummy3);
					j --;
					if(j == 0) break;
				}
			}
			
		}
		
		// Update solution when an item is picked up
		private void includeSolution(Solution solToAdd, String itemN, Double cost, Double score) {
			solToAdd.cumCost += cost;
			solToAdd.cumSimScore += score;
			
			int totalItem = 0;
			if(solToAdd.cumEachTypeItemPicked.containsKey(itemN)) 
				totalItem = solToAdd.cumEachTypeItemPicked.get(itemN);
			solToAdd.cumEachTypeItemPicked.put(itemN, totalItem + 1);
			
			List<Double> itemSimScore = new ArrayList<Double>();
			if(solToAdd.similarityScore.containsKey(itemN))
				for(double s: solToAdd.similarityScore.get(itemN))
					itemSimScore.add(s);
			itemSimScore.add(score);
			solToAdd.similarityScore.put(itemN, itemSimScore);
			
			List<Double> itemCost = new ArrayList<Double>();
			if(solToAdd.cost.containsKey(itemN))
				for(double c: solToAdd.cost.get(itemN))
					itemCost.add(c);
			itemCost.add(cost);
			solToAdd.cost.put(itemN, itemCost);
			
		}
		
		// Replace solution 
		private void replaceSolution(Solution solToAdd, String itemN, List<String> itemName, List<Double> simScore,
				List<Double> cost, ItemPruning userI, List<Double> simScoreForSimScoreOrder,
				List<Double> costForSimScoreOrder, List<String> itemNameForSimScoreOrder, 
				int item, UserClass user, boolean flag) {
			List<Double> currentScoreList = new ArrayList<Double>();
			List<Double> currentCostList = new ArrayList<Double>();
			
			for(double x: solToAdd.cost.get(itemN)) currentCostList.add(x);
			for(double x: solToAdd.similarityScore.get(itemN))  currentScoreList.add(x);
			
			String itemNameToInclude ="";
			double simScoreToInclude = 0.0;
			double costToInclude = -1;
			userI.twoArrayListSort(currentScoreList, currentCostList);
			for(int pos = 0; pos < solToAdd.cumEachTypeItemPicked.get(itemN); pos ++){
				if(solToAdd.cumCost -  currentCostList.get(pos) + costForSimScoreOrder.get(item) > user.getBudgetConstraint()){
					if(!itemNameToInclude.equals("")) continue;
					if(flag){
						itemNameToInclude = itemNameForSimScoreOrder.get(item);
						simScoreToInclude = simScoreForSimScoreOrder.get(item);
						costToInclude = costForSimScoreOrder.get(item);
					}
					continue;
				}
				if(currentScoreList.get(pos) > simScoreForSimScoreOrder.get(item)){
					if(!itemNameToInclude.equals("")) break;
					if(flag){
						itemNameToInclude = itemNameForSimScoreOrder.get(item);
						simScoreToInclude = simScoreForSimScoreOrder.get(item);
						costToInclude = costForSimScoreOrder.get(item);
					}
					break;
				}
				solToAdd.cumCost += (costForSimScoreOrder.get(item) - currentCostList.get(pos));
				solToAdd.cumSimScore += (simScoreForSimScoreOrder.get(item) - currentScoreList.get(pos));
				currentScoreList.set(pos, simScoreForSimScoreOrder.get(item));
				solToAdd.similarityScore.put(itemN, currentScoreList);
				currentCostList.set(pos, costForSimScoreOrder.get(item));
				solToAdd.cost.put(itemN, currentCostList);
				break;
			}
			if(costToInclude != -1){
				itemName.add(itemNameToInclude);
				simScore.add(simScoreToInclude);
				cost.add(costToInclude);
			}
		}
				
		// Compute solution if item score is before similarity score
		private void computeScoreItemBeforeSimScore(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum){
			List<Solution> newSolution = new ArrayList<Solution>();
			int index = 0;
			for(int vertex: fromVertex){
				for(Solution S: solOnVertex.get(Arrays.asList(timeToLook.get(index),vertex))){
					Solution solToAdd = new Solution(
							S.path, 
							S.similarityScore,
							S.cost, 
							S.nodeVisit, 
							S.cumEachTypeItemPicked,
							S.edges,
							S.cumSimScore, 
							S.cumCost);
					if(solToAdd.edges.contains(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex))) continue;
					solToAdd.edges.add(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex));
					solToAdd.path.add(onVertex);
					if(vertexToVisit.contains(onVertex) && !solToAdd.nodeVisit.contains(onVertex)){
						solToAdd.nodeVisit.add(onVertex);
					}
					// Get the items on the vertex
					List<String> itemName = new ArrayList<String>();
					List<String> itemNameForSimScoreOrder = new ArrayList<String>();
					List<Double> cost = new ArrayList<Double>();
					List<Double> costForSimScoreOrder = new ArrayList<Double>();
					List<Double> simScore = new ArrayList<Double>();
					List<Double> simScoreForSimScoreOrder = new ArrayList<Double>();
					List<Double> itemScoreCostRatio = new ArrayList<Double>();
					for(String str: userI.getItemValues().get(onVertex).keySet()){
						for(List<Double> costScore: userI.getItemValues().get(onVertex).get(str)){
							itemName.add(str);
							simScore.add(costScore.get(0));
							cost.add(costScore.get(1));
							itemScoreCostRatio.add((1.0 / (double) user.getNumItems().get(str)) / costScore.get(1));
						}
					}
					// Sort the list of items by non increasing ratio
					fourListSortOnRatio(itemName, cost, simScore, itemScoreCostRatio);
					// Pick items to maximize number of item pick up score
					for(int item = 0; item < itemName.size(); item++){
						String itemN = itemName.get(item);
						if(user.getNumItems().get(itemN) == solToAdd.cumEachTypeItemPicked.get(itemN)){
							itemNameForSimScoreOrder.add(itemName.get(item));
							simScoreForSimScoreOrder.add(simScore.get(item));
							costForSimScoreOrder.add(cost.get(item));
							continue;
						}
						if(solToAdd.cumCost + cost.get(item) > user.getBudgetConstraint()){
							itemNameForSimScoreOrder.add(itemName.get(item));
							simScoreForSimScoreOrder.add(simScore.get(item));
							costForSimScoreOrder.add(cost.get(item));
							continue;
						}
						
						// pick the item and add the values to the current solution
						includeSolution(solToAdd, itemN, cost.get(item), simScore.get(item));
					}
					
					// compute ratio for ordering by similarity score
					List<Double> simScoreCostRatio = new ArrayList<Double>();
					int i = 0;
					for(String str: itemNameForSimScoreOrder){
						simScoreCostRatio.add((1.0 / (double) user.getNumItems().get(str)) / costForSimScoreOrder.get(i));
						i ++;
					}
					
					// Sort the list of items by non increasing ratio
					fourListSortOnRatio(itemNameForSimScoreOrder, costForSimScoreOrder, simScoreForSimScoreOrder, simScoreCostRatio);
					
					// Pick/replace items to maximize similarity score
					for(int item = 0; item < simScoreCostRatio.size(); item++){
						String itemN = itemNameForSimScoreOrder.get(item);
						if(user.getNumItems().get(itemN) == solToAdd.cumEachTypeItemPicked.get(itemN) && 
								solToAdd.cumEachTypeItemPicked.containsKey(itemN))
							// Try and replace
							replaceSolution(solToAdd, itemN, itemName, simScore, cost, userI, simScoreForSimScoreOrder, 
									costForSimScoreOrder, itemNameForSimScoreOrder, item, user, false);
						else{
							if(solToAdd.cumCost + cost.get(item) > user.getBudgetConstraint() && 
									solToAdd.cumEachTypeItemPicked.containsKey(itemN))
								// Try and replace
								replaceSolution(solToAdd, itemN, itemName, simScore, cost, userI, simScoreForSimScoreOrder, 
										costForSimScoreOrder, itemNameForSimScoreOrder, item, user, false);
							else if(solToAdd.cumCost + cost.get(item) <= user.getBudgetConstraint())
								// Pick item
								includeSolution(solToAdd, itemN, costForSimScoreOrder.get(item), simScoreForSimScoreOrder.get(item));
						}
					}
					newSolution.add(solToAdd);
				}
				index ++;
			}
			// sort the new solutions
			newSolution = sortOnPerm(newSolution, permNum, user);
			List<Solution> solForVertex = new ArrayList<Solution>();
			for(index = 0; index < Math.min(user.getNumRecommendation(), newSolution.size()); index++){
				solForVertex.add(newSolution.get(index));
			}
			solOnVertex.put(Arrays.asList(currentTime, onVertex), solForVertex);
		}

		// Compute solution if similarity score is before item score
		private void computeScoreSimScoreBeforeItem(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum){
			List<Solution> newSolution = new ArrayList<Solution>();
			int index = 0;
			for(int vertex: fromVertex){
				for(Solution S: solOnVertex.get(Arrays.asList(timeToLook.get(index),vertex))){
					Solution solToAdd = new Solution(
							S.path, 
							S.similarityScore,
							S.cost, 
							S.nodeVisit, 
							S.cumEachTypeItemPicked,
							S.edges,
							S.cumSimScore, 
							S.cumCost);
					if(solToAdd.edges.contains(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex))) continue;
					solToAdd.edges.add(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex));
					solToAdd.path.add(onVertex);
					if(vertexToVisit.contains(onVertex) && !solToAdd.nodeVisit.contains(onVertex)){
						solToAdd.nodeVisit.add(onVertex);
					}
					// Get the items on the vertex
					List<String> itemName = new ArrayList<String>();
					List<String> itemNameForSimScoreOrder = new ArrayList<String>();
					List<Double> cost = new ArrayList<Double>();
					List<Double> costForSimScoreOrder = new ArrayList<Double>();
					List<Double> simScore = new ArrayList<Double>();
					List<Double> simScoreForSimScoreOrder = new ArrayList<Double>();
					List<Double> simScoreCostRatio = new ArrayList<Double>();
					for(String str: userI.getItemValues().get(onVertex).keySet()){
						for(List<Double> costScore: userI.getItemValues().get(onVertex).get(str)){
							itemNameForSimScoreOrder.add(str);
							simScoreForSimScoreOrder.add(costScore.get(0));
							costForSimScoreOrder.add(costScore.get(1));
							simScoreCostRatio.add((1.0 / (double) user.getNumItems().get(str)) / costScore.get(1));
						}
					}
					// Sort the list of items by non increasing ratio
					fourListSortOnRatio(itemNameForSimScoreOrder, costForSimScoreOrder, simScoreForSimScoreOrder, simScoreCostRatio);
					
					// Pick/replace items to maximize similarity score
					int  pos = 0;
					for(int item = 0; item < simScoreCostRatio.size(); item++){
						String itemN = itemNameForSimScoreOrder.get(item);
						if(user.getNumItems().get(itemN) == solToAdd.cumEachTypeItemPicked.get(itemN) &&
								solToAdd.cumEachTypeItemPicked.containsKey(itemN))
							// Try and replace
							replaceSolution(solToAdd, itemN, itemName, simScore, cost, userI, simScoreForSimScoreOrder, 
									costForSimScoreOrder, itemNameForSimScoreOrder, item, user, true);
						else{
							if(solToAdd.cumCost + costForSimScoreOrder.get(item) > user.getBudgetConstraint() &&
									solToAdd.cumEachTypeItemPicked.containsKey(itemN))
								// Try and replace
								replaceSolution(solToAdd, itemN, itemName, simScore, cost, userI, simScoreForSimScoreOrder, 
										costForSimScoreOrder, itemNameForSimScoreOrder, item, user, true);
								
							else if(solToAdd.cumCost + costForSimScoreOrder.get(item) < user.getBudgetConstraint())
								// Pick item
								includeSolution(solToAdd, itemN, costForSimScoreOrder.get(item), simScoreForSimScoreOrder.get(item));
						}
					}
					
					// compute ratio for ordering by similarity score
					List<Double> itemScoreCostRatio = new ArrayList<Double>();
					pos = 0;
					for(String str: itemName){
						itemScoreCostRatio.add((1.0 / (double) user.getNumItems().get(str)) / cost.get(pos));
						pos ++;
					}
					
					// Sort the list of items by non increasing ratio
					fourListSortOnRatio(itemName, cost, simScore, itemScoreCostRatio);
					
					// Pick items to maximize number of item pick up score
					for(int item = 0; item < itemName.size(); item++){
						String itemN = itemName.get(item);
						if(user.getNumItems().get(itemN) == solToAdd.cumEachTypeItemPicked.get(itemN)) continue;
						if(solToAdd.cumCost + cost.get(item) > user.getBudgetConstraint()) continue;	
						// pick the item and add the values to the current solution
						includeSolution(solToAdd, itemN, cost.get(item), simScore.get(item));
					}
					newSolution.add(solToAdd);
				}
				index ++;
			}
			// sort the new solutions
			newSolution = sortOnPerm(newSolution, permNum, user);
			List<Solution> solForVertex = new ArrayList<Solution>();
			for(index = 0; index < Math.min(user.getNumRecommendation(), newSolution.size()); index++){
				solForVertex.add(newSolution.get(index));
			}
			solOnVertex.put(Arrays.asList(currentTime, onVertex), solForVertex);
		}

		// Print the final solution
		public void prtinSolution(GraphPruning userG){
			int k = 1;
			for(Solution S: finalSolution){
				List<Integer> pathSol = S.path;
				int time = 0;
				for(int index = 1; index < pathSol.size(); index++){
					time += userG.edgeWeightList().get(pathSol.get(index)).
							get(userG.edgeList().get(pathSol.get(index)).indexOf(pathSol.get(index - 1)));
				}
				System.out.println("-----------------------------------------------------------------------");
				System.out.println("Recommendation " + k);
				System.out.println("Time Taken                : " + time);
				System.out.println("Path                      : " + S.path);
				System.out.println("Similarity Score          : " + S.similarityScore);
				System.out.println("Cost                      : " + S.cost);
				System.out.println("Node Visited              : " + S.nodeVisit);
				System.out.println("Cum Each Type Item Picked : " + S.cumEachTypeItemPicked);
				System.out.println("Cum Sim Score             : " + S.cumSimScore);
				System.out.println("Cum Cost                  : " + S.cumCost);
				k ++;
			}
		}
}
