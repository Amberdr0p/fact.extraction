package tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import utils.ProcessingFile;

public class Launcher {

	private static int SHIFT = 2000;

	public static void main(String[] args) throws IOException {
		Set<Pair<String, String>> setSentTag = new TreeSet<Pair<String, String>>();
		List<Pair<String, String>> listNETag = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> listNE = ProcessingFile.readDictionary("listNE");

		MaxentTagger tagger = new MaxentTagger("C://Users//Ivan//Desktop//NLP//russian-ud-mfmini.tagger");

		for (int i = 0; i < listNE.size(); i++) {
			Pair<String, String> lineNE = listNE.get(i);

			// возможно надо поменять, разделитель слов пробел
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
				// System.out.println(res);
				String[] resT = res.split("_");
				if (resT.length == 2) {
					resLineOnlyTag.append(resT[1]);
					resLineTag.append(res);
				} else {
					System.out.println("EEEEE " + res);
				}
			}

			setSentTag.add(new MutablePair<String, String>(resLineOnlyTag.toString(), lineNE.getRight()));
			listNETag.add(new MutablePair<String, String>(resLineTag.toString(), lineNE.getRight()));
			// System.out.println(resLine.toString());
			if (i % 2000 == 0) {
				ProcessingFile.writeToEndFile("neWithTags.txt", listNETag);
				listNETag.clear();
				System.out.println("Processed " + i + " NE");
			}
		}

		ProcessingFile.writeToFile("templatesTagNE.txt", setSentTag);
	}

}
