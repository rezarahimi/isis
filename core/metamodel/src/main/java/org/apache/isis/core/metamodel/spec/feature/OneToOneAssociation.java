/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.spec.feature;

import com.google.common.base.Function;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Provides reflective access to a field on a domain object that is used to
 * reference another domain object.
 */
public interface OneToOneAssociation extends ObjectAssociation, OneToOneFeature, MutableCurrentHolder {

    /**
     * Initialise this field in the specified object with the specified
     * reference - this call should only affect the specified object, and not
     * any related objects. It should also not be distributed. This is strictly
     * for re-initialising the object and not specifying an association, which
     * is only done once.
     */
    void initAssociation(ObjectAdapter inObject, ObjectAdapter associate);



    /**
     * Determines if the specified reference is valid for setting this field in
     * the specified object, represented as a {@link Consent}.
     */
    public Consent isAssociationValid(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter proposedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Set up the association represented by this field in the specified object
     * with the specified reference - this call sets up the logical state of the
     * object and might affect other objects that share this association (such
     * as back-links or bidirectional association). To initialise a recreated
     * object to this logical state the <code>initAssociation</code> method
     * should be used on each of the objects.
     * 
     * @deprecated - see {@link MutableCurrentHolder#set(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)}
     * @see #initAssociation(ObjectAdapter, ObjectAdapter)
     */
    @Deprecated
    void setAssociation(
            ObjectAdapter inObject,
            ObjectAdapter associate,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Clear this reference field (make it <code>null</code>) in the specified
     * object, and remove any association back-link.
     * 
     * @see #setAssociation(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)
     * @deprecated - see {@link MutableCurrentHolder#set(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)}
     */
    @Deprecated
    void clearAssociation(ObjectAdapter inObject, final InteractionInitiatedBy interactionInitiatedBy);

    
    // //////////////////////////////////////////////////////
    // Functions
    // //////////////////////////////////////////////////////
    
    public static class Functions {
        public static Function<String, OneToOneAssociation> fromId(final ObjectSpecification noSpec) {
            return new Function<String, OneToOneAssociation>() {
                @Override
                public OneToOneAssociation apply(final String id) {
                    return (OneToOneAssociation) noSpec.getAssociation(id);
                }
            };
        }
    }

}
