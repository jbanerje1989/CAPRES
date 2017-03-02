package CAPRESMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DynamicProgram {
	// Class Variable
		// Single solution instance
		private class Solution{
			List<Integer> path;
			HashMap<String, Integer> itemsPicked;
			HashMap<String, List<Double>> similarityScore;
			HashMap<String, List<Integer>> cost;
			List<Integer> nodeVisit;
			List<Integer> cumEachTypeItemPicked;
			double cumSimScore;
			int cumItemsPicked;
			int cumCost;
			Solution(List<Integer> p, 
					HashMap<String, Integer> iP, 
					HashMap<String, List<Double>> sS, 
					HashMap<String, List<Integer>> c, 
					List<Integer> nV,
					List<Integer> cETIP, 
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
				
				cumSimScore = cSS;
				cumItemsPicked = cIP;
				cumCost = cC;
			}
		}
		
		// Solution containing on the vertices (first integer is the vertex and second integer is the time index)
		HashMap<List<Integer>, List<Solution>> solOnVertex;
		
		// Constructor
		DynamicProgram(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
				List<Integer> vertexToVisit, int permNum) {
			//Initial Solution
			Solution initSol = new Solution(
					Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
					new HashMap<String, Integer>(),
					new HashMap<String, List<Double>>(),
					new HashMap<String, List<Integer>>(),
					new ArrayList<Integer>(),
					new ArrayList<Integer>(),
					0.0, 0, 0);
			solOnVertex = new HashMap<List<Integer>, List<Solution>>();
			solOnVertex.put(Arrays.asList(0, roadNetworkObj.getVertexLocation(user.getStartDest())), Arrays.asList(initSol));
			System.out.println(roadNetworkObj.getShortestPath(
					roadNetworkObj.getVertexLocation(user.getStartDest()), roadNetworkObj.getVertexLocation(user.getEndDest())));
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
					 if(fromVertexList.size() != 0){
						 if(!solOnVertex.containsKey(Arrays.asList(time, onVertex))){
							 initSol = new Solution(
									Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
									new HashMap<String, Integer>(),
									new HashMap<String, List<Double>>(),
									new HashMap<String, List<Integer>>(),
									new ArrayList<Integer>(),
									new ArrayList<Integer>(),
									0.0, 0, 0);
		
							  solOnVertex.put(Arrays.asList(time, onVertex), Arrays.asList(initSol));
						 }
						 findSolution(onVertex, fromVertexList, timeToLook, permNum, user, userI, vertexToVisit, time);
					 }
				}
			}
		}
		
		private void findSolution(
				int onVertex, 
				List<Integer> fromVertex, 
				List<Integer> timeToLook,
				int permNum, 
				UserClass user, 
				ItemPruning userI,
				List<Integer> vertexToVisit,
				int currentTime){
				
		}
}
