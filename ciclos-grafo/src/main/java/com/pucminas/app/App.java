package com.pucminas.app;

import java.awt.Point;
import java.util.List;
import java.util.Scanner;

import com.pucminas.graph.CycleUtil;
import com.pucminas.graph.Graph;

public class App {
  public static void clearScreen() {  
		Scanner scan = new Scanner(System.in);
    System.out.print("\n\n✅ Pressione Enter para concluir...");
    
    scan.nextLine();

		System.out.print("\033[H\033[2J");  
		System.out.flush(); 

		scan.close(); 
	} 
	public static void main(String[] args) {
		Graph g = new Graph();

		g.addBidirectionalEdge(new Point(0, 0), new Point(0, 1));
		g.addBidirectionalEdge(new Point(0, 1), new Point(3, 1));
		g.addBidirectionalEdge(new Point(3, 1), new Point(3, 0));
		g.addBidirectionalEdge(new Point(3, 0), new Point(0, 0));
		g.addBidirectionalEdge(new Point(3, 1), new Point(6, 1));
		g.addBidirectionalEdge(new Point(6, 1), new Point(6, 0));
		g.addBidirectionalEdge(new Point(6, 0), new Point(3, 0));

		long start = System.currentTimeMillis();
			CycleUtil cycleUtil = new CycleUtil(g);
			List<Graph> cycles = cycleUtil.listAllCycles();
		long end = System.currentTimeMillis();

		System.out.println("\n\n🛑 Iniciando grafo... \n");

		for (Graph cycle : cycles) {
			System.out.println("  ↪️ㅤCaminho dos vértices do ciclo: ");
			System.out.println("     " + cycle.getVertices() + "\n");
		}

		System.out.print ("⏲️ㅤTempo total de execução: " + (end - start) + " ms");
		clearScreen();
	}
}
