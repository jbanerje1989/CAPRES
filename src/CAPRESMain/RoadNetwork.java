package CAPRESMain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RoadNetwork {
	// Variables to get the road network universe
	private List<Integer> vertex; // vertex list of pruned graph
	private HashMap<Integer, Double> latitude; // Latitude of the vertex
	private HashMap<Integer, Double> longitude; // Longitude of the vertex
	private HashMap<String, Integer> vertexName; // vertex Name 
	private HashMap<String, Integer> vertexLocation; // Vertex Location
	private HashMap<Integer, List<Integer>> edge; // edgeList of pruned graph
	private HashMap<Integer, List<Integer>> edgeWeight; // edgeWeight of pruned graph
	private HashMap<Integer, List<Integer>> edgeRev; // edgeList of pruned graph reversed
	private HashMap<Integer, List<Integer>> edgeWeightRev; // edgeWeight of pruned graph reversed
	private HashMap<List<Integer>, Integer> shortestPath; // all pair shortest path
	
	RoadNetwork() throws FileNotFoundException{
		vertex = new ArrayList<Integer>(); 
		latitude = new HashMap<Integer, Double>();
		longitude = new HashMap<Integer, Double>();
		vertexName = new HashMap<String, Integer>();
		vertexLocation = new HashMap<String, Integer>();
		edge = new HashMap<Integer, List<Integer>>(); 
		edgeWeight = new HashMap<Integer, List<Integer>>(); 
		edgeRev = new HashMap<Integer, List<Integer>>(); 
		edgeWeightRev = new HashMap<Integer, List<Integer>>(); 
		shortestPath = new HashMap<List<Integer>, Integer>(); 
		
		// Input File containing road network node data
		Scanner scan = new Scanner(new File("InputDataFiles/AllNodesCombined.txt"));
		while(scan.hasNext()){
			String[] line = scan.nextLine().split(";");
			vertex.add(Integer.parseInt(line[0]));
			latitude.put(Integer.parseInt(line[0]), Double.parseDouble(line[1]));
			longitude.put(Integer.parseInt(line[0]), Double.parseDouble(line[2]));
			vertexName.put(line[3], Integer.parseInt(line[0]));
			vertexLocation.put(line[4], Integer.parseInt(line[0]));
		}
		
		// Input file containing edge information
		scan = new Scanner(new File("InputDataFiles/CommuteTimeMatrix.txt"));
		int index = 0; // Index to hold the current vertex 
		String[][] comMatrix = new String[vertex.size()][vertex.size()];
		while(scan.hasNext()){
			comMatrix[index] = scan.nextLine().split(" ");
			index ++;
		}
		
		for(int i = 0; i < vertex.size(); i++){
			List<Integer> edgeList = new ArrayList<Integer>();
			List<Integer> edgeListWeight = new ArrayList<Integer>();
			for(int j = 0; j < vertex.size(); j++){
				if(Integer.parseInt(comMatrix[j][i]) != 0){
					edgeList.add(j);
					edgeListWeight.add(Integer.parseInt(comMatrix[j][i]));
				}
			}
			edge.put(i, edgeList);
			edgeWeight.put(i, edgeListWeight);
		}
		
		for(int i = 0; i < vertex.size(); i++){
			List<Integer> edgeList = new ArrayList<Integer>();
			List<Integer> edgeListWeight = new ArrayList<Integer>();
			for(int j = 0; j < vertex.size(); j++){
				if(Integer.parseInt(comMatrix[i][j]) != 0){
					edgeList.add(i);
					edgeListWeight.add(Integer.parseInt(comMatrix[i][j]));
				}
			}
			edgeRev.put(i, edgeList);
			edgeWeightRev.put(i, edgeListWeight);
		}
		scan.close();
	}
	
	// Compute all pair shortest Path
	public void allPairShortestPath(){
		//Initialization
		for(int v: vertex){
			for(int u: vertex){
				if(v == u) shortestPath.put(Arrays.asList(v, v), 0);
				else shortestPath.put(Arrays.asList(v, u), 10000); // if there is no edge the commute time is 10000
			}
			List<Integer> vWeights = edgeWeight.get(v);
			int index = 0;
			for(int u: edge.get(v)){
				shortestPath.put(Arrays.asList(v,u), vWeights.get(index));
				index ++;
			}
		}
		
		//Compute All Pair Shortest Path
		for(int k: vertex){	
			for(int i: vertex){
				for(int j: vertex){
					if(shortestPath.get(Arrays.asList(i,j)) >
						shortestPath.get(Arrays.asList(i,k)) + shortestPath.get(Arrays.asList(k,j)))	
						shortestPath.replace(Arrays.asList(i,j),
								shortestPath.get(Arrays.asList(i,k)) + shortestPath.get(Arrays.asList(k,j)));
				}
			}
		}
	}	

	// Get shortestPath between a pair of nodes
	public int getShortestPath(int u, int v){ 
		return shortestPath.get(Arrays.asList(u,v));}
	
	// Return Vertex list
	public List<Integer> vertexList() { return vertex;}
	
	// Return Edge List
	public HashMap<Integer, List<Integer>> edgeList() { return edge;}
	
	// Return Edge Weight
	public HashMap<Integer, List<Integer>> edgeWeightList() { return edgeWeight;}
	
	// Return Edge List of reverse graph
	public HashMap<Integer, List<Integer>> edgeListRev() { return edgeRev;}
		
	// Return Edge Weight of reverse graph
	public HashMap<Integer, List<Integer>> edgeWeightListRev() { return edgeWeightRev;}
	
	// Return vertex latitude
	public HashMap<Integer, Double> getLatitude() { return latitude;}
	
	// Return vertex latitude
	public HashMap<Integer, Double> getLongitude() { return longitude;}
	
	// Return vertex Name
	public int getVertexName(String name) { return vertexName.get(name);}
	
	// Return vertex Name
	public int getVertexLocation(String loc){ return vertexLocation.get(loc);}
	
	
}
