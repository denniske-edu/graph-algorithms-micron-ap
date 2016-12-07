package tester.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.micron.ap.Anml;
import com.micron.ap.AnmlNetwork;
import com.micron.ap.ApException;
import com.micron.ap.Automaton;
import com.micron.ap.ExpressionDB;

import shell.ShellExec;

public class Wrapper {

	static String folder = "C:\\Uni\\Masterarbeit\\Anml\\";

	Anml anml;
	ExpressionDB db;
	String name;
	Automaton automaton;

	public Wrapper(Anml anml, String name) throws ApException {
		this.anml = anml;
		this.name = name;

		this.exportAnml(folder + name + ".anml");
	}

	public Wrapper(ExpressionDB db, String name) {
		this.db = db;
		this.name = name;
	}

	void exportAnml(String path) throws ApException {
	
		AnmlNetwork anmlNet = this.anml.createAutomataNetwork();
		anmlNet.exportAnml(path);
	}

	public void compile() throws ApException, IOException {

		long startTime = System.currentTimeMillis();
		
		File fsm = this.db != null ? null : new File(folder + name + "-" + this.getAnmlHash() + ".fsm");
		if(fsm != null && fsm.exists() && !fsm.isDirectory()) { 
			
			System.out.println("Using cached automaton...");
			
			this.automaton = new Automaton();
			this.automaton.restore(folder + name + "-" + this.getAnmlHash() + ".fsm");
		
		} else {
		
			System.out.println("Compiling...");
	
			if (this.anml != null)
				this.automaton = this.anml.compileAnml(0).getFirst();
			else
				this.automaton = this.db.compile(0);	
		}

		System.out.println("BlockRectArea = " + this.automaton.getInfo().getBlockRectArea());
		System.out.println("BlocksPowerManaged = " + this.automaton.getInfo().getBlocksPowerManaged());
		System.out.println("BlocksUsed = " + this.automaton.getInfo().getBlocksUsed());
		System.out.println("BooleanUsed = " + this.automaton.getInfo().getBooleanUsed());
		System.out.println("CounterUsed = " + this.automaton.getInfo().getCounterUsed());
		System.out.println("EventUsed = " + this.automaton.getInfo().getEventUsed());
		System.out.println("SteUsed = " + this.automaton.getInfo().getSteUsed());
		System.out.println("SubgraphCount = " + this.automaton.getInfo().getSubgraphCount());
		System.out.println("Status = " + this.automaton.getInfo().getStatus());
		System.out.println("ClockDivisor = " + this.automaton.getInfo().getClockDivisor());
		
		// AP_MOD_PCRE_QUASI_BACKREFS

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime / (double) 1000 + "s");
		
		this.automaton.save(folder + name + "-" + this.getAnmlHash() + ".fsm");
		this.automaton.saveYed(folder + name + "-" + this.getAnmlHash());
	}
	
	private String getAnmlHash() throws IOException {
		if (anmlHash == null) {
			anmlHash = this.db != null ? "db" : MD5(new String(Files.readAllBytes(Paths.get(folder + name + ".anml")), StandardCharsets.UTF_8));
		}
		return anmlHash;
	}

	private String getCacheFolder() throws IOException {
		if (cacheFolderInstance == null) {
			cacheFolderInstance = new File(folder + "Cache\\" + this.getAnmlHash());
			cacheFolderInstance.mkdirs();
		}
		return this.getAnmlHash();
	}

	public String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	String lastOutput = "";
	String anmlHash = null;
	File cacheFolderInstance = null;

	public ArrayList<Match> emulateString(String string, String cacheKey) throws IOException, ApException {
		
		String output = "";

		String cacheFile = folder + "Cache\\" + getCacheFolder() + "\\" + cacheKey + ".txt";

		if (Files.exists(Paths.get(cacheFile))) {
			output = new String(Files.readAllBytes(Paths.get(cacheFile)), StandardCharsets.UTF_8);
		} else {

			// System.out.println("Emulating...");

			ShellExec exec = new ShellExec(true, false);

			// int exitCode = exec.execute("apemulate", null, true,
			// "--version");

			int exitCode = exec.execute("apemulate", null, true, "--all", folder + name + "-" + this.getAnmlHash() + ".fsm", string);

			output = exec.getOutput();
			lastOutput = output;

			if (output.startsWith("Error [-15]: Couldn't find license library or the library is invalid")) {
				throw new NumberFormatException("License expired (Custom error).");
			}

			//System.out.println(exec.getOutput());

			PrintWriter writer = new PrintWriter(cacheFile, "UTF-8");
			writer.print(output);
			writer.close();
		}

		// Match result:
		// Offset 1
		// Automaton 0 Report-codes: 1
		// Offset 4
		// Automaton 0 Report-codes: 1

		ArrayList<Match> matches = new ArrayList<Match>();

		String NL = System.getProperty("line.separator", "\r\n");

		Queue<String> q = new LinkedList<String>(Arrays.asList(output.split(NL)));

		while (!q.isEmpty()) {
			String line = q.poll();

			if (line.startsWith("  Offset")) {

				line = line.substring(9);

				int offset = Integer.parseInt(line.replace(" ", ""));

				line = q.poll();
				line = line.substring(31);

				List<Integer> reportCodes = Arrays.asList(line.split(" ")).stream().map(Integer::parseInt)
						.collect(Collectors.toList());

				matches.add(new Match(offset, reportCodes));
			}
		}

		return matches;

	}

	public List<Match> emulateFile(String path) throws IOException, ApException {

		String string = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		return emulateString(path, MD5(string));
	}
	
	public List<Match> emulateString(String string) throws IOException, ApException {

		return emulateString(string, MD5(string));
	}
	
	public List<Match> emulateStringViaFile(String input) throws IOException, ApException {
		
		PrintWriter writer = new PrintWriter("C:\\Uni\\Masterarbeit\\Anml\\Buffer.txt", "UTF-8");
		writer.print(input);
		writer.close();
		
		return emulateString("C:\\Uni\\Masterarbeit\\Anml\\Buffer.txt", MD5(input));
	}

}
