package grails.test.app

import gorm.graphql.GrailsGraphQLDataBinder
import groovy.transform.CompileStatic

/**
 * Created by jameskleeh on 7/17/17.
 */
@CompileStatic
class UserDataBinder extends GrailsGraphQLDataBinder {

    @Override
    void bind(Object object, Map data) {
        //These properties are guaranteed to be here because they are
        //created with nullable(false)
        Integer first = (Integer)data.remove('firstNumber')
        Integer second = (Integer)data.remove('secondNumber')
        if (first != null && second != null) {
            data.put('addedNumbers', first + second)
        }
        super.bind(object, data)
    }
}
