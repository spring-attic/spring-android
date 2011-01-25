package org.springframework.http.converter.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class MockObject {

   @Element
   private String text;

   @Attribute
   private int index;

   public MockObject() {
      super();
   }  

   public MockObject(String text, int index) {
      this.text = text;
      this.index = index;
   }

   public String getMessage() {
      return text;
   }

   public int getId() {
      return index;
   }
}
