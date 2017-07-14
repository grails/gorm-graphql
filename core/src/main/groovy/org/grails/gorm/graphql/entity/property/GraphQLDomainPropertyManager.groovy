package org.grails.gorm.graphql.entity.property

import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.gorm.graphql.GraphQLEntityHelper
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping

import java.lang.reflect.Method

/**
 * Created by jameskleeh on 7/12/17.
 */
class GraphQLDomainPropertyManager {

    PersistentEntity entity
    GraphQLMapping mapping

    Set<String> excludedProperties = new HashSet<>()
    boolean identifiers = true
    Closure customCondition = null

    //To support older versions of GORM
    private static Method derivedMethod
    static {
        try {
            derivedMethod = Property.class.getMethod("isDerived", (Class<?>[]) null)
        } catch (NoSuchMethodException | SecurityException e) {
            // no-op
        }
    }

    GraphQLDomainPropertyManager(PersistentEntity entity) {
        this(entity, GraphQLEntityHelper.getMapping(entity))
    }

    GraphQLDomainPropertyManager(PersistentEntity entity, GraphQLMapping mapping) {
        this.entity = entity
        this.mapping = mapping
    }

    GraphQLDomainPropertyManager identifiers(boolean identifiers) {
        this.identifiers = identifiers
        this
    }

    GraphQLDomainPropertyManager excludeVersion() {
        excludedProperties.add('version')
        this
    }

    GraphQLDomainPropertyManager excludeTimestamps() {
        excludedProperties.addAll(['dateCreated', 'lastUpdated'])
        this
    }

    GraphQLDomainPropertyManager exclude(String... props) {
        excludedProperties.addAll(props)
        this
    }

    GraphQLDomainPropertyManager condition(Closure closure) {
        this.customCondition = closure
        this
    }

    List<GraphQLDomainProperty> getProperties() {
        List<GraphQLDomainProperty> properties = []
        MappingContext mappingContext = entity.mappingContext
        GraphQLMapping mapping = this.mapping
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

        entity.persistentProperties.each { PersistentProperty prop ->
            if (!mapping.excluded.contains(prop.name) && !excludedProperties.contains(prop.name)) {
                if (customCondition == null || customCondition.call(prop)) {
                    if (prop instanceof Embedded) {
                        PersistentEntity associatedEntity = ((Embedded)prop).associatedEntity

                        GraphQLDomainPropertyManager associatedManager = new GraphQLDomainPropertyManager(associatedEntity, mapping.createEmbeddedMapping(prop.name)).identifiers(false).exclude(excludedProperties as String[])

                        properties.addAll(associatedManager.getProperties())
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
            }
        }

        properties.addAll(mapping.additional)

        properties
    }

}
