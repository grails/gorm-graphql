package org.grails.gorm.graphql.binding.manager

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.DataBinder

/**
 * A class to manage instances of data binders to be used
 * with GraphQL operations on GORM entities
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class GraphQLDataBinderManager {

    protected final Map<Class, GraphQLDataBinder> dataBinders = [:]

    GraphQLDataBinderManager() {
        //Create the default data binder
        register(Object, new GraphQLDataBinder() {
            @Override
            void bind(Object object, Map data) {
                DataBinder dataBinder = new DataBinder(object)
                dataBinder.bind(new MutablePropertyValues(data))
            }
        })
    }

    GraphQLDataBinderManager(GraphQLDataBinder defaultDataBinder) {
        register(Object, defaultDataBinder)
    }

    /**
     * Register a data binder for use with the provided class or its subclasses
     *
     * @param clazz The class to be bound
     * @param dataBinder The data binding instance to be used
     */
    void register(Class clazz, GraphQLDataBinder dataBinder) {
        dataBinders.put(clazz, dataBinder)
    }

    /**
     * Returns a data binder to be used for the provided class
     *
     * @param clazz The class to be bound
     * @return The data binding instance to be used
     */
    GraphQLDataBinder getDataBinder(Class clazz) {
        if (dataBinders.containsKey(clazz)) {
            return dataBinders.get(clazz)
        }
        List<Class> keys = dataBinders.keySet().toList()
        keys.reverse(true)
        for (Class key: keys) {
            if (key.isAssignableFrom(clazz)) {
                return dataBinders.get(key)
            }
        }
        null
    }
}
