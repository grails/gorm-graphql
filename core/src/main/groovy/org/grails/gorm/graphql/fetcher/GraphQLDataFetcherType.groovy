package org.grails.gorm.graphql.fetcher

/**
 * Created by jameskleeh on 7/14/17.
 */
enum GraphQLDataFetcherType {

    CREATE(BindingGormDataFetcher),
    GET(ReadingGormDataFetcher),
    LIST(ReadingGormDataFetcher),
    UPDATE(BindingGormDataFetcher),
    DELETE(DeletingGormDataFetcher)

    final Class requiredClass

    GraphQLDataFetcherType(Class requiredClass) {
        this.requiredClass = requiredClass
    }
}