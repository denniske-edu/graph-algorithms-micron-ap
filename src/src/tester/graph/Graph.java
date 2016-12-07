package tester.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tester.util.SymbolHelper;

public class Graph {

	public int nodes;
	public List<Edge> edges;

	Graph() {
		this.edges = new ArrayList<Edge>();
	}

	public void writeEdgesFile(String path) {

		try {
			FileWriter fileWriter = new FileWriter(path);

			for (Edge edge : edges) {
				fileWriter.write(SymbolHelper.nodeToSymbol(edge.x));
				fileWriter.write(SymbolHelper.nodeToSymbol(edge.y));
			}

			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Graph readFromDimacsFile(String path) {

		Graph graph = new Graph();

		try {
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while (bufferedReader.ready()) {
				String currentRow = bufferedReader.readLine();

				if (currentRow.startsWith("p")) {

					String[] parts = currentRow.split("\\s+");

					String format = parts[1];
					int nodes = Integer.parseInt(parts[2]);
					int edges = Integer.parseInt(parts[3]);

					graph.nodes = nodes;
				}

				if (currentRow.startsWith("e")) {

					String[] parts = currentRow.split(" ");

					int a = Integer.parseInt(parts[1]);
					int b = Integer.parseInt(parts[2]);

					graph.edges.add(new Edge(Math.min(a, b), Math.max(a, b)));
				}
			}

			bufferedReader.close();

		} catch (FileNotFoundException e) {
			System.out.println("Die gewählte Datei wurde nicht gefunden." + e.getMessage());
		} catch (IOException e) {
			System.out.println("Ein-/Ausgabefehler:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}

	void swap(int a, int b) {

		edges.forEach((e) -> {
			if (e.x == a) {
				e.x = b;
			} else if (e.x == b) {
				e.x = a;
			}
			if (e.y == a) {
				e.y = b;
			} else if (e.y == b) {
				e.y = a;
			}
		});

	}

	public void sort() {

		// for(int i=1; i< this.nodes; i++) {
		// for(int j=0; j<this.nodes-i; j++) {
		// if(this.neighbours(j).size() > this.neighbours(j+1).size()) {
		//
		// swap(j, j+1);
		// }
		//
		// }
		// }
		//
		//
		// for(int i = 1; i < this.nodes; i++) {
		// System.out.println(this.neighbours(i).size());
		// }

		Collections.sort(edges, new Comparator<Edge>() {
			@Override
			public int compare(Edge container2, Edge container1) {

				if (container2.x == container1.x)
					return container2.y - container1.y;

				return container2.x - container1.x;
			}
		});
	}

	HashMap<String, List<Integer>> cache = new HashMap<String, List<Integer>>();

	public List<Integer> neighbours(int p) {

		if (this.cache.containsKey("n" + p)) {
			return this.cache.get("n" + p);
		}

		List<Integer> arr = new ArrayList<Integer>();

		for (Edge edge : edges) {
			if (edge.x == p)
				arr.add(edge.y);

			if (edge.y == p)
				arr.add(edge.x);
		}

		this.cache.put("n" + p, arr);

		return arr;
	}

	public boolean isClique(String clique) {
		String[] parts = clique.split(" ");

		for (int i = 0; i < parts.length; i++) {
			for (int j = 0; j < parts.length; j++) {
				if (i == j)
					continue;
				if (this.neighbours(Integer.parseInt(parts[i])).indexOf(Integer.parseInt(parts[j])) == -1) {
					return false;
				}
			}
		}

		return true;
	}

	HashMap<String, BitSet> cache2 = new HashMap<String, BitSet>();

	public BitSet bitneighbours(int p) {

		if (this.cache2.containsKey("n" + p)) {
			return this.cache2.get("n" + p);
		}

		BitSet arr = new BitSet();

		for (Edge edge : edges) {
			if (edge.x == p)
				arr.set(edge.y);

			if (edge.y == p)
				arr.set(edge.x);
		}

		this.cache2.put("n" + p, arr);

		return arr;
	}
}
