package org.insight.semeval;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.insight.word2vec.DiskWord2Vec;
import org.insight.word2vec.RI2Vec;
import org.insight.word2vec.Word2Vec;
import org.insight.word2vec.WordVectorSpace;
import org.insight.word2vec.util.GloveTextModelLoader;
import org.insight.word2vec.util.ModelLoader;
import org.insight.word2vec.util.VectorMath;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeMemoryInput;


public class ModelComparator {

	public static void main(String[] args) throws IOException {

		// Load Data:

		List<Sentence> sentences = new ArrayList<Sentence>();

		try {

			LineIterator lines = FileUtils.lineIterator(new File("/home/igor/git/SemEval2014-Task1/data/SICK_test_annotated.txt"));
			// skip header:
			lines.next();

			while (lines.hasNext()) {
				sentences.add(new Sentence(lines.next()));
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		// Generate Similarities:

		Map<String, WordVectorSpace> models = new HashMap<String,WordVectorSpace>();
		
		models.put("w2v", ModelLoader.load("/home/igor/git/SemEval2014-Task1/models/w2v.5.300.bin"));
		
		models.put("glove", GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/glove.5.300"));
		
		
		
	//	models.add(GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/vectors.6B.200d"));
	//	models.add(ModelLoader.load("/home/igor/git/word2vec-java/src/main/resources/vectors.bin"));
	//	models.add(GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/vectors.840B.300d"));
		
	//	models.add();
		
		//DiskWord2Vec model = new DiskWord2Vec("/fastdata/wikipedia-pubmed-and-PMC-w2v.bin");
		
		/*
		 * W2V
		 */
		
		//Word2Vec model = ModelLoader.load("/home/igor/git/SemEval2014-Task1/models/w2v.5.300.bin");
		
		/*
		 * Glove
		 */
		
		//Word2Vec model = GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/glove.5.300");

		
				
		/*
		 * RI:
		 * 
		 */
		
		System.out.println("Loading from file: " + "/home/igor/git/SemEval2014-Task1/models/ri.5.5000.kryo");
			
		// KRYO Store:
		RI2Vec clazz = new RI2Vec();
		Kryo kryo = new Kryo();
		kryo.register(clazz.getClass());
		UnsafeMemoryInput input = new UnsafeMemoryInput(new FileInputStream("/home/igor/git/SemEval2014-Task1/models/ri.5.5000.s.kryo"));
							
		RI2Vec mdl = kryo.readObject(input, clazz.getClass());
		
		System.out.println("Loaded model: " + ".kryo");

		input.close();
				
				/*
				 * 
				 */

		
		models.put("ridxs", mdl);
		
		
		
		// For large models:
		// Preload vectors:
		/*
		HashSet<String> words = new HashSet<String>();
		for (Sentence sentence : sentences) {
			words.addAll(Arrays.asList(sentence.sA.toLowerCase().split(" ")));
			words.addAll(Arrays.asList(sentence.sB.toLowerCase().split(" ")));
		}
		
		System.out.println("Total Words to Scan: " + words.size());
		
		model.preCache(words);
*/
						
		for (Entry<String, WordVectorSpace> e : models.entrySet()) {
			
			WordVectorSpace model = e.getValue();
			
			File outputFile = new File("/home/igor/git/SemEval2014-Task1/results/Insight/"+ e.getKey() +"-text8.txt");
			File outputFileRaw = new File("/home/igor/git/SemEval2014-Task1/results/Insight/"+ e.getKey() +"-text8-raw.txt");

			List<Sentence> results_sentences = new ArrayList<Sentence>();

			// Write Results Header:
			
			FileUtils.writeStringToFile(outputFile, "pair_ID\tentailment_judgment\trelatedness_score" + "\n", false);
			FileUtils.writeStringToFile(outputFileRaw, "pair_ID\tentailment_judgment\trelatedness_score" + "\n", false);
			
			for (Sentence sentence : sentences) {
				
				double rawSim = VectorMath.cosineSimilarity(
						model.sentenceVector(false, sentence.sA),
						model.sentenceVector(false, sentence.sB)
						);
				
				float similarity = interpolateScore(rawSim);
				
				Sentence newSentence = sentence.withSimilarity(similarity);
				
				//results_sentences.add();
			
				String resultline = String.format("%s\tNA\t%s\n", newSentence.id, newSentence.similarity);
				FileUtils.writeStringToFile(outputFile, resultline, true);
			
			
				String resultlineRaw = String.format("%s\tNA\t%s\t%s\n", newSentence.id, newSentence.similarity, rawSim);
				FileUtils.writeStringToFile(outputFileRaw, resultlineRaw, true);
			}
			
			
							
			//for (Sentence result : results_sentences) {
			//}

		}

	}

	/*
	 * Cosine Similarity to 1..5 Scale:
	 */
	public static float interpolateScore(double similarity) {
		//SemEVAL sentence relatedness:
		//       cos: 0 ..                1
		// Unrelated: 1 .. 2 .. 3 .. 4 .. 5 ..Same
		
		double score = 1.0;
		
		
		if (similarity < 0.1D) {
			return 1.0f;
		} else {
			return (float) ((similarity * 4) + 1);
		}
		
				
		
		
		
		
	}

	public static double normalizeScore(float score) {				
		return (score - 1) / 4;		
	}
	
	
	


}
