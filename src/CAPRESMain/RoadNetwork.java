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
	private HashMap<List<Integer>, Integer> shortestPath; // all pair shortest path
	
	RoadNetwork() throws FileNotFoundException{
		vertex = new ArrayList<Integer>(); 
		latitude = new HashMap<Integer, Double>();
		longitude = new HashMap<Integer, Double>();
		vertexName = new HashMap<String, Integer>();
		vertexLocation = new HashMap<String, Integer>();
		edge = new HashMap<Integer, List<Integer>>(); 
		edgeWeight = new HashMap<Integer, List<Integer>>(); 
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
		while(scan.hasNext()){
			String[] line = scan.nextLine().split(" ");
			List<Integer> edgeList = new ArrayList<Integer>();
			List<Integer> edgeListWeight = new ArrayList<Integer>();
			int index2 = 0; // holds the vertex number connected to index
			for(String str: line){
				if(Integer.parseInt(str) != 0){
					edgeList.add(index2);
					edgeListWeight.add(Integer.parseInt(str));
				}
				index2 ++;
			}
			edge.put(index, edgeList);
			edgeWeight.put(index, edgeListWeight);
			index ++;
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
	
	// Return vertex latitude
	public HashMap<Integer, Double> getLatitude() { return latitude;}
	
	// Return vertex latitude
	public HashMap<Integer, Double> getLongitude() { return longitude;}
	
	// Return vertex Name
	public int getVertexName(String name) { return vertexName.get(name);}
	
	// Return vertex Name
	public int getVertexLocation(String loc){ return vertexLocation.get(loc);}
	
	
}
