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

public interface OrderSchema {
	
	public enum Currency {USD}
	
	void _(Order root);
	
	Order order();
	Order.Customer customer();
	
	Order.Product product();
	Order.Product.ProductChild name(String name);
	Order.Product.ProductChild sku(String text);
	Order.Product.ProductChild quantity(int amt);
	Order.Product.Price price();
	
	Order.ShipTo shipTo();
	Order.ShipTo.ShipToChild street(String text);
	Order.ShipTo.ShipToChild city(String text);
	Order.ShipTo.ShipToChild state(String text);
	Order.ShipTo.ShipToChild zip(String text);
	
	Order.Total subtotal();
	Order.Total total();
	
	Order.Tax tax();
	
	Order.Shipping shipping();
	
	interface Order {
		interface OrderChild {}
		
		Order _(OrderChild node);
		
		interface Customer extends OrderChild {
			Customer id(String id);
			Customer $(String text);
		}
		
		interface Product extends OrderChild {
			interface ProductChild {}

			Product _(ProductChild child);
			
			interface Price extends ProductChild {
				Price currency(Currency currency);
				Price $(String text);
			}
		}

		interface ShipTo extends OrderChild {
			interface ShipToChild {}
			
			ShipTo _(ShipToChild child);
		}
		
		interface Total extends OrderChild {
			Total currency(Currency curr);
			Total $(String text);
		}
		
		interface Tax extends OrderChild {
			Tax rate(String rate);
			Tax currency(Currency curr);
			Tax $(String amt);
		}
		
		interface Shipping extends OrderChild {
			Shipping method(String method);
			Shipping currency(Currency curr);
			Shipping $(String amt);
		}
	}
}
