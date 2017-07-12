package org.grails.gorm.graphql

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.config.Entity
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.IllegalMappingException
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.entity.dsl.GraphQLPropertyMapping
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.entity.property.GraphQLDomainPropertyManager
import org.grails.gorm.graphql.entity.property.PersistentGraphQLProperty

import java.lang.reflect.Method

@CompileStatic
class GraphQLEntityHelper {

    private static Map<PersistentEntity, GraphQLMapping> mappings = [:]
    private static Map<PersistentEntity, String> descriptions = [:]

    static String getDescription(final PersistentEntity entity) {
        if (descriptions.containsKey(entity)) {
            return descriptions.get(entity)
        }

        String description = getMapping(entity)?.description

        if (description == null) {
            GraphQL graphQL = entity.javaClass.getAnnotation(GraphQL)
            if (graphQL != null) {
                description = graphQL.value()
            }
            else {
                try {
                    Class hibernateMapping = Class.forName( "org.grails.orm.hibernate.cfg.Mapping" )
                    Entity mapping = entity.mapping.mappedForm
                    if (hibernateMapping.isAssignableFrom(mapping.class)) {
                        description = hibernateMapping.getMethod('getComment').invoke(mapping)
                    }
                } catch(ClassNotFoundException e) {}
            }
        }
        descriptions.put(entity, description)
        description
    }

    static GraphQLMapping getMapping(final PersistentEntity entity) {
        if (mappings.containsKey(entity)) {
            return mappings.get(entity)
        }
        def graphql = ClassPropertyFetcher.getStaticPropertyValue(entity.javaClass, 'graphql', Object)
        GraphQLMapping mapping = null
        if (graphql != null) {
            if (graphql == Boolean.TRUE) {
                mapping = new GraphQLMapping()
            }
            else if (graphql instanceof Closure) {
                mapping = new GraphQLMapping().build(graphql)
            }
            else if (graphql instanceof GraphQLMapping) {
                mapping = (GraphQLMapping)graphql
            }

            if (!(mapping instanceof GraphQLMapping)) {
                throw new IllegalMappingException("The static graphql property on ${entity.name} is not a Boolean, Closure, or GraphQLMapping")
            }
        }
        mappings.put(entity, mapping)
        mapping
    }

}
