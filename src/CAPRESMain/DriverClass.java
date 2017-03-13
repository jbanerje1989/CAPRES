package CAPRESMain;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

// Author: Joydeep Banerjee
public class DriverClass {
	static RoadNetwork roadNetworkObj;
	static ItemInventory itemInventoryObj;
	
	private void PreInitialization() throws FileNotFoundException{
		roadNetworkObj = new RoadNetwork();
		roadNetworkObj.allPairShortestPath();
		itemInventoryObj = new ItemInventory();
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		/* Permutation ordering for solution
		 * 0: (item, node, simScore)
		 * 1: (item, simScore, node)
		 * 2: (node, item, simScore)
		 * 3: (node, simScore, item)
		 * 4: (simScore, node, item)
		 * 5: (simScore, item, node)
		 */
		int permNum = 1;
		if(permNum <= -1 && permNum >= 6){
			System.out.println(" Enter a valid permutation ordering");
			return;
		}
		// Should have only one object
		DriverClass object = new DriverClass();
		object.PreInitialization();
		// Exclusive to single user. For each user a separate class is required to be created
		UserClass user = new UserClass("queryDataRaw_infrequent_2");
		
		// Execute graph pruning
		long startTime = System.currentTimeMillis();
		GraphPruning userG = new GraphPruning();
		userG.constructPrunedGraph(
				roadNetworkObj.getVertexLocation(user.getStartDest()),
				roadNetworkObj.getVertexLocation(user.getEndDest()),
				user.getTimeConstraint(),
				roadNetworkObj);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total Time to prune Graph: " + (double) totalTime / 1000.0 + " seconds"); 
		
		boolean flag = true;
		for(Integer vertex: userG.edgeList().keySet())
			if(userG.edgeList().get(vertex).size() != 0) flag = false;
		
		if(flag){
			System.out.println("Nothing to solve, increase time constraint");
			return;
		}
		
		// Execute Item pruning
		startTime = System.currentTimeMillis();
		ItemPruning userI = new ItemPruning(
				userG, 
				itemInventoryObj, 
				user.getItemList(), 
				user.getNumItems(), 
				user.getPersona());
		endTime   = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Total Time to prune Items: " + (double) totalTime / 1000.0 + " seconds"); 
		
		List<Integer> vertexToVisit = new ArrayList<Integer>();
		for(String loc : user.getNodeToVisit())
			if(userG.vertexList().contains(roadNetworkObj.getVertexLocation(loc)))
				vertexToVisit.add(roadNetworkObj.getVertexLocation(loc));
		
		// Execute Dynamic Program with the retrieved values
		System.out.println("================================================================================");
		System.out.println("Dynamic Program Solution");
		startTime = System.currentTimeMillis();
		DynamicProgram DP = new DynamicProgram(roadNetworkObj, user, userG, userI, vertexToVisit, permNum);
		DP.printSolution(userG);
		endTime   = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		System.out.println("================================================================================");
		
		// Execute Heuristic with the retrieved values
		System.out.println("================================================================================");
		System.out.println("Heuristic Solution");
		startTime = System.currentTimeMillis();
		Heuristic1 obj = new Heuristic1(roadNetworkObj, user, userG, userI, vertexToVisit, permNum); 
		endTime   = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Total Time: " + (double) totalTime / 1000.0 + " seconds"); 
		obj.printSolution(userG);
		System.out.println("================================================================================");
	}
}
