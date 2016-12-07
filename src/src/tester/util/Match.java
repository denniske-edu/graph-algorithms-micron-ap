package tester.util;

import java.util.List;

public class Match {
		
	public int offset;
	public List<Integer> reportCodes;

	public Match(int offset, List<Integer> reportCodes) {
	
		this.offset = offset;
		this.reportCodes = reportCodes;
	}	
}
