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
package org.copalis.xml;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A dynamic proxy for schema interfaces that writes to a DOM Document
 *
 * @author gilesjb
 */
public class DocBinder implements InvocationHandler {
	
	private final Schema<?> schema;
	private final Document doc;
	private final Node node;
	private final Element elem;
	
	DocBinder(Schema<?> schema, Document doc, Node node, Element elem) {
		this.schema = schema;
		this.doc = doc;
		this.node = node;
		this.elem = elem;
	}
	
	private DocBinder bind(Schema<?> schema, Element val) {
		return new DocBinder(schema, doc, val, val);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class == method.getDeclaringClass()) {
			return method.invoke(this, args);
		}
		switch (schema.typeOf(method)) {
		case CHILD:
			node.appendChild(((DocBinder) Proxy.getInvocationHandler(args[0])).node);
			break;
		case TEXT:
			node.appendChild(doc.createTextNode(args[0].toString()));
			break;
		case TEXT_NODE_FACTORY:
			Element child = doc.createElementNS(schema.namespace(), method.getName());
			child.appendChild(doc.createTextNode(args[0].toString()));
			return proxy(method.getReturnType(), bind(schema, child));
		case FACTORY:
			return proxy(method.getReturnType(), bind(schema, doc.createElementNS(
					schema.namespace(), method.getName())));
		case ATTRIBUTE:
			elem.setAttributeNS(null, method.getName(), args[0].toString());
			break;
		}
		return proxy;
	}

	private static <T> T proxy(Class<T> schema, DocBinder binder) {
		return schema.cast(Proxy.newProxyInstance(schema.getClassLoader(),
				new Class<?>[] {schema}, binder));
	}

	/**
	 * Creates a new instance of the supplied interface and binds it
	 * to a Document.
	 * Invoking the methods of the created instance will build the document tree
	 * @param <T>
	 * @param definition A schema interface
	 * @param document An empty Document
	 * @return A new instance of schema, bound to the document
	 */
	public static <T> T bind(Class<T> definition, final Document document) {
		return bind(new Schema<T>(definition), document);
	}

	public static <T> T bind(Schema<T> schema, final Document document) {
		return proxy(schema.definition(), new DocBinder(schema, document, document, null));
	}
}
