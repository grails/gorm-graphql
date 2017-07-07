package org.grails.gorm.graphql.fetcher

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import graphql.Scalars
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLScalarType
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.config.Property
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.reflect.ClassUtils

@InheritConstructors
@Slf4j
class EntityDataFetcher<T extends Collection> extends GormDataFetcher<T> {

    Map<String, Boolean> batchModeEnabled = [:]

    private static Class hibernatePropertyConfig

    static {
        try {
            hibernatePropertyConfig = ClassUtils.forName('org.grails.orm.hibernate.cfg.PropertyConfig')
        } catch (ClassNotFoundException e) {}
    }

    EntityDataFetcher(PersistentEntity entity) {
        super(entity)
        entity.associations.each {
            Property mapping = it.mapping.mappedForm
            if (hibernatePropertyConfig != null && hibernatePropertyConfig.isAssignableFrom(mapping.class)) {
                batchModeEnabled.put(it.name, ((Integer)mapping.invokeMethod('getBatchSize', [] as Object[])) > 1)
            }
            else {
                batchModeEnabled.put(it.name, true)
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

        ARGUMENTS.keySet().each {
            if (environment.containsArgument(it)) {
                def arg = environment.getArgument(it)
                if (arg != null) {
                    queryArgs.put(it, arg)
                }
            }
        }

        if (queryArgs.containsKey('max') || queryArgs.containsKey('offset')) {
            if (queryArgs.containsKey('fetch')) {
                Map fetch = queryArgs.get('fetch')
                boolean showWarning = false
                fetch.keySet().each { String key ->
                    fetch.put(key, "default")
                    if (!batchModeEnabled.get(key)) {
                        showWarning = true
                    }
                }
                if (showWarning) {
                    log.warn("Pagination parameters were supplied for query ${environment.fields[0].name} in addition to a joined collection. The fetch mode will be lazy. Configure a batchSize for better performance.")
                }
            }
        }

        (T)new DetachedCriteria(entity.javaClass).list(queryArgs)
    }
}
