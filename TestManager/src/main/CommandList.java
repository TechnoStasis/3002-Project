package main;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
public class CommandList {
	private String cmd1;
	private int cmd2;
	private String cmd3;
	
	public CommandList(String cmd1, int cmd2, String cmd3) {
		this.cmd1 = cmd1;
		this.cmd2 = cmd2;
		this.cmd3 = cmd3;		
	}
	
	public String getcmd1() {
		return cmd1;
	}
	
	public int getcmd2() {
		return cmd2;
	}
	
	public String getcmd3() {
		return cmd3;
	}
	
}