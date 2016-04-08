package de.wenzlaff.phonebook;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Fritzbox Phonebook (http://www.wehavemorefun.de/fritzbox/Phonebook) bearbeiten.
 * 
 * Bearbeitet alle Telefonnummern:
 * 
 * Entfernt: blank, (, ) - also aus (0)511 23456-78 wird 05112345678 und alle Telefonnummern die ohne +49 für Deutschland und mit 0 starten werden
 * 
 * zu +49 ohne 0 also z.B. aus 051123456 wird +4051123456
 *
 * Diese Abhängigkeit wird benötigt.
 * 
 * <pre>
         <dependency>
            <groupId>org.jdom</groupId>
            <artifactId>jdom</artifactId>
            <version>2.0.2</version>
        </dependency>
 * </pre>
 *
 * @author Thomas Wenzlaff http://www.wenzlaff.info
 * @version 0.1
 */
public class Converter {

	private static final String HILFE = "Aufruf: de.wenzlaff.phonebook.Converter [Fritzbox Phonebook Datei Name] [output Datei Name]";

	/**
	 * Startmethode. Aufruf de.wenzlaff.phonebook.Converter [input Datei Name] [output Datei Name]
	 * 
	 * @param args
	 *            Aufruf: [input Datei Name] [output Datei Name]
	 * @throws Exception
	 *             bei Fehler
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println(HILFE);
			return;
		}

		String inputDatei = args[0];
		String outputDatei = args[1];

		if (inputDatei.isEmpty() || outputDatei.isEmpty()) {
			System.err.println(HILFE);
			return;
		}

		File inDatei = new File(inputDatei);
		if (!(inDatei.isFile() && inDatei.canRead())) {
			System.err.println("Fritzbox Phonebook Datei kann nicht gelesen werden. " + HILFE);
			return;
		}

		File ausDatei = new File(outputDatei);

		System.out.println("Starte das einlesen der Fritzbox Phonebook " + inDatei.getAbsolutePath() + " Datei und schreibe in Datei " + ausDatei.getAbsolutePath());

		Document dokument = new SAXBuilder().build(inDatei);
		Element rootPhoneBooks = dokument.getRootElement();

		int count = 0;

		List<Element> phonebooks = rootPhoneBooks.getChildren("phonebook");
		for (Element phonebook : phonebooks) {
			List<Element> contacts = phonebook.getChildren("contact");
			for (Element contact : contacts) {
				List<Element> telephonys = contact.getChildren("telephony");
				for (Element nummern : telephonys) {
					List<Element> numbers = nummern.getChildren("number");
					for (Element number : numbers) {
						bearbeiteTelefonNummer(number);
						count++;
					}
				}
			}
		}

		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(dokument, new FileWriter(ausDatei));
		System.out.println("Umwandlung von " + count + " Telefonnummern beendet. Ergebnis in " + ausDatei.getAbsolutePath());
	}

	private static void bearbeiteTelefonNummer(Element number) {
		String orginalNr = number.getText();
		// Entferne: blank, (, ) -
		String neueNr = orginalNr.replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "");
		// alle Nummern die ohne +49 für Deutschland und mit 0 starten werden zu +49 ohne 0
		String ergebnisNr = "";
		if (!neueNr.startsWith("+") && neueNr.startsWith("0")) {
			ergebnisNr = "+49" + neueNr.substring(1, neueNr.length());
		}
		number.setText(ergebnisNr);
	}

}