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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Schema represents information about a schema interface
 * <p>
 * A schema interface must contain 1 root-node method and one or more
 * factory methods for creating nodes:
 * </p>
 * <table border=1>
 * <tr><th>Purpose</th><th>Declaration</th></tr>
 * <tr><td>Set the root node</td><td>void _(<i>node-type</i> rootNode);</td></tr>
 * <tr><td>Create a new node</td><td><i>node-type node-name</i>();</td></tr>
 * </table>
 * <p>
 * The interfaces defining nodes may contain any of the following method types:
 * </p>
 * <table border=1>
 * <tr><th>Purpose</th><th>Declaration</th></tr>
 * <tr><td>Set an attribute</td><td><i>node-type attr-name</i>(String attrValue);</td></tr>
 * <tr><td>Add a child node</td><td><i>node-type</i> <b>_</b>(<i>node-type</i> childNode);</td></tr>
 * <tr><td>Add text</td><td><i>node-type</i> <b>$</b>(String value);</td></tr>
 * </table>
 *
 * @author gilesjb
 */
public class Schema<T> {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Text {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Child {
		String value();
	}
	
	private final Class<T> definition;
	private final String namespace;
	
	private final String childName;
	private final String textName;
	
	public enum MethodType {
		CHILD,
		FACTORY,
		TEXT,
		TEXT_NODE_FACTORY,
		ATTRIBUTE;
		
		static MethodType of(Schema<?> schema, Method m) {
			String name =       m.getName();
			Class<?>[] params = m.getParameterTypes();
			boolean root =  m.getDeclaringClass() == schema.definition;
			
			if (schema.childName.equals(name)) {
				if (params.length == 1) return CHILD;
			} else if (schema.textName.equals(name)) {
				if (!root && params.length == 1 && params[0] == String.class) return TEXT;
			} else if (root) {
				if (params.length == 0) return FACTORY;
				if (params.length == 1) return TEXT_NODE_FACTORY;
			} else {
				if (params.length == 1) return ATTRIBUTE;
			}
			
			throw new IllegalArgumentException("Illegal Schema method: " + m);
		}
	}
	
	public MethodType typeOf(Method m) {
		return MethodType.of(this, m);
	}
	
	/**
	 * Constructs a new Schema object from a schema interface,
	 * using the default names of "_" for child-node methods,
	 * and "$" for text-node methods
	 * 
	 * @param definition
	 */
	public Schema(Class<T> definition) {
		this(definition, "", definition.getAnnotation(Child.class), definition.getAnnotation(Text.class));
	}
	
	private Schema(Class<T> definition, String namespace, Child child, Text text) {
		this(definition, namespace, child != null? child.value() : "_", text != null? text.value() : "$");
	}
	
	/**
	 * Constructs a new Schema object from an interface,
	 * using the specified child-node and text-node method names
	 * 
	 * @param definition A schema interface
	 * @param namspace The namespace URI
	 * @param child Name of child-node methods
	 * @param text Name of text-node methods
	 */
	public Schema(Class<T> definition, String namespace, String child, String text) {
		this.namespace = namespace;
		this.definition = definition;
		this.childName = child;
		this.textName = text;
	}
	
	public Class<T> definition() {
		return definition;
	}
	
	public String namespace() {
		return namespace;
	}
	
	public boolean isNamespace(String uri) {
		return namespace.equals(uri);
	}
	
	/**
	 * Finds the text-node method in a node interface
	 * 
	 * @param parent The interface of the parent node
	 * @return The text-node method defined by the parent node interface
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Method getTextMethod(Class<?> parent) throws SecurityException, NoSuchMethodException {
		return parent.getMethod(textName, String.class);
	}
	
	/**
	 * Finds a child-node method
	 * 
	 * @param parent The interface of the parent node
	 * @param child The interface of a child node
	 * @return The child-node method defined by the parent node interface that accepts
	 * an instance of the child node interface
	 * @throws NoSuchMethodException
	 */
	public Method getChildMethod(Class<?> parent, Class<?> child) throws NoSuchMethodException {
		for (Method m : parent.getMethods()) {
			if (typeOf(m) == MethodType.CHILD && m.getParameterTypes()[0].isAssignableFrom(child)) {
				return m;
			}
		}
		throw new NoSuchMethodException("No child method for " + child.getSimpleName() + " found in " + parent.getSimpleName());
	}
	
	/**
	 * Finds a node factory method
	 * 
	 * @param name The tag name of the node to be created
	 * @return A method that creates instances of the node
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Method getFactoryMethod(String name) throws SecurityException, NoSuchMethodException {
		return definition.getMethod(name);
	}
	
	/**
	 * Finds a node factory method
	 * 
	 * @param name The tag name of the node to be created
	 * @return A method that creates instances of the node
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Method getTextNodeFactoryMethod(String name) throws SecurityException, NoSuchMethodException {
		return definition.getMethod(name, String.class);
	}
	
	public Method getAttributeMethod(Class<?> nodeType, String name) throws NoSuchMethodException {
		for (Method m : nodeType.getMethods()) {
			if (m.getName().equals(name) && typeOf(m) == MethodType.ATTRIBUTE) {
				return m;
			}
		}
		throw new NoSuchMethodException("No attribute method " + nodeType.getCanonicalName() + '.' + name);
	}
	
	public String toString() {
		return namespace();
	}
	
	public static Object invoke(Method method, Object object, String val)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return method.invoke(object, valueOf(val, method.getParameterTypes()[0]));
	}

	private static Object valueOf(String str, Class<?> type)
			throws IllegalArgumentException {
		if (type.isPrimitive()) {
			if (type == char.class) type = Character.class;
			else if (type == boolean.class) type = Boolean.class;
			else if (type == byte.class) type = Byte.class;
			else if (type == short.class) type = Short.class;
			else if (type == int.class) type = Integer.class;
			else if (type == long.class) type = Long.class;
			else if (type == float.class) type = Float.class;
			else type = Double.class;
		} else if (str == null) {
			return null;
		}
		if (type == Class.class)
			try {
				return Class.forName(str);
			} catch (ClassNotFoundException e1) {
				throw new RuntimeException(e1);
			}
		if (Enum.class.isAssignableFrom(type)) {
			for (Object obj : type.getEnumConstants()) {
				if (obj.toString().equals(str))
					return obj;
			}
		}
		if (type == String.class)
			return str;
		try {
			return type.getMethod("valueOf", String.class).invoke(null, str);
//		try {
//			return type.getConstructor(String.class).newInstance(str);
		} catch (Exception e) {
			if ("".equals(str))
				return null;
			throw new IllegalArgumentException("Not able to construct "
					+ type.getName() + " from value: " + str, e);
		}
	}

}
