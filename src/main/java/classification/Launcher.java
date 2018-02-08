package classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class Launcher {

	private final static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("datasets//dataSetListVectors4.arff"));
		Instances data = new Instances(reader);
		reader.close();
		if (data.classIndex() == -1)
			data.setClassIndex(0);

		Instances[][] split = crossValidationSplit(data, 10);
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];

		// SMO smo = new SMO();
		// smo.buildClassifier(data);
		Classifier[] models = { new SMO()// new IBk(), new J48(), new PART(), new NaiveBayes(), new OneR(), new SMO(),
				// new Logistic(), new AdaBoostM1(), new LogitBoost(), new DecisionStump()
		};

		// for each model
		for (int j = 0; j < models.length; j++) {
			List<Prediction> predictions = new ArrayList<Prediction>();
			for (int i = 0; i < trainingSplits.length; i++) {
				Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);
				predictions.addAll(validation.predictions());
			}
			double accuracy = calculateAccuracy(predictions);
			logger.info("Accuracy of {}: {}", models[j].getClass().getSimpleName(), accuracy);
		}
	}

	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];
		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}
		return split;
	}

	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);
		return evaluation;
	}

	public static double calculateAccuracy(List<Prediction> predictions) {
		double correct = 0;
		for (Prediction prediction : predictions) {
			if (prediction.predicted() == prediction.actual()) {
				correct++;
			}
		}
		return 100 * correct / predictions.size();
	}

}
