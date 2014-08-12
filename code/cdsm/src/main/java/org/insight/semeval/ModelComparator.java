package org.insight.semeval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.insight.word2vec.DiskWord2Vec;
import org.insight.word2vec.Word2Vec;
import org.insight.word2vec.util.GloveTextModelLoader;
import org.insight.word2vec.util.ModelLoader;
import org.insight.word2vec.util.VectorMath;


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

		List<Word2Vec> models = new ArrayList<Word2Vec>();
		
		
	//	models.add(GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/vectors.6B.200d"));
	//	models.add(ModelLoader.load("/home/igor/git/word2vec-java/src/main/resources/vectors.bin"));
	//	models.add(GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/vectors.840B.300d"));
		
	//	models.add();
		
		DiskWord2Vec model = new DiskWord2Vec("/fastdata/wikipedia-pubmed-and-PMC-w2v.bin");
		
		// Preload vectors:
		
		HashSet<String> words = new HashSet<String>();
		for (Sentence sentence : sentences) {
			words.addAll(Arrays.asList(sentence.sA.toLowerCase().split(" ")));
			words.addAll(Arrays.asList(sentence.sB.toLowerCase().split(" ")));
		}
		
		System.out.println("Total Words to Scan: " + words.size());
		
		model.preCache(words);

						
	//	for (Word2Vec model : models) {
			
			File outputFile = new File("/home/igor/git/SemEval2014-Task1/results/Insight/Insight_w2v_wiki_stopwords.txt");

			List<Sentence> results_sentences = new ArrayList<Sentence>();

			// Write Results Header:
			
			FileUtils.writeStringToFile(outputFile, "pair_ID\tentailment_judgment\trelatedness_score" + "\n", false);

			
			for (Sentence sentence : sentences) {
				float similarity = interpolateScore(
						VectorMath.cosineSimilarity(
								model.sentenceVector(true, sentence.sA),
								model.sentenceVector(true, sentence.sB)
								)
						);
				
				Sentence newSentence = sentence.withSimilarity(similarity);
				
				//results_sentences.add();
			
				String resultline = String.format("%s\tNA\t%s\n", newSentence.id, newSentence.similarity);
				FileUtils.writeStringToFile(outputFile, resultline, true);
			
			
			}
			
			
							
			//for (Sentence result : results_sentences) {
			//}

		}

	//}

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
			return (float) ((similarity * 4) + 0.5);
		}
		
				
		
		
		
		
	}

	public static double normalizeScore(float score) {				
		return (score - 1) / 4;		
	}

}
