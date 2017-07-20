package org.grails.gorm.graphql.fetcher.impl

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLInputType
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
 * @since 1.0.0
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
        } catch (ClassNotFoundException e) { }
    }

    EntityDataFetcher(PersistentEntity entity) {
        super(entity)
        for (Association association: entity.associations) {
            //Workaround for groovy issue (Groovy thinks association.mapping.mappedForm is a Collection)
            PropertyMapping<Property> propertyMapping = association.mapping
            Property mapping = propertyMapping.mappedForm
            if (hibernatePropertyConfig?.isAssignableFrom(mapping.class)) {
                batchModeEnabled.put(association.name, ((Integer)mapping.invokeMethod('getBatchSize', [] as Object[])) > 1)
            }
            else {
                batchModeEnabled.put(association.name, true)
            }
        }
    }

    static final Map<String, GraphQLInputType> ARGUMENTS = [:]

    static {
        ARGUMENTS.with {
            put('max', Scalars.GraphQLInt)
            put('offset', Scalars.GraphQLInt)
            put('sort', Scalars.GraphQLString)
            put('order', Scalars.GraphQLString)
            put('cache', Scalars.GraphQLBoolean)
            put('lock', Scalars.GraphQLBoolean)
            put('ignoreCase', Scalars.GraphQLBoolean)
        }
    }

    @Override
    @Transactional(readOnly = true)
    T get(DataFetchingEnvironment environment) {
        Map queryArgs = defaultQueryOptions(environment)

        for (Map.Entry<String, Object> entry: environment.arguments) {
            if (entry.value != null) {
                queryArgs.put(entry.key, entry.value)
            }
        }

        if (queryArgs.containsKey('fetch') && (queryArgs.containsKey('max') || queryArgs.containsKey('offset'))) {
            Map<String, String> fetch = (Map)queryArgs.get('fetch')
            boolean showWarning = false
            for (String key: fetch.keySet()) {
                fetch.put(key, 'default')
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
    boolean supports(GraphQLDataFetcherType type) {
        type == GraphQLDataFetcherType.LIST
    }
}
