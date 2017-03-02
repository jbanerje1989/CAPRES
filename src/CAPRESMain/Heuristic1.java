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
			HashMap<String, Integer> itemsPicked;
			HashMap<String, List<Double>> similarityScore;
			HashMap<String, List<Integer>> cost;
			List<Integer> nodeVisit;
			List<Integer> cumEachTypeItemPicked;
			List<List<Integer>> edges;
			double cumSimScore;
			int cumItemsPicked;
			int cumCost;
			Solution(List<Integer> p, 
					HashMap<String, Integer> iP, 
					HashMap<String, List<Double>> sS, 
					HashMap<String, List<Integer>> c, 
					List<Integer> nV,
					List<Integer> cETIP,
					List<List<Integer>> e,
					double cSS, int cIP, int cC){
				path = new ArrayList<Integer>();
				for(int v: p) path.add(v);
				
				itemsPicked = new HashMap<String, Integer>();
				for(String i: iP.keySet()) itemsPicked.put(i, iP.get(i));
				
				similarityScore = new HashMap<String, List<Double>>();
				for(String s: sS.keySet()) similarityScore.put(s, sS.get(s));
				
				cost = new HashMap<String, List<Integer>>();
				for(String cst: c.keySet()) cost.put(cst, c.get(cst));
				
				nodeVisit = new ArrayList<Integer>();
				for(int n: nV) nodeVisit.add(n);
				
				cumEachTypeItemPicked = new ArrayList<Integer>();
				for(int cE: cETIP) cumEachTypeItemPicked.add(cE);
				
				edges = new ArrayList<List<Integer>>();
				for(List<Integer> ed: e) edges.add(ed);
				
				cumSimScore = cSS;
				cumItemsPicked = cIP;
				cumCost = cC;
			}
		}
		
		// Solution containing on the vertices (first integer is the vertex and second integer is the time index)
		HashMap<List<Integer>, List<Solution>> solOnVertex;
		
		// Constructor
		Heuristic1(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
				List<Integer> vertexToVisit, int permNum) {
			//Initial Solution
			Solution initSol = new Solution(
					Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
					new HashMap<String, Integer>(),
					new HashMap<String, List<Double>>(),
					new HashMap<String, List<Integer>>(),
					new ArrayList<Integer>(),
					new ArrayList<Integer>(Collections.nCopies(user.getNumItems().size(), 0)),
					new ArrayList<List<Integer>>(),
					0.0, 0, 0);
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
						 findSolution(onVertex, fromVertexList, timeToLook, permNum, user, userI, vertexToVisit, time);
				}
			}
		}
		
		// Get the heuristic score update based on requested permutation
		private void findSolution( int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, int permNum, 
				UserClass user, ItemPruning userI, List<Integer> vertexToVisit, int currentTime){
			if(permNum == 0)
				simItemNodeHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
				
			else if(permNum == 1)
				simNodeItemHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
			
			else if(permNum == 2)
				nodeSimItemHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
			
			else if(permNum == 3)
				nodeItemSimHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
			
			else if(permNum == 4)
				itemSimNodeHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
			
			else if(permNum == 5)
				itemNodeSimHeu(onVertex, fromVertex, timeToLook, user, userI, vertexToVisit, currentTime, permNum);
				
		}

		// Comparator method 1
		private int compareItemPicked(List<Integer> o1, List<Integer> o2, UserClass user){
			double score1 = 0;
			for(int index = 0; index < user.getNumItems().size(); index++){
				score1 += (double) o1.get(index) / (double) user.getNumItems().get(index);
			}
			double score2 = 0;
			for(int index = 0; index < user.getNumItems().size(); index++){
				score2 += (double) o2.get(index) / (double) user.getNumItems().get(index);
			}
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
							S.itemsPicked, 
							S.similarityScore,
							S.cost, 
							S.nodeVisit, 
							S.cumEachTypeItemPicked,
							S.edges,
							S.cumSimScore, 
							S.cumItemsPicked, 
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
			List<Solution> solForVertex = new ArrayList<Solution>();
			for(index = 0; index < Math.min(user.getNumRecommendation(), newSolutions.size()); index++){
				solForVertex.add(newSolutions.get(index));
			}
			solOnVertex.put(Arrays.asList(currentTime, onVertex), solForVertex);
		}
		
		// Compute Next solution based on permutation in order items picked up, number of vertices in query list visited and simScore
		private void itemNodeSimHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}
		}

		// Compute Next solution based on permutation in order items picked up, simScore and number of vertices in query list visited
		private void itemSimNodeHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}
		}

		// Compute Next solution based on permutation in order number of vertices in query list visited, items picked up and simScore 
		private void nodeItemSimHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}
		}

		// Compute Next solution based on permutation in order number of vertices in query list visited, simScore and items picked up 
		private void nodeSimItemHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}
		}

		// Compute Next solution based on permutation in order simScore, number of vertices in query list visited and items picked up
		private void simNodeItemHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}	
		}

		// Compute Next solution based on permutation in order simScore, items picked up and number of vertices in query list visited
		private void simItemNodeHeu(int onVertex, List<Integer> fromVertex, List<Integer> timeToLook, UserClass user, 
				ItemPruning userI, List<Integer> vertexToVisit, int currentTime, int permNum) {
			// If the vertex has no item to pick
			if(userI.getItemValues().size() == 0){
				
			}
			// If the vertex has items to pick
			else{
				compSolNoItemInVertex(onVertex, fromVertex, timeToLook, user, vertexToVisit, currentTime, permNum);
			}	
		}
}
