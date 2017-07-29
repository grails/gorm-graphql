package org.grails.gorm.graphql.entity.operations

import graphql.schema.GraphQLOutputType
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.dsl.helpers.Typed
import org.grails.gorm.graphql.types.GraphQLTypeManager

class SimpleOperation extends CustomOperation<SimpleOperation> implements Typed<SimpleOperation> {

    @Override
    protected GraphQLOutputType getType(GraphQLTypeManager typeManager, MappingContext mappingContext) {
        resolveOutputType(typeManager, mappingContext)
    }

    void validate() {
        super.validate()

        if (returnType == null) {
            throw new IllegalArgumentException('A return type is required for creating custom operations')
        }
    }
}
