package extraction;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fact.extraction.dictionary.DialogNE;
import utils.ProcessingFile;

public class Test1 {

	private static final Pattern pattern = Pattern.compile("^[А-яЁёA-z0-9]+$"); // ???????

	private final static Logger logger = LoggerFactory.getLogger(Test1.class);

	public static void main(String[] args) throws IOException {

		logger.info("Start testing {}.", 4);

		logger.info("Start init dictionary and tagger..");
		Map<String, List<Pair<String, Integer>>> map = initDictionary();
		MaxentTagger tagger = new MaxentTagger("C://Users//Ivan//Desktop//NLP//russian-ud-mfmini.tagger");
		logger.info("End init dictionary and tagger..");

		test1(map, tagger);

		logger.info("End testing.");
	}

	private static void test1(Map<String, List<Pair<String, Integer>>> map, MaxentTagger tagger) throws IOException {
		List<File> folders = new ArrayList<File>() {
			{
				add(new File("C://Users//Ivan//workspace//dialogue-21//factRuEval-2016//devset"));
				add(new File("C://Users//Ivan//workspace//dialogue-21//factRuEval-2016//testset"));
			}
		};

		for (File folder : folders) {
			File[] filesObjects = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".objects");
				}
			});
			File[] filesTokens = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".tokens");
				}
			});

			for (int i = 0; i < filesObjects.length; i++) {
				processingFiles(map, tagger, filesObjects[i], filesTokens[i]);
				logger.info("Processing files {}  and {}  is complete", filesObjects[i].getName(),
						filesTokens[i].getName());
			}
		}
	}

	private static void processingFiles(Map<String, List<Pair<String, Integer>>> map, MaxentTagger tagger,
			File filesObjects, File filesTokens) throws IOException {
		// List<List<String>> sentences = p
		logger.info("Start processing file {}", filesTokens.getName());
		List<Sentence> sentences = new ArrayList<Sentence>();
		preprocessingSentences(sentences, filesTokens);
		List<Pair<String, String>> listFact = getListFacts(filesObjects);

		// максимальная длина последовательности NER из тэгов
		int maxLength = 12;

		for (Sentence sent : sentences) {
			List<String> sentence = sent.getWords();
			List<String> tagsSent = getTagsSent(tagger, sentence);
			sent.setTagsSent(tagsSent);

			for (int i = 0; i < tagsSent.size(); i++) {
				for (int j = maxLength > tagsSent.size() - i ? tagsSent.size() - i : maxLength; j > 0; j--) {
					String templTagS = String.join(" ", tagsSent.subList(i, i+j)).trim();
					if (map.containsKey(templTagS)) {
						String ne = String.join(" ", sentence.subList(i, i+j)).trim();
						Pair<String, Integer> pair = getTopNoThingPairType(map.get(templTagS));
						logger.info("NER: {}, TAGs: {}, Type: {}, CountType: {}", ne, templTagS, pair.getLeft(), String.valueOf(pair.getRight()));
						sent.addNE(new MutablePair<String, String>(ne, pair.getLeft()));
						i += j;
					}
				}
			}
		}
		logger.info("End processing file {}", filesTokens.getName());
		
		logger.info("Found named entities:");
		for(Sentence s : sentences) {
			List<Pair<String,String>> nes = s.getNEs();
			for(Pair<String,String> ne : nes) {
				logger.info("NE: {}, Type: {}", ne.getLeft(), ne.getRight());
			}
		}
		logger.info("Specified named entities:");
		for(Pair<String,String> pair : listFact) {
			logger.info("NE: {}, Type: {}", pair.getLeft(), pair.getRight());
		}
	}

	private static List<String> getTagsSent(MaxentTagger tagger, List<String> sentence) {
		List<String> tagsSent = new ArrayList<String>();
		for (String token : sentence) {
			String res = tagger.tagTokenizedString(token);
			String[] resT = res.split("_");
			if (resT.length == 2) {
				tagsSent.add(resT[1]);
			} else {
				logger.error("Word haven't tag: {}", res);
			}
		}

		return tagsSent;
	}

	private static List<Pair<String, String>> getListFacts(File filesObjects) throws IOException {
		List<String> list = ProcessingFile.readFile(filesObjects);
		List<Pair<String, String>> listNE = new ArrayList<Pair<String, String>>();
		for (String str : list) {
			String ne = str.substring(str.indexOf("#") + 2);

			int startType = str.indexOf(" ") + 1;
			String type = str.substring(startType, str.indexOf(" ", startType));
			if (type == "LocOrg") {
				type = "Organisa";
				listNE.add(new MutablePair<String, String>(ne, "Org"));
				listNE.add(new MutablePair<String, String>(ne, "Location"));
			} else {
				listNE.add(new MutablePair<String, String>(ne, type));
			}
		}
		return listNE;
	}

	private static void preprocessingSentences(List<Sentence> sentences, File file) throws IOException {
		List<String> list = ProcessingFile.readFile(file);

		List<String> sentenceWords = new ArrayList<String>();
		List<String> sourceWords = new ArrayList<String>();
		List<Integer> indexes = new ArrayList<Integer>();
		int i = 0;
		for (String line : list) {
			if (line.isEmpty()) {
				sentences.add(new Sentence(sourceWords, sentenceWords, indexes));

				sourceWords = new ArrayList<String>();
				sentenceWords = new ArrayList<String>();
				indexes = new ArrayList<Integer>();
				i = 0;
			} else {
				String word = line.split(" ")[3];
				sourceWords.add(word);
				if (word.length() != 1 || matchOneSymbolWord(word)) {
					sentenceWords.add(word);
					indexes.add(i);
				}
			}
			i++;
		}
	}

	private static boolean matchOneSymbolWord(String word) {
		return pattern.matcher(word).matches();
	}

	private static Map<String, List<Pair<String, Integer>>> initDictionary() throws IOException {
		Map<String, List<Pair<String, Integer>>> map = new HashMap<String, List<Pair<String, Integer>>>();
		List<String> list = ProcessingFile.readFile(new File("templatesTagNEStatistic.txt"));
		for (String line : list) {
			String[] splitLine = line.split("\t");
			Pair<String, Integer> pair = new MutablePair<String, Integer>(splitLine[1], Integer.valueOf(splitLine[2]));
			if (map.containsKey(splitLine[0])) {
				map.get(splitLine[0]).add(pair);
			} else {
				map.put(splitLine[0], new ArrayList<Pair<String, Integer>>() {
					{
						add(pair);
					}
				});
			}
		}
		return map;
	}

	private static Pair<String, Integer> getTopNoThingPairType(List<Pair<String, Integer>> listTypes) {
		if (listTypes.size() == 1) {
			return listTypes.get(0);
		} else {
			Pair<String, Integer> topPair = new MutablePair<String, Integer>("", 0);// listTypes.get(0);
			for (int i = 1; i < listTypes.size(); i++) {
				if (!listTypes.get(i).getLeft().equals("Thing") && listTypes.get(i).getRight() > topPair.getRight()) {
					topPair = listTypes.get(i);
				}
			}
			return topPair;
		}
	}

}
