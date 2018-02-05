package w2v;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.extraction.dictionary.DialogNE;
import utils.ProcessingFile;

public class Launcher {

	private final static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args) throws IOException {
		File gModel;
		if (args.length == 0) {
			gModel = new File("C://Users//Ivan//workspace//W2V//project//model1.txt");
		} else {
			gModel = new File(args[0]);
		}
		Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);

		 /*Collection<String> lst3 = vec.wordsNearest("Сергей", 10);
		 boolean s = vec.hasWord("Сергей");
		 INDArray adr = vec.getWordVectorsMean(lst3);
		 Collection<String> lst33 = vec.similarWordsInVocabTo("сергей", 0.7);
		 //vec.
		 System.out.println(lst3); */

		// double[] wordVector = vec.getWordVector("дом");
		// for(double val : wordVector) {
		// System.out.print(val);
		// }

		// List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		// DialogNE.processingDialogSource(list);
		List<Pair<String, String>> list = ProcessingFile.readDictionary("listDialogNE.txt");
		List<String> resList = new ArrayList<String>();

		logger.info("Start adding vectors..");
		for (Pair<String, String> pair : list) {
			String vectorString = genVectorForNE(pair.getLeft(), vec);
			if (vectorString != null) {
				resList.add(pair.getRight() + " # " + vectorString);
			}
		}
		ProcessingFile.writeToFile("neListVectors.txt", resList);
		
		List<Pair<String, String>> otherTokens = ProcessingFile.readDictionary("otherTokens.txt");
		List<String> resOtherTokens = new ArrayList<String>();
		for (Pair<String, String> pair : otherTokens) {
			String vectorString = genVectorForNE(pair.getLeft(), vec);
			if (vectorString != null) {
				resOtherTokens.add(pair.getRight() + " # " + vectorString);
			}
		}
		ProcessingFile.writeToFile("otherListVectors.txt", resOtherTokens);
	}

	private static String genVectorForNE(String neStr, Word2Vec vec) {
		neStr = neStr.toLowerCase();
		String[] ne = neStr.split(" ");
		double[] averageVector = new double[100];
		Arrays.fill(averageVector, 0);
		for (String word : ne) {
			double[] wordVector = vec.getWordVector(word);
			if (wordVector == null) {
				logger.info("Vector for word {} = null. ({})", word, ne);
				return null;
			}
			sumAverageArrays(averageVector, wordVector, ne.length);
			logger.info(vectorToString(averageVector));
		}
		String vectorString = vectorToString(averageVector);
		logger.info("Vector for NE {}: {}", ne, vectorString);

		return vectorString;
	}

	private static String vectorToString(double[] wordVector) {
		StringBuilder sb = new StringBuilder();
		for (double val : wordVector) {
			sb.append(val).append(" ");
		}
		return sb.toString();
	}

	private static void sumAverageArrays(double[] arr1, double[] arr2, double size) {
		if (arr1.length == arr2.length) {
			for (int i = 0; i < arr1.length; i++) {
				arr1[i] += arr2[i] / size;
			}
		} else {
			logger.error("!!!!!!!!!!!!!!!      arr1 != arr2         !!!!!!!!!");
		}
	}

}
