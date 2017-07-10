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
 */
@CompileStatic
class GraphQLDataBinderManager {

    protected final Map<Class, GraphQLDataBinder> dataBinders = new LinkedHashMap<>()

    GraphQLDataBinderManager() {
        //Create the default data binder
        dataBinders.put(Object, new GraphQLDataBinder() {
            @Override
            void bind(Object object, Map data) {
                DataBinder dataBinder = new DataBinder(object)
                dataBinder.bind(new MutablePropertyValues(data))
            }
        })
    }

    void register(Class clazz, GraphQLDataBinder dataBinder) {
        dataBinders.put(clazz, dataBinder)
    }

    GraphQLDataBinder getDataBinder(Class clazz) {
        for (Class key: dataBinders.keySet()) {
            if (key == clazz || key.isAssignableFrom(clazz)) {
                return dataBinders.get(key)
            }
        }
        null
    }
}
