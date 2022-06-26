/*
 *  Copyright 2009 Giles Burgess
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.copalis.xml.samples;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.copalis.xml.DocBinder;
import org.copalis.xml.SAXHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class TestSAXHandler {
	
	public static InputStream input() {
		return TestSAXHandler.class.getResourceAsStream("people.xml");
	}

	public static void main(String... args) throws Exception {
		PeopleSchemaImpl impl = new PeopleSchemaImpl();
		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(new SAXHandler(PeopleSchema.class, impl));
		parser.parse(new InputSource(input())); //new StringReader(text)));
		impl.print();

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(new SAXHandler(PeopleSchema.class,
				DocBinder.bind(PeopleSchema.class, doc)));
		parser.parse(new InputSource(input()));
        TransformerFactory.newInstance().newTransformer()
				.transform(new DOMSource(doc), new StreamResult(System.out));
	}
}
