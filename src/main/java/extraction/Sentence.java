package extraction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class Sentence {
	
	List<String> sourceWords;
	List<String> words; // preproc
	List<String> tags;
	List<Integer> indexesTags;
	
	List<Pair<String, String>> nes = new ArrayList<Pair<String, String>>(); // найденные
	
	public Sentence(List<String> sourceWords, List<String> words, List<Integer> indexesTags) {
		this.sourceWords = sourceWords;
		this.words = words;
		this.indexesTags = indexesTags;
	}
	
	public List<String> getWords() {
		return words;
	}
	
	public void setTagsSent(List<String> tags) {
		this.tags = tags;
	}
	
	public void addNE(Pair<String, String> NE) {
		nes.add(NE);
	}
	
	public List<Pair<String, String>> getNEs() {
		return nes;
	}
	
}
