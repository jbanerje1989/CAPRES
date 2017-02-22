package CAPRESMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Heuristic1 {
	// Class Variable
		// Single solution instance
		private class Solution{
			List<Integer> path;
			List<Integer> itemsPicked;
			List<Double> similarityScore;
			List<Integer> cost;
			List<Integer> nodeVisit;
			List<Integer> cumEachTypeItemPicked;
			double cumSimScore;
			int cumItemsPicked;
			int cumCost;
			Solution(List<Integer> p, List<Integer> iP, List<Double> sS, List<Integer> c, List<Integer> nV,
					List<Integer> cETIP, double cSS, int cIP, int cC){
				path = new ArrayList<Integer>();
				for(int v: p) path.add(v);
				itemsPicked = new ArrayList<Integer>();
				for(int i: iP) itemsPicked.add(i);
				similarityScore = new ArrayList<Double>();
				for(double s: sS) similarityScore.add(s);
				cost = new ArrayList<Integer>();
				for(int cst: c) cost.add(cst);
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
		HashMap<Integer, HashMap<Integer, List<Solution>>> solOnVertex;
		
		Heuristic1(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
				List<Integer> vertexToVisit, int orderingType) {
			//Initial Solution
			solOnVertex = new HashMap<Integer, HashMap<Integer, List<Solution>>>();
			Solution initSol = new Solution(Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
					new ArrayList<Integer>(),
					new ArrayList<Double>(),
					new ArrayList<Integer>(),
					new ArrayList<Integer>(),
					new ArrayList<Integer>(),
					0.0, 0, 0);
			HashMap<Integer, List<Solution>> initSol2 = new HashMap<Integer, List<Solution>>();
			initSol2.put(0, Arrays.asList(initSol));
			solOnVertex.put(roadNetworkObj.getVertexLocation(user.getStartDest()), initSol2);
			
			// Iterate over all time steps and nodes
			for(int time = 0; time <= user.getTimeConstraint(); time++){
				for(int onVertex: userG.vertexList()){
					 int fromVertexIndex = 0;
					 for(int fromVertex: userG.edgeList().get(onVertex)){
						 if(time - userG.edgeWeightList().get(onVertex).get(fromVertexIndex) >= 0 &&
							time + roadNetworkObj.getShortestPath(
									onVertex, roadNetworkObj.getVertexLocation(user.getEndDest())) 
									<= user.getTimeConstraint() &&
							solOnVertex.containsKey(fromVertex)){
							
							if(solOnVertex.get(fromVertex).containsKey(
									time - userG.edgeWeightList().get(onVertex).get(fromVertexIndex))){
								
							}	 
						 }
						 fromVertexIndex ++;
					 }
				}
			}
		}
}
