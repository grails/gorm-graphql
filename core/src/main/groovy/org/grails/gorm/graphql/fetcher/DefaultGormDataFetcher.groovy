package org.grails.gorm.graphql.fetcher

import grails.gorm.DetachedCriteria
import graphql.language.Field
import graphql.language.Selection
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * A generic class to assist with querying entities with GraphQL
 *
 * @param <T> The domain type to query
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class DefaultGormDataFetcher<T> implements DataFetcher<T> {

    protected List<String> associationNames
    protected PersistentEntity entity

    DefaultGormDataFetcher(PersistentEntity entity) {
        this.entity = entity
        this.associationNames = entity.associations*.name
    }

    @SuppressWarnings('NestedForLoop')
    protected Map defaultQueryOptions(DataFetchingEnvironment environment) {
        Set<String> joinProperties = []

        for (Field field: environment.fields) {
            for (Selection selection: field.selectionSet.selections) {
                if (selection instanceof Field) {
                    final String NAME = ((Field)selection).name
                    if (associationNames.contains(NAME)) {
                        joinProperties.add(NAME)
                    }
                }
            }
        }

        if (joinProperties) {
            [fetch: joinProperties.collectEntries { [(it): 'join'] } ]
        }
        else {
            [:]
        }
    }

    protected GormEntity queryInstance(DataFetchingEnvironment environment) {
        List<String> idNames = []

        if (entity.identity != null) {
            idNames.add(entity.identity.name)
        }
        else if (entity.compositeIdentity != null) {
            for (PersistentProperty p: entity.compositeIdentity) {
                idNames.add(p.name)
            }
        }

        (GormEntity)new DetachedCriteria(entity.javaClass).build {
            for (String prop: idNames) {
                eq(prop, environment.getArgument(prop))
            }
        }.get(defaultQueryOptions(environment))
    }

    abstract T get(DataFetchingEnvironment environment)
}
