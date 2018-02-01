package tagger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import utils.ProcessingFile;

public class GenTemplates {

	public static void main(String[] args) throws IOException {
		Map<Pair<String, String>, Integer> map = new HashMap<Pair<String, String>, Integer>();
		Set<Pair<String, String>> setSentTag = new TreeSet<Pair<String, String>>();
		List<String> listWithTags = ProcessingFile.readFile(new File("neWithTags.txt"));
		for (String lineWithTags : listWithTags) {
			String[] lineWT = lineWithTags.split("\t\t\t");
			String[] words = lineWT[0].split(" ");
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			for (String word : words) {
				if (!isFirst) {
					sb.append(" ");
				}
				isFirst = false;
				String[] wordAndTag = word.split("_");
				sb.append(wordAndTag[1]);
			}
			if (lineWT[1].equals("LocOrg")) {
				addPair(map, setSentTag, sb, "Location");
				addPair(map, setSentTag, sb, "Org");
			} else {
				addPair(map, setSentTag, sb, lineWT[1]);
			}

		}
		ProcessingFile.writeToFile("templatesTagNE.txt", setSentTag);
		ProcessingFile.writeToFile("templatesTagNEStatistic.txt", sortByValue(map));
	}

	private static void addPair(Map<Pair<String, String>, Integer> map, Set<Pair<String, String>> setSentTag,
			StringBuilder sb, String type) {
		Pair<String, String> pair = new MutablePair<String, String>(sb.toString(), type);
		setSentTag.add(pair);
		if (map.containsKey(pair)) {
			map.replace(pair, map.get(pair).intValue() + 1);
		} else {
			map.put(pair, 1);
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

}
