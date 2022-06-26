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

import java.util.LinkedList;
import java.util.List;

public class PeopleSchemaImpl implements PeopleSchema {
	PeopleImpl root;
	
	public static class PeopleImpl implements PeopleSchema.People {
		List<PersonImpl> persons = new LinkedList<PersonImpl>();

		public People _(Person person) {
			persons.add((PersonImpl) person);
			return this;
		}
	}
	
	public static class PersonImpl implements PeopleSchema.Person.Attrs {
		String first;
		String last;
		String text = "";
		Integer age = null;

		public PersonImpl $(String text) {
			this.text += text;
			return this;
		}

		public Attrs first(String name) {
			first = name;
			return this;
		}

		public Attrs last(String name) {
			last = name;
			return this;
		}

		public Attrs age(int age) {
			this.age = age;
			return this;
		}
	}

	public void _(People people) {
		this.root = (PeopleImpl) people;
	}

	public People people() {
		return new PeopleImpl();
	}

	public PersonImpl person() {
		return new PersonImpl();
	}
	
	public Person person(String text) {
		return new PersonImpl().$(text);
	}
	
	public void print() {
		for (PersonImpl p : root.persons) {
			System.out.print("Person");
			if (p.first != null) System.out.print(" first: " + p.first);
			if (p.last != null) System.out.print(" last: " + p.last);
			if (p.text.length() > 0) System.out.print(" text: " + p.text);
			if (p.age != null) System.out.print(" age: " + p.age);
			System.out.println();
		}
	}
}
