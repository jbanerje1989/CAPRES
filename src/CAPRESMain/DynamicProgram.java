package CAPRESMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DynamicProgram {
	// Class Variable
		// Single solution instance
		private class DynSolution{
			HashMap<Integer, HashMap<String, List<Integer>>> itemToNodeMap;
			List<Integer> path;
			HashMap<String, List<Double>> similarityScore;
			HashMap<String, List<Double>> cost;
			List<Integer> nodeVisit;
			HashMap<String, Integer> cumEachTypeItemPicked;
			List<List<Integer>> edges;
			double cumSimScore;
			int cumCost;
			DynSolution(HashMap<Integer, HashMap<String, List<Integer>>> iNM,
					List<Integer> p, 
					HashMap<String, List<Double>> sS, 
					HashMap<String, List<Double>> c, 
					List<Integer> nV,
					HashMap<String, Integer> cETIP,
					List<List<Integer>> e,
					double cSS, int cC){
				itemToNodeMap = new HashMap<Integer, HashMap<String, List<Integer>>>();
				for(int y: iNM.keySet()) itemToNodeMap.put(y, iNM.get(y));
				
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
		private HashMap<List<Integer>, HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>>> solOnVertex;
		// Maintains a mapping of index position in array list and the item name
		private HashMap<String, Integer> itemNameToIndexMap = new HashMap<String, Integer>();
		private HashMap<Integer, String> indexMapToItemName = new HashMap<Integer, String>();
		// Solution for end vertex
		private List<DynSolution> finalSolution;
		
		// Constructor
		DynamicProgram(RoadNetwork roadNetworkObj, UserClass user, GraphPruning userG, ItemPruning userI,
				List<Integer> vertexToVisit, int permNum) {
			// Populate itemNameToIndexMap
			int indexVal = 0;
			for(String itemName: user.getItemList()){ 
				itemNameToIndexMap.put(itemName, indexVal); 
				indexMapToItemName.put(indexVal, itemName);
				indexVal ++;
			}
			//Initial Solution
			DynSolution initSol = new DynSolution(
					new HashMap<Integer, HashMap<String, List<Integer>>>(),
					Arrays.asList(roadNetworkObj.getVertexLocation(user.getStartDest())),
					new HashMap<String, List<Double>>(),
					new HashMap<String, List<Double>>(),
					new ArrayList<Integer>(),
					new HashMap<String, Integer>(),
					new ArrayList<List<Integer>>(),
					0.0, 0);
			
			solOnVertex = new HashMap<List<Integer>, HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>>>();
			HashMap<List<Integer>, List<DynSolution>> initSolOfItem = new HashMap<List<Integer>, List<DynSolution>>();
			initSolOfItem.put(Collections.nCopies(user.getItemList().size() + 1, 0), Arrays.asList(initSol));
			HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>> initSolOfNodeVisit = 
					new HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>>();
			initSolOfNodeVisit.put(0, initSolOfItem);
			solOnVertex.put(Arrays.asList(0, roadNetworkObj.getVertexLocation(user.getStartDest())), initSolOfNodeVisit);
			
			// Iterate over all time steps and nodes
			for(int time = 0; time <= user.getTimeConstraint(); time++){
				System.out.println(time);
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
					 
					 // Compute Scores on the vertex
					 if(fromVertexList.size() != 0)
						 computeScore(onVertex, fromVertexList, timeToLook, user, userI, vertexToVisit, time);
								
					 // Delete non required solutions on this vertex
					 if(roadNetworkObj.getVertexLocation(user.getEndDest()) == onVertex) continue;
					 int timeStep = time - Collections.max(userG.edgeWeightListRev().get(onVertex));
					 if(solOnVertex.containsKey(Arrays.asList(timeStep, onVertex)))
						 solOnVertex.remove(Arrays.asList(timeStep, onVertex));
				}
			}
			
			//Get the final solution
			finalSolution = new ArrayList<DynSolution>();
			List<DynSolution> allFinalSolution = new ArrayList<DynSolution>();
			for(int time = 0; time <= user.getTimeConstraint(); time ++){
				if(solOnVertex.containsKey(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest())))){
					for(Integer nodeSolutions: 
					 solOnVertex.get(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest()))).keySet()){
						for(List<Integer> itemVectorSolution:
						 solOnVertex.get(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest()))).
						 get(nodeSolutions).keySet()){
							for(DynSolution S: 
							 solOnVertex.get(Arrays.asList(time, roadNetworkObj.getVertexLocation(user.getEndDest()))).
							 get(nodeSolutions).
							 get(itemVectorSolution)){
								allFinalSolution.add(S);
							}
						}
					}
				}
			}
			solOnVertex.clear();
			allFinalSolution = sortOnPerm(allFinalSolution, permNum, user);
			for(int index = 0; index < Math.min(allFinalSolution.size(), user.getNumRecommendation()); index++){
				finalSolution.add(allFinalSolution.get(index));
			}
			allFinalSolution.clear();
		}

		private void computeScore(int onVertex, List<Integer> fromVertexList, List<Integer> timeToLook, UserClass user,
				ItemPruning userI, List<Integer> vertexToVisit, int time) {
			// Initialize the possible number of items to be picked in item vector as all items
			List<Integer> itemVectorAppendCost = new ArrayList<Integer>(Collections.nCopies(user.getItemList().size() + 1, 0));
			for(String itemName: itemNameToIndexMap.keySet()) 
				itemVectorAppendCost.set(itemNameToIndexMap.get(itemName), user.getNumItems().get(itemName));
			// Iterate over all budget
			for(int currentCost = 0; currentCost <= user.getBudgetConstraint(); currentCost ++){
				// Iterate over all nodes to be visited
				for(int numNodeVisit = 0; numNodeVisit <= vertexToVisit.size(); numNodeVisit ++){
					itemVectorAppendCost.set(itemVectorAppendCost.size() - 1, currentCost);
					HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>> solOfNodeVisit = 
							new HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>>();
					if(!solOnVertex.containsKey(Arrays.asList(time, onVertex))){
						solOfNodeVisit.put(numNodeVisit, new HashMap<List<Integer>, List<DynSolution>>());
						solOnVertex.put(Arrays.asList(time, onVertex), solOfNodeVisit);
					}
					else{
						for(Integer nodeV: solOnVertex.get(Arrays.asList(time, onVertex)).keySet())
								solOfNodeVisit.put(nodeV, solOnVertex.get(Arrays.asList(time, onVertex)).get(nodeV));
						solOfNodeVisit.put(numNodeVisit, new HashMap<List<Integer>, List<DynSolution>>());
						solOnVertex.replace(Arrays.asList(time, onVertex), solOfNodeVisit);
								
					}
					computeRecursiveScore(onVertex, fromVertexList, timeToLook, userI, vertexToVisit, 
							itemVectorAppendCost, user, time, numNodeVisit, currentCost);
				}
			}
		}

		private void computeRecursiveScore(int onVertex, List<Integer> fromVertexList, List<Integer> timeToLook,
				ItemPruning userI, List<Integer> vertexToVisit, List<Integer> itemVectorAppendCost, 
				UserClass user, int time, int numNodeVisit, int currentCost) {
			// Get the positions where max score needs to be computed
			List<DynSolution> newSolution = new ArrayList<DynSolution>();
			
			// Iterate to get the max score
			for(int fromVertexIndex = 0; fromVertexIndex < fromVertexList.size(); fromVertexIndex++){
				// Get the solution for the previous vertex at the given item Vector values
				if(solOnVertex.get(
						Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
						containsKey(numNodeVisit - 1)){
					if(solOnVertex.get(
							Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
							get(numNodeVisit - 1).
							containsKey(itemVectorAppendCost)){
						for(DynSolution S: solOnVertex.get(
							Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
							get(numNodeVisit - 1).
							get(itemVectorAppendCost)){
				
							DynSolution solToAdd = new DynSolution(
								S.itemToNodeMap,
								S.path, 
								S.similarityScore,
								S.cost, 
								S.nodeVisit, 
								S.cumEachTypeItemPicked,
								S.edges,
								S.cumSimScore, 
								S.cumCost);
							if(solToAdd.edges.contains(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex))) 
								continue;
							if(solToAdd.nodeVisit.contains(onVertex) || !vertexToVisit.contains(onVertex))
								continue;
							solToAdd.edges.add(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex));
							solToAdd.path.add(onVertex);
							solToAdd.nodeVisit.add(onVertex);
							newSolution.add(solToAdd);
							newSolution = sortOnPerm(newSolution, 6, user);
							if(newSolution.size() > user.getNumRecommendation()) 
								newSolution.remove(newSolution.size() - 1);
						}
					}
				}
				
				if(solOnVertex.get(
						Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
						containsKey(numNodeVisit)){
					if(solOnVertex.get(
							Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
							get(numNodeVisit).
							containsKey(itemVectorAppendCost)){
						for(DynSolution S: solOnVertex.get(
							Arrays.asList(timeToLook.get(fromVertexIndex), fromVertexList.get(fromVertexIndex))).
							get(numNodeVisit).
							get(itemVectorAppendCost)){
				
							DynSolution solToAdd = new DynSolution(
								S.itemToNodeMap,
								S.path, 
								S.similarityScore,
								S.cost, 
								S.nodeVisit, 
								S.cumEachTypeItemPicked,
								S.edges,
								S.cumSimScore, 
								S.cumCost);
							if(solToAdd.edges.contains(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex))) 
								continue;
							if(solToAdd.nodeVisit.contains(onVertex) ^ vertexToVisit.contains(onVertex))
								continue;
							solToAdd.edges.add(Arrays.asList(solToAdd.path.get(solToAdd.path.size() - 1), onVertex));
							solToAdd.path.add(onVertex);
							newSolution.add(solToAdd);
							newSolution = sortOnPerm(newSolution, 6, user);
							if(newSolution.size() > user.getNumRecommendation()) 
								newSolution.remove(newSolution.size() - 1);
						}
					}
				}
		
				for(int itemPosIndex = 0; itemPosIndex < user.getItemList().size(); itemPosIndex ++){
					List<Integer> newItemVectorAppendCost = new ArrayList<Integer>();
					for(int numItem: itemVectorAppendCost) newItemVectorAppendCost.add(numItem);
					if(newItemVectorAppendCost.get(itemPosIndex) == 0) continue;
					newItemVectorAppendCost.set(itemPosIndex, itemVectorAppendCost.get(itemPosIndex) - 1);
					for(int costToCompute = 0; costToCompute <= currentCost; costToCompute++){
						newItemVectorAppendCost.set(newItemVectorAppendCost.size() - 1, costToCompute);
						// solve for numNodeVisit
						if(solOnVertex.get(
								Arrays.asList(time, onVertex)).
								containsKey(numNodeVisit)){
							if(!solOnVertex.
									get(Arrays.asList(time, onVertex)).
									get(numNodeVisit).
									containsKey(newItemVectorAppendCost))
								computeRecursiveScore(onVertex, fromVertexList, timeToLook, userI, vertexToVisit, 
										newItemVectorAppendCost, user, time, numNodeVisit, costToCompute);
							if(!userI.getItemValues().get(onVertex).containsKey(indexMapToItemName.get(itemPosIndex))) continue;
							if(solOnVertex.
									get(Arrays.asList(time, onVertex)).
									get(numNodeVisit).
									containsKey(newItemVectorAppendCost)){
								for(DynSolution S: solOnVertex.get(
										Arrays.asList(time, onVertex)).
										get(numNodeVisit).
										get(newItemVectorAppendCost)){
							
									DynSolution solToAdd = new DynSolution(
										S.itemToNodeMap,
										S.path, 
										S.similarityScore,
										S.cost, 
										S.nodeVisit, 
										S.cumEachTypeItemPicked,
										S.edges,
										S.cumSimScore, 
										S.cumCost);
									// check if any item can be included
									double maxScore = -1;
									double costToInclude = -1;
									int indexToInclude = -1;
									int indexToIncludeItr = 0;
									for(List<Double> simScoreCost: 
										userI.getItemValues().get(onVertex).get(indexMapToItemName.get(itemPosIndex))){
										if(simScoreCost.get(1) + solToAdd.cumCost > currentCost){
											indexToIncludeItr ++;
											continue;
										}
										boolean flag = false;
										if(solToAdd.itemToNodeMap.containsKey(onVertex))
											if(solToAdd.itemToNodeMap.get(onVertex).
											 containsKey(indexMapToItemName.get(itemPosIndex)))
												flag = true;
										if(flag)
											if(solToAdd.itemToNodeMap.get(onVertex).
											 get(indexMapToItemName.get(itemPosIndex)).contains(indexToIncludeItr)){
												indexToIncludeItr ++;
												continue;
											}
										if(simScoreCost.get(0) > maxScore){
											maxScore = simScoreCost.get(0);
											costToInclude = simScoreCost.get(1);
											indexToInclude = indexToIncludeItr;
										}
										indexToIncludeItr ++;
									}

									if(indexToInclude == -1) continue;
									
									solToAdd.cumSimScore += maxScore;
									solToAdd.cumCost += costToInclude;
									
									if(!solToAdd.cumEachTypeItemPicked.
											containsKey(indexMapToItemName.get(itemPosIndex))){
										solToAdd.cumEachTypeItemPicked.put(indexMapToItemName.get(itemPosIndex), 1);
										solToAdd.cost.put(indexMapToItemName.get(itemPosIndex), 
												Arrays.asList(costToInclude));
										solToAdd.similarityScore.put(indexMapToItemName.get(itemPosIndex), 
												Arrays.asList(maxScore));
									}	
									else{
										int numItemVal = solToAdd.cumEachTypeItemPicked.
												get(indexMapToItemName.get(itemPosIndex));
										solToAdd.cumEachTypeItemPicked.replace(indexMapToItemName.get(itemPosIndex), numItemVal + 1);
										List<Double> costNew = new ArrayList<Double>();
										for(Double c: solToAdd.cost.get(indexMapToItemName.get(itemPosIndex)))
											costNew.add(c);
										costNew.add(costToInclude);
										solToAdd.cost.put(indexMapToItemName.get(itemPosIndex), costNew);
										List<Double> simNew = new ArrayList<Double>();
										for(Double s: solToAdd.similarityScore.get(indexMapToItemName.get(itemPosIndex)))
											simNew.add(s);
										simNew.add(maxScore);
										solToAdd.similarityScore.put(indexMapToItemName.get(itemPosIndex), simNew);
									}
									
									if(!solToAdd.itemToNodeMap.containsKey(onVertex)){
										HashMap<String, List<Integer>> itemToIndexMap = new HashMap<String, List<Integer>>();
										itemToIndexMap.put(indexMapToItemName.get(itemPosIndex), 
												Arrays.asList(indexToInclude));
										solToAdd.itemToNodeMap.put(onVertex, itemToIndexMap);
									}
									else{
										HashMap<String, List<Integer>> itemToIndexMap = new HashMap<String, List<Integer>>();
										for(String itemNameVal: solToAdd.itemToNodeMap.get(onVertex).keySet())
											itemToIndexMap.put(itemNameVal, 
													solToAdd.itemToNodeMap.get(onVertex).get(itemNameVal));
										
										if(!solToAdd.itemToNodeMap.get(onVertex).
												containsKey(indexMapToItemName.get(itemPosIndex))){
											itemToIndexMap.put(indexMapToItemName.get(itemPosIndex), 
													Arrays.asList(indexToInclude));
											solToAdd.itemToNodeMap.put(onVertex, itemToIndexMap);
										}
										else{
											List<Integer> itemContent = new ArrayList<Integer>();
											for(int itemContentIndex: 
											 itemToIndexMap.get(indexMapToItemName.get(itemPosIndex)))
												itemContent.add(itemContentIndex);
											itemContent.add(indexToInclude);
											itemToIndexMap.replace(indexMapToItemName.get(itemPosIndex), 
													itemContent);
											solToAdd.itemToNodeMap.put(onVertex, itemToIndexMap);
										}
									}
									newSolution.add(solToAdd);								
									newSolution = sortOnPerm(newSolution, 6, user);
									if(newSolution.size() > user.getNumRecommendation()) 
										newSolution.remove(newSolution.size() - 1);
								}
							}
						}
					}
				}
			}
			
			// put the solution to the index
			if(newSolution.size() == 0) return;
			HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>> newSolOfNodeVisit = 
					new HashMap<Integer, HashMap<List<Integer>, List<DynSolution>>>();
			for(Integer nodeV: solOnVertex.get(Arrays.asList(time, onVertex)).keySet())
				newSolOfNodeVisit.put(nodeV, solOnVertex.get(Arrays.asList(time, onVertex)).get(nodeV));
			if(!newSolOfNodeVisit.containsKey(numNodeVisit)){
				HashMap<List<Integer>, List<DynSolution>> newSolOfItem = new HashMap<List<Integer>, List<DynSolution>>();
				newSolOfItem.put(itemVectorAppendCost, newSolution);
				newSolOfNodeVisit.put(numNodeVisit, newSolOfItem);
			}
			else{
				HashMap<List<Integer>, List<DynSolution>> newSolOfItem = new HashMap<List<Integer>, List<DynSolution>>();
				for(List<Integer> vec: newSolOfNodeVisit.get(numNodeVisit).keySet())
					newSolOfItem.put(vec, newSolOfNodeVisit.get(numNodeVisit).get(vec));
				newSolOfItem.put(itemVectorAppendCost, newSolution);
				newSolOfNodeVisit.replace(numNodeVisit, newSolOfItem);
			}
			solOnVertex.replace(Arrays.asList(time, onVertex), newSolOfNodeVisit);
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
		
		// sort based on ordering
		private List<DynSolution> sortOnPerm(List<DynSolution> newSolutions, int permNum, UserClass user) {
			if (permNum == 0)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return itemComparison != 0 ? itemComparison:
							   nodeComparison != 0 ? nodeComparison:
							   simScoreComparison;
					}
				});
			else if (permNum == 1)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return itemComparison != 0 ? itemComparison:
							   simScoreComparison != 0 ? simScoreComparison:
							   nodeComparison;
					}
				});
			else if (permNum == 2)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return nodeComparison != 0 ? nodeComparison:
							   itemComparison != 0 ? itemComparison:
							   simScoreComparison;
					}
				});
			else if (permNum == 3)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return nodeComparison != 0 ? nodeComparison:
							   simScoreComparison != 0 ? simScoreComparison:
							   itemComparison;
					}
				});
			else if (permNum == 4)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return simScoreComparison != 0 ? simScoreComparison:
							   nodeComparison != 0 ? nodeComparison:
							   itemComparison;
					}
				});
			else if (permNum == 5)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						int itemComparison = compareItemPicked(o1.cumEachTypeItemPicked, o2.cumEachTypeItemPicked, user);
						int nodeComparison = compareNumNodeVisit(o1.nodeVisit, o2.nodeVisit);
						int simScoreComparison = compareSimScore(o1.cumSimScore, o2.cumSimScore);
						return simScoreComparison != 0 ? simScoreComparison:
							   itemComparison != 0 ? itemComparison:
							   nodeComparison;
					}
				});
			else if (permNum == 6)
				Collections.sort(newSolutions, new Comparator<DynSolution>(){
					@Override
					public int compare(DynSolution o1, DynSolution o2){
						if(o2.cumSimScore > o1.cumSimScore) return 1;
						else if(o2.cumSimScore < o1.cumSimScore) return -1;
						else return 0;
					}
				});
			return newSolutions;
		}
		
		// Print the final solution
		public void printSolution(GraphPruning userG){
			int k = 1;
			for(DynSolution S: finalSolution){
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
