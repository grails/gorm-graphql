package org.grails.gorm.graphql.fetcher.manager

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.BindingGormDataFetcher
import org.grails.gorm.graphql.fetcher.DeletingGormDataFetcher
import org.grails.gorm.graphql.fetcher.impl.CreateEntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.DeleteEntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.EntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.SingleEntityDataFetcher
import org.grails.gorm.graphql.fetcher.impl.UpdateEntityDataFetcher

import static org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType.*
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.ReadingGormDataFetcher
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler
import spock.lang.Shared
import spock.lang.Specification

class GraphQLDataFetcherManagerSpec extends Specification {

    @Shared GraphQLDataFetcherManager manager
    @Shared ReadingGormDataFetcher mockReadingFetcher
    @Shared BindingGormDataFetcher mockBindingFetcher
    @Shared DeletingGormDataFetcher mockDeletingFetcher

    PersistentEntity getMockEntity(Class clazz) {
        Stub(PersistentEntity) {
            getJavaClass() >> clazz
            getAssociations() >> []
        }
    }

    void setupSpec() {
        manager = new DefaultGraphQLDataFetcherManager()
        mockReadingFetcher = new ReadingGormDataFetcher() {
            @Override
            boolean supports(GraphQLDataFetcherType type) {
                return false
            }

            @Override
            Object get(DataFetchingEnvironment environment) {
                return null
            }
        }
        mockBindingFetcher = new BindingGormDataFetcher() {
            GraphQLDataBinder dataBinder

            @Override
            boolean supports(GraphQLDataFetcherType type) {
                return false
            }

            @Override
            Object get(DataFetchingEnvironment environment) {
                return null
            }
        }
        mockDeletingFetcher = new DeletingGormDataFetcher() {
            GraphQLDeleteResponseHandler responseHandler

            @Override
            boolean supports(GraphQLDataFetcherType type) {
                return false
            }

            @Override
            Object get(DataFetchingEnvironment environment) {
                return null
            }
        }
    }

    void "test building a manager with the wrong types"() {
        when:
        new DefaultGraphQLDataFetcherManager([
                (type): fetcher
        ])

        then:
        thrown(IllegalArgumentException)

        where:
        type   | fetcher
        DELETE | mockReadingFetcher
        CREATE | mockReadingFetcher
        UPDATE | mockReadingFetcher

        DELETE | mockBindingFetcher
        GET    | mockBindingFetcher
        LIST   | mockBindingFetcher

        LIST   | mockDeletingFetcher
        GET    | mockDeletingFetcher
        CREATE | mockDeletingFetcher
        UPDATE | mockDeletingFetcher
    }

    void "test building a manager with correct types"() {
        when:
        new DefaultGraphQLDataFetcherManager([
                (DELETE): mockDeletingFetcher,
                (CREATE): mockBindingFetcher,
                (UPDATE): mockBindingFetcher,
                (GET): mockReadingFetcher,
                (LIST): mockReadingFetcher
        ])

        then:
        noExceptionThrown()
        GraphQLDataFetcherType.values().size() == 5
    }

    void "test get binding fetcher with wrong argument type"() {
        when:
        manager.getBindingFetcher(getMockEntity(String), null, type)

        then:
        thrown(IllegalArgumentException)

        where:
        type << [GET, LIST, DELETE]
    }

    void "test get reading fetcher with wrong argument type"() {
        when:
        manager.getReadingFetcher(getMockEntity(String), type)

        then:
        thrown(IllegalArgumentException)

        where:
        type << [CREATE, UPDATE, DELETE]
    }

    void "test registering a binding fetcher"() {
        given:
        GormEnhancer.STATIC_APIS.put(ConnectionSource.DEFAULT, ['java.lang.String': Mock(GormStaticApi)])

        when:
        manager.registerBindingDataFetcher(String, mockBindingFetcher)

        then: "the binder is not registered because it doesn't support any type"
        manager.getBindingFetcher(getMockEntity(String), null, CREATE) instanceof CreateEntityDataFetcher
        manager.getBindingFetcher(getMockEntity(String), null, UPDATE) instanceof UpdateEntityDataFetcher
    }

