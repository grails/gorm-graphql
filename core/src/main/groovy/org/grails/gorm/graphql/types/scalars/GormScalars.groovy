package org.grails.gorm.graphql.types.scalars

import graphql.AssertException
import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

@SuppressWarnings(['unchecked'])
class GormScalars {

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
    private static final BigInteger BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    private static final BigInteger SHORT_MAX = BigInteger.valueOf(Short.MAX_VALUE);
    private static final BigInteger SHORT_MIN = BigInteger.valueOf(Short.MIN_VALUE);

    // true if its a number or string that we will attempt to convert to a number via toNumber()
    private static boolean isNumberIsh(Object input) {
        return input instanceof Number || input instanceof String;
    }

    private static Number toNumber(Object input) {
        if (input instanceof Number) {
            return (Number) input;
        }
        if (input instanceof String) {
            // we go to double and then let each scalar type decide what precision they want from it.  This
            // will allow lenient behavior in string input as well as Number input... eg "42.3" as a string to a Long
            // scalar is the same as new Double(42.3) to a Long scalar.
            //
            // each type will use Java Narrow casting to turn this into the desired type (Long, Integer, Short etc...)
            //
            // See http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.3
            //
            return Double.parseDouble((String) input);
        }
        // we never expect this and if we do, the code is wired wrong
        throw new AssertException("Unexpected case - this call should be protected by a previous call to isNumberIsh()");
    }

    public static GraphQLScalarType GraphQLInt = new GraphQLScalarType("Int", "Built-in Int", new Coercing<Integer, Integer>() {
        @Override
        public Integer serialize(Object input) {
            if (input instanceof Integer) {
                return (Integer) input;
            } else if (isNumberIsh(input)) {
                return toNumber(input).intValue();
            } else {
                return null;
            }
        }

        @Override
        public Integer parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Integer parseLiteral(Object input) {
            if (!(input instanceof IntValue)) return null;
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(INT_MIN) == -1 || value.compareTo(INT_MAX) == 1) {
                return null;
            }
            return value.intValue();
        }
    });

    public static GraphQLScalarType GraphQLLong = new GraphQLScalarType("Long", "Long type", new Coercing<Long, Long>() {
        @Override
        public Long serialize(Object input) {
            if (input instanceof Long) {
                return (Long) input;
            } else if (isNumberIsh(input)) {
                return toNumber(input).longValue();
            } else {
                return null;
            }
        }

        @Override
        public Long parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Long parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return Long.parseLong(((StringValue) input).getValue());
            } else if (input instanceof IntValue) {
                BigInteger value = ((IntValue) input).getValue();
                // Check if out of bounds.
                if (value.compareTo(LONG_MIN) < 0 || value.compareTo(LONG_MAX) > 0) {
                    return null;
                }
                return value.longValue();
            }
            return null;
        }
    });

    public static GraphQLScalarType GraphQLShort = new GraphQLScalarType("Short", "Built-in Short as Int", new Coercing<Short, Short>() {
        @Override
        public Short serialize(Object input) {
            if (input instanceof Short) {
                return (Short) input;
            } else if (isNumberIsh(input)) {
                return toNumber(input).shortValue();
            } else {
                return null;
            }
        }

        @Override
        public Short parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Short parseLiteral(Object input) {
            if (!(input instanceof IntValue)) return null;
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(SHORT_MIN) < 0 || value.compareTo(SHORT_MAX) > 0) {
                return null;
            }
            return value.shortValue();
        }
    });

    public static GraphQLScalarType GraphQLByte = new GraphQLScalarType("Byte", "Built-in Byte as Int", new Coercing<Byte, Byte>() {
        @Override
        public Byte serialize(Object input) {
            if (input instanceof Byte) {
                return (Byte) input;
            } else if (isNumberIsh(input)) {
                return toNumber(input).byteValue();
            } else {
                return null;
            }
        }

        @Override
        public Byte parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Byte parseLiteral(Object input) {
            if (!(input instanceof IntValue)) return null;
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(BYTE_MIN) < 0 || value.compareTo(BYTE_MAX) > 0) {
                return null;
            }
            return value.byteValue();
        }
    });

}
