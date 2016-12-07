package tester.util;

import com.pengyifan.commons.collections.tree.Tree;

public class StringTree extends Tree<String, StringTree> {
	public StringTree(String s) {
		setObject(s);
	}
	public StringTree(String s, int possibleExtensions) {
		setObject(s);
		setPossibleExtensions(possibleExtensions);
	}
	
	  public int getPossibleExtensions() {
		return possibleExtensions;
	}

	public void setPossibleExtensions(int possibleExtensions) {
		this.possibleExtensions = possibleExtensions;
	}

	private int possibleExtensions = 100;
	  
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (!(obj instanceof StringTree)) {
//			return false;
//		}
//		StringTree rhs = (StringTree) obj;
//		return Objects.equals(getObject(), rhs.getObject());
//	}
}
