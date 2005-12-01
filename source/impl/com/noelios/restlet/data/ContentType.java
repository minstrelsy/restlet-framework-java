/*
 * Copyright 2005 J�r�me LOUVEL
 * 
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * http://www.opensource.org/licenses/cddl1.txt 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL 
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information: 
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.data;

import java.io.IOException;

import org.restlet.Manager;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

/**
 * Represents a content type header.
 */
public class ContentType
{
   protected MediaType mediaType;
   protected CharacterSet characterSet;
   
   /**
    * Constructor.
    * @param headerValue The content-type header to parse.
    */
   public ContentType(String headerValue)
   {
      try
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, headerValue);
         Preference pref;
         pref = pr.readPreference();
         this.mediaType = (MediaType)pref.getMetadata();

         String charSet = this.mediaType.getParameterValue("charset");
         if(charSet != null)
         {
            this.characterSet = Manager.createCharacterSet(charSet);
         }
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
   
   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType()
   {
      return mediaType;
   }
   
   /**
    * Returns the character set.
    * @return The character set.
    */
   public CharacterSet getCharacterSet()
   {
      return characterSet;
   }
   
}
