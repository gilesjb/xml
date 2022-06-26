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

import java.lang.reflect.Method;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX ContentHandler that converts SAX events into calls to a
 * schema implementation.
 *
 * @author gilesjb
 */
public class SAXHandler extends DefaultHandler {
	
	private final Schema<?> schema;
	private final Object handler;
	
	private final LinkedList<Element> elements = new LinkedList<Element>();
	
	public <T> SAXHandler(Class<T> definition, T handler) {
		this(new Schema<T>(definition), handler);
	}
	
	public <T> SAXHandler(Schema<T> schema, T handler) {
		this.schema = schema;
		this.handler = handler;
		
		elements.push(new NodeElement(schema.definition(), handler));
	}
	
    @Override public void startElement(String uri, String local, String name, Attributes attrs)
    		throws SAXException {
    	if (!schema.isNamespace(uri)) 
    		return;
    	try {
    		elements.push(elements.peek().start(name, attrs));
    	} catch (Exception e) {
    		throw new SAXException(e);
    	}
    }
    
    @Override public void characters(char ch[], int start, int length) throws SAXException {
    	try {
    		String text = String.copyValueOf(ch, start, length).trim();
    		if (text.length() > 0) elements.push(elements.pop().text(text));
    	} catch (Exception e) {
    		throw new SAXException(e);
    	}
    }

    @Override public void endElement(String uri, String local, String name) throws SAXException {
    	if (!schema.isNamespace(uri)) 
    		return;
    	try {
    		Element element = elements.pop().end((NodeElement) elements.pop());
    		if (element != null) elements.push(element);
    	} catch (Exception e) {
    		throw new SAXException(e);
    	}
    }
    
    interface Element {
    	Element start(String name, Attributes attrs) throws Exception;
    	Element text(String text) throws Exception;
    	Element end(NodeElement parent) throws Exception;
    }
    
    private class NodeElement implements Element {
    	final Class<?> type;
    	final Object node;
    	
    	NodeElement(Class<?> type, Object node) {
    		this.type = type;
    		this.node = node;
    	}
    	
    	public Element start(String name, Attributes attrs) throws Exception {
    		if (attrs.getLength() == 0) {
    			try {
    				return new TextElement(name);
    			} catch (NoSuchMethodException e) {}
    		}
			Method method = schema.getFactoryMethod(name);
			Class<?> type = method.getReturnType();
			Object node = method.invoke(handler);
			
	    	for (int i = 0, n = attrs.getLength(); i < n; i++) {
	    		Method m = schema.getAttributeMethod(type, attrs.getLocalName(i));
	    		type = m.getReturnType();
				node = Schema.invoke(m, node, attrs.getValue(i));
	    	}
			
			return new NodeElement(type, node);
    	}
    	
    	public Element text(String text) throws Exception {
    		Method method = schema.getTextMethod(type);
    		return new NodeElement(method.getReturnType(), method.invoke(node, text));
    	}
    	
    	public Element end(NodeElement parent) throws Exception {
    		Method m = schema.getChildMethod(parent.type, type);
    		Object obj = m.invoke(parent.node, node);
    		return m.getReturnType() != void.class? new NodeElement(m.getReturnType(), obj) : null;
    	}
    }
    
    class TextElement implements Element {
    	final Method method;
    	final StringBuilder text = new StringBuilder();
    	
    	TextElement(String name) throws NoSuchMethodException {
    		this.method = schema.getTextNodeFactoryMethod(name);
    	}
    	
    	public Element start(String name, Attributes attrs) {
    		throw new UnsupportedOperationException();
    	}

		public Element text(String text) {
			this.text.append(text);
			return this;
		}

		public Element end(NodeElement parent) throws Exception {
			Method m = schema.getChildMethod(parent.type, method.getReturnType());
			Object obj = m.invoke(parent.node, method.invoke(handler, text.toString()));
    		return m.getReturnType() != void.class? new NodeElement(m.getReturnType(), obj) : null;
		}
    }
}
