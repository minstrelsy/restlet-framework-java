/*
 * Copyright � 2005 J�r�me LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.data;

/**
 * Method to execute when handling a uniform call.
 * @see org.restlet.data.Methods
 */
public interface Method extends ControlData
{
   /**
    * Returns the technical name of the method.
    * @return The technical name of the method.
    */
   public String getName();

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri();

   /**
    * Indicates if the method is equal to a given one.
    * @param method  The method to compare to.
    * @return        True if the method is equal to a given one.
    */
   public boolean equals(Method method);

}
