package tester.util;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolHelper {

	static ArrayList<String> alphabet;
	static HashMap<String, String> alphabetStr;
	
	static {

		String hex = "0123456789ABCDEF";
		
		alphabet = new ArrayList<String>();
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				//if(i == 15 && (j == 14 || j == 15)) continue;
				alphabet.add("\\x" + hex.charAt(i) + hex.charAt(j));
			}
		}		

		alphabetStr = new HashMap<String, String>();
		
		int k = 0;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				//if(i == 15 && (j == 14 || j == 15)) continue;
				alphabetStr.put(String.valueOf(++k), "\\x" + hex.charAt(i) + hex.charAt(j));
			}
		}
	}
		
	public static String nodeToSymbol(int node) {
				
		return alphabet.get(node - 1);
	}
	
	public static String nodeStrToSymbol(String node) {
				
		return alphabetStr.get(node);
	}

}
