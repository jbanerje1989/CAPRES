package CAPRESMain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class UserClass {
	private int timeConstraint;
	private int budgetConstraint;
	private String startDest;
	private String endDest;
	private List<String> itemList;
	private HashMap<String, Integer> numItems;
	private List<String> nodeToVisit;
	private List<Double> persona;
	private int numRecommendation;
	UserClass(String file) throws FileNotFoundException{
		Scanner scan = new Scanner(new File("UserDataFiles/" + file + ".txt"));
		timeConstraint = Integer.parseInt(scan.nextLine());
		budgetConstraint = Integer.parseInt(scan.nextLine());
		startDest = scan.nextLine();
		endDest = scan.nextLine();
		itemList = Arrays.asList(scan.nextLine().split("; "));
		String[] itemNums = scan.nextLine().split(" ");
		numItems = new HashMap<String, Integer>();
		int index = 0;
		for(String str: itemNums) { numItems.put(itemList.get(index), Integer.parseInt(str)); index++;}
		nodeToVisit = Arrays.asList(scan.nextLine().split("; "));
		String[] personaVal = scan.nextLine().split(" ");
		persona = new ArrayList<Double>();
		for(String str: personaVal) persona.add(Double.parseDouble(str));
		numRecommendation = scan.nextInt();
		scan.close();
	}
	
	public int getTimeConstraint() { return timeConstraint;}
	public int getBudgetConstraint() { return budgetConstraint;}
	public String getStartDest() { return startDest;}
	public String getEndDest() { return endDest;}
	public List<String> getItemList() { return itemList;}
	public HashMap<String, Integer> getNumItems() { return numItems;}
	public List<String> getNodeToVisit() { return nodeToVisit;}
	public List<Double> getPersona() { return persona;}
	public int getNumRecommendation() { return numRecommendation;}
}
