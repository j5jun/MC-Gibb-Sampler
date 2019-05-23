import java.awt.print.Printable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.text.PlainDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the Gibbs Sampling Stochastic Simulation method for
 * estimating Bayesian Network probabilities with/without evidence. <b>You
 * should only modify the simulate method.</b> Algorithm from Section 4.4.3 of
 * Pearl, Judea. "Probabilistic Reasoning in Intelligent Systems"
 */

public class BNGibbsSampler {
	/** iteration frequency of progress reports */
	public static int reportFrequency = 200000;
	/** total iterations; each non-evidence variable is updated in each iteration */
	public static int iterations = 1000000;

	/**
	 * Initialize parameters, parse input, display BN information, and perform Gibbs
	 * sampling. <b>You should not modify this method</b>
	 * 
	 * @param args an array of command-line arguments
	 * @throws ParseException standard input does not match grammar for Bayesian
	 *                        network specification. (See assignment documentation
	 *                        for BNF grammar.)
	 */
	public static void main(java.lang.String[] args) throws ParseException {
		// Initialize iterations and update frequency
		if (args.length > 0) {
			iterations = Integer.parseInt(args[0]);
			reportFrequency = (args.length > 1) ? Integer.parseInt(args[1]) : iterations;
		}

		// Read in belief net specification from System.in
		new BNParse(System.in).parseInput();
		// BNNode.printBN();
		// System.out.println("-------------------------------");

		// Do stochastic simulation.
		simulate();
	}

	/**
	 * Perform Stochastic Simulation as described in Section 4.4.3 of Pearl, Judea.
	 * "Probabilistic Reasoning in Intelligent Systems". The enclosed file pearl.out
	 * shows the output format given the input: java BNGibbsSampler 1000000 200000
	 * &lt; sample.in &gt; sample.out <b>This is the only method you should
	 * modify.</b>
	 */

	// Project code implementation starts here
	public static void simulate() {
		Random rand = new Random();
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		HashMap<String, Double> avg = new HashMap<String, Double>();
		for (BNNode node : BNNode.nodes) {
			if (!node.isEvidence) {
				hmap.put(node.name, 0);
				avg.put(node.name, 0.0);
				int value = rand.nextInt(2);
				if (value == 0) {
					node.value = false;
				} else {
					node.value = true;
				}
			}
		}

		BNNode.printBN();
		System.out.println("---------------------------------------------");

		for (int j = 1; j <= iterations; j++) {
			if (j % reportFrequency == 0) {
				System.out.println("After iteration " + j + ":");
				System.out.println("Variable, Average Conditional Probability, Fraction True");
				for (BNNode node : BNNode.nodes) {
					if (!node.isEvidence) {
						double fractionTrue = 1.0 * (hmap.get(node.name));
						double avgProb = 1.0 * (avg.get(node.name));
						System.out.println(node.name + ", " + (avgProb / j) + ", " + (fractionTrue / j));
					}
				}
				System.out.println();
			}

			for (BNNode node : BNNode.nodes) {
				double outputtrue = 0.0;
				double outputfalse = 0.0;
				if (!node.isEvidence) {
					node.value = true;
					outputtrue = node.cptLookup();

					BNNode[] childrenList = node.children;

					for (BNNode childnode : childrenList) {
						if (childnode.value == true) {
							outputtrue = outputtrue * childnode.cptLookup();
						} else {
							outputtrue = outputtrue * (1.0 - childnode.cptLookup());
						}

					}
					node.value = false;
					outputfalse = 1 - node.cptLookup();

					for (BNNode childnode : childrenList) {
						if (childnode.value == true) {
							outputfalse = outputfalse * childnode.cptLookup();
						} else {
							outputfalse = outputfalse * (1.0 - childnode.cptLookup());
						}

					}

					double denomenator = outputtrue + outputfalse;
					double normalized = outputtrue / denomenator;
					avg.put(node.name, ((avg.get(node.name) + normalized)));

					Random r = new Random();
					double rangeMin = 0.0;
					double rangeMax = 1.0;
					double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

					if (randomValue < normalized) {
						node.value = true;
						hmap.put(node.name, (hmap.get(node.name) + 1));
					} else {
						node.value = false;
					}
				}
			}
		}
	}
}
