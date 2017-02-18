package CAPRESMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphPruning {
	private List<Integer> vertex; // vertex list of pruned graph
	private HashMap<Integer, List<Integer>> edge; // edgeList of pruned graph
	private HashMap<Integer, List<Integer>> edgeWeight; // edgeWeight of pruned graph
	
	GraphPruning(){
		vertex = new ArrayList<Integer>(); 
		edge = new HashMap<Integer, List<Integer>>(); 
		edgeWeight = new HashMap<Integer, List<Integer>>(); 
	}
	
	/* Construct and return a pruned graph based on a Time constraint T, start destination S and end destination D from the 
	 * graph containing vertices and edges as in the class variables such that a vertex k is included if 
	 * SP(S,k) + SP(k,D) <= T */
	
	public void constructPrunedGraph(int S, int D, int T, RoadNetwork object){
		// Create the vertex list
		HashMap<Integer, Integer> vertexMap = new HashMap<Integer, Integer>(); // for faster vertex retrieval
		vertex.add(S);
		vertex.add(D);
		vertexMap.put(S, S);
		vertexMap.put(D, D);
		for(int v: object.vertexList()){
			if(v == S || v == D) continue;
			if(object.getShortestPath(S, v) + object.getShortestPath(v, D) <= T){
				vertex.add(v);
				vertexMap.put(v,v);
			}
		}
		System.out.println(vertex.size());
		// Create the edge list
		for(int v: vertex){
			List<Integer> vEdge = new ArrayList<Integer>();
			List<Integer> vEdgeWeight = new ArrayList<Integer>();
			int index = 0;
			for(int u : object.edgeList().get(v)){
				if(vertexMap.containsKey(u)){
					if(object.getShortestPath(S, v) + object.getShortestPath(v, u) + object.getShortestPath(u, D) <= T){
						vEdge.add(u);
						vEdgeWeight.add(object.edgeWeightList().get(v).get(index));
					}
				}
				index ++;
			}	
			System.out.println(vEdge.size());
			edge.put(v, vEdge);
			edgeWeight.put(v, vEdgeWeight);
		}
		
		vertexMap.clear(); // free memory	
	}
	
	// Return Vertex list
	public List<Integer> vertexList() { return vertex;}
	
	// Return Edge List
	public HashMap<Integer, List<Integer>> edgeList() { return edge;}
	
	// Return Edge Weight
	public HashMap<Integer, List<Integer>> edgeWeightList() { return edgeWeight;}
	
}
