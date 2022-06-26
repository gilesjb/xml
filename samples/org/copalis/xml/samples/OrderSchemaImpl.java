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

import org.copalis.xml.samples.OrderSchema.Order.Customer;
import org.copalis.xml.samples.OrderSchema.Order.Product;
import org.copalis.xml.samples.OrderSchema.Order.ShipTo;
import org.copalis.xml.samples.OrderSchema.Order.Shipping;
import org.copalis.xml.samples.OrderSchema.Order.Tax;
import org.copalis.xml.samples.OrderSchema.Order.Total;

public class OrderSchemaImpl implements OrderSchema {

	AnOrder root;
	
	public void _(Order root) {
		this.root = (AnOrder) root;
	}
	
	public void copy(OrderSchema schema) {
		schema._(root.copy(schema));
	}
	
	// factory methods
	
	public Order order() {
		return new AnOrder();
	}
	
	public Order.Customer customer() {
		return new ACustomer();
	}
	
	public Order.Product product() {
		return new AProduct();
	}
	
	public Order.Product.ProductChild name(final String name) {
		return new AProduct.AChild() {
			public Order.Product.ProductChild copy(OrderSchema schema) {
				return schema.name(name);
			}
			@Override public String toString() {
				return "name:" + name;
			}
		};
	}
	
	public Order.Product.ProductChild quantity(final int text) {
		return new AProduct.AChild() {
			public Order.Product.ProductChild copy(OrderSchema schema) {
				return schema.quantity(text);
			}
			@Override public String toString() {
				return "quantity:" + text;
			}
		};
	}
	
	public Order.Product.ProductChild sku(final String text) {
		return new AProduct.AChild() {
			public Order.Product.ProductChild copy(OrderSchema schema) {
				return schema.sku(text);
			}
			@Override public String toString() {
				return "name:" + text;
			}
		};
	}
	
	public Order.Product.Price price() {
		return new APrice();
	}
	
	public Order.ShipTo shipTo() {
		return new AShipTo();
	}
	
	public Order.ShipTo.ShipToChild city(final String text) {
		return new AShipTo.AChild("city", text) {
			public Order.ShipTo.ShipToChild copy(OrderSchema schema) {
				return schema.city(text);
			}
		};
	}
	
	public Order.ShipTo.ShipToChild state(final String text) {
		return new AShipTo.AChild("state", text) {
			public Order.ShipTo.ShipToChild copy(OrderSchema schema) {
				return schema.state(text);
			}
		};
	}
	
	public Order.ShipTo.ShipToChild street(final String text) {
		return new AShipTo.AChild("street", text) {
			public Order.ShipTo.ShipToChild copy(OrderSchema schema) {
				return schema.street(text);
			}
		};
	}
	
	public Order.ShipTo.ShipToChild zip(final String text) {
		return new AShipTo.AChild("zip", text) {
			public Order.ShipTo.ShipToChild copy(OrderSchema schema) {
				return schema.zip(text);
			}
		};
	}
	
	public Order.Shipping shipping() {
		return new AShipping();
	}
	
	public Order.Total subtotal() {
		return new ATotal("Subtotal") {
			public Total copy(OrderSchema schema) {
				return schema.subtotal().currency(currency).$(amount);
			}
		};
	}

	public Order.Total total() {
		return new ATotal("Total") {
			public Total copy(OrderSchema schema) {
				return schema.total().currency(currency).$(amount);
			}
		};
	}
	
	public Order.Tax tax() {
		return new ATax();
	}
	
	@Override public String toString() {
		return root.toString();
	}
	
	//// classes
	
	static class AnOrder implements Order {
		List<AChild> children = new LinkedList<AChild>();

		public Order _(Order.OrderChild node) {
			children.add((AChild) node);
			return this;
		}
		
		public Order copy(OrderSchema schema) {
			Order order = schema.order();
			for (AChild child : children) order._(child.copy(schema));
			return order;
		}

		@Override public String toString() {
			return "\nOrder: " + children.toString();
		}
		
		static abstract class AChild implements Order.OrderChild {
			abstract Order.OrderChild copy(OrderSchema schema);
		}
	}
	