    void "test fetchers for the exact class are resolved first (binding)"() {
        GraphQLDataFetcherManager manager = new DefaultGraphQLDataFetcherManager([
                (DELETE): mockDeletingFetcher,
                (CREATE): mockBindingFetcher,
                (UPDATE): mockBindingFetcher,
                (GET): mockReadingFetcher,
                (LIST): mockReadingFetcher
        ])
        BindingGormDataFetcher myBindingFetcher = new BindingGormDataFetcher() {
            GraphQLDataBinder dataBinder

            @Override
            boolean supports(GraphQLDataFetcherType type) {
                type == CREATE
            }

            @Override
            Object get(DataFetchingEnvironment environment) { null }
        }

        expect: 'The default binders are returned'
        manager.getBindingFetcher(getMockEntity(String), null, CREATE) == mockBindingFetcher
        manager.getBindingFetcher(getMockEntity(String), null, UPDATE) == mockBindingFetcher

        when: 'A binder is registered for String that supports CREATE'
        manager.registerBindingDataFetcher(String, myBindingFetcher)

        then: 'The binder is returned for CREATE, and the Object binder is returned for update'
        manager.getBindingFetcher(getMockEntity(String), null, CREATE) == myBindingFetcher

        when: 'The manager has no default binders'
        manager = new DefaultGraphQLDataFetcherManager()

        then: 'The GORM defaults are used'
        manager.getBindingFetcher(getMockEntity(String), null, CREATE) instanceof CreateEntityDataFetcher
        manager.getBindingFetcher(getMockEntity(String), null, UPDATE) instanceof UpdateEntityDataFetcher

        when: 'A custom binder is registered'
        manager.registerBindingDataFetcher(String, myBindingFetcher)

        then: 'The binder is returned'
        manager.getBindingFetcher(getMockEntity(String), null, CREATE) == myBindingFetcher
    }

    void "test fetchers for the exact class are resolved first (reading)"() {
        GraphQLDataFetcherManager manager = new DefaultGraphQLDataFetcherManager([
                (DELETE): mockDeletingFetcher,
                (CREATE): mockBindingFetcher,
                (UPDATE): mockBindingFetcher,
                (GET): mockReadingFetcher,
                (LIST): mockReadingFetcher
        ])
        ReadingGormDataFetcher myReadingFetcher = new ReadingGormDataFetcher() {
            @Override
            boolean supports(GraphQLDataFetcherType type) {
                type == GET
            }

            @Override
            Object get(DataFetchingEnvironment environment) { null }
        }

        expect: 'The default binders are returned'
        manager.getReadingFetcher(getMockEntity(String), GET) == mockReadingFetcher
        manager.getReadingFetcher(getMockEntity(String), LIST) == mockReadingFetcher

        when: 'A binder is registered for String that supports CREATE'
        manager.registerReadingDataFetcher(String, myReadingFetcher)

        then: 'The binder is returned for CREATE, and the Object binder is returned for update'
        manager.getReadingFetcher(getMockEntity(String), GET) == myReadingFetcher

        when: 'The manager has no default binders'
        manager = new DefaultGraphQLDataFetcherManager()

        then: 'The GORM defaults are used'
        manager.getReadingFetcher(getMockEntity(String), GET) instanceof SingleEntityDataFetcher
        manager.getReadingFetcher(getMockEntity(String), LIST) instanceof EntityDataFetcher

        when: 'A custom binder is registered'
        manager.registerReadingDataFetcher(String, myReadingFetcher)

        then: 'The binder is returned'
        manager.getReadingFetcher(getMockEntity(String), GET) == myReadingFetcher
    }

    void "test fetchers for the exact class are resolved first (deleting)"() {
        GraphQLDataFetcherManager manager = new DefaultGraphQLDataFetcherManager([
                (DELETE): mockDeletingFetcher,
                (CREATE): mockBindingFetcher,
                (UPDATE): mockBindingFetcher,
                (GET): mockReadingFetcher,
                (LIST): mockReadingFetcher
        ])
        DeletingGormDataFetcher myDeletingFetcher = new FakeDeletingFetcher()

        expect: 'The default binders are returned'
        manager.getDeletingFetcher(getMockEntity(String), null) == mockDeletingFetcher

        when: 'A binder is registered for String that supports CREATE'
        manager.registerDeletingDataFetcher(String, myDeletingFetcher)

        then: 'The binder is returned for CREATE, and the Object binder is returned for update'
        manager.getDeletingFetcher(getMockEntity(String), null) == myDeletingFetcher

        when: 'The manager has no default binders'
        manager = new DefaultGraphQLDataFetcherManager()

        then: 'The GORM defaults are used'
        manager.getDeletingFetcher(getMockEntity(String), null) instanceof DeleteEntityDataFetcher

        when: 'A custom binder is registered'
        manager.registerDeletingDataFetcher(String, myDeletingFetcher)

        then: 'The binder is returned'
        manager.getDeletingFetcher(getMockEntity(String), null) == myDeletingFetcher
    }

    class FakeDeletingFetcher implements DeletingGormDataFetcher {

        GraphQLDeleteResponseHandler responseHandler

        @Override
        Object get(DataFetchingEnvironment environment) {
            return null
        }
    }
}
