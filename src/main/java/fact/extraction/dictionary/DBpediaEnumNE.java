package fact.extraction.dictionary;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DB;

public enum DBpediaEnumNE {
	THING_OWL("http://nerd.eurecom.fr/ontology#Thing", "Thing"), // 480ê
	PERSON_DBO("http://nerd.eurecom.fr/ontology#Person", "Person"), // 150ê
	LOCATION_DBO("http://nerd.eurecom.fr/ontology#Location", "Location"), // 130ê
	ORGANISATION_DBO("http://nerd.eurecom.fr/ontology#Organisation", "Org"), // 28ê
	ANIMAL("http://nerd.eurecom.fr/ontology#Animal", "Animal"); // 16ê

	private final String type;
	private final String strType;

	DBpediaEnumNE(String type, String strType) {
		this.type = type;
		this.strType = strType;
	}

	public String getType() {
		return type;
	}

	public String getStrType() {
		return strType;
	}

	public static DBpediaEnumNE get(String type) {
		if (type != null && !type.isEmpty()) {
			for (DBpediaEnumNE val : DBpediaEnumNE.values()) {
				if (val.type.equals(type)) {
					return val;
				}
			}
		}
		return null;
	}

}
