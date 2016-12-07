package cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import tester.graph.Graph;

public class BranchAndBound1 {

	static Graph graph;
	static List<Integer> cStar;
	int ms = 0;
	int run = 0;
		
	static void clique(List<Integer> c, List<Integer> p) {
		
		//System.out.println("Clique " +  c + " => " + p);

		if (c.size() > cStar.size()) {
			cStar = c;
		}

		if (c.size() + p.size() > cStar.size()) {

			for (int i = 0; i < p.size(); i++) { // Math.min(2, p.length)

				List<Integer> pW = ArrayHelper.without(p, p.get(i));

				List<Integer> c_ = ArrayHelper.union(c, Arrays.asList(p.get(i)));
				List<Integer> p_ = ArrayHelper.intersection(pW, graph.neighbours(p.get(i)));

				clique(c_, p_);
			}

		}
	}
	
	public static void main(String[] args) {

		try {

			//String source = "OSTERGARD8.1.clq";
			//String source = "DEN6.1.clq";
			// String source = "p_hat300-1.clq";
			//String source = "C125.9.clq";
			String source = "DENC10.1.clq";

			graph = Graph.readFromDimacsFile("C:\\Uni\\Masterarbeit\\Dimacs\\" + source);
			graph.sort();

			
			long startTime = System.currentTimeMillis();
			
			
			cStar = new ArrayList<Integer>();
			
			List<Integer> v = IntStream.rangeClosed(1, graph.nodes).boxed().collect(Collectors.toList());
			clique(new ArrayList<Integer>(), v);

			System.out.println(cStar);
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(elapsedTime / (double) 1000 + "s");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
