package fact.extraction.dictionary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import extraction.Test1;
import utils.ProcessingFile;

public class DialogNE {

	private final static Logger logger = LoggerFactory.getLogger(DialogNE.class);

	public static void main(String[] args) throws IOException {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> otherTokens = new ArrayList<Pair<String, String>>();
		processingDialogSource(list, otherTokens);
		ProcessingFile.writeToFile("listDialogNE.txt", list);
		ProcessingFile.writeToFile("otherTokens.txt", otherTokens);
	}

	public static void processingDialogSource(List<Pair<String, String>> listNE, List<Pair<String, String>> otherTokens)
			throws IOException {
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
			File[] filesSpans = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".spans");
				}
			});
			File[] filesTokens = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".tokens");
				}
			});

			Arrays.sort(filesObjects);
			Arrays.sort(filesSpans);
			Arrays.sort(filesTokens);

			for (int i = 0; i < filesObjects.length; i++) {
				processingFile(listNE, otherTokens, filesObjects[i], filesSpans[i], filesTokens[i]);
				logger.info("Processing files {}, {}, {} is complete", filesObjects[i].getName(),
						filesSpans[i].getName(), filesTokens[i].getName());
			}
		}
	}

	private static void processingFile(List<Pair<String, String>> listNE, List<Pair<String, String>> otherTokens, File filesObjects,
			File filesSpans, File filesTokens) throws IOException {
		List<String> listObjects = ProcessingFile.readFile(filesObjects);
		List<String> listSpans = ProcessingFile.readFile(filesSpans);
		List<String> listTokens = ProcessingFile.readFile(filesTokens);
		processingList(listNE, otherTokens, listObjects, listSpans, listTokens);
	}

	private static void processingList(List<Pair<String, String>> listNE, List<Pair<String, String>> otherTokens,
			List<String> listObjects, List<String> listSpans, List<String> listTokens) {
		Map<Integer, String> mapTokens = getFileTokens(listTokens);
		Map<Integer, List<Integer>> mapSpans = getFileSpans(listSpans);
		List<Pair<List<Integer>, String>> listNEs = getNEs(listObjects);
		List<Integer> usedTokens = new ArrayList<Integer>();
		for (Pair<List<Integer>, String> pairNE : listNEs) {
			String ne = getNETokens(pairNE.getLeft(), mapSpans, mapTokens, usedTokens);

			String type = pairNE.getRight();
			if (type.equals("LocOrg")) {
				addToList(listNE, ne, "Org");
				addToList(listNE, ne, "Location");
			} else {
				addToList(listNE, ne, type);
			}
		}
		for (Map.Entry<Integer, String> entry : mapTokens.entrySet())
		{
		    if(!usedTokens.contains(entry.getKey())) {
		    	otherTokens.add(new MutablePair(entry.getValue(), "Other"));
		    }
		}
	}

	private static List<Pair<List<Integer>, String>> getNEs(List<String> listObjects) {
		List<Pair<List<Integer>, String>> listNE = new ArrayList<Pair<List<Integer>, String>>();

		for (String str : listObjects) {
			if (!str.isEmpty()) {
				String spansIndexNE = str.substring(str.indexOf(" ", str.indexOf(" ") + 1) + 1, str.indexOf("#") - 1);
				String spansIndNE[] = spansIndexNE.split(" ");

				List<Integer> spansInd = Arrays.asList(spansIndNE).stream().mapToInt(Integer::parseInt).boxed()
						.collect(Collectors.toList());
				int startType = str.indexOf(" ") + 1;
				String type = str.substring(startType, str.indexOf(" ", startType));
				if (listNE.isEmpty()) {
					listNE.add(new MutablePair<List<Integer>, String>(spansInd, type));
				} else {
					for (int i = 0; i < listNE.size(); i++) {
						Pair<List<Integer>, String> pair = listNE.get(i);
						List<Integer> list = pair.getLeft();
						boolean containsAll = true;
						if (list.size() > spansInd.size()) {
							for (Integer spanInd : spansInd) {
								if (!list.contains(spanInd)) {
									containsAll = false;
								}
							}
						} else {
							for (Integer indList : list) {
								if (!spansInd.contains(indList)) {
									containsAll = false;
								}
							}
						}
						if (containsAll) {
							if (list.size() < spansInd.size()) {
								listNE.remove(i);
								listNE.add(i, new MutablePair<List<Integer>, String>(spansInd, type));
								logger.info("Replace list: {}, spansInd: {}",
										list.stream().map(Object::toString).collect(Collectors.joining(",")),
										spansInd.stream().map(Object::toString).collect(Collectors.joining(",")));
							} else {
								logger.info("Found containsAll list: {}, spansInd: {}",
										list.stream().map(Object::toString).collect(Collectors.joining(",")),
										spansInd.stream().map(Object::toString).collect(Collectors.joining(",")));
							}
							i += listNE.size();
						} else if (i == listNE.size() - 1) {
							containsAll = true;
							listNE.add(new MutablePair<List<Integer>, String>(spansInd, type));
							i++;
						}
					}
				}
			}
		}

		return listNE;
	}

	private static Map<Integer, List<Integer>> getFileSpans(List<String> listSpans) {
		Map<Integer, List<Integer>> mapSpans = new HashMap<Integer, List<Integer>>(); // id|id_tokens
		for (String span : listSpans) {
			if (!span.isEmpty()) {
				String[] spans = span.split(" ");
				int countTokens = Integer.valueOf(spans[5]);
				List<Integer> listIdTokens = new ArrayList<Integer>();
				for (int i = 0; i < countTokens; i++) {
					listIdTokens.add(Integer.valueOf(spans[i + 8]));
				}

				mapSpans.put(Integer.valueOf(spans[0]), listIdTokens);
			}
		}
		return mapSpans;
	}

	private static Map<Integer, String> getFileTokens(List<String> listTokens) {
		Map<Integer, String> mapTokens = new HashMap<Integer, String>(); // id|token
		for (String tokenStr : listTokens) {
			if (!tokenStr.isEmpty()) {
				String[] token = tokenStr.split(" ");
				mapTokens.put(Integer.valueOf(token[0]), token[3]);
			}
		}
		return mapTokens;

	}

	private static String getNETokens(List<Integer> spansIndNE, Map<Integer, List<Integer>> mapSpans,
			Map<Integer, String> tokens, List<Integer> usedTokens) {
		List<Integer> tokensNE = mapSpans.get(Integer.valueOf(spansIndNE.get(0)));
		for (int i = 1; i < spansIndNE.size(); i++) {
			List<Integer> list = mapSpans.get(Integer.valueOf(spansIndNE.get(i)));
			for (Integer index : list) {
				if (!tokensNE.contains(index)) {
					tokensNE.add(index);
				}
			}
		}
		usedTokens.addAll(tokensNE);
		
		return getNE(tokens, tokensNE);
	}

	private static String getNE(Map<Integer, String> tokens, List<Integer> ne) {
		Collections.sort(ne);
		StringBuilder sbNE = new StringBuilder();
		for (Integer index : ne) {
			sbNE.append(tokens.get(index)).append(" ");
		}
		return sbNE.toString().trim();
	}

	private static void addToList(List<Pair<String, String>> listNE, String ne, String type) {
		Pair<String, String> pair = new MutablePair(ne, type);
		if (!listNE.contains(pair)) {
			listNE.add(pair);
		}
	}

}
