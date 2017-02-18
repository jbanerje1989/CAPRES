package CAPRESMain;

import java.util.HashMap;
import java.util.List;

public class DynamicProgram {
	
	DynamicProgram(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
			List<Integer> vertexToVisit) {
		// Iterate over all time steps
		for(int time = 0; time <= user.getTimeConstraint(); time++){
			for(int onVertex: userG.vertexList()){
				
			}
		}
		
	}
	
}
