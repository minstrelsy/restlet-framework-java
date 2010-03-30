/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.IntoRrcInjector;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * Instances represents a root resource class, see chapter 3 of JAX-RS
 * specification.
 * 
 * @author Stephan Koops
 */
public class RootResourceClass extends ResourceClass {

    /**
     * Checks, if the class is public and so on.
     * 
     * @param jaxRsClass
     *                JAX-RS root resource class or JAX-RS provider.
     * @param typeName
     *                "root resource class" or "provider"
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     */
    private static void checkClassForPathAnnot(Class<?> jaxRsClass,
            String typeName) throws MissingAnnotationException {
        if (!jaxRsClass.isAnnotationPresent(Path.class)) {
            String msg = "The "
                    + typeName
                    + " "
                    + jaxRsClass.getName()
                    + " is not annotated with @Path. The class will be ignored.";
            throw new MissingAnnotationException(msg);
        }
    }

    private final Constructor<?> constructor;

    private final ParameterList constructorParameters;

    private final IntoRrcInjector injectHelper;

    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *                the root resource class to wrap
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link JaxRsRouter}.
     * @param entityProviders
     *                all entity providers.
     * @param allCtxResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the logger to use.
     * @see WrapperFactory#getRootResourceClass(Class)
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     *                 if no valid constructor could be found
     */
    RootResourceClass(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers, ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException, MissingConstructorException {
        super(jaxRsClass, tlContext, entityProviders, allCtxResolvers, extensionBackwardMapping,
                logger, logger);
        Util.checkClassConcrete(getJaxRsClass(), "root resource class");
        checkClassForPathAnnot(jaxRsClass, "root resource class");
        this.injectHelper = new IntoRrcInjector(jaxRsClass, tlContext,
                isLeaveEncoded(), entityProviders, allCtxResolvers, extensionBackwardMapping);
        this.constructor = WrapperUtil.findJaxRsConstructor(getJaxRsClass(),
                "root resource class");
        boolean constructorLeaveEncoded = this.isLeaveEncoded()
                || constructor.isAnnotationPresent(Encoded.class);
        this.constructorParameters = new ParameterList(this.constructor,
                tlContext, constructorLeaveEncoded, entityProviders,
                allCtxResolvers, extensionBackwardMapping, logger);
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param objectFactory
     *                object responsible for instantiating the root resource
     *                class. Optional, thus can be null.
     * @return
     * @throws InvocationTargetException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     */
    public ResourceObject createInstance(ObjectFactory objectFactory)
            throws InstantiateException, InvocationTargetException {
        Object instance = null;
        if (objectFactory != null)
            instance = objectFactory.getInstance(jaxRsClass);
        if (instance == null) {
            try {
                Object[] args = constructorParameters.get();
                instance = WrapperUtil.createInstance(constructor, args);
            } catch (ConvertRepresentationException e) {
                // is not possible
                throw new ImplementationException("Must not be possible", e);
            }
        }
        ResourceObject rootResourceObject = new ResourceObject(instance, this);
        try {
            this.injectHelper.injectInto(instance);
        } catch (InjectException e) {
            throw new InstantiateException(e);
        }
        return rootResourceObject;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof RootResourceClass))
            return false;
        RootResourceClass otherRootResourceClass = (RootResourceClass) anotherObject;
        return this.jaxRsClass.equals(otherRootResourceClass.jaxRsClass);
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    @Override
    public PathRegExp getPathRegExp() {
        return super.getPathRegExp();
    }
}