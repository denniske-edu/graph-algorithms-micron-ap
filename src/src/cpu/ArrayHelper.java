package cpu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayHelper {
	
	public static List<Integer> intersection(List<Integer> a, List<Integer> b) {
		
		return a.stream().filter(b::contains).collect(Collectors.toList());
	}
	
	public static List<Integer> without(List<Integer> a, Integer i) {

		a = a.stream().collect(Collectors.toList());
		
		a.remove(i);

		return a;
	}
	
	public static List<Integer> union(List<Integer> a, List<Integer> b) {

		return Stream.concat(a.stream(), b.stream()).distinct().collect(Collectors.toList());
	}
}