	static class ACustomer extends AnOrder.AChild implements Order.Customer {
		String id;
		String text = "";

		public ACustomer $(String text) {
			this.text += text;
			return this;
		}

		public Customer copy(OrderSchema schema) {
			return schema.customer().id(id).$(text);
		}

		public ACustomer id(String id) {
			this.id = id;
			return this;
		}

		@Override public String toString() {
			return "\nCustomer id:" + id + " name:" + text;
		}
	}
	
	private static class AProduct extends AnOrder.AChild implements Order.Product {
		List<AChild> children = new LinkedList<AChild>();

		public Product _(Order.Product.ProductChild child) {
			children.add((AChild) child);
			return this;
		}

		public Order.Product copy(OrderSchema schema) {
			Product product = schema.product();
			for (AChild child : children) product._(child.copy(schema));
			return product;
		}

		@Override public String toString() {
			return "\nProduct: " + children.toString();
		}
		
		static abstract class AChild implements Order.Product.ProductChild {
			abstract Order.Product.ProductChild copy(OrderSchema schema);
		}
	}
	
	private static class APrice extends AProduct.AChild implements Order.Product.Price {
		String text = "";
		Currency currency;

		public APrice $(String text) {
			this.text += text;
			return this;
		}

		public Order.Product.ProductChild copy(OrderSchema schema) {
			return schema.price().currency(currency).$(text);
		}

		public APrice currency(Currency currency) {
			this.currency = currency;
			return this;
		}

		@Override public String toString() {
			return "currency:" + currency + " amount:" + text;
		}
	}

	private static class AShipping extends AnOrder.AChild implements Order.Shipping {
		String method, amount = "";
		Currency currency;

		public Shipping $(String amt) {
			this.amount += amt;
			return this;
		}

		public Shipping copy(OrderSchema schema) {
			return schema.shipping().currency(currency).method(method).$(amount);
		}

		public Shipping currency(Currency curr) {
			this.currency = curr;
			return this;
		}

		public Shipping method(String method) {
			this.method = method;
			return this;
		}

		@Override public String toString() {
			return "Shipping currency: " + currency + " method: " + method + " amount:" + amount;
		}
	}

	private static class AShipTo extends AnOrder.AChild implements Order.ShipTo {
		List<AChild> children = new LinkedList<AChild>();

		public ShipTo _(Order.ShipTo.ShipToChild child) {
			children.add((AChild) child);
			return this;
		}

		public ShipTo copy(OrderSchema schema) {
			ShipTo shipTo = schema.shipTo();
			for (AChild child : children) shipTo._(child.copy(schema));
			return shipTo;
		}

		@Override public String toString() {
			return "\nShipTo:" + children.toString();
		}

		static abstract class AChild implements Order.ShipTo.ShipToChild {
			final String text;
			final String type;
			
			AChild(String type, String value) {
				this.type = type;
				this.text = value;
			}
			
			abstract Order.ShipTo.ShipToChild copy(OrderSchema schema);
			
			@Override public String toString() {
				return type + ":" + text;
			}
		}
	}
	
	private static class ATax extends AnOrder.AChild implements Order.Tax {
		String rate, amount = "";
		Currency currency;

		public Tax $(String amt) {
			this.amount += amt;
			return this;
		}

		public Tax copy(OrderSchema schema) {
			return schema.tax().currency(currency).rate(rate).$(amount);
		}

		public Tax currency(Currency curr) {
			this.currency = curr;
			return this;
		}

		public Tax rate(String rate) {
			this.rate = rate;
			return this;
		}

		@Override public String toString() {
			return "\nTax currency:" + currency + " rate:" + rate + " amount: " + amount;
		}
	}
	
	abstract static class ATotal extends AnOrder.AChild implements Order.Total {
		String type, amount = "";
		Currency currency;
		
		ATotal(String type) {
			this.type = type;
		}

		public Total $(String text) {
			this.amount += text;
			return this;
		}

		abstract Total copy(OrderSchema schema);
		
		public Total currency(Currency curr) {
			this.currency = curr;
			return this;
		}
		
		@Override public String toString() {
			return "\n" + type + " currency:" + currency + " amount:" + amount;
		}
	}
}
