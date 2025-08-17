package net.blixate.deadlight.scoreboard;

/** Way better sidebar builder */
public class SidebarBuilder {
	
	String[] data;
	
	public SidebarBuilder() {
		data = new String[0];
	}
	
	public SidebarBuilder writeIf(boolean b, String line) {
		return (b) ? write(line) : this;
	}
	
	public SidebarBuilder writeIfElse(boolean b, String line, String lineElse) {
		return (b) ? write(line) : write(lineElse);
	}
	
	public SidebarBuilder write(String line) {
		String[] data2 = new String[data.length + 1];
		System.arraycopy(data, 0, data2, 0, data.length);
		data2[data.length] = line;
		this.data = data2;
		return this;
	}
	
	public SidebarBuilder writeBlank() {
		return write("");
	}
	
	public void build(SidebarBoard board) {
		int i = data.length;
		board.updateSize(data.length);
		while (i > 0) {
			board.updateLine(i - 1, data[data.length - i]);
			i--;
		}
		data = new String[0];
	}
}
