package grails.test.app

import org.grails.gorm.graphql.plugin.GraphQLPostProcessor
import grails.gorm.DetachedCriteria
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLScalarType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.fetcher.DeletingGormDataFetcher
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.SingleEntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.SoftDeleteEntityDataFetcher
import org.grails.gorm.graphql.fetcher.manager.GraphQLDataFetcherManager
import org.grails.gorm.graphql.types.GraphQLTypeManager

import java.time.OffsetDateTime

@CompileStatic
class GraphQLCustomizer extends GraphQLPostProcessor {

    @Override
    void doWith(GraphQLTypeManager typeManager) {
        typeManager.registerType(OffsetDateTime, new GraphQLScalarType("OffsetDateTime", "Built in offset date time", new Coercing<OffsetDateTime, OffsetDateTime>() {
            @Override
            OffsetDateTime serialize(Object input) {
                if (input instanceof OffsetDateTime) {
                    (OffsetDateTime)input
                }
                else {
                    null
                }
            }

            @Override
            OffsetDateTime parseValue(Object input) {
                parseLiteral(input)
            }

            @Override
            OffsetDateTime parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    OffsetDateTime.parse(input.value)
                }
                else {
                    null
                }
            }
        }))
    }

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
}
