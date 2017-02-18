package CAPRESMain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ItemInventory {
	// Variables to create Item Inventory
	/* First Maps the vertex index to the inventory inside the index
	 * The inner map map's a String array of size 3 {men/women/none, itemType, itemNumber} to information of the item
	 * along with a number that gives the number of items of this type in the inventory of the vertex*/
	private HashMap<Integer, HashMap<List<String>, List<String>>> itemInventory;
	
	/* Stores different type of vertices (like Shoe, TShirt) in a particular node
	 * The inner Map keeps a track of number of units available of a particular item type */
	private HashMap<Integer, HashMap<String, Integer>> itemContentInVertex; 
	
	/* While creating Node network the shop vertices should be number from [0, numberShops -1] */
	private final int numShops = 39; // Hard code for number of shops with each shop vertex labeled as [0,numShops - 1]
	
	ItemInventory() throws FileNotFoundException{
		itemInventory = new HashMap<Integer, HashMap<List<String>, List<String>>>();
		itemContentInVertex = new HashMap<Integer, HashMap<String, Integer>>();
		
		for(int index = 0; index < numShops; index++){
			HashMap<List<String>, List<String>> itemList = new HashMap<List<String>, List<String>>(); //stores item List for a particular vertex
			HashMap<String, Integer> itemTypes = new HashMap<String, Integer>(); // stores all type of items and number of units in a particular vertex
			Scanner scan = new Scanner(new File("InputDataFiles/" + index + ".txt"));
			scan.nextLine();
			// fill up the inventory
			while(scan.hasNext()){
				String[] line = scan.nextLine().split(", ");
				List<String> key = new ArrayList<String>();
				List<String> value = new ArrayList<String>();
				int index2 = 0;
				for(String str: line){
					if(index2 != 1){ value.add(str); index2++; continue;}
					
					String[] keyVals = str.split(" ");
					
					// Fill the key value for itemInventory
					if(keyVals[0].equals("Men's")) key.add("Men");
					else key.add("Women");
					
					key.add(keyVals[1]);
					if(itemTypes.containsKey(key.get(0) + " " + key.get(1))){
						key.add(String.valueOf(itemTypes.get(key.get(0) + " " + key.get(1)) + 1));
						int updateNum = itemTypes.get(key.get(0) + " "  + key.get(1)) + 1;
						itemTypes.replace(key.get(0) + " " + key.get(1), updateNum);
					}
					else{ key.add("0"); itemTypes.put(key.get(0) + " " + key.get(1), 0);}
					index2 ++;
				}
				itemList.put(key,  value);
			}
			
			itemInventory.put(index, itemList);
			itemContentInVertex.put(index, itemTypes);
			scan.close();
		}
	}
	
	public HashMap<Integer, HashMap<List<String>, List<String>>> getItemInventory() { return itemInventory;}
	public HashMap<Integer, HashMap<String, Integer>> getItemContentInVertex() { return itemContentInVertex;}
}
