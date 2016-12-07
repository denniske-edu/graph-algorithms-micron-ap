package cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import tester.graph.Graph;

public class BranchAndBound2 {

	static int cliqueCount;
	static long cliqueSize;
	static Graph graph;
	static int max;
	//static boolean found;
	//static int[] c;
	static List<Integer> cStar;
	
	static String genTab(int t) {
		String str = "";
		for (int i = 0; i < t; i++) {
			str += " |";
		}
		return str + "-";
	}
	
	static void clique(List<Integer> u, int size, List<Integer> star) {
		
		if(u.size() == 0) {
			//System.out.println();
			if(size > max) {
				max = size;
				System.out.println("Found " + size);				
				System.out.println("Clique " +  star);
			}
			return;
		}

		cStar.add(u.get(0));

		//System.out.println("Clique " +  u);
		
		while(!u.isEmpty()) {
			if(size + u.size() <= max) {
					//System.out.print("out 1 ");
				
					return;
			} else {

				//System.out.print("      ");
			}
			
			int i = u.stream().mapToInt(o -> o).min().getAsInt();

			cliqueSize += 1 + star.size() + 1 + 1 + 1;
			cliqueCount++;
			//System.out.println(genTab(size) + i + " ");
			u.remove((Integer) i);
			
			List<Integer> u2 = new ArrayList<Integer>(u.size());
			u2.addAll(u);
						
			
			List<Integer> star2 = new ArrayList<Integer>(star.size());
			star2.addAll(star);
			star2.add((Integer) i);
						
			
			clique(ArrayHelper.intersection(u2, graph.neighbours(i)), size+1, star2);
		}		
	}
	
	public static void main(String[] args) {

		try {

			//String source = "OSTERGARD8.1.clq";
			//String source = "DEN9.1.clq";
			// String source = "p_hat300-1.clq";
			//String source = "C125.9.clq";
			String source = "DENC40.1.clq";

			graph = Graph.readFromDimacsFile("C:\\Uni\\Masterarbeit\\Dimacs\\" + source);
			graph.sort();

			
			long startTime = System.currentTimeMillis();
			
			
			max = 0;

			cStar = new ArrayList<Integer>();
			
			//List<Integer> list = IntStream.rangeClosed(1, graph.nodes).boxed().collect(Collectors.toList());
			//clique(list, 0);
			
			List<Integer> list = IntStream.rangeClosed(1, graph.nodes).boxed().collect(Collectors.toList());
			clique(list, 0, new ArrayList<Integer>());

			//System.out.println(cStar);
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(elapsedTime / (double) 1000 + "s");
			System.out.println(cliqueCount + " cliques");
			
			System.out.println(cliqueSize + " symbols");
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
