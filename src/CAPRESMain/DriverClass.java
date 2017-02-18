package CAPRESMain;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class DriverClass {
	static RoadNetwork roadNetworkObj;
	static ItemInventory itemInventoryObj;
	
	private void PreInitialization() throws FileNotFoundException{
		roadNetworkObj = new RoadNetwork();
		roadNetworkObj.allPairShortestPath();
		itemInventoryObj = new ItemInventory();
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		// Should have only one object
		DriverClass object = new DriverClass();
		object.PreInitialization();
		
		// Exclusive to single user. For each user a separate class is required to be created
		UserClass user = new UserClass("queryDataRaw_infrequent_1");
		GraphPruning userG = new GraphPruning();
		
		userG.constructPrunedGraph(
				roadNetworkObj.getVertexLocation(user.getStartDest()),
				roadNetworkObj.getVertexLocation(user.getEndDest()),
				user.getTimeConstraint(),
				roadNetworkObj);
		
		ItemPruning userI = new ItemPruning(userG, 
				itemInventoryObj, 
				user.getItemList(), 
				user.getNumItems(), 
				user.getPersona());
		
		List<Integer> vertexToVisit = new ArrayList<Integer>();
		for(String loc : user.getNodeToVisit())
			if(userG.vertexList().contains(roadNetworkObj.getVertexLocation(loc)))
				vertexToVisit.add(roadNetworkObj.getVertexLocation(loc));
		
		// Execute Dynamic Program with the retrieved values
		DynamicProgram DP = new DynamicProgram(roadNetworkObj, user, userG, userI, vertexToVisit);
		
		// Execute Heuristic with the retrieved values
	}
}
