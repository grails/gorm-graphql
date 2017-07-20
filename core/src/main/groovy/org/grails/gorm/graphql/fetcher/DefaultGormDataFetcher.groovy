package org.grails.gorm.graphql.fetcher

import grails.gorm.DetachedCriteria
import graphql.language.Field
import graphql.language.Selection
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Identity

/**
 * A generic class to assist with querying entities with GraphQL
 *
 * @param <T> The domain type to query
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
abstract class DefaultGormDataFetcher<T> implements DataFetcher<T> {

    protected Map<String, PersistentEntity> associations = [:]
    protected PersistentEntity entity
    protected Set<String> associationNames

    DefaultGormDataFetcher(PersistentEntity entity) {
        this.entity = entity
        for (Association association: entity.associations) {
            associations.put(association.name, association.associatedEntity)
        }
        associationNames = associations.keySet()
    }

    protected boolean shouldJoinProperty(Field selectedField) {
        boolean join = false
        if (associationNames.contains(selectedField.name)) {
            join = true
            PersistentEntity entity = associations.get(selectedField.name)
            List<Selection> selections = selectedField.selectionSet?.selections ?: (List<Selection>)[]
            if (selections.size() == 1 && selections[0] instanceof Field) {
                Field field = (Field)selections[0]
                if (entity.isIdentityName(field.name)) {
                    join = false
                }
            }
        }
        join
    }

    @SuppressWarnings('NestedForLoop')
    protected Map defaultQueryOptions(DataFetchingEnvironment environment) {
        Set<String> joinProperties = []

        for (Field field: environment.fields) {
            for (Selection selection: field.selectionSet.selections) {
                if (selection instanceof Field) {
                    Field selectedField = (Field)selection
                    if (shouldJoinProperty(selectedField)) {
                        joinProperties.add(selectedField.name)
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

    protected Object loadEntity(PersistentEntity entity, Object argument) {
        GormStaticApi api = (GormStaticApi)GormEnhancer.findStaticApi(entity.javaClass)
        api.load((Serializable)argument)
    }

    protected Map<String, Object> getIdentifierValues(DataFetchingEnvironment environment) {
        Map<String, Object> idProperties = [:]

        PersistentProperty identity = entity.identity
        if (identity != null) {
            idProperties.put(identity.name, environment.getArgument(identity.name))
        }
        else if (entity.compositeIdentity != null) {
            for (PersistentProperty p: entity.compositeIdentity) {
                Object value
                Object argument = environment.getArgument(p.name)
                if (associations.containsKey(p.name)) {
                    PersistentEntity associatedEntity = associations.get(p.name)
                    value = loadEntity(associatedEntity, argument)
                } else {
                    value = argument
                }
                idProperties.put(p.name, value)
            }
        }

        idProperties
    }

    protected GormEntity queryInstance(DataFetchingEnvironment environment) {
        Map<String, Object> idProperties = getIdentifierValues(environment)

        (GormEntity)new DetachedCriteria(entity.javaClass).build {
            for (Map.Entry<String, Object> prop: idProperties) {
                eq(prop.key, prop.value)
            }
        }.get(defaultQueryOptions(environment))
    }

    abstract T get(DataFetchingEnvironment environment)
}
