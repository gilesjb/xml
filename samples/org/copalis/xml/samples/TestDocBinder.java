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

import org.copalis.xml.DocBinder;
import org.copalis.xml.samples.util.DocumentModel;
import org.w3c.dom.Document;


public class TestDocBinder {

	private static void write(PeopleSchema s) {
		s._(s.people()
			._(s.person().first("Giles").last("Burgess").age(40))
			._(s.person("Name unknown"))
			._(s.person().first("Olive")));
	}

	public static void main(String... args) throws Exception {
		PeopleSchemaImpl impl = new PeopleSchemaImpl();
		write(impl);
		impl.print();
		
		Document doc = DocumentModel.create();
		write(DocBinder.bind(PeopleSchema.class, doc));
		DocumentModel.print(doc);
	}
}
