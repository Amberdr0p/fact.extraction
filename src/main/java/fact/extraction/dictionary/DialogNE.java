package fact.extraction.dictionary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import utils.ProcessingFile;

public class DialogNE {

	public static void processingDialogSource(List<Pair<String, String>> listNE) throws IOException {
		List<File> folders = new ArrayList<File>() {
			{
				add(new File("C://Users//Ivan//workspace//dialogue-21//factRuEval-2016//devset"));
				add(new File("C://Users//Ivan//workspace//dialogue-21//factRuEval-2016//testset"));
			}
		};

		for (File folder : folders) {
			File[] files = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".objects");
				}
			});

			for (File file : files) {
				processingFile(listNE, file);
				System.out.println("Processing file " + file.getName() + " is complete");
			}
		}
	}

	private static void processingFile(List<Pair<String, String>> listNE, File file) throws IOException {
		List<String> list = ProcessingFile.readFile(file);
		processingList(listNE, list);
	}

	private static List<String> processingList(List<Pair<String, String>> listNE, List<String> list) {
		List<String> res = new ArrayList<String>();
		for (String str : list) {
			String ne = str.substring(str.indexOf("#") + 2);

			int startType = str.indexOf(" ") + 1;
			String type = str.substring(startType, str.indexOf(" ", startType));
			if (type == "LocOrg") {
				type = "Organisa";
				addToList(listNE, ne, "Org");
				addToList(listNE, ne, "Location");
			} else {
				addToList(listNE, ne, type);
			}
		}
		return res;
	}

	private static void addToList(List<Pair<String, String>> listNE, String ne, String type) {
		Pair<String, String> pair = new MutablePair(ne, type);
		if (!listNE.contains(pair)) {
			listNE.add(pair);
		}
	}

}
