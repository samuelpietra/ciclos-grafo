package com.pucminas.graph;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CycleUtil {
	private Graph originalGraph;

	public CycleUtil(Graph originalGraph) {
		this.originalGraph = originalGraph;
	}

	/**
	 * Enumera todos os ciclos do grafo
	 */
	public List<Graph> listAllCycles() {
		if (originalGraph == null) {
			throw new IllegalArgumentException("Grafo não pode ser nulo");
		}

		List<List<Vertex>> cycleBasis = computeCycleBasisOfGraph(originalGraph);
		List<Graph> allCycles = listAllCyclesFromBasis(originalGraph, cycleBasis);
		
    return allCycles;
	}

	/** 
	* A base do ciclo de um grafo não-dirigido é um conjunto de ciclos simples que formam
	* uma base do espaço do ciclo do grafo. Ou seja, é um conjunto mínimo de
	* ciclos que permitem que cada subgrafo Euleriano seja expresso como um simétrico
	* diferença dos ciclos da base.
	*/

	private List<List<Vertex>> computeCycleBasisOfGraph(Graph g) {
		// Cópia de segurança, não alterando o grafo original
		Graph graphCopy = new Graph(new ArrayList<Vertex>(g.getVertices()), new ArrayList<Edge>(g.getEdges()));

		List<Edge> backEdges = new ArrayList<Edge>(graphCopy.getEdges());

		// Cria uma árvore de extensão mínima e seu conjunto associado de arestas de retorno
		Graph minimalSpanningTree = createMinimalSpanningTree(graphCopy);

		backEdges.removeAll(minimalSpanningTree.getEdges());
    
    /*
    * Se a aresta 'e' em B é de retorno, insere a mesma nas arestas 'E' da
    * árvore de extensão mínima para formar um conjunto E' = E + {e}.
    * O grafo resultante G = (V, E') tem exatamente um ciclo, que pode ser
    * construído ao aplicar a busca em profundidade
    */
		List<List<Vertex>> cycles = new ArrayList<List<Vertex>>();
		for (Edge backEdge : backEdges) {
			Edge forward = new Edge(backEdge.getSource(), backEdge.getDestination());
			Edge backwards = new Edge(backEdge.getDestination(), backEdge.getSource());

      // Processa apenas as arestas para as quais ainda não processamos seu oposto
			if (backEdges.indexOf(backwards) > backEdges.indexOf(forward)) {
				minimalSpanningTree.getEdges().add(forward);
				minimalSpanningTree.getEdges().add(backwards);

				findCycle(minimalSpanningTree, forward.getSource(), forward.getSource(), new HashSet<Vertex>(), new ArrayList<Vertex>(), cycles);

				minimalSpanningTree.getEdges().remove(forward);
				minimalSpanningTree.getEdges().remove(backwards);
			}
		}

		return cycles;
	}

	private List<Graph> listAllCyclesFromBasis(Graph g, List<List<Vertex>> basisCycles) {
    // Agora que temos todos os ciclos básicos, podemos criar seus vetores de incidência
		List<BigInteger> incidenceVectors = new ArrayList<BigInteger>();
		for (List<Vertex> cycle : basisCycles) {
			BigInteger iv = incidenceVectorOfCycle(cycle, g.getEdges());
			incidenceVectors.add(iv);
		}

    // Cria todas as possíveis combinações de incidência dos vetores
		List<List<BigInteger>> powerSet = powerSet(incidenceVectors);

    // Cria nova combinação do vetor de incidência ao fazer um XOR dos vetores a cada combinação
		List<BigInteger> incidenceCombinations = new ArrayList<BigInteger>();
		for (List<BigInteger> combination : powerSet) {
			if (combination.size() > 0) {
				BigInteger result = combination.get(0);

				for (int i = 1; i < combination.size(); i++) {
					result = result.xor(combination.get(i));
				}

				incidenceCombinations.add(result);
			}
		}

		List<Graph> allCycles = new ArrayList<Graph>();

		for (BigInteger incidenceVector : incidenceCombinations) {
			Graph cycle = cycleFromIncidenceVector(incidenceVector, g.getEdges());
			allCycles.add(cycle);
		}

		return allCycles;
	}

	private Graph createMinimalSpanningTree(Graph g) {
		// Cria uma árvore de extensão mínima e seu conjunto associado de arestas de retorno
		Vertex start = g.getVertices().get(0);

    // Computa as arestas de retorno
		Set<Edge> backEdges = backEdges(g, start, start, new HashSet<Vertex>(), new HashSet<Edge>());

		// A árvore de extensão mínima é basicamente o grafo original (todos os vértices),
    // mas sem as arestas de retorno
		List<Edge> spanningTreeEdges = new ArrayList<Edge>(g.getEdges());
		spanningTreeEdges.removeAll(backEdges);
		Graph minimalSpanningTree = new Graph(g.getVertices(), spanningTreeEdges);

		return minimalSpanningTree;
	}

	private Set<Edge> backEdges(Graph g, Vertex root, Vertex current, Set<Vertex> visited, Set<Edge> backEdges) {
		visited.add(current);

		for (Vertex n : g.getNeighbors(current)) {
			if (!visited.contains(n)) {
				backEdges(g, current, n, visited, backEdges);
			} else if (!n.equals(root)) {
				// Encontra aresta de retorno
				Edge edge1 = new Edge(current, n);
				Edge edge2 = new Edge(n, current);

				// Remove do grafo
				g.removeEdge(edge1);
				g.removeEdge(edge2);

				// Adiciona o conjunto de resultado
				backEdges.add(edge1);
				backEdges.add(edge2);
			}
		}

		return backEdges;
	}

	private void findCycle(Graph g, Vertex root, Vertex current, Set<Vertex> visited, List<Vertex> stack, List<List<Vertex>> basisCycles) {
		visited.add(current);
		stack.add(current);

		for (Vertex n : g.getNeighbors(current)) {
			if (!visited.contains(n)) {
				findCycle(g, current, n, visited, stack, basisCycles);
			} else if (!n.equals(root) && stack.contains(n)) {
				// Encontra um ciclo, adiciona o conjunto de resultado
				basisCycles.add(new ArrayList<Vertex>(stack));
			}
		}

		stack.remove(current);
	}

	private BigInteger incidenceVectorOfCycle(List<Vertex> cycle, List<Edge> originalEdges) {
		StringBuilder sb = new StringBuilder();

		// Cria lista de arestas que construíram o ciclo
		List<Edge> cycleEdges = new ArrayList<Edge>();
		for (int i = 0; i < cycle.size() - 1; i++) {
			cycleEdges.add(new Edge(cycle.get(i), cycle.get(i + 1)));
			cycleEdges.add(new Edge(cycle.get(i + 1), cycle.get(i)));
		}

		cycleEdges.add(new Edge(cycle.get(0), cycle.get(cycle.size() - 1)));
		cycleEdges.add(new Edge(cycle.get(cycle.size() - 1), cycle.get(0)));

		// Cria vetor de incidência
		for (Edge oe : originalEdges) {
			if (cycleEdges.contains(oe)) {
				sb.append("1");
			} else {
				sb.append("0");
			}
		}

		BigInteger mask = new BigInteger(sb.toString(), 2);

		return mask;
	}

	private Graph cycleFromIncidenceVector(BigInteger vector, List<Edge> originalEdges) {
		List<Vertex> cycleVertices = new ArrayList<Vertex>();
		List<Edge> cycleEdges = new ArrayList<Edge>();

		for (int i = 0; i < originalEdges.size(); i++) {
			if (vector.testBit(i)) {
				// porque, quando geramos nossa String do vetor de incidência '1xxxx', o 1 estava no index 0,
        // mas o index 0 do Big Int é o index 4 da String
				Edge oe = originalEdges.get(originalEdges.size() - 1 - i);
				Vertex source = oe.getSource();
				Vertex destination = oe.getDestination();

				if (cycleVertices.contains(source) == false) {
					cycleVertices.add(source);
				}

				if (cycleVertices.contains(destination) == false) {
					cycleVertices.add(destination);
				}

				cycleEdges.add(oe);
			}
		}

		return new Graph(cycleVertices, cycleEdges);
	}

	private <T> List<List<T>> powerSet(List<T> list) {
		List<List<T>> powerSet = new ArrayList<List<T>>();

		powerSet.add(new ArrayList<T>());

		for (T e : list) {
			List<List<T>> newPowerSet = new ArrayList<List<T>>();

			for (List<T> subSet : powerSet) {
				newPowerSet.add(subSet);

				List<T> newSubSet = new ArrayList<T>();
				newSubSet.addAll(subSet);
				newSubSet.add(e);

				newPowerSet.add(newSubSet);
			}

			powerSet = newPowerSet;
		}

		return powerSet;
	}
}
