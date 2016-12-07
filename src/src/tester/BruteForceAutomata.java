package tester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.micron.ap.Anml;
import com.micron.ap.AnmlDefs;
import com.micron.ap.AnmlNetwork;
import com.micron.ap.ApException;
import com.micron.ap.ElementRef;

import tester.util.Match;
import tester.util.SymbolHelper;
import tester.util.Wrapper;

public class BruteForceAutomata {

	static int cliqueSize = 3;

	public static void main(String[] args) {

		try {
			Anml anml = new Anml();

			AnmlNetwork anmlNet = anml.createAutomataNetwork();

			Set<Integer> nodes = GetIntegerSet(29);
			ICombinatoricsVector<Integer> initialVector = Factory.createVector(nodes);
			Generator<Integer> gen = Factory.createSimpleCombinationGenerator(initialVector, cliqueSize);

			int j = 0;
			for (ICombinatoricsVector<Integer> combination : gen) {

				System.out.println(combination);
				System.out.println(j + 1);

				createAuto(anmlNet, combination, j + 1);

				j++;
			}
			
			Wrapper auto = new Wrapper(anml, "test-c-16-4");
			auto.compile();

			List<Match> matches = auto.emulateFile("C:\\Uni\\Masterarbeit\\Dimacs\\DENC10.1.edges");

			System.out.println(matches.size() + " results.");

		} catch (ApException exception) {
			exception.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void createAuto(AnmlNetwork anmlNet, ICombinatoricsVector<Integer> nodes, int id) throws ApException {

		ICombinatoricsVector<Integer> initialVector = Factory.createVector(nodes);
		Generator<Integer> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);
		List<ICombinatoricsVector<Integer>> list = gen.generateAllObjects();

		ArrayList<ElementRef> elements = new ArrayList<ElementRef>();

		for (int i = 0; i < list.size(); i++) {

			List<Integer> edgeNodes = list.get(i).getVector();

			for (int j = 0; j < edgeNodes.size(); j++) {

				String symbol = SymbolHelper.nodeToSymbol(edgeNodes.get(j));

				if (i == 0 && j == 0) {

					// First
					elements.add(anmlNet.addSTE(symbol, AnmlDefs.ALL_INPUT));

				} else if (i == list.size() - 1 && j == edgeNodes.size() - 1) {

					// Last
					elements.add(anmlNet.addSTE(symbol));

				} else {

					// Middle
					elements.add(anmlNet.addSTE(symbol));
				}

			}

		}

		for (int i = 0; i < elements.size() - 1; i++) {

			anmlNet.addAnmlEdge(elements.get(i), elements.get(i + 1));
		}

		for (int i = 1; i < elements.size() - 1; i += 2) {

			ElementRef reg1 = anmlNet.addRegex("*");
			ElementRef reg2 = anmlNet.addRegex("*");

			anmlNet.addAnmlEdge(reg1, reg2);
			anmlNet.addAnmlEdge(reg2, reg1);

			anmlNet.addAnmlEdge(elements.get(i), reg1);
			anmlNet.addAnmlEdge(reg2, elements.get(i + 1));
		}

		
		ElementRef last = elements.get(elements.size()-1);
		ElementRef report = anmlNet.addSTE("[\\xFF]", AnmlDefs.NO_START, "p" + id, id, true);
		
		anmlNet.addAnmlEdge(last, report);
	}
	
	private static Set<Integer> GetIntegerSet(int max) {
		Set<Integer> availableNumbers = new HashSet<>(0);

		for (int i = 1; i <= max; i++) {
			availableNumbers.add(i);
		}
		return availableNumbers;
	}
}
