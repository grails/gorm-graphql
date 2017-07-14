package org.grails.gorm.graphql.fetcher.impl

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PropertyMapping
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.reflect.ClassUtils
import org.grails.gorm.graphql.fetcher.DefaultGormDataFetcher
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.ReadingGormDataFetcher

/**
 * A class for retrieving a list of entities with GraphQL
 *
 * @param <T> The domain type to query
 * @author James Kleeh
 */
@InheritConstructors
@Slf4j
@CompileStatic
class EntityDataFetcher<T extends Collection> extends DefaultGormDataFetcher<T> implements ReadingGormDataFetcher {

    Map<String, Boolean> batchModeEnabled = [:]

    private static Class hibernatePropertyConfig

    static {
        try {
            hibernatePropertyConfig = ClassUtils.forName('org.grails.orm.hibernate.cfg.PropertyConfig')
        } catch (ClassNotFoundException e) {}
    }

    EntityDataFetcher(PersistentEntity entity) {
        super(entity)
        entity.associations.each { Association association ->
            //Workaround for groovy issue (Groovy thinks association.mapping.mappedForm is a Collection)
            PropertyMapping<Property> propertyMapping = association.mapping
            Property mapping = propertyMapping.mappedForm
            if (hibernatePropertyConfig != null && hibernatePropertyConfig.isAssignableFrom(mapping.class)) {
                batchModeEnabled.put(association.name, ((Integer)mapping.invokeMethod('getBatchSize', [] as Object[])) > 1)
            }
            else {
                batchModeEnabled.put(association.name, true)
            }
        }
    }

    static final Map<String, GraphQLScalarType> ARGUMENTS = [
        max: Scalars.GraphQLInt,
        offset: Scalars.GraphQLInt,
        sort: Scalars.GraphQLString,
        order: Scalars.GraphQLString,
        cache: Scalars.GraphQLBoolean,
        lock: Scalars.GraphQLBoolean,
        ignoreCase: Scalars.GraphQLBoolean
    ]

    @Override
    @Transactional(readOnly = true)
    T get(DataFetchingEnvironment environment) {
        Map queryArgs = defaultQueryOptions(environment)

        environment.arguments.each { String key, Object value ->
            if (value != null) {
                queryArgs.put(key, value)
            }
        }

        if (queryArgs.containsKey('fetch') && (queryArgs.containsKey('max') || queryArgs.containsKey('offset'))) {
            Map<String, String> fetch = (Map)queryArgs.get('fetch')
            boolean showWarning = false
            fetch.keySet().each { String key ->
                fetch.put(key, "default")
                if (!batchModeEnabled.get(key)) {
                    showWarning = true
                }
            }
            if (showWarning) {
                log.warn("Pagination parameters were supplied for query ${environment.fields[0].name} in addition to a joined collection. The fetch mode will be lazy to ensure the correct data is returned. Configure a batchSize for better performance.")
            }
        }

        (T)new DetachedCriteria(entity.javaClass).list(queryArgs)
    }

    @Override
    GraphQLDataFetcherType getType() {
        GraphQLDataFetcherType.LIST
    }
}
