package org.grails.gorm.graphql.fetcher.runtime

import graphql.schema.DataFetcher
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * A runtime data fetcher implementation used for fetchers
 * that read data
 *
 * @see AbstractRuntimeDataFetcher
 *
 * @param <T> The domain type to return
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
@InheritConstructors
class ReadingRuntimeDataFetcher<T> extends AbstractRuntimeDataFetcher<T> {

    @Override
    DataFetcher resolveFetcher() {
        manager.getReadingFetcher(entity, type)
    }
}
