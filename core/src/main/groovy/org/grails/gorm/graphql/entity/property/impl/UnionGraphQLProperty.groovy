package org.grails.gorm.graphql.entity.property.impl

import graphql.schema.DataFetcher
import graphql.schema.GraphQLType
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.dsl.helpers.*
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetcher
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

/**
 * Implementation of {@link GraphQLDomainProperty} to be used for defining custom union properties
 *
 * @author James Hardwick
 * @since 2.0.2
 */
@AutoClone
@CompileStatic
abstract class UnionGraphQLProperty<T> extends OrderedGraphQLProperty implements Named<T>, Describable<T>, Deprecatable<T>, Nullable<T>, Arguable<T> {

    Integer order = null
    boolean input = false
    boolean output = true
    Closure closureDataFetcher = null
    boolean collection = false

    T collection(boolean collection) {
        this.collection = collection
        (T)this
    }

    T dataFetcher(Closure dataFetcher) {
        this.closureDataFetcher = dataFetcher
        (T)this
    }
    
    T order(Integer order) {
        this.order = order
        (T)this
    }
    
    //should be set by the property manager
    protected MappingContext mappingContext

    void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext
    }

    @Override
    abstract GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType)

    abstract Set<GraphQLType> getUnionTypes()

    DataFetcher getDataFetcher() {
        closureDataFetcher ? new ClosureDataFetcher(closureDataFetcher) : null
    }

    void validate() {
        if (name == null) {
            throw new IllegalArgumentException('A name is required for creating custom properties')
        }
    }

}
