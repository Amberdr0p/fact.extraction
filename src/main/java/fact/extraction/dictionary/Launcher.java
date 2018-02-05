package fact.extraction.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import utils.ProcessingFile;

public class Launcher {
	
	

	public static void main(String[] args) throws IOException {
		/*DialogNE.processingDialogSource(listNE);

		for (Pair<String, String> pair : listNE) {
			System.out.println(pair.getLeft() + " " + pair.getRight());
		} */
		
		List<Pair<String, String>> listNE = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> otherTokens = new ArrayList<Pair<String, String>>();
		
		KBNE.processingKB(listNE);
		DialogNE.processingDialogSource(listNE, otherTokens);
		ProcessingFile.writeToFile("listNE", listNE);
	}
	
}
