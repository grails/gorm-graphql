package org.grails.gorm.graphql.entity.property.impl

import graphql.TypeResolutionEnvironment
import graphql.schema.*
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.grails.gorm.graphql.entity.dsl.helpers.CustomTyped
import org.grails.gorm.graphql.entity.dsl.helpers.ExecutesClosures
import org.grails.gorm.graphql.types.GraphQLPropertyType
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLUnionType.newUnionType

/**
 * Used to represent a custom union property consisting of custom unioned types
 *
 * @author James Hardwick
 * @since 2.0.2
 */
@AutoClone
@CompileStatic
class ComplexUnionGraphQLProperty extends UnionGraphQLProperty<ComplexUnionGraphQLProperty> implements ExecutesClosures {

    String typeName
    private Map<Class, CustomTyped> unionTypedMap = [:]
    private Set<GraphQLObjectType> graphQLTypesMap = [] as Set<GraphQLObjectType>


    ComplexUnionGraphQLProperty typeName(String typeName) {
        this.typeName = typeName
        this
    }

    @Override
    GraphQLType getGraphQLType(GraphQLTypeManager typeManager, GraphQLPropertyType propertyType) {

        unionTypedMap.each { k, v ->
            String name = typeManager.namingConvention.getType(k.simpleName, propertyType)
            graphQLTypesMap.add((GraphQLObjectType)v.buildCustomType(name, typeManager, mappingContext))
        }

        String name = typeManager.namingConvention.getType(typeName, propertyType)
        GraphQLUnionType.Builder obj = newUnionType()
                .name(name)
                .description(description)
                .typeResolver(new TypeResolver() {
                    @Override
                    GraphQLObjectType getType(TypeResolutionEnvironment env) {
                        String resolvedName = typeManager.namingConvention.getType(env.getObject().class.simpleName, propertyType)
                        (GraphQLObjectType)env.schema.getType(resolvedName)
                    }
                })

        for(GraphQLObjectType possibleType : graphQLTypesMap) {
            obj.possibleType(possibleType)
        }

        GraphQLOutputType type = obj.build()
        if (collection) {
            type = GraphQLList.list(type)
        }
        type
    }

    @Override
    Set<GraphQLObjectType> getUnionTypes() {
        graphQLTypesMap
    }

    /**
     * Defines a custom type object to be used as part of a union, and as such, changes this property into a union type.
     * <p>
     * Per the latest GraphQL Spec, union member types must all be of an Object base type. Scalar, Interface, and Union
     * types may not be member types of a union. Similarly, wrapping types must not be member types of a union. Unions
     * are also not currently considered valid input. Defining this property as a union will automatically set its
     * {@link CustomGraphQLProperty#input} property to false.
     * <p>
     *
     * @param type A concrete class type that may ultimately be returned by this properties data fetcher
     * @param closure A closure for defining the custom type
     *
     * @see {<a href="http://spec.graphql.org/June2018/#sec-Unions">GraphQL June 2018 Spec - Unions</a>}
     * @see {<a href="https://github.com/graphql/graphql-spec/issues/488">[RFC] GraphQL Input Union type</a>}
     */
    void type(Class type, @DelegatesTo(value = CustomTyped, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        CustomTyped<CustomTyped> typed = new Object().withTraits(CustomTyped)
        withDelegate(closure, typed)
        unionTypedMap.put(type, typed)
    }

    void validate() {
        super.validate()

        if (typeName == null) {
            throw new IllegalArgumentException('The type name must be specified for custom properties with a complex type')
        }
        for(CustomTyped typed : unionTypedMap.values()) {
            if (typed.fields.empty) {
                throw new IllegalArgumentException("$name: At least 1 field is required for creating a custom property")
            }
        }
    }
}
