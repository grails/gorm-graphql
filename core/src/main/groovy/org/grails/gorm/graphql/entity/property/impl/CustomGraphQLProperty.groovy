package org.grails.gorm.graphql.entity.property.impl

import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.dsl.helpers.Deprecatable
import org.grails.gorm.graphql.entity.dsl.helpers.Describable
import org.grails.gorm.graphql.entity.dsl.helpers.Named
import org.grails.gorm.graphql.entity.dsl.helpers.Nullable
import org.grails.gorm.graphql.entity.property.GraphQLDomainProperty

/**
 * Implementation of {@link GraphQLDomainProperty} to be used to define
 * additional properties beyond the ones defined in GORM entities
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@AutoClone
@CompileStatic
abstract class CustomGraphQLProperty<T> implements GraphQLDomainProperty, Cloneable, Named<T>, Describable<T>, Deprecatable<T>, Nullable<T> {

    boolean input = true
    boolean output = true
    Closure dataFetcher = null

    T dataFetcher(Closure dataFetcher) {
        this.dataFetcher = dataFetcher
        (T)this
    }

    T input(boolean input) {
        this.input = input
        (T)this
    }

    T output(boolean output) {
        this.output = output
        (T)this
    }
    
    //should be set by the property manager
    protected MappingContext mappingContext

    void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext
    }

    void validate() {
        if (name == null) {
            throw new IllegalArgumentException('A name is required for creating custom properties')
        }
    }
}
