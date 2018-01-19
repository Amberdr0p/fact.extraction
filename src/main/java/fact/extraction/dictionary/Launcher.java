package fact.extraction.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import utils.ProcessingFile;

public class Launcher {
	
	private static List<Pair<String, String>> listNE = new ArrayList<Pair<String, String>>();

	public static void main(String[] args) throws IOException {
		/*DialogNE.processingDialogSource(listNE);

		for (Pair<String, String> pair : listNE) {
			System.out.println(pair.getLeft() + " " + pair.getRight());
		} */
		
		KBNE.processingKB(listNE);
		DialogNE.processingDialogSource(listNE);
		ProcessingFile.writeToFile("listNE", listNE);
	}
	
}
