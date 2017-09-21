package grails.test.app

import org.grails.gorm.graphql.binding.manager.GraphQLDataBinderManager
import org.grails.gorm.graphql.plugin.GraphQLPostProcessor
import grails.gorm.DetachedCriteria
import graphql.schema.DataFetchingEnvironment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.DeletingGormDataFetcher
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.SingleEntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.SoftDeleteEntityDataFetcher
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager


@CompileStatic
class GraphQLCustomizer extends GraphQLPostProcessor {

    @Override
    void doWith(GraphQLDataFetcherManager fetcherManager) {
        PersistentEntity entity = SoftDelete.gormPersistentEntity
        DeletingGormDataFetcher softDelete = new SoftDeleteEntityDataFetcher(entity, 'active', false)
        fetcherManager.registerDeletingDataFetcher(SoftDelete, softDelete)
        fetcherManager.registerReadingDataFetcher(SoftDelete, new SingleEntityDataFetcher(entity) {
            @Override
            protected DetachedCriteria buildCriteria(DataFetchingEnvironment environment) {
                super.buildCriteria(environment).where {
                    eq('active', true)
                }
            }
        })
        fetcherManager.registerReadingDataFetcher(SoftDelete, new EntityDataFetcher(entity) {
            @Override
            protected DetachedCriteria buildCriteria(DataFetchingEnvironment environment) {
                super.buildCriteria(environment).where {
                    eq('active', true)
                }
            }
        })
    }

    @Override
    void doWith(GraphQLDataBinderManager binderManager) {
        binderManager.registerDataBinder(User, new UserDataBinder())
        binderManager.registerDataBinder(Role, new RoleDataBinder())
    }
}
