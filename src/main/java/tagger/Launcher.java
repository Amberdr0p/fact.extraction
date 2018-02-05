package tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import extraction.Test1;
import utils.ProcessingFile;

public class Launcher {

	private static int SHIFT = 2000;
	private final static Logger logger = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args) throws IOException {
		// String log4j = Launcher.class.getClassLoader().getResource("log4j.properties").getPath();
		// PropertyConfigurator.configure(log4j);

		Set<Pair<String, String>> setSentTag = new TreeSet<Pair<String, String>>();
		List<Pair<String, String>> listNETag = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> listNE = ProcessingFile.readDictionary("listNE");

		MaxentTagger tagger = new MaxentTagger("C://Users//Ivan//Desktop//NLP//russian-ud-mfmini.tagger");

		for (int i = 818001; i < listNE.size(); i++) {
			Pair<String, String> lineNE = listNE.get(i);

			StringBuffer resLineOnlyTag = new StringBuffer();
			StringBuffer resLineTag = new StringBuffer();
			String[] tokens = lineNE.getLeft().split(" ");
			boolean isFirst = true;

			for (String token : tokens) {
				if (!isFirst) {
					resLineOnlyTag.append(" ");
					resLineTag.append(" ");
				}
				String res = tagger.tagTokenizedString(token);
				String[] resT = res.split("_");
				if (resT.length == 2) {
					resLineOnlyTag.append(resT[1]);
					resLineTag.append(res);
				} else {
					logger.info("Not found tag: " + res);
				}
			}

			setSentTag.add(new MutablePair<String, String>(resLineOnlyTag.toString(), lineNE.getRight()));
			listNETag.add(new MutablePair<String, String>(resLineTag.toString(), lineNE.getRight()));

		}
		ProcessingFile.writeToEndFile("neWithTags2.txt", listNETag);

		ProcessingFile.writeToFile("templatesTagNE2.txt", setSentTag);
	}

}
