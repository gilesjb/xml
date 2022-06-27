## xml

Copalis XML is a toolkit for reading and writing XML.
You define a schema by creating a Java interface in which method names correspond, by convention, to XML elements and attributes.
Copalis XML will then create an instance of your interface which can be used to write to a DOM.

### Example

If we define a Java interface like this:

	public interface PeopleSchema {
	    
	    void _(People people);
	
	    People people();
	    
	    public interface People {
	        People _(Person person);
	    }
	    
	    Person.Attrs person();
	    
	    public interface Person {
	        public interface Attrs extends Person {
	            Attrs first(String name);
	            Attrs last(String name);
	        }
	        
	        Person $(String text);
	    }
	}

And assume some conventions:

* A named method such as `person()` in the primary interface creates an element of the same name
* Named methods that accept a single parameter value add an attribute to the current element
* Methods called `_` add a child node, or set the root node
* A method named `$` adds a text child node
 
Then any legal Java expression that can be created from an instance of this interface can be thought of as corresponding to an XML document. The interface therefore defines an XML grammar.

An instance of a schema interface can be create without having to write any implementation code;
*Copalis XML*'s `DocBinder`class generates a dynamic proxy that writes to a DOM Document object.

For example:

    PeopleSchema s = DocBinder.bind(PeopleSchema.class, domDocument);

    s._(s.people()
        ._(s.person().first("Giles").last("Burgess").$("author"))
        ._(s.person().$("Name unknown"))
        ._(s.person().first("Olive")));

now printing the contents of `domDocument` displays:

	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
	<people>
	  <person first="Giles" last="Burgess">author</person>
	  <person>Name unknown</person>
	  <person first="Olive"/>
	</people>
