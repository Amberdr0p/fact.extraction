package fact.extraction.dictionary;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tagger.Launcher;
import utils.ProcessingFile;
import utils.RDFStore;

public class KBNE {

	private static final String QUERY_SELECT_COUNT = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "select (count(?label) as ?count) where {?uri rdfs:label ?label. ?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type.}";
	private static final String QUERY_SELECT_DATA = "select ?uri ?label ?type where {?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label. "
			+ "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type.} "
			+ "ORDER BY ?uri LIMIT ${LIMIT} OFFSET ${OFFSET}";
	private static final Pattern p = Pattern
			.compile("^[А-яЁё\\d\\s\\.\\,\\-\\\\\\/\\№\\'\\’\\!\\+\\&\\—\\«\\»\\\"\\`\\:\\;]*[А-яЁё]+"
					+ "[А-яЁё\\d\\s\\.\\,\\-\\\\\\/\\№\\'\\’\\!\\+\\&\\—\\«\\»\\\"\\`\\:\\;]*$");

	private final static Logger logger = LoggerFactory.getLogger(KBNE.class);
	
	private static final String KEY_COUNT = "count";
	private static final String KEY_URI = "uri";
	private static final String KEY_LABEL = "label";
	private static final String KEY_TYPE = "type";

	private static final int SHIFT = 30000;

	public static void processingKB(List<Pair<String, String>> listNE) {
		RDFStore store = new RDFStore();
		int count = selectCount(store);
		for (int i = 0; i < count; i += SHIFT) {
			ResultSet res = store.select(QUERY_SELECT_DATA.replace("${LIMIT}", String.valueOf(SHIFT))
					.replace("${OFFSET}", String.valueOf(i)));
			if (res != null) {
				while (res.hasNext()) {
					QuerySolution qs = res.next();

					RDFNode labelNode = qs.get(KEY_LABEL);
					RDFNode typeNode = qs.get(KEY_TYPE);
					if (labelNode != null && typeNode != null) {
						String label = labelNode.asLiteral().getString();
						DBpediaEnumNE typeE = DBpediaEnumNE.get(typeNode.asResource().getURI());
						if (typeE != null) {
							String type = typeE.getStrType();

							Pair<String, String> pair = new MutablePair<String, String>(label, type);
							if (!listNE.contains(pair)) {
								listNE.add(pair);
							}
						} else {
							logger.info("Type for label {} equals null: ", label);
						}
					}
				}
			}
			logger.info("Size listNE = {}",listNE.size());
			ProcessingFile.writeToFile("listNE", listNE);
		}
	}

	private static int selectCount(RDFStore store) {
		ResultSet res = store.select(QUERY_SELECT_COUNT);
		while (res != null && res.hasNext()) {
			QuerySolution qs = res.next();
			return qs.get(KEY_COUNT).asLiteral().getInt();
		}

		return 0;
	}
}
