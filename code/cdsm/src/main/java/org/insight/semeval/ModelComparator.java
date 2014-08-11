package org.insight.semeval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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
		models.add(GloveTextModelLoader.load("/home/igor/git/SemEval2014-Task1/models/vectors.840B.300d"));
		
		
		for (Word2Vec model : models) {
			
			File outputFile = new File("/home/igor/git/SemEval2014-Task1/results/Insight/Insight_w2v_glove840b.txt");

			List<Sentence> results_sentences = new ArrayList<Sentence>();

			for (Sentence sentence : sentences) {
				float similarity = interpolateScore(
						VectorMath.cosineSimilarity(
								VectorMath.normalize(model.sentenceVector(sentence.sA)),
								VectorMath.normalize(model.sentenceVector(sentence.sB)))
						);
				results_sentences.add(sentence.withSimilarity(similarity));
			}
			
			
			// Write Results Header:
			
			FileUtils.writeStringToFile(outputFile, "pair_ID\tentailment_judgment\trelatedness_score" + "\n", false);
						
			for (Sentence result : results_sentences) {
				String resultline = String.format("%s\tNA\t%s\n", result.id, result.similarity);
				FileUtils.writeStringToFile(outputFile, resultline, true);
			}

		}

	}

	/*
	 * Cosine Similarity to 1..5 Scale:
	 */
	public static float interpolateScore(double similarity) {
		//SemEVAL sentence relatedness:
		//       cos: 0 ..                1
		// Unrelated: 1 .. 2 .. 3 .. 4 .. 5 ..Same
		double score = (similarity <= 0.0D) ? 0.0D : similarity;
		return (float) ((score * 4) + 1);		
	}

	public static double normalizeScore(float score) {				
		return (score - 1) / 4;		
	}

}
