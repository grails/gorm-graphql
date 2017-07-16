package org.grails.gorm.graphql.entity.property.manager

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.impl.PersistentGraphQLProperty

import java.lang.reflect.Method

/**
 * A class to retrieve {@link PersistentProperty} instances in combination
 * with a {@link GraphQLMapping} to produce a list of {@link GraphQLDomainProperty}
 * instances used in creation of the GraphQL schema.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLDomainPropertyManager implements GraphQLDomainPropertyManager {

    //To support older versions of GORM
    private static Method derivedMethod
    static {
        try {
            derivedMethod = Property.getMethod('isDerived', (Class<?>[]) null)
        } catch (NoSuchMethodException | SecurityException e) { }
    }

    @Override
    Builder builder() {
        new Builder()
    }

    private static class Builder implements GraphQLDomainPropertyManager.Builder {

        Set<String> excludedProperties = [] as Set
        boolean identifiers = true
        Closure customCondition = null

        @Override
        Builder excludeIdentifiers() {
            this.identifiers = false
            this
        }

        @Override
        Builder excludeVersion() {
            excludedProperties.add('version')
            this
        }

        @Override
        Builder excludeTimestamps() {
            excludedProperties.addAll(['dateCreated', 'lastUpdated'])
            this
        }

        @Override
        Builder exclude(String... props) {
            excludedProperties.addAll(props)
            this
        }

        @Override
        Builder condition(Closure closure) {
            this.customCondition = closure
            this
        }

        @Override
        List<GraphQLDomainProperty> getProperties(PersistentEntity entity) {
            getProperties(entity, GraphQLEntityHelper.getMapping(entity))
        }

        @Override
        List<GraphQLDomainProperty> getProperties(PersistentEntity entity, GraphQLMapping mapping) {
            List<GraphQLDomainProperty> properties = []
            MappingContext mappingContext = entity.mappingContext
            if (mapping == null) {
                mapping = new GraphQLMapping()
            }

            if (identifiers) {
                if (entity.identity != null) {
                    properties.add(new PersistentGraphQLProperty(mappingContext, entity.identity, mapping.propertyMappings.getOrDefault(entity.identity.name, new GraphQLPropertyMapping())))
                }
                else if (entity.compositeIdentity?.length > 0) {
                    properties.addAll(entity.compositeIdentity.collect { PersistentProperty prop ->
                        new PersistentGraphQLProperty(mappingContext, prop, mapping.propertyMappings.getOrDefault(prop.name, new GraphQLPropertyMapping()))
                    })
                }
            }

            for (PersistentProperty prop: entity.persistentProperties) {
                if (mapping.excluded.contains(prop.name)) {
                    continue
                }
                if (excludedProperties.contains(prop.name)) {
                    continue
                }
                if (customCondition != null && !customCondition.call(prop)) {
                    continue
                }

                if (prop instanceof Embedded) {
                    PersistentEntity associatedEntity = ((Embedded)prop).associatedEntity

                    Builder associatedBuilder = new Builder().excludeIdentifiers().exclude(excludedProperties as String[])

                    properties.addAll(associatedBuilder.getProperties(associatedEntity, mapping.createEmbeddedMapping(prop.name)))
                }
                else {
                    boolean input = true
                    if (derivedMethod != null) {
                        Property property = prop.mapping.mappedForm
                        if (derivedMethod.invoke(property, (Object[]) null)) {
                            input = false
                        }
                    }

                    properties.add(new PersistentGraphQLProperty(mappingContext, prop, mapping.propertyMappings.getOrDefault(prop.name, new GraphQLPropertyMapping(input: input))))
                }

            }

            properties.addAll(mapping.additional)

            properties
        }
    }

}
