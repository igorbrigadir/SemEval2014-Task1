package org.insight.semeval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.insight.word2vec.util.StopWords;
import org.insight.word2vec.util.VectorMath;
import org.insight.word2vec.util.WordSim;


/*
 * A Java wrapper for Word2Vec - Only Reads a pre trained model!
 */
public class RI2Vec extends HashMap<String, int[]> {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Check if word is in vocab:
	 */
	public boolean contains(String word) {
		return this.containsKey(word);
	}

	/*
	 * Get Vector Representation of a word
	 */
	public float[] vector(String word) {
		return convertVector(this.get(word), true);
	}

	/*
	 * Get Vector Representation of a word
	 */
	public String debugvector(String word) {
		float[] v = convertVector(this.get(word), true);
		
		StringBuilder sb = new StringBuilder();
		
		for (float f : v) {
			sb.append(String.format("%.6f, ", f));			
		}
		
		return sb.toString();
	}

	/*
	 * Cosine Distance between 2 words:
	 */
	public Double cosineDistance(String word1, String word2) {
		if (!this.contains(word1)) {
			//System.err.println( "Out of vocab word: " + word1 );
			//word1 = "</s>";
			return Double.NaN;
		}
		if (!this.contains(word2)) {
			//System.err.println( "Out of vocab word: " + word2 );
			//word2 = "</s>";
			return Double.NaN;
		}
		return VectorMath.cosineSimilarity(vector(word1), vector(word2));
	}

	
	// Adding several words:
	
	
	
	public float[] sentenceVector(boolean filter, String sentence) {
		return sentenceVector(filter, sentence.toLowerCase().split(" "));
	}
	public float[] sentenceVector(boolean filter, String... words) {
		List<float[]> vectors = new ArrayList<float[]>();
		for (String w : words) {
			
			// Stopword filter:
			if (filter && StopWords.isStopWord(w)) {
				continue;
			}
			
			if (this.contains(w)) {
				vectors.add(vector(w));
			}
		}
		return VectorMath.addAll(vectors);
	}
	
	
	// Linear search k Nearest Neighbour:		
	public List<String> knn(String word, int k) {
		if (!this.contains(word)) {
			System.err.println( "Out of vocab word: " + word );
			return new ArrayList<String>();
		}
		return knn(this.vector(word), k);
	}	
	
	// Used by System, tweets are vecs - together withsentenseVector
	public List<String> knn(float[] vec, int k) {
		List<WordSim> words = knn(vec, k, true);
		
		List<String> ret = new ArrayList<String>();
		for (WordSim w : words) {
				ret.add(w.getString());
		}
		return ret;
	}
	
	// Internal! but can be called to get similarities!
	public List<WordSim> knn(float[] vec, int k, boolean withScores) {
		PriorityQueue<WordSim> kSimilarWords = new PriorityQueue<WordSim>(k);
		for (Entry<String, int[]> e : this.entrySet()) {
			
			float[] e_vec = convertVector(e.getValue(), true);
			
			WordSim sim = new WordSim(e.getKey(), VectorMath.cosineSimilarity(vec, e_vec));
			kSimilarWords.add(sim);
		
		}
		List<WordSim> col = new ArrayList<WordSim>();
		//col.clear();
		for (int i=0; i < k; i++  ) {
				col.add(kSimilarWords.poll());		
		}
		return col;
	}
	
	
	public static float[] convertVector(int[] input, boolean decompress) {

		if (decompress) {		
			return convertVector(Compression.decompress(input, 5000));
		} else {
			return convertVector(input);
		}

	}

	public static float[] convertVector(int[] input)
	{
		if (input == null)
		{
			return new float[5000]; // Or throw an exception?
		}

		float[] output = new float[input.length];

		for (int i = 0; i < input.length; i++)
		{
			output[i] = input[i];
		}
		return output;
	}

}



