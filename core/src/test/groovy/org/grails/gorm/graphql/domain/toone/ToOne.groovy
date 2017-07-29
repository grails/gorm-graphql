package org.grails.gorm.graphql.domain.toone

import grails.gorm.annotation.Entity

import java.sql.Blob
import java.sql.Clob
import java.sql.Time
import java.sql.Timestamp

@Entity
class ToOne {

    One one
    Enum anEnum

    Integer integer
    Long aLong
    Short aShort
    Byte aByte
    Double aDouble
    Float aFloat
    BigInteger bigInteger
    BigDecimal bigDecimal
    String string
    Boolean aBoolean
    Character character
    UUID uuid
    URL url

/*    Time time
    Timestamp timestamp
    Date date
    Currency currency
    TimeZone timeZone
    Byte[] bytes
    Character[] characters
    Blob blob
    Clob clob
    URI uri

    char[] charsPrimitive
    byte[] bytesPrimitive*/
    int intPrimitive
    long longPrimitive
    short shortPrimitive
    byte bytePrimitive
    double doublePrimitive
    float floatPrimitive
    char charPrimitive
    boolean booleanPrimitive


    static graphql = true
}
