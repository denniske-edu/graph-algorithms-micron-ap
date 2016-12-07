package tester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.micron.ap.Anml;
import com.micron.ap.AnmlDefs;
import com.micron.ap.AnmlNetwork;
import com.micron.ap.ApException;
import com.micron.ap.ElementRef;

import tester.graph.Graph;
import tester.util.Match;
import tester.util.StringTree;
import tester.util.SymbolHelper;
import tester.util.Wrapper;

public class BranchAndBoundAutomata {

	static int staticOutputLength = 50;
	static int staticChunkLength = 1 * 1000;
	static boolean staticCompressed = true;
	static String staticNodes = "30";
	static double staticRuntime;
	static double staticCputime;

	public static void main(String[] args) {

		try {

			String source = "DENC" + staticNodes + ".1.clq";

			Graph graph = Graph.readFromDimacsFile("C:\\Uni\\Masterarbeit\\Dimacs\\" + source);
			graph.sort();

			StringTree root = new StringTree("R");

			for (int i = 1; i <= graph.nodes; i++) {

				StringTree tree = new StringTree(String.valueOf(i), graph.nodes - i);
				root.add(tree);
			}

			Wrapper auto = new Wrapper(CreateAutomaton(graph), "clique-exp");
			auto.compile();

			int maxClique = 0;
			int maxCliqueTarget = 2;

			StringTree tree = root;

			while (tree.getChildCount() > 0) {

				// Build input

				Map<Integer, ArrayList<StringTree>> batch = new HashMap<Integer, ArrayList<StringTree>>();

				StringBuilder input = new StringBuilder();

				int z = 1;
				int length = 0;
				int count = 0;

				Iterator<StringTree> iterator = tree.leavesIterator();

				StringTree base = null;

				ArrayList<StringTree> outputQueue = new ArrayList<>();
				int lastReport = 0;

				ArrayList<StringTree> toRemove = new ArrayList<>();

				int outputGiven = 0;

				while (iterator.hasNext() && (z < staticChunkLength || outputGiven == 0)) {

					StringTree tr = iterator.next();

					if (tr.getLevel() + tr.getPossibleExtensions() <= maxClique) {
						toRemove.add(tr);
						continue;
					}

					if (staticCompressed && tr.getLevel() > 2 + 1 && (base == null || !tr.isNodeAncestor(base))) {

						base = tr.getParent().getParent();

						input.append("\\xFF");
						base.getPathFromRoot().stream().skip(1).forEach(trr -> input.append(SymbolHelper.nodeStrToSymbol(trr.getObject())));

						length += base.getLevel() / 4 + 4;
						z += base.getLevel() / 4 + 1;
					}

					if (base != null && tr.isNodeAncestor(base)) {

						input.append(input.lastIndexOf("\\xFF") >= input.lastIndexOf("\\xFE") ? "\\xFE" : "\\xFD");
						tr.getPathFromAncestor(base).stream().forEach(trr -> input.append(SymbolHelper.nodeStrToSymbol(trr.getObject())));

						length += (tr.getLevel() - base.getLevel()) / 4 + 4;
						z += (tr.getLevel() - base.getLevel()) + 1;
						count++;
						
						outputQueue.add(tr);
					} else {

						input.append("\\xFF");
						tr.getPathFromRoot().stream().skip(1).forEach(trr -> input.append(SymbolHelper.nodeStrToSymbol(trr.getObject())));

						length += tr.getLevel() / 4 + 4;
						z += tr.getLevel() + 1;
						count++;

						outputQueue.add(tr);
					}

					if (outputQueue.size() >= staticOutputLength) {
						input.append("\\xFC");
						batch.put(z, outputQueue);
						outputQueue = new ArrayList<>();
						length += 4;
						z += 1;
						lastReport = z;
						outputGiven++;
					}
				}

				if (outputQueue.size() > 0) {
					input.append("\\xFC");
					batch.put(z, outputQueue);
					outputQueue = new ArrayList<>();
				}

				input.append("\\xFF");

				toRemove.forEach(BranchAndBoundAutomata::removeFromTreeWithParents);

				// Emulate
				List<Match> matches = auto.emulateStringViaFile(input.toString());

				// Process output
				if (!matches.isEmpty()) {

					int k = 0;
					for (Match match : matches) {

						ArrayList<StringTree> leafs = batch.get(match.offset);

						Map<StringTree, List<Integer>> leafMap = new HashMap<StringTree, List<Integer>>();

						for (Integer next : match.reportCodes) {
							int index = next >> 8;
							int code = next & 255;
							StringTree leaf = leafs.get(leafs.size() - index - 1);
							if (!leafMap.containsKey(leaf)) {
								leafMap.put(leaf, new ArrayList<Integer>());
							}
							leafMap.get(leaf).add(code);
						}

						for (Map.Entry<StringTree, List<Integer>> entry : leafMap.entrySet()) {
							StringTree leaf = entry.getKey();
							List<Integer> reportCodes = entry.getValue();

							int maxLen = leaf.getLevel() + reportCodes.size();

							if (maxLen > Math.max(maxClique, maxCliqueTarget - 1)) {

								for (Integer next : reportCodes) {

									int ext = (int) reportCodes.stream().filter(n -> n > next).count();
									leaf.add(new StringTree(String.valueOf(next), ext));
								}

								leafs.remove(leaf);
							}
						}
					}
				}

				// Final cliques
				List<StringTree> remaining = batch.values().stream().flatMap(c -> c.stream()).collect(Collectors.toList());

				for (StringTree cliqueFinTree : remaining) {

					int cliqueFinLength = cliqueFinTree.getLevel();

					if (cliqueFinLength > maxClique) {
						maxClique = cliqueFinLength;

						String cliqueFin = cliqueFinTree.getPathFromRoot().stream().skip(1).map(trr -> trr.getObject()).collect(Collectors.joining(" "));

						System.out.println("Found clique " + cliqueFinLength + ": " + cliqueFin);
						System.out.println("cliques.size(): " + root.getChildCount() + ", input.length():" + input.length());
					}

					removeFromTreeWithParents(cliqueFinTree);
				}
			}

		} catch (ApException exception) {
			exception.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeFromTreeWithParents(StringTree cliqueFinTree) {
		StringTree cursor = cliqueFinTree;

		if (cursor.getChildCount() > 0) {
			try {
				throw new Exception("ddd");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while (cursor.getChildCount() == 0) {
			StringTree parent = cursor.getParent();
			if (parent == null)
				break;
			parent.remove(cursor);
			cursor = parent;
		}
	}

	public static Anml CreateAutomaton(Graph graph) throws ApException {

		Anml anml = new Anml();

		AnmlNetwork anmlNet = anml.createAutomataNetwork();

		int outputQueue = 22;

		for (int i = 0; i < graph.nodes; i++) {
			final int j = i;
			List<Integer> neighbours = graph.neighbours(i + 1);

			String joined = neighbours.stream().filter(n -> n < j + 1).map(SymbolHelper::nodeToSymbol).collect(Collectors.joining(""));

			if (joined.length() > 0) {
				ElementRef a = anmlNet.addSTE("\\xFF", AnmlDefs.ALL_INPUT);
				ElementRef b = anmlNet.addSTE("[" + joined + "]");

				anmlNet.addAnmlEdge(a, b);
				anmlNet.addAnmlEdge(b, b);

				ElementRef ex1 = anmlNet.addSTE("[\\xFE]");
				ElementRef ex2 = anmlNet.addSTE("[^\\xFF]");
				ElementRef ex3 = anmlNet.addSTE("[\\xFD]");

				anmlNet.addAnmlEdge(b, ex1);
				anmlNet.addAnmlEdge(ex1, b);

				anmlNet.addAnmlEdge(ex1, ex2);
				anmlNet.addAnmlEdge(ex2, ex2);
				anmlNet.addAnmlEdge(ex2, ex3);
				anmlNet.addAnmlEdge(ex3, b);

				ElementRef last = b;

				for (int k = 0; k < outputQueue; k++) {

					int reportCode = k << 8 | (i + 1);

					if (k == 0) {
						ElementRef s2 = anmlNet.addSTE("[\\xFF\\xFD]");
						ElementRef s3 = anmlNet.addSTE("[\\xFC]", AnmlDefs.NO_START, "p" + reportCode, reportCode, true);

						anmlNet.addAnmlEdge(last, s2);
						anmlNet.addAnmlEdge(s2, s3);

						anmlNet.addAnmlEdge(last, s3);

						last = s2;

					} else {

						ElementRef s1 = anmlNet.addSTE("[^\\xFF\\xFD\\xFC]");
						ElementRef s2 = anmlNet.addSTE("[\\xFF\\xFD]");
						ElementRef s3 = anmlNet.addSTE("[\\xFC]", AnmlDefs.NO_START, "p" + reportCode, reportCode, true);

						anmlNet.addAnmlEdge(last, s1);
						anmlNet.addAnmlEdge(s1, s1);
						anmlNet.addAnmlEdge(s1, s2);
						anmlNet.addAnmlEdge(s2, s3);

						anmlNet.addAnmlEdge(s1, s3);

						last = s2;
					}
				}
			}
		}

		return anml;
	}
}
