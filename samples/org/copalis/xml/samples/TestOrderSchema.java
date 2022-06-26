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

import static org.copalis.xml.samples.OrderSchema.Currency.USD;

import org.copalis.xml.DocBinder;
import org.copalis.xml.samples.util.DocumentModel;
import org.w3c.dom.Document;

public class TestOrderSchema {

	static void createOrder(OrderSchema s) {
		s._(s.order()
			._(s.customer().id("c32").$("Chez Fred"))
			._(s.product()
				._(s.name("Birdsong Clock"))
				._(s.sku("244"))
				._(s.quantity(12))
				._(s.price().currency(USD).$("21.95")))
			._(s.shipTo()
				._(s.street("135 Airline Highway"))
				._(s.city("Narraganet"))
				._(s.state("RI"))
				._(s.zip("02882")))
			._(s.subtotal().currency(USD).$("263.40"))
			._(s.tax().rate("7.0").currency(USD).$("18.44"))
			._(s.shipping().method("USPS").currency(USD).$("8.95"))
			._(s.total().currency(USD).$("290.79")));
	}
	
	public static void main(String... args) {
		Document doc = DocumentModel.create();
		createOrder(DocBinder.bind(OrderSchema.class, doc));
		DocumentModel.print(doc);
	}
}
