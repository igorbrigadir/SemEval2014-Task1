package org.insight.semeval;

public class Sentence {
	
	public final int id;
	public final String sA, sB;
	public final float similarity;
	public final Entailment entailment;

	public Sentence(final int id, final String sA, final String sB, final float similarity, final Entailment entailment) {
		this.id = id;
		this.sA = sA;
		this.sB = sB;
		this.similarity = similarity;
		this.entailment = entailment;
	}
	
	public Sentence(String line) {
		String[] parts = line.split("\t");
		
		id = Integer.parseInt(parts[0]);
		sA = parts[1];
		sB = parts[2];
		similarity = Float.parseFloat(parts[3]);
		entailment = Entailment.fromString(parts[4]);
	}
	
	public Sentence withSimilarity (final float similarity) {
		return new Sentence(this.id, this.sA, this.sB, similarity, this.entailment);
	}	
	
	public enum Entailment{
		NEUTRAL, ENTAILMENT, CONTRADICTION;
		public static Entailment fromString(String resource) {
			if (resource != null) {
				for (Entailment e : Entailment.values()) {
					if (resource.equalsIgnoreCase(e.toString())) {
						return e;
					}
				}
			}
			return null;
		}
	}
}
