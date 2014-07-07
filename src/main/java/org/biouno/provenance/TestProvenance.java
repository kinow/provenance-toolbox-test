package org.biouno.provenance;

import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.WasStartedBy;
import org.openprovenance.prov.xml.ProvFactory;
import org.openprovenance.prov.xml.ProvSerialiser;

public class TestProvenance {

	public static final String PC1_NS = "http://www.ipaw.info/pc1/";
	public static final String PC1_PREFIX = "pc1";
	public static final String PRIM_NS = "http://openprovenance.org/primitives#";
	public static final String PRIM_PREFIX = "prim";

	class A {
		public String name;
	}

	public static QualifiedName q(ProvFactory factory, String n) {
		return factory.newQualifiedName(PC1_NS, n, PC1_PREFIX);
	}

	public static XMLGregorianCalendar xmlgcal(Date d) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(d);
		XMLGregorianCalendar date2;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			return date2;
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Entity newFile(ProvFactory factory, String id, String label,
			String file, String location) {
		Entity a = factory.newEntity(q(factory, id), label);
		factory.addType(a,
				URI.create("http://openprovenance.org/primitives#File"));

		addUrl(factory, a, location + file);
		return a;
	}

	public static void addUrl(ProvFactory factory, HasOther p1, String val) {
		p1.getOther().add(
				factory.newOther(PC1_NS, "url", PC1_PREFIX, val,
						factory.getName().XSD_STRING));
	}

	public static void main(String[] args) throws Exception {
		ProvFactory factory = new ProvFactory();
		Document doc = factory.newDocument();

		List<StatementOrBundle> stsBundles = doc.getStatementOrBundle();

		// workflow step
		Activity step1 = factory.newActivity(q(factory, "step1"),
				"Invoke Maven build step");
		step1.setStartTime(xmlgcal(new Date()));
		factory.addType(step1, "mvn clean test install",
				factory.getName().XSD_STRING);
		step1.setEndTime(xmlgcal(new Date()));

		// committer
		Agent agent = factory.newAgent(q(factory, "Bruno P. Kinoshita"));
		WasStartedBy startedBy = factory
				.newWasStartedBy(q(factory, "Bruno P. Kinoshita"));

		// image generated by workflow
		// Entity inputFile = factory.newEntity(q("pom.xml"));
		//inputFile.getType().add(factory.newType("size", q("size")));
		Entity inputFile = newFile(factory, "pom.xml", "Maven POM", "pom.xml", "/tmp");
	
		stsBundles.add(step1);
		stsBundles.add(agent);
		stsBundles.add(inputFile);

		doc.setNamespace(Namespace.gatherNamespaces(doc));
		// System.out.println("Default ns is: " + nss.getDefaultNamespace());
		// System.out.println("All prefixes: " + nss.getPrefixes());
		// System.out.println("All ns: " + nss.getNamespaces());

		ProvSerialiser serial = ProvSerialiser.getThreadProvSerialiser();
		Namespace.withThreadNamespace(doc.getNamespace());
		// serial.serialiseDocument(new File("test.xml"), doc, true);
		StringWriter sw = new StringWriter();
		serial.serialiseDocument(sw, doc, true);
		System.out.println(sw.toString());
		
		//https://github.com/lucmoreau/ProvToolbox/blob/master/prov-xml/src/test/java/org/openprovenance/prov/xml/PC1FullTest.java
	}

}
