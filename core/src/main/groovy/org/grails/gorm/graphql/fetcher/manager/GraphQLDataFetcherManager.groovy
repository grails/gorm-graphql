package org.grails.gorm.graphql.fetcher.manager

import graphql.schema.DataFetcher
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.grails.gorm.graphql.fetcher.BindingGormDataFetcher
import org.grails.gorm.graphql.fetcher.DeletingGormDataFetcher
import org.grails.gorm.graphql.fetcher.GraphQLDataFetcherType
import org.grails.gorm.graphql.fetcher.ReadingGormDataFetcher
import org.grails.gorm.graphql.response.delete.GraphQLDeleteResponseHandler

import javax.xml.crypto.Data

/**
 * An interface to register and retrieve data fetcher instances
 *
 * @author James Kleeh
 */
interface GraphQLDataFetcherManager {

    /**
     * Register a fetcher instance to be used for CREATE or UPDATE for the
     * provided class.
     *
     * @param clazz The class to be updated or deleted
     * @param fetcher The fetcher instance to be used
     */
    void registerBindingDataFetcher(Class clazz, BindingGormDataFetcher fetcher)

    /**
     * Register a fetcher instance to be used for DELETE for the
     * provided class.
     *
     * @param clazz The class to be deleted
     * @param fetcher The fetcher instance to be used
     */
    void registerDeletingDataFetcher(Class clazz, DeletingGormDataFetcher fetcher)

    /**
     * Register a fetcher instance to be used for GET or LIST for the
     * provided class.
     *
     * @param clazz The class to be retrieved
     * @param fetcher The fetcher instance to be used
     */
    void registerReadingDataFetcher(Class clazz, ReadingGormDataFetcher fetcher)

    /**
     * Returns a data fetcher instance to be used in CREATE or UPDATE
     *
     * @param entity The entity representing the domain used in the fetcher
     * @param dataBinder The data binder responsible for binding data to the domain instance
     * @param type Which type of fetcher to return (CREATE or UPDATE)
     * @return The matched data binder
     */
    DataFetcher getBindingFetcher(PersistentEntity entity, GraphQLDataBinder dataBinder, GraphQLDataFetcherType type)

    /**
     * Returns a data fetcher instance to be used in DELETE
     *
     * @param entity The entity representing the domain used in the fetcher
     * @param responseHandler The response handler responsible for generating the delete response
     * @return The matched data binder
     */
    DataFetcher getDeletingFetcher(PersistentEntity entity, GraphQLDeleteResponseHandler responseHandler)

    /**
     * Returns a data fetcher instance to be used in GET or LIST
     *
     * @param entity The entity representing the domain used in the fetcher
     * @param type Which type of fetcher to return (GET or LIST)
     * @return The matched data binder
     */
    DataFetcher getReadingFetcher(PersistentEntity entity, GraphQLDataFetcherType type)

}
