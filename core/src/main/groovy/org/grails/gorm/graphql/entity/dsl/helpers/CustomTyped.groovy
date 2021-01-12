package org.grails.gorm.graphql.entity.dsl.helpers

import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.fields.ComplexField
import org.grails.gorm.graphql.entity.fields.Field
import org.grails.gorm.graphql.entity.fields.SimpleField
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition

/**
 * Decorates a class with the ability to build a custom type
 *
 * @param <T> The implementing class
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
trait CustomTyped<T> extends ExecutesClosures {
    List<Field> fields = []

    boolean defaultNull = true

    T defaultNull(boolean defaultNull) {
        this.defaultNull = defaultNull
        (T)this
    }

    /**
     * Builds a custom object returnType if the supplied return returnType is a Map
     *
     * @param typeManager The returnType manager
     * @param mappingContext The mapping context
     * @return The custom returnType
     */
    GraphQLOutputType buildCustomType(String name, GraphQLTypeManager typeManager, MappingContext mappingContext) {
        GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
                .name(name)

        for (Field field: fields) {
            if (field.output) {
                builder.field(newFieldDefinition()
                        .name(field.name)
                        .description(field.description)
                        .deprecate(field.deprecationReason)
                        .type(field.getType(typeManager, mappingContext)))
            }
        }
        builder.build()
    }

    private void handleField(Closure closure, Field field) {
        field.nullable(defaultNull)
        withDelegate(closure, (Object)field)
        handleField(field)
    }

    private void handleField(Field field) {
        field.validate()
        fields.add(field)
    }

    void field(String name, List<Class> type, @DelegatesTo(value = SimpleField, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        Field field = new SimpleField().name(name).returns(type)
        handleField(closure, field)
    }

    void field(String name, Class type, @DelegatesTo(value = SimpleField, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        Field field = new SimpleField().name(name).returns(type)
        handleField(closure, field)
    }

    void field(String name, String typeName, @DelegatesTo(value = ComplexField, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Field field = new ComplexField().name(name).typeName(typeName)
        handleField(closure, field)
    }

    void field(ComplexField field) {
        handleField(field)
    }
}
