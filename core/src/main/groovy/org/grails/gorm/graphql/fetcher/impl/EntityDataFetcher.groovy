package org.grails.gorm.graphql.fetcher.impl

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import graphql.schema.DataFetchingEnvironment
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
 * @param <T> The domain returnType to query
 * @author James Kleeh
 * @since 1.0.0
 */
@InheritConstructors
@Slf4j
@CompileStatic
class EntityDataFetcher<T extends Collection> extends DefaultGormDataFetcher<T> implements ReadingGormDataFetcher {

    Map<String, Boolean> batchModeEnabled

    private static Class hibernatePropertyConfig

    static {
        try {
            hibernatePropertyConfig = ClassUtils.forName('org.grails.orm.hibernate.cfg.PropertyConfig')
        } catch (ClassNotFoundException e) { }
    }

    protected void initializeEntity(PersistentEntity entity) {
        super.initializeEntity(entity)
        batchModeEnabled = [:]
        for (Association association: associations.values()) {
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

    //The new LinkedHasMap is to work around a static compilation bug
    static final Map<String, Class> ARGUMENTS = new LinkedHashMap<String, Class>([
       max: Integer,
       offset: Integer,
       sort: String,
       order: String,
       cache: Boolean,
       lock: Boolean,
       ignoreCase: Boolean
    ])

    protected Map<String, Object> getArguments(DataFetchingEnvironment environment) {
        environment.arguments
    }

    @Override
    @Transactional(readOnly = true)
    T get(DataFetchingEnvironment environment) {
        Map queryArgs = getFetchArguments(environment)

        for (Map.Entry<String, Object> entry: getArguments(environment)) {
            if (ARGUMENTS.containsKey(entry.key) && entry.value != null) {
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

        (T)executeQuery(environment, queryArgs)
    }

    protected DetachedCriteria buildCriteria(DataFetchingEnvironment environment) {
        new DetachedCriteria(entity.javaClass)
    }

    protected List executeQuery(DataFetchingEnvironment environment, Map queryArgs) {
        buildCriteria(environment).list(queryArgs)
    }

    @Override
    boolean supports(GraphQLDataFetcherType type) {
        type == GraphQLDataFetcherType.LIST
    }
}
