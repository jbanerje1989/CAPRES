package CAPRESMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ItemPruning {
	private HashMap<Integer, HashMap<String, List<List<Double>>>> itemValues;
	ItemPruning(GraphPruning gObject, 
			ItemInventory itemObject,
			List<String> itemList, 
			HashMap<String, Integer> itemNum, 
			List<Double> persona){
		
		itemValues = new HashMap<Integer, HashMap<String, List<List<Double>>>>();
		for(int vertex: gObject.vertexList()){
			HashMap<String, List<List<Double>>> itemValuesInVertex = new HashMap<String, List<List<Double>>>();
			if(itemObject.getItemInventory().containsKey(vertex)){
				for(String item: itemList){	
					if(itemObject.getItemContentInVertex().get(vertex).containsKey(item)){
						List<List<Double>> itemValuesReturned = returnSortedList(
								itemObject.getItemInventory().get(vertex),
								itemObject.getItemContentInVertex().get(vertex).get(item),
								item,
								itemNum.get(item),
								persona);
						itemValuesInVertex.put(item, itemValuesReturned);
					}
				}
			}
			itemValues.put(vertex, itemValuesInVertex);
		}
	}
	
	private List<List<Double>> returnSortedList(
			HashMap<List<String>, List<String>> itemsInVertex, 
			int numItemsInVertex, 
			String item,
			int itemConstraint, 
			List<Double> personaVal) {
		List<Double> pScore = new ArrayList<Double>();
		List<Double> cost = new ArrayList<Double>();
		for(int index = 0; index <= numItemsInVertex; index++){
			List<String> key =  new ArrayList<String>();
			String[] itemPart = item.split(" ");
			key.add(itemPart[0]);
			key.add(itemPart[1]);
			key.add(Integer.toString(index));
			List<String> value = itemsInVertex.get(key);
			List<Double> itemFeatureVal = new ArrayList<Double>();
			for(int index2 = value.size() - 4; index2 < value.size(); index2++)
				itemFeatureVal.add(Double.parseDouble(value.get(index2)));
			double itemCost = Math.ceil(Double.parseDouble(value.get(value.size()-6)) /100);
			double score = similarityScore(personaVal, itemFeatureVal);
			if(pScore.size() == itemConstraint){
				if(pScore.get(0) < score){
					pScore.set(0, score);
					cost.set(0, itemCost);
					twoArrayListSort(pScore, cost);
				}
			}
			else{
				pScore.add(score);
				cost.add(itemCost);
				twoArrayListSort(pScore, cost);
			}
		}
		List<List<Double>> returnVal = new ArrayList<List<Double>>();
		for(int index = pScore.size() - 1; index >= 0; index --){
			List<Double> valueToAdd = Arrays.asList(pScore.get(index), cost.get(index));
			returnVal.add(valueToAdd);
		}
		return returnVal;
	}
	
	private double similarityScore(List<Double> personaVal, List<Double> itemFeatureVal){
		List<Double> weights = Arrays.asList(3.04, 2.57, 2.57, 3.10); // the weights of the features
		double simScore = 0.0;
		for(int index = 0; index < weights.size(); index++){
			simScore += weights.get(index) * (1/ (1 + 
					Math.pow(Math.abs(personaVal.get(index) - itemFeatureVal.get(index)),2)));
		}
		return simScore;
	}
	
	// sort list1 and based on ordering of list1 order list2
	public void twoArrayListSort(List<Double> list1, List<Double> list2){
		for(int i = 1; i < list1.size(); i++){
			int j = i;
			while(list1.get(j-1) > list1.get(j)){
				double dummy1 = list1.get(j);
				list1.set(j, list1.get(j-1));
				list1.set(j-1, dummy1);
				double dummy2 = list2.get(j);
				list2.set(j, list2.get(j-1));
				list2.set(j-1, dummy2);
				j --;
				if(j == 0) break;
			}
		}
	}

	public HashMap<Integer, HashMap<String, List<List<Double>>>> getItemValues() { return itemValues;}
}
