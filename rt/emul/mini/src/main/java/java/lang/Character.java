/*
 * Copyright (c) 2002, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 * The {@code Character} class wraps a value of the primitive
 * type {@code char} in an object. An object of type
 * {@code Character} contains a single field whose type is
 * {@code char}.
 * <p>
 * In addition, this class provides several methods for determining
 * a character's category (lowercase letter, digit, etc.) and for converting
 * characters from uppercase to lowercase and vice versa.
 * <p>
 * Character information is based on the Unicode Standard, version 6.0.0.
 * <p>
 * The methods and data of class {@code Character} are defined by
 * the information in the <i>UnicodeData</i> file that is part of the
 * Unicode Character Database maintained by the Unicode
 * Consortium. This file specifies various properties including name
 * and general category for every defined Unicode code point or
 * character range.
 * <p>
 * The file and its description are available from the Unicode Consortium at:
 * <ul>
 * <li><a href="http://www.unicode.org">http://www.unicode.org</a>
 * </ul>
 *
 * <h4><a name="unicode">Unicode Character Representations</a></h4>
 *
 * <p>The {@code char} data type (and therefore the value that a
 * {@code Character} object encapsulates) are based on the
 * original Unicode specification, which defined characters as
 * fixed-width 16-bit entities. The Unicode Standard has since been
 * changed to allow for characters whose representation requires more
 * than 16 bits.  The range of legal <em>code point</em>s is now
 * U+0000 to U+10FFFF, known as <em>Unicode scalar value</em>.
 * (Refer to the <a
 * href="http://www.unicode.org/reports/tr27/#notation"><i>
 * definition</i></a> of the U+<i>n</i> notation in the Unicode
 * Standard.)
 *
 * <p><a name="BMP">The set of characters from U+0000 to U+FFFF is
 * sometimes referred to as the <em>Basic Multilingual Plane (BMP)</em>.
 * <a name="supplementary">Characters</a> whose code points are greater
 * than U+FFFF are called <em>supplementary character</em>s.  The Java
 * platform uses the UTF-16 representation in {@code char} arrays and
 * in the {@code String} and {@code StringBuffer} classes. In
 * this representation, supplementary characters are represented as a pair
 * of {@code char} values, the first from the <em>high-surrogates</em>
 * range, (&#92;uD800-&#92;uDBFF), the second from the
 * <em>low-surrogates</em> range (&#92;uDC00-&#92;uDFFF).
 *
 * <p>A {@code char} value, therefore, represents Basic
 * Multilingual Plane (BMP) code points, including the surrogate
 * code points, or code units of the UTF-16 encoding. An
 * {@code int} value represents all Unicode code points,
 * including supplementary code points. The lower (least significant)
 * 21 bits of {@code int} are used to represent Unicode code
 * points and the upper (most significant) 11 bits must be zero.
 * Unless otherwise specified, the behavior with respect to
 * supplementary characters and surrogate {@code char} values is
 * as follows:
 *
 * <ul>
 * <li>The methods that only accept a {@code char} value cannot support
 * supplementary characters. They treat {@code char} values from the
 * surrogate ranges as undefined characters. For example,
 * {@code Character.isLetter('\u005CuD840')} returns {@code false}, even though
 * this specific value if followed by any low-surrogate value in a string
 * would represent a letter.
 *
 * <li>The methods that accept an {@code int} value support all
 * Unicode characters, including supplementary characters. For
 * example, {@code Character.isLetter(0x2F81A)} returns
 * {@code true} because the code point value represents a letter
 * (a CJK ideograph).
 * </ul>
 *
 * <p>In the Java SE API documentation, <em>Unicode code point</em> is
 * used for character values in the range between U+0000 and U+10FFFF,
 * and <em>Unicode code unit</em> is used for 16-bit
 * {@code char} values that are code units of the <em>UTF-16</em>
 * encoding. For more information on Unicode terminology, refer to the
 * <a href="http://www.unicode.org/glossary/">Unicode Glossary</a>.
 *
 * @author  Lee Boynton
 * @author  Guy Steele
 * @author  Akira Tanaka
 * @author  Martin Buchholz
 * @author  Ulf Zibis
 * @since   1.0
 */
public final
class Character implements java.io.Serializable, Comparable<Character> {
    /**
     * The minimum radix available for conversion to and from strings.
     * The constant value of this field is the smallest value permitted
     * for the radix argument in radix-conversion methods such as the
     * {@code digit} method, the {@code forDigit} method, and the
     * {@code toString} method of class {@code Integer}.
     *
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Integer#toString(int, int)
     * @see     Integer#valueOf(String)
     */
    public static final int MIN_RADIX = 2;

    /**
     * The maximum radix available for conversion to and from strings.
     * The constant value of this field is the largest value permitted
     * for the radix argument in radix-conversion methods such as the
     * {@code digit} method, the {@code forDigit} method, and the
     * {@code toString} method of class {@code Integer}.
     *
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Integer#toString(int, int)
     * @see     Integer#valueOf(String)
     */
    public static final int MAX_RADIX = 36;

    /**
     * The constant value of this field is the smallest value of type
     * {@code char}, {@code '\u005Cu0000'}.
     *
     * @since   1.0.2
     */
    public static final char MIN_VALUE = '\u0000';

    /**
     * The constant value of this field is the largest value of type
     * {@code char}, {@code '\u005CuFFFF'}.
     *
     * @since   1.0.2
     */
    public static final char MAX_VALUE = '\uFFFF';

    /**
     * The {@code Class} instance representing the primitive type
     * {@code char}.
     *
     * @since   1.1
     */
    public static final Class<Character> TYPE = Class.getPrimitiveClass("char");

    /*
     * Normative general types
     */

    /*
     * General character types
     */

    /**
     * General category "Cn" in the Unicode specification.
     * @since   1.1
     */
    public static final byte UNASSIGNED = 0;

    /**
     * General category "Lu" in the Unicode specification.
     * @since   1.1
     */
    public static final byte UPPERCASE_LETTER = 1;

    /**
     * General category "Ll" in the Unicode specification.
     * @since   1.1
     */
    public static final byte LOWERCASE_LETTER = 2;

    /**
     * General category "Lt" in the Unicode specification.
     * @since   1.1
     */
    public static final byte TITLECASE_LETTER = 3;

    /**
     * General category "Lm" in the Unicode specification.
     * @since   1.1
     */
    public static final byte MODIFIER_LETTER = 4;

    /**
     * General category "Lo" in the Unicode specification.
     * @since   1.1
     */
    public static final byte OTHER_LETTER = 5;

    /**
     * General category "Mn" in the Unicode specification.
     * @since   1.1
     */
    public static final byte NON_SPACING_MARK = 6;

    /**
     * General category "Me" in the Unicode specification.
     * @since   1.1
     */
    public static final byte ENCLOSING_MARK = 7;

    /**
     * General category "Mc" in the Unicode specification.
     * @since   1.1
     */
    public static final byte COMBINING_SPACING_MARK = 8;

    /**
     * General category "Nd" in the Unicode specification.
     * @since   1.1
     */
    public static final byte DECIMAL_DIGIT_NUMBER        = 9;

    /**
     * General category "Nl" in the Unicode specification.
     * @since   1.1
     */
    public static final byte LETTER_NUMBER = 10;

    /**
     * General category "No" in the Unicode specification.
     * @since   1.1
     */
    public static final byte OTHER_NUMBER = 11;

    /**
     * General category "Zs" in the Unicode specification.
     * @since   1.1
     */
    public static final byte SPACE_SEPARATOR = 12;

    /**
     * General category "Zl" in the Unicode specification.
     * @since   1.1
     */
    public static final byte LINE_SEPARATOR = 13;

    /**
     * General category "Zp" in the Unicode specification.
     * @since   1.1
     */
    public static final byte PARAGRAPH_SEPARATOR = 14;

    /**
     * General category "Cc" in the Unicode specification.
     * @since   1.1
     */
    public static final byte CONTROL = 15;

    /**
     * General category "Cf" in the Unicode specification.
     * @since   1.1
     */
    public static final byte FORMAT = 16;

    /**
     * General category "Co" in the Unicode specification.
     * @since   1.1
     */
    public static final byte PRIVATE_USE = 18;

    /**
     * General category "Cs" in the Unicode specification.
     * @since   1.1
     */
    public static final byte SURROGATE = 19;

    /**
     * General category "Pd" in the Unicode specification.
     * @since   1.1
     */
    public static final byte DASH_PUNCTUATION = 20;

    /**
     * General category "Ps" in the Unicode specification.
     * @since   1.1
     */
    public static final byte START_PUNCTUATION = 21;

    /**
     * General category "Pe" in the Unicode specification.
     * @since   1.1
     */
    public static final byte END_PUNCTUATION = 22;

    /**
     * General category "Pc" in the Unicode specification.
     * @since   1.1
     */
    public static final byte CONNECTOR_PUNCTUATION = 23;

    /**
     * General category "Po" in the Unicode specification.
     * @since   1.1
     */
    public static final byte OTHER_PUNCTUATION = 24;

    /**
     * General category "Sm" in the Unicode specification.
     * @since   1.1
     */
    public static final byte MATH_SYMBOL = 25;

    /**
     * General category "Sc" in the Unicode specification.
     * @since   1.1
     */
    public static final byte CURRENCY_SYMBOL = 26;

    /**
     * General category "Sk" in the Unicode specification.
     * @since   1.1
     */
    public static final byte MODIFIER_SYMBOL = 27;

    /**
     * General category "So" in the Unicode specification.
     * @since   1.1
     */
    public static final byte OTHER_SYMBOL = 28;

    /**
     * General category "Pi" in the Unicode specification.
     * @since   1.4
     */
    public static final byte INITIAL_QUOTE_PUNCTUATION = 29;

    /**
     * General category "Pf" in the Unicode specification.
     * @since   1.4
     */
    public static final byte FINAL_QUOTE_PUNCTUATION = 30;

    /**
     * Error flag. Use int (code point) to avoid confusion with U+FFFF.
     */
    static final int ERROR = 0xFFFFFFFF;


    /**
     * Undefined bidirectional character type. Undefined {@code char}
     * values have undefined directionality in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_UNDEFINED = -1;

    /**
     * Strong bidirectional character type "L" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    /**
     * Strong bidirectional character type "R" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;

    /**
    * Strong bidirectional character type "AL" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

    /**
     * Weak bidirectional character type "EN" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;

    /**
     * Weak bidirectional character type "ES" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

    /**
     * Weak bidirectional character type "ET" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

    /**
     * Weak bidirectional character type "AN" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;

    /**
     * Weak bidirectional character type "CS" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

    /**
     * Weak bidirectional character type "NSM" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;

    /**
     * Weak bidirectional character type "BN" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;

    /**
     * Neutral bidirectional character type "B" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

    /**
     * Neutral bidirectional character type "S" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

    /**
     * Neutral bidirectional character type "WS" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_WHITESPACE = 12;

    /**
     * Neutral bidirectional character type "ON" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;

    /**
     * Strong bidirectional character type "LRE" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

    /**
     * Strong bidirectional character type "LRO" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

    /**
     * Strong bidirectional character type "RLE" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

    /**
     * Strong bidirectional character type "RLO" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

    /**
     * Weak bidirectional character type "PDF" in the Unicode specification.
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;

    /**
     * The minimum value of a
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode high-surrogate code unit</a>
     * in the UTF-16 encoding, constant {@code '\u005CuD800'}.
     * A high-surrogate is also known as a <i>leading-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MIN_HIGH_SURROGATE = '\uD800';

    /**
     * The maximum value of a
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode high-surrogate code unit</a>
     * in the UTF-16 encoding, constant {@code '\u005CuDBFF'}.
     * A high-surrogate is also known as a <i>leading-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MAX_HIGH_SURROGATE = '\uDBFF';

    /**
     * The minimum value of a
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode low-surrogate code unit</a>
     * in the UTF-16 encoding, constant {@code '\u005CuDC00'}.
     * A low-surrogate is also known as a <i>trailing-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MIN_LOW_SURROGATE  = '\uDC00';

    /**
     * The maximum value of a
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode low-surrogate code unit</a>
     * in the UTF-16 encoding, constant {@code '\u005CuDFFF'}.
     * A low-surrogate is also known as a <i>trailing-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MAX_LOW_SURROGATE  = '\uDFFF';

    /**
     * The minimum value of a Unicode surrogate code unit in the
     * UTF-16 encoding, constant {@code '\u005CuD800'}.
     *
     * @since 1.5
     */
    public static final char MIN_SURROGATE = MIN_HIGH_SURROGATE;

    /**
     * The maximum value of a Unicode surrogate code unit in the
     * UTF-16 encoding, constant {@code '\u005CuDFFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_SURROGATE = MAX_LOW_SURROGATE;

    /**
     * The minimum value of a
     * <a href="http://www.unicode.org/glossary/#supplementary_code_point">
     * Unicode supplementary code point</a>, constant {@code U+10000}.
     *
     * @since 1.5
     */
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

    /**
     * The minimum value of a
     * <a href="http://www.unicode.org/glossary/#code_point">
     * Unicode code point</a>, constant {@code U+0000}.
     *
     * @since 1.5
     */
    public static final int MIN_CODE_POINT = 0x000000;

    /**
     * The maximum value of a
     * <a href="http://www.unicode.org/glossary/#code_point">
     * Unicode code point</a>, constant {@code U+10FFFF}.
     *
     * @since 1.5
     */
    public static final int MAX_CODE_POINT = 0X10FFFF;


    /**
     * Instances of this class represent particular subsets of the Unicode
     * character set.  The only family of subsets defined in the
     * {@code Character} class is {@link Character.UnicodeBlock}.
     * Other portions of the Java API may define other subsets for their
     * own purposes.
     *
     * @since 1.2
     */
    public static class Subset  {

        private String name;

        /**
         * Constructs a new {@code Subset} instance.
         *
         * @param  name  The name of this subset
         * @exception NullPointerException if name is {@code null}
         */
        protected Subset(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.name = name;
        }

        /**
         * Compares two {@code Subset} objects for equality.
         * This method returns {@code true} if and only if
         * {@code this} and the argument refer to the same
         * object; since this method is {@code final}, this
         * guarantee holds for all subclasses.
         */
        public final boolean equals(Object obj) {
            return (this == obj);
        }

        /**
         * Returns the standard hash code as defined by the
         * {@link Object#hashCode} method.  This method
         * is {@code final} in order to ensure that the
         * {@code equals} and {@code hashCode} methods will
         * be consistent in all subclasses.
         */
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * Returns the name of this subset.
         */
        public final String toString() {
            return name;
        }
    }

    // See http://www.unicode.org/Public/UNIDATA/Blocks.txt
    // for the latest specification of Unicode Blocks.


    /**
     * The value of the {@code Character}.
     *
     * @serial
     */
    private final char value;

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 3786198910865385080L;

    /**
     * Constructs a newly allocated {@code Character} object that
     * represents the specified {@code char} value.
     *
     * @param  value   the value to be represented by the
     *                  {@code Character} object.
     */
    public Character(char value) {
        this.value = value;
    }

    private static class CharacterCache {
        private CharacterCache(){}

        static final Character cache[] = new Character[127 + 1];

        static {
            for (int i = 0; i < cache.length; i++)
                cache[i] = new Character((char)i);
        }
    }

    /**
     * Returns a <tt>Character</tt> instance representing the specified
     * <tt>char</tt> value.
     * If a new <tt>Character</tt> instance is not required, this method
     * should generally be used in preference to the constructor
     * {@link #Character(char)}, as this method is likely to yield
     * significantly better space and time performance by caching
     * frequently requested values.
     *
     * This method will always cache values in the range {@code
     * '\u005Cu0000'} to {@code '\u005Cu007F'}, inclusive, and may
     * cache other values outside of this range.
     *
     * @param  c a char value.
     * @return a <tt>Character</tt> instance representing <tt>c</tt>.
     * @since  1.5
     */
    public static Character valueOf(char c) {
        if (c <= 127) { // must cache
            return CharacterCache.cache[(int)c];
        }
        return new Character(c);
    }

    /**
     * Returns the value of this {@code Character} object.
     * @return  the primitive {@code char} value represented by
     *          this object.
     */
    public char charValue() {
        return value;
    }

    /**
     * Returns a hash code for this {@code Character}; equal to the result
     * of invoking {@code charValue()}.
     *
     * @return a hash code value for this {@code Character}
     */
    public int hashCode() {
        return (int)value;
    }

    /**
     * Compares this object against the specified object.
     * The result is {@code true} if and only if the argument is not
     * {@code null} and is a {@code Character} object that
     * represents the same {@code char} value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  {@code true} if the objects are the same;
     *          {@code false} otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Character) {
            return value == ((Character)obj).charValue();
        }
        return false;
    }

    /**
     * Returns a {@code String} object representing this
     * {@code Character}'s value.  The result is a string of
     * length 1 whose sole component is the primitive
     * {@code char} value represented by this
     * {@code Character} object.
     *
     * @return  a string representation of this object.
     */
    public String toString() {
        char buf[] = {value};
        return String.valueOf(buf);
    }

    /**
     * Returns a {@code String} object representing the
     * specified {@code char}.  The result is a string of length
     * 1 consisting solely of the specified {@code char}.
     *
     * @param c the {@code char} to be converted
     * @return the string representation of the specified {@code char}
     * @since 1.4
     */
    public static String toString(char c) {
        return String.valueOf(c);
    }

    /**
     * Determines whether the specified code point is a valid
     * <a href="http://www.unicode.org/glossary/#code_point">
     * Unicode code point value</a>.
     *
     * @param  codePoint the Unicode code point to be tested
     * @return {@code true} if the specified code point value is between
     *         {@link #MIN_CODE_POINT} and
     *         {@link #MAX_CODE_POINT} inclusive;
     *         {@code false} otherwise.
     * @since  1.5
     */
    public static boolean isValidCodePoint(int codePoint) {
        // Optimized form of:
        //     codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT
        int plane = codePoint >>> 16;
        return plane < ((MAX_CODE_POINT + 1) >>> 16);
    }

    /**
     * Determines whether the specified character (Unicode code point)
     * is in the <a href="#BMP">Basic Multilingual Plane (BMP)</a>.
     * Such code points can be represented using a single {@code char}.
     *
     * @param  codePoint the character (Unicode code point) to be tested
     * @return {@code true} if the specified code point is between
     *         {@link #MIN_VALUE} and {@link #MAX_VALUE} inclusive;
     *         {@code false} otherwise.
     * @since  1.7
     */
    public static boolean isBmpCodePoint(int codePoint) {
        return codePoint >>> 16 == 0;
        // Optimized form of:
        //     codePoint >= MIN_VALUE && codePoint <= MAX_VALUE
        // We consistently use logical shift (>>>) to facilitate
        // additional runtime optimizations.
    }

    /**
     * Determines whether the specified character (Unicode code point)
     * is in the <a href="#supplementary">supplementary character</a> range.
     *
     * @param  codePoint the character (Unicode code point) to be tested
     * @return {@code true} if the specified code point is between
     *         {@link #MIN_SUPPLEMENTARY_CODE_POINT} and
     *         {@link #MAX_CODE_POINT} inclusive;
     *         {@code false} otherwise.
     * @since  1.5
     */
    public static boolean isSupplementaryCodePoint(int codePoint) {
        return codePoint >= MIN_SUPPLEMENTARY_CODE_POINT
            && codePoint <  MAX_CODE_POINT + 1;
    }

    /**
     * Determines if the given {@code char} value is a
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * Unicode high-surrogate code unit</a>
     * (also known as <i>leading-surrogate code unit</i>).
     *
     * <p>Such values do not represent characters by themselves,
     * but are used in the representation of
     * <a href="#supplementary">supplementary characters</a>
     * in the UTF-16 encoding.
     *
     * @param  ch the {@code char} value to be tested.
     * @return {@code true} if the {@code char} value is between
     *         {@link #MIN_HIGH_SURROGATE} and
     *         {@link #MAX_HIGH_SURROGATE} inclusive;
     *         {@code false} otherwise.
     * @see    Character#isLowSurrogate(char)
     * @see    Character.UnicodeBlock#of(int)
     * @since  1.5
     */
    public static boolean isHighSurrogate(char ch) {
        // Help VM constant-fold; MAX_HIGH_SURROGATE + 1 == MIN_LOW_SURROGATE
        return ch >= MIN_HIGH_SURROGATE && ch < (MAX_HIGH_SURROGATE + 1);
    }

    /**
     * Determines if the given {@code char} value is a
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * Unicode low-surrogate code unit</a>
     * (also known as <i>trailing-surrogate code unit</i>).
     *
     * <p>Such values do not represent characters by themselves,
     * but are used in the representation of
     * <a href="#supplementary">supplementary characters</a>
     * in the UTF-16 encoding.
     *
     * @param  ch the {@code char} value to be tested.
     * @return {@code true} if the {@code char} value is between
     *         {@link #MIN_LOW_SURROGATE} and
     *         {@link #MAX_LOW_SURROGATE} inclusive;
     *         {@code false} otherwise.
     * @see    Character#isHighSurrogate(char)
     * @since  1.5
     */
    public static boolean isLowSurrogate(char ch) {
        return ch >= MIN_LOW_SURROGATE && ch < (MAX_LOW_SURROGATE + 1);
    }

    /**
     * Determines if the given {@code char} value is a Unicode
     * <i>surrogate code unit</i>.
     *
     * <p>Such values do not represent characters by themselves,
     * but are used in the representation of
     * <a href="#supplementary">supplementary characters</a>
     * in the UTF-16 encoding.
     *
     * <p>A char value is a surrogate code unit if and only if it is either
     * a {@linkplain #isLowSurrogate(char) low-surrogate code unit} or
     * a {@linkplain #isHighSurrogate(char) high-surrogate code unit}.
     *
     * @param  ch the {@code char} value to be tested.
     * @return {@code true} if the {@code char} value is between
     *         {@link #MIN_SURROGATE} and
     *         {@link #MAX_SURROGATE} inclusive;
     *         {@code false} otherwise.
     * @since  1.7
     */
    public static boolean isSurrogate(char ch) {
        return ch >= MIN_SURROGATE && ch < (MAX_SURROGATE + 1);
    }

    /**
     * Determines whether the specified pair of {@code char}
     * values is a valid
     * <a href="http://www.unicode.org/glossary/#surrogate_pair">
     * Unicode surrogate pair</a>.

     * <p>This method is equivalent to the expression:
     * <blockquote><pre>
     * isHighSurrogate(high) && isLowSurrogate(low)
     * </pre></blockquote>
     *
     * @param  high the high-surrogate code value to be tested
     * @param  low the low-surrogate code value to be tested
     * @return {@code true} if the specified high and
     * low-surrogate code values represent a valid surrogate pair;
     * {@code false} otherwise.
     * @since  1.5
     */
    public static boolean isSurrogatePair(char high, char low) {
        return isHighSurrogate(high) && isLowSurrogate(low);
    }

    /**
     * Determines the number of {@code char} values needed to
     * represent the specified character (Unicode code point). If the
     * specified character is equal to or greater than 0x10000, then
     * the method returns 2. Otherwise, the method returns 1.
     *
     * <p>This method doesn't validate the specified character to be a
     * valid Unicode code point. The caller must validate the
     * character value using {@link #isValidCodePoint(int) isValidCodePoint}
     * if necessary.
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  2 if the character is a valid supplementary character; 1 otherwise.
     * @see     Character#isSupplementaryCodePoint(int)
     * @since   1.5
     */
    public static int charCount(int codePoint) {
        return codePoint >= MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1;
    }

    /**
     * Converts the specified surrogate pair to its supplementary code
     * point value. This method does not validate the specified
     * surrogate pair. The caller must validate it using {@link
     * #isSurrogatePair(char, char) isSurrogatePair} if necessary.
     *
     * @param  high the high-surrogate code unit
     * @param  low the low-surrogate code unit
     * @return the supplementary code point composed from the
     *         specified surrogate pair.
     * @since  1.5
     */
    public static int toCodePoint(char high, char low) {
        // Optimized form of:
        // return ((high - MIN_HIGH_SURROGATE) << 10)
        //         + (low - MIN_LOW_SURROGATE)
        //         + MIN_SUPPLEMENTARY_CODE_POINT;
        return ((high << 10) + low) + (MIN_SUPPLEMENTARY_CODE_POINT
                                       - (MIN_HIGH_SURROGATE << 10)
                                       - MIN_LOW_SURROGATE);
    }

    /**
     * Returns the code point at the given index of the
     * {@code CharSequence}. If the {@code char} value at
     * the given index in the {@code CharSequence} is in the
     * high-surrogate range, the following index is less than the
     * length of the {@code CharSequence}, and the
     * {@code char} value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at the given index is returned.
     *
     * @param seq a sequence of {@code char} values (Unicode code
     * units)
     * @param index the index to the {@code char} values (Unicode
     * code units) in {@code seq} to be converted
     * @return the Unicode code point at the given index
     * @exception NullPointerException if {@code seq} is null.
     * @exception IndexOutOfBoundsException if the value
     * {@code index} is negative or not less than
     * {@link CharSequence#length() seq.length()}.
     * @since  1.5
     */
    public static int codePointAt(CharSequence seq, int index) {
        char c1 = seq.charAt(index++);
        if (isHighSurrogate(c1)) {
            if (index < seq.length()) {
                char c2 = seq.charAt(index);
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }

    /**
     * Returns the code point at the given index of the
     * {@code char} array. If the {@code char} value at
     * the given index in the {@code char} array is in the
     * high-surrogate range, the following index is less than the
     * length of the {@code char} array, and the
     * {@code char} value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at the given index is returned.
     *
     * @param a the {@code char} array
     * @param index the index to the {@code char} values (Unicode
     * code units) in the {@code char} array to be converted
     * @return the Unicode code point at the given index
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException if the value
     * {@code index} is negative or not less than
     * the length of the {@code char} array.
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index) {
        return codePointAtImpl(a, index, a.length);
    }

    /**
     * Returns the code point at the given index of the
     * {@code char} array, where only array elements with
     * {@code index} less than {@code limit} can be used. If
     * the {@code char} value at the given index in the
     * {@code char} array is in the high-surrogate range, the
     * following index is less than the {@code limit}, and the
     * {@code char} value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at the given index is returned.
     *
     * @param a the {@code char} array
     * @param index the index to the {@code char} values (Unicode
     * code units) in the {@code char} array to be converted
     * @param limit the index after the last array element that
     * can be used in the {@code char} array
     * @return the Unicode code point at the given index
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException if the {@code index}
     * argument is negative or not less than the {@code limit}
     * argument, or if the {@code limit} argument is negative or
     * greater than the length of the {@code char} array.
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index, int limit) {
        if (index >= limit || limit < 0 || limit > a.length) {
            throw new IndexOutOfBoundsException();
        }
        return codePointAtImpl(a, index, limit);
    }

    // throws ArrayIndexOutofBoundsException if index out of bounds
    static int codePointAtImpl(char[] a, int index, int limit) {
        char c1 = a[index++];
        if (isHighSurrogate(c1)) {
            if (index < limit) {
                char c2 = a[index];
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }

    /**
     * Returns the code point preceding the given index of the
     * {@code CharSequence}. If the {@code char} value at
     * {@code (index - 1)} in the {@code CharSequence} is in
     * the low-surrogate range, {@code (index - 2)} is not
     * negative, and the {@code char} value at {@code (index - 2)}
     * in the {@code CharSequence} is in the
     * high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at {@code (index - 1)} is
     * returned.
     *
     * @param seq the {@code CharSequence} instance
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if {@code seq} is null.
     * @exception IndexOutOfBoundsException if the {@code index}
     * argument is less than 1 or greater than {@link
     * CharSequence#length() seq.length()}.
     * @since  1.5
     */
    public static int codePointBefore(CharSequence seq, int index) {
        char c2 = seq.charAt(--index);
        if (isLowSurrogate(c2)) {
            if (index > 0) {
                char c1 = seq.charAt(--index);
                if (isHighSurrogate(c1)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c2;
    }

    /**
     * Returns the code point preceding the given index of the
     * {@code char} array. If the {@code char} value at
     * {@code (index - 1)} in the {@code char} array is in
     * the low-surrogate range, {@code (index - 2)} is not
     * negative, and the {@code char} value at {@code (index - 2)}
     * in the {@code char} array is in the
     * high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at {@code (index - 1)} is
     * returned.
     *
     * @param a the {@code char} array
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException if the {@code index}
     * argument is less than 1 or greater than the length of the
     * {@code char} array
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index) {
        return codePointBeforeImpl(a, index, 0);
    }

    /**
     * Returns the code point preceding the given index of the
     * {@code char} array, where only array elements with
     * {@code index} greater than or equal to {@code start}
     * can be used. If the {@code char} value at {@code (index - 1)}
     * in the {@code char} array is in the
     * low-surrogate range, {@code (index - 2)} is not less than
     * {@code start}, and the {@code char} value at
     * {@code (index - 2)} in the {@code char} array is in
     * the high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at {@code (index - 1)} is
     * returned.
     *
     * @param a the {@code char} array
     * @param index the index following the code point that should be returned
     * @param start the index of the first array element in the
     * {@code char} array
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException if the {@code index}
     * argument is not greater than the {@code start} argument or
     * is greater than the length of the {@code char} array, or
     * if the {@code start} argument is negative or not less than
     * the length of the {@code char} array.
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index, int start) {
        if (index <= start || start < 0 || start >= a.length) {
            throw new IndexOutOfBoundsException();
        }
        return codePointBeforeImpl(a, index, start);
    }

    // throws ArrayIndexOutofBoundsException if index-1 out of bounds
    static int codePointBeforeImpl(char[] a, int index, int start) {
        char c2 = a[--index];
        if (isLowSurrogate(c2)) {
            if (index > start) {
                char c1 = a[--index];
                if (isHighSurrogate(c1)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c2;
    }

    /**
     * Returns the leading surrogate (a
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
     * high surrogate code unit</a>) of the
     * <a href="http://www.unicode.org/glossary/#surrogate_pair">
     * surrogate pair</a>
     * representing the specified supplementary character (Unicode
     * code point) in the UTF-16 encoding.  If the specified character
     * is not a
     * <a href="Character.html#supplementary">supplementary character</a>,
     * an unspecified {@code char} is returned.
     *
     * <p>If
     * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
     * is {@code true}, then
     * {@link #isHighSurrogate isHighSurrogate}{@code (highSurrogate(x))} and
     * {@link #toCodePoint toCodePoint}{@code (highSurrogate(x), }{@link #lowSurrogate lowSurrogate}{@code (x)) == x}
     * are also always {@code true}.
     *
     * @param   codePoint a supplementary character (Unicode code point)
     * @return  the leading surrogate code unit used to represent the
     *          character in the UTF-16 encoding
     * @since   1.7
     */
    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10)
            + (MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
    }

    /**
     * Returns the trailing surrogate (a
     * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
     * low surrogate code unit</a>) of the
     * <a href="http://www.unicode.org/glossary/#surrogate_pair">
     * surrogate pair</a>
     * representing the specified supplementary character (Unicode
     * code point) in the UTF-16 encoding.  If the specified character
     * is not a
     * <a href="Character.html#supplementary">supplementary character</a>,
     * an unspecified {@code char} is returned.
     *
     * <p>If
     * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
     * is {@code true}, then
     * {@link #isLowSurrogate isLowSurrogate}{@code (lowSurrogate(x))} and
     * {@link #toCodePoint toCodePoint}{@code (}{@link #highSurrogate highSurrogate}{@code (x), lowSurrogate(x)) == x}
     * are also always {@code true}.
     *
     * @param   codePoint a supplementary character (Unicode code point)
     * @return  the trailing surrogate code unit used to represent the
     *          character in the UTF-16 encoding
     * @since   1.7
     */
    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + MIN_LOW_SURROGATE);
    }

    /**
     * Converts the specified character (Unicode code point) to its
     * UTF-16 representation. If the specified code point is a BMP
     * (Basic Multilingual Plane or Plane 0) value, the same value is
     * stored in {@code dst[dstIndex]}, and 1 is returned. If the
     * specified code point is a supplementary character, its
     * surrogate values are stored in {@code dst[dstIndex]}
     * (high-surrogate) and {@code dst[dstIndex+1]}
     * (low-surrogate), and 2 is returned.
     *
     * @param  codePoint the character (Unicode code point) to be converted.
     * @param  dst an array of {@code char} in which the
     * {@code codePoint}'s UTF-16 value is stored.
     * @param dstIndex the start index into the {@code dst}
     * array where the converted value is stored.
     * @return 1 if the code point is a BMP code point, 2 if the
     * code point is a supplementary code point.
     * @exception IllegalArgumentException if the specified
     * {@code codePoint} is not a valid Unicode code point.
     * @exception NullPointerException if the specified {@code dst} is null.
     * @exception IndexOutOfBoundsException if {@code dstIndex}
     * is negative or not less than {@code dst.length}, or if
     * {@code dst} at {@code dstIndex} doesn't have enough
     * array element(s) to store the resulting {@code char}
     * value(s). (If {@code dstIndex} is equal to
     * {@code dst.length-1} and the specified
     * {@code codePoint} is a supplementary character, the
     * high-surrogate value is not stored in
     * {@code dst[dstIndex]}.)
     * @since  1.5
     */
    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        if (isBmpCodePoint(codePoint)) {
            dst[dstIndex] = (char) codePoint;
            return 1;
        } else if (isValidCodePoint(codePoint)) {
            toSurrogates(codePoint, dst, dstIndex);
            return 2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Converts the specified character (Unicode code point) to its
     * UTF-16 representation stored in a {@code char} array. If
     * the specified code point is a BMP (Basic Multilingual Plane or
     * Plane 0) value, the resulting {@code char} array has
     * the same value as {@code codePoint}. If the specified code
     * point is a supplementary code point, the resulting
     * {@code char} array has the corresponding surrogate pair.
     *
     * @param  codePoint a Unicode code point
     * @return a {@code char} array having
     *         {@code codePoint}'s UTF-16 representation.
     * @exception IllegalArgumentException if the specified
     * {@code codePoint} is not a valid Unicode code point.
     * @since  1.5
     */
    public static char[] toChars(int codePoint) {
        if (isBmpCodePoint(codePoint)) {
            return new char[] { (char) codePoint };
        } else if (isValidCodePoint(codePoint)) {
            char[] result = new char[2];
            toSurrogates(codePoint, result, 0);
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }

    static void toSurrogates(int codePoint, char[] dst, int index) {
        // We write elements "backwards" to guarantee all-or-nothing
        dst[index+1] = lowSurrogate(codePoint);
        dst[index] = highSurrogate(codePoint);
    }

    /**
     * Returns the number of Unicode code points in the text range of
     * the specified char sequence. The text range begins at the
     * specified {@code beginIndex} and extends to the
     * {@code char} at index {@code endIndex - 1}. Thus the
     * length (in {@code char}s) of the text range is
     * {@code endIndex-beginIndex}. Unpaired surrogates within
     * the text range count as one code point each.
     *
     * @param seq the char sequence
     * @param beginIndex the index to the first {@code char} of
     * the text range.
     * @param endIndex the index after the last {@code char} of
     * the text range.
     * @return the number of Unicode code points in the specified text
     * range
     * @exception NullPointerException if {@code seq} is null.
     * @exception IndexOutOfBoundsException if the
     * {@code beginIndex} is negative, or {@code endIndex}
     * is larger than the length of the given sequence, or
     * {@code beginIndex} is larger than {@code endIndex}.
     * @since  1.5
     */
    public static int codePointCount(CharSequence seq, int beginIndex, int endIndex) {
        int length = seq.length();
        if (beginIndex < 0 || endIndex > length || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        int n = endIndex - beginIndex;
        for (int i = beginIndex; i < endIndex; ) {
            if (isHighSurrogate(seq.charAt(i++)) && i < endIndex &&
                isLowSurrogate(seq.charAt(i))) {
                n--;
                i++;
            }
        }
        return n;
    }

    /**
     * Returns the number of Unicode code points in a subarray of the
     * {@code char} array argument. The {@code offset}
     * argument is the index of the first {@code char} of the
     * subarray and the {@code count} argument specifies the
     * length of the subarray in {@code char}s. Unpaired
     * surrogates within the subarray count as one code point each.
     *
     * @param a the {@code char} array
     * @param offset the index of the first {@code char} in the
     * given {@code char} array
     * @param count the length of the subarray in {@code char}s
     * @return the number of Unicode code points in the specified subarray
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException if {@code offset} or
     * {@code count} is negative, or if {@code offset +
     * count} is larger than the length of the given array.
     * @since  1.5
     */
    public static int codePointCount(char[] a, int offset, int count) {
        if (count > a.length - offset || offset < 0 || count < 0) {
            throw new IndexOutOfBoundsException();
        }
        return codePointCountImpl(a, offset, count);
    }

    static int codePointCountImpl(char[] a, int offset, int count) {
        int endIndex = offset + count;
        int n = count;
        for (int i = offset; i < endIndex; ) {
            if (isHighSurrogate(a[i++]) && i < endIndex &&
                isLowSurrogate(a[i])) {
                n--;
                i++;
            }
        }
        return n;
    }

    /**
     * Returns the index within the given char sequence that is offset
     * from the given {@code index} by {@code codePointOffset}
     * code points. Unpaired surrogates within the text range given by
     * {@code index} and {@code codePointOffset} count as
     * one code point each.
     *
     * @param seq the char sequence
     * @param index the index to be offset
     * @param codePointOffset the offset in code points
     * @return the index within the char sequence
     * @exception NullPointerException if {@code seq} is null.
     * @exception IndexOutOfBoundsException if {@code index}
     *   is negative or larger then the length of the char sequence,
     *   or if {@code codePointOffset} is positive and the
     *   subsequence starting with {@code index} has fewer than
     *   {@code codePointOffset} code points, or if
     *   {@code codePointOffset} is negative and the subsequence
     *   before {@code index} has fewer than the absolute value
     *   of {@code codePointOffset} code points.
     * @since 1.5
     */
    public static int offsetByCodePoints(CharSequence seq, int index,
                                         int codePointOffset) {
        int length = seq.length();
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }

        int x = index;
        if (codePointOffset >= 0) {
            int i;
            for (i = 0; x < length && i < codePointOffset; i++) {
                if (isHighSurrogate(seq.charAt(x++)) && x < length &&
                    isLowSurrogate(seq.charAt(x))) {
                    x++;
                }
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i;
            for (i = codePointOffset; x > 0 && i < 0; i++) {
                if (isLowSurrogate(seq.charAt(--x)) && x > 0 &&
                    isHighSurrogate(seq.charAt(x-1))) {
                    x--;
                }
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    /**
     * Returns the index within the given {@code char} subarray
     * that is offset from the given {@code index} by
     * {@code codePointOffset} code points. The
     * {@code start} and {@code count} arguments specify a
     * subarray of the {@code char} array. Unpaired surrogates
     * within the text range given by {@code index} and
     * {@code codePointOffset} count as one code point each.
     *
     * @param a the {@code char} array
     * @param start the index of the first {@code char} of the
     * subarray
     * @param count the length of the subarray in {@code char}s
     * @param index the index to be offset
     * @param codePointOffset the offset in code points
     * @return the index within the subarray
     * @exception NullPointerException if {@code a} is null.
     * @exception IndexOutOfBoundsException
     *   if {@code start} or {@code count} is negative,
     *   or if {@code start + count} is larger than the length of
     *   the given array,
     *   or if {@code index} is less than {@code start} or
     *   larger then {@code start + count},
     *   or if {@code codePointOffset} is positive and the text range
     *   starting with {@code index} and ending with {@code start + count - 1}
     *   has fewer than {@code codePointOffset} code
     *   points,
     *   or if {@code codePointOffset} is negative and the text range
     *   starting with {@code start} and ending with {@code index - 1}
     *   has fewer than the absolute value of
     *   {@code codePointOffset} code points.
     * @since 1.5
     */
    public static int offsetByCodePoints(char[] a, int start, int count,
                                         int index, int codePointOffset) {
        if (count > a.length-start || start < 0 || count < 0
            || index < start || index > start+count) {
            throw new IndexOutOfBoundsException();
        }
        return offsetByCodePointsImpl(a, start, count, index, codePointOffset);
    }

    static int offsetByCodePointsImpl(char[]a, int start, int count,
                                      int index, int codePointOffset) {
        int x = index;
        if (codePointOffset >= 0) {
            int limit = start + count;
            int i;
            for (i = 0; x < limit && i < codePointOffset; i++) {
                if (isHighSurrogate(a[x++]) && x < limit &&
                    isLowSurrogate(a[x])) {
                    x++;
                }
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i;
            for (i = codePointOffset; x > start && i < 0; i++) {
                if (isLowSurrogate(a[--x]) && x > start &&
                    isHighSurrogate(a[x-1])) {
                    x--;
                }
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    /**
     * Determines if the specified character is a lowercase character.
     * <p>
     * A character is lowercase if its general category type, provided
     * by {@code Character.getType(ch)}, is
     * {@code LOWERCASE_LETTER}, or it has contributory property
     * Other_Lowercase as defined by the Unicode Standard.
     * <p>
     * The following are examples of lowercase characters:
     * <p><blockquote><pre>
     * a b c d e f g h i j k l m n o p q r s t u v w x y z
     * '&#92;u00DF' '&#92;u00E0' '&#92;u00E1' '&#92;u00E2' '&#92;u00E3' '&#92;u00E4' '&#92;u00E5' '&#92;u00E6'
     * '&#92;u00E7' '&#92;u00E8' '&#92;u00E9' '&#92;u00EA' '&#92;u00EB' '&#92;u00EC' '&#92;u00ED' '&#92;u00EE'
     * '&#92;u00EF' '&#92;u00F0' '&#92;u00F1' '&#92;u00F2' '&#92;u00F3' '&#92;u00F4' '&#92;u00F5' '&#92;u00F6'
     * '&#92;u00F8' '&#92;u00F9' '&#92;u00FA' '&#92;u00FB' '&#92;u00FC' '&#92;u00FD' '&#92;u00FE' '&#92;u00FF'
     * </pre></blockquote>
     * <p> Many other Unicode characters are lowercase too.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isLowerCase(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is lowercase;
     *          {@code false} otherwise.
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#toLowerCase(char)
     * @see     Character#getType(char)
     */
    public static boolean isLowerCase(char ch) {
        return ch == toLowerCase(ch);
    }

    /**
     * Determines if the specified character is an uppercase character.
     * <p>
     * A character is uppercase if its general category type, provided by
     * {@code Character.getType(ch)}, is {@code UPPERCASE_LETTER}.
     * or it has contributory property Other_Uppercase as defined by the Unicode Standard.
     * <p>
     * The following are examples of uppercase characters:
     * <p><blockquote><pre>
     * A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     * '&#92;u00C0' '&#92;u00C1' '&#92;u00C2' '&#92;u00C3' '&#92;u00C4' '&#92;u00C5' '&#92;u00C6' '&#92;u00C7'
     * '&#92;u00C8' '&#92;u00C9' '&#92;u00CA' '&#92;u00CB' '&#92;u00CC' '&#92;u00CD' '&#92;u00CE' '&#92;u00CF'
     * '&#92;u00D0' '&#92;u00D1' '&#92;u00D2' '&#92;u00D3' '&#92;u00D4' '&#92;u00D5' '&#92;u00D6' '&#92;u00D8'
     * '&#92;u00D9' '&#92;u00DA' '&#92;u00DB' '&#92;u00DC' '&#92;u00DD' '&#92;u00DE'
     * </pre></blockquote>
     * <p> Many other Unicode characters are uppercase too.<p>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isUpperCase(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is uppercase;
     *          {@code false} otherwise.
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#toUpperCase(char)
     * @see     Character#getType(char)
     * @since   1.0
     */
    public static boolean isUpperCase(char ch) {
        return ch == toUpperCase(ch);
    }

    /**
     * Determines if the specified character is a titlecase character.
     * <p>
     * A character is a titlecase character if its general
     * category type, provided by {@code Character.getType(ch)},
     * is {@code TITLECASE_LETTER}.
     * <p>
     * Some characters look like pairs of Latin letters. For example, there
     * is an uppercase letter that looks like "LJ" and has a corresponding
     * lowercase letter that looks like "lj". A third form, which looks like "Lj",
     * is the appropriate form to use when rendering a word in lowercase
     * with initial capitals, as for a book title.
     * <p>
     * These are some of the Unicode characters for which this method returns
     * {@code true}:
     * <ul>
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
     * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
     * </ul>
     * <p> Many other Unicode characters are titlecase too.<p>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isTitleCase(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is titlecase;
     *          {@code false} otherwise.
     * @see     Character#isLowerCase(char)
     * @see     Character#isUpperCase(char)
     * @see     Character#toTitleCase(char)
     * @see     Character#getType(char)
     * @since   1.0.2
     */
    public static boolean isTitleCase(char ch) {
        return isTitleCase((int)ch);
    }

    /**
     * Determines if the specified character (Unicode code point) is a titlecase character.
     * <p>
     * A character is a titlecase character if its general
     * category type, provided by {@link Character#getType(int) getType(codePoint)},
     * is {@code TITLECASE_LETTER}.
     * <p>
     * Some characters look like pairs of Latin letters. For example, there
     * is an uppercase letter that looks like "LJ" and has a corresponding
     * lowercase letter that looks like "lj". A third form, which looks like "Lj",
     * is the appropriate form to use when rendering a word in lowercase
     * with initial capitals, as for a book title.
     * <p>
     * These are some of the Unicode characters for which this method returns
     * {@code true}:
     * <ul>
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON}
     * <li>{@code LATIN CAPITAL LETTER L WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER N WITH SMALL LETTER J}
     * <li>{@code LATIN CAPITAL LETTER D WITH SMALL LETTER Z}
     * </ul>
     * <p> Many other Unicode characters are titlecase too.<p>
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is titlecase;
     *          {@code false} otherwise.
     * @see     Character#isLowerCase(int)
     * @see     Character#isUpperCase(int)
     * @see     Character#toTitleCase(int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isTitleCase(int codePoint) {
        return getType(codePoint) == Character.TITLECASE_LETTER;
    }

    /**
     * Determines if the specified character is a digit.
     * <p>
     * A character is a digit if its general category type, provided
     * by {@code Character.getType(ch)}, is
     * {@code DECIMAL_DIGIT_NUMBER}.
     * <p>
     * Some Unicode character ranges that contain digits:
     * <ul>
     * <li>{@code '\u005Cu0030'} through {@code '\u005Cu0039'},
     *     ISO-LATIN-1 digits ({@code '0'} through {@code '9'})
     * <li>{@code '\u005Cu0660'} through {@code '\u005Cu0669'},
     *     Arabic-Indic digits
     * <li>{@code '\u005Cu06F0'} through {@code '\u005Cu06F9'},
     *     Extended Arabic-Indic digits
     * <li>{@code '\u005Cu0966'} through {@code '\u005Cu096F'},
     *     Devanagari digits
     * <li>{@code '\u005CuFF10'} through {@code '\u005CuFF19'},
     *     Fullwidth digits
     * </ul>
     *
     * Many other character ranges contain digits as well.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isDigit(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is a digit;
     *          {@code false} otherwise.
     * @see     Character#digit(char, int)
     * @see     Character#forDigit(int, int)
     * @see     Character#getType(char)
     */
    public static boolean isDigit(char ch) {
        return String.valueOf(ch).matches("\\d");
    }

    /**
     * Determines if the specified character (Unicode code point) is a digit.
     * <p>
     * A character is a digit if its general category type, provided
     * by {@link Character#getType(int) getType(codePoint)}, is
     * {@code DECIMAL_DIGIT_NUMBER}.
     * <p>
     * Some Unicode character ranges that contain digits:
     * <ul>
     * <li>{@code '\u005Cu0030'} through {@code '\u005Cu0039'},
     *     ISO-LATIN-1 digits ({@code '0'} through {@code '9'})
     * <li>{@code '\u005Cu0660'} through {@code '\u005Cu0669'},
     *     Arabic-Indic digits
     * <li>{@code '\u005Cu06F0'} through {@code '\u005Cu06F9'},
     *     Extended Arabic-Indic digits
     * <li>{@code '\u005Cu0966'} through {@code '\u005Cu096F'},
     *     Devanagari digits
     * <li>{@code '\u005CuFF10'} through {@code '\u005CuFF19'},
     *     Fullwidth digits
     * </ul>
     *
     * Many other character ranges contain digits as well.
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is a digit;
     *          {@code false} otherwise.
     * @see     Character#forDigit(int, int)
     * @see     Character#getType(int)
     * @since   1.5
     */
    public static boolean isDigit(int codePoint) {
        return fromCodeChars(codePoint).matches("\\d");
    }
    
    @JavaScriptBody(args = "c", body = "return String.fromCharCode(c);")
    private native static String fromCodeChars(int codePoint);

    /**
     * Determines if a character is defined in Unicode.
     * <p>
     * A character is defined if at least one of the following is true:
     * <ul>
     * <li>It has an entry in the UnicodeData file.
     * <li>It has a value in a range defined by the UnicodeData file.
     * </ul>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isDefined(int)} method.
     *
     * @param   ch   the character to be tested
     * @return  {@code true} if the character has a defined meaning
     *          in Unicode; {@code false} otherwise.
     * @see     Character#isDigit(char)
     * @see     Character#isLetter(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#isUpperCase(char)
     * @since   1.0.2
     */
    public static boolean isDefined(char ch) {
        return isDefined((int)ch);
    }

    /**
     * Determines if a character (Unicode code point) is defined in Unicode.
     * <p>
     * A character is defined if at least one of the following is true:
     * <ul>
     * <li>It has an entry in the UnicodeData file.
     * <li>It has a value in a range defined by the UnicodeData file.
     * </ul>
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character has a defined meaning
     *          in Unicode; {@code false} otherwise.
     * @see     Character#isDigit(int)
     * @see     Character#isLetter(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#isUpperCase(int)
     * @since   1.5
     */
    public static boolean isDefined(int codePoint) {
        return getType(codePoint) != Character.UNASSIGNED;
    }

    /**
     * Determines if the specified character is a letter.
     * <p>
     * A character is considered to be a letter if its general
     * category type, provided by {@code Character.getType(ch)},
     * is any of the following:
     * <ul>
     * <li> {@code UPPERCASE_LETTER}
     * <li> {@code LOWERCASE_LETTER}
     * <li> {@code TITLECASE_LETTER}
     * <li> {@code MODIFIER_LETTER}
     * <li> {@code OTHER_LETTER}
     * </ul>
     *
     * Not all letters have case. Many characters are
     * letters but are neither uppercase nor lowercase nor titlecase.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isLetter(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is a letter;
     *          {@code false} otherwise.
     * @see     Character#isDigit(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isJavaLetter(char)
     * @see     Character#isJavaLetterOrDigit(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isLowerCase(char)
     * @see     Character#isTitleCase(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @see     Character#isUpperCase(char)
     */
    public static boolean isLetter(char ch) {
        return String.valueOf(ch).matches("\\w") && !isDigit(ch);
    }

    /**
     * Determines if the specified character (Unicode code point) is a letter.
     * <p>
     * A character is considered to be a letter if its general
     * category type, provided by {@link Character#getType(int) getType(codePoint)},
     * is any of the following:
     * <ul>
     * <li> {@code UPPERCASE_LETTER}
     * <li> {@code LOWERCASE_LETTER}
     * <li> {@code TITLECASE_LETTER}
     * <li> {@code MODIFIER_LETTER}
     * <li> {@code OTHER_LETTER}
     * </ul>
     *
     * Not all letters have case. Many characters are
     * letters but are neither uppercase nor lowercase nor titlecase.
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is a letter;
     *          {@code false} otherwise.
     * @see     Character#isDigit(int)
     * @see     Character#isJavaIdentifierStart(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isLowerCase(int)
     * @see     Character#isTitleCase(int)
     * @see     Character#isUnicodeIdentifierStart(int)
     * @see     Character#isUpperCase(int)
     * @since   1.5
     */
    public static boolean isLetter(int codePoint) {
        return fromCodeChars(codePoint).matches("\\w") && !isDigit(codePoint);
    }

    /**
     * Determines if the specified character is a letter or digit.
     * <p>
     * A character is considered to be a letter or digit if either
     * {@code Character.isLetter(char ch)} or
     * {@code Character.isDigit(char ch)} returns
     * {@code true} for the character.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isLetterOrDigit(int)} method.
     *
     * @param   ch   the character to be tested.
     * @return  {@code true} if the character is a letter or digit;
     *          {@code false} otherwise.
     * @see     Character#isDigit(char)
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isJavaLetter(char)
     * @see     Character#isJavaLetterOrDigit(char)
     * @see     Character#isLetter(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @since   1.0.2
     */
    public static boolean isLetterOrDigit(char ch) {
        return String.valueOf(ch).matches("\\w");
    }

    /**
     * Determines if the specified character (Unicode code point) is a letter or digit.
     * <p>
     * A character is considered to be a letter or digit if either
     * {@link #isLetter(int) isLetter(codePoint)} or
     * {@link #isDigit(int) isDigit(codePoint)} returns
     * {@code true} for the character.
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is a letter or digit;
     *          {@code false} otherwise.
     * @see     Character#isDigit(int)
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isLetter(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @since   1.5
     */
    public static boolean isLetterOrDigit(int codePoint) {
        return fromCodeChars(codePoint).matches("\\w");
    }
    
    static int getType(int x) {
        throw new UnsupportedOperationException();
    }
 
    /**
     * Determines if the specified character is
     * permissible as the first character in a Java identifier.
     * <p>
     * A character may start a Java identifier if and only if
     * one of the following conditions is true:
     * <ul>
     * <li> {@link #isLetter(char) isLetter(ch)} returns {@code true}
     * <li> {@link #getType(char) getType(ch)} returns {@code LETTER_NUMBER}
     * <li> {@code ch} is a currency symbol (such as {@code '$'})
     * <li> {@code ch} is a connecting punctuation character (such as {@code '_'}).
     * </ul>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isJavaIdentifierStart(int)} method.
     *
     * @param   ch the character to be tested.
     * @return  {@code true} if the character may start a Java identifier;
     *          {@code false} otherwise.
     * @see     Character#isJavaIdentifierPart(char)
     * @see     Character#isLetter(char)
     * @see     Character#isUnicodeIdentifierStart(char)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.1
     */
    public static boolean isJavaIdentifierStart(char ch) {
        return isJavaIdentifierStart((int)ch);
    }

    /**
     * Determines if the character (Unicode code point) is
     * permissible as the first character in a Java identifier.
     * <p>
     * A character may start a Java identifier if and only if
     * one of the following conditions is true:
     * <ul>
     * <li> {@link #isLetter(int) isLetter(codePoint)}
     *      returns {@code true}
     * <li> {@link #getType(int) getType(codePoint)}
     *      returns {@code LETTER_NUMBER}
     * <li> the referenced character is a currency symbol (such as {@code '$'})
     * <li> the referenced character is a connecting punctuation character
     *      (such as {@code '_'}).
     * </ul>
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character may start a Java identifier;
     *          {@code false} otherwise.
     * @see     Character#isJavaIdentifierPart(int)
     * @see     Character#isLetter(int)
     * @see     Character#isUnicodeIdentifierStart(int)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.5
     */
    public static boolean isJavaIdentifierStart(int codePoint) {
        return 
            ('A' <= codePoint && codePoint <= 'Z') ||
            ('a' <= codePoint && codePoint <= 'z');
    }

    /**
     * Determines if the specified character may be part of a Java
     * identifier as other than the first character.
     * <p>
     * A character may be part of a Java identifier if any of the following
     * are true:
     * <ul>
     * <li>  it is a letter
     * <li>  it is a currency symbol (such as {@code '$'})
     * <li>  it is a connecting punctuation character (such as {@code '_'})
     * <li>  it is a digit
     * <li>  it is a numeric letter (such as a Roman numeral character)
     * <li>  it is a combining mark
     * <li>  it is a non-spacing mark
     * <li> {@code isIdentifierIgnorable} returns
     * {@code true} for the character
     * </ul>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isJavaIdentifierPart(int)} method.
     *
     * @param   ch      the character to be tested.
     * @return {@code true} if the character may be part of a
     *          Java identifier; {@code false} otherwise.
     * @see     Character#isIdentifierIgnorable(char)
     * @see     Character#isJavaIdentifierStart(char)
     * @see     Character#isLetterOrDigit(char)
     * @see     Character#isUnicodeIdentifierPart(char)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.1
     */
    public static boolean isJavaIdentifierPart(char ch) {
        return isJavaIdentifierPart((int)ch);
    }

    /**
     * Determines if the character (Unicode code point) may be part of a Java
     * identifier as other than the first character.
     * <p>
     * A character may be part of a Java identifier if any of the following
     * are true:
     * <ul>
     * <li>  it is a letter
     * <li>  it is a currency symbol (such as {@code '$'})
     * <li>  it is a connecting punctuation character (such as {@code '_'})
     * <li>  it is a digit
     * <li>  it is a numeric letter (such as a Roman numeral character)
     * <li>  it is a combining mark
     * <li>  it is a non-spacing mark
     * <li> {@link #isIdentifierIgnorable(int)
     * isIdentifierIgnorable(codePoint)} returns {@code true} for
     * the character
     * </ul>
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return {@code true} if the character may be part of a
     *          Java identifier; {@code false} otherwise.
     * @see     Character#isIdentifierIgnorable(int)
     * @see     Character#isJavaIdentifierStart(int)
     * @see     Character#isLetterOrDigit(int)
     * @see     Character#isUnicodeIdentifierPart(int)
     * @see     javax.lang.model.SourceVersion#isIdentifier(CharSequence)
     * @since   1.5
     */
    public static boolean isJavaIdentifierPart(int codePoint) {
        return isJavaIdentifierStart(codePoint) ||
            ('0' <= codePoint && codePoint <= '9') || codePoint == '$';
    }
   
    /**
     * Converts the character argument to lowercase using case
     * mapping information from the UnicodeData file.
     * <p>
     * Note that
     * {@code Character.isLowerCase(Character.toLowerCase(ch))}
     * does not always return {@code true} for some ranges of
     * characters, particularly those that are symbols or ideographs.
     *
     * <p>In general, {@link String#toLowerCase()} should be used to map
     * characters to lowercase. {@code String} case mapping methods
     * have several benefits over {@code Character} case mapping methods.
     * {@code String} case mapping methods can perform locale-sensitive
     * mappings, context-sensitive mappings, and 1:M character mappings, whereas
     * the {@code Character} case mapping methods cannot.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #toLowerCase(int)} method.
     *
     * @param   ch   the character to be converted.
     * @return  the lowercase equivalent of the character, if any;
     *          otherwise, the character itself.
     * @see     Character#isLowerCase(char)
     * @see     String#toLowerCase()
     */
    public static char toLowerCase(char ch) {
        return String.valueOf(ch).toLowerCase().charAt(0);
    }

    /**
     * Converts the character argument to uppercase using case mapping
     * information from the UnicodeData file.
     * <p>
     * Note that
     * {@code Character.isUpperCase(Character.toUpperCase(ch))}
     * does not always return {@code true} for some ranges of
     * characters, particularly those that are symbols or ideographs.
     *
     * <p>In general, {@link String#toUpperCase()} should be used to map
     * characters to uppercase. {@code String} case mapping methods
     * have several benefits over {@code Character} case mapping methods.
     * {@code String} case mapping methods can perform locale-sensitive
     * mappings, context-sensitive mappings, and 1:M character mappings, whereas
     * the {@code Character} case mapping methods cannot.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #toUpperCase(int)} method.
     *
     * @param   ch   the character to be converted.
     * @return  the uppercase equivalent of the character, if any;
     *          otherwise, the character itself.
     * @see     Character#isUpperCase(char)
     * @see     String#toUpperCase()
     */
    public static char toUpperCase(char ch) {
        return String.valueOf(ch).toUpperCase().charAt(0);
    }

    /**
     * Returns the numeric value of the character {@code ch} in the
     * specified radix.
     * <p>
     * If the radix is not in the range {@code MIN_RADIX} &le;
     * {@code radix} &le; {@code MAX_RADIX} or if the
     * value of {@code ch} is not a valid digit in the specified
     * radix, {@code -1} is returned. A character is a valid digit
     * if at least one of the following is true:
     * <ul>
     * <li>The method {@code isDigit} is {@code true} of the character
     *     and the Unicode decimal digit value of the character (or its
     *     single-character decomposition) is less than the specified radix.
     *     In this case the decimal digit value is returned.
     * <li>The character is one of the uppercase Latin letters
     *     {@code 'A'} through {@code 'Z'} and its code is less than
     *     {@code radix + 'A' - 10}.
     *     In this case, {@code ch - 'A' + 10}
     *     is returned.
     * <li>The character is one of the lowercase Latin letters
     *     {@code 'a'} through {@code 'z'} and its code is less than
     *     {@code radix + 'a' - 10}.
     *     In this case, {@code ch - 'a' + 10}
     *     is returned.
     * <li>The character is one of the fullwidth uppercase Latin letters A
     *     ({@code '\u005CuFF21'}) through Z ({@code '\u005CuFF3A'})
     *     and its code is less than
     *     {@code radix + '\u005CuFF21' - 10}.
     *     In this case, {@code ch - '\u005CuFF21' + 10}
     *     is returned.
     * <li>The character is one of the fullwidth lowercase Latin letters a
     *     ({@code '\u005CuFF41'}) through z ({@code '\u005CuFF5A'})
     *     and its code is less than
     *     {@code radix + '\u005CuFF41' - 10}.
     *     In this case, {@code ch - '\u005CuFF41' + 10}
     *     is returned.
     * </ul>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #digit(int, int)} method.
     *
     * @param   ch      the character to be converted.
     * @param   radix   the radix.
     * @return  the numeric value represented by the character in the
     *          specified radix.
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(char)
     */
    public static int digit(char ch, int radix) {
        return digit((int)ch, radix);
    }

    /**
     * Returns the numeric value of the specified character (Unicode
     * code point) in the specified radix.
     *
     * <p>If the radix is not in the range {@code MIN_RADIX} &le;
     * {@code radix} &le; {@code MAX_RADIX} or if the
     * character is not a valid digit in the specified
     * radix, {@code -1} is returned. A character is a valid digit
     * if at least one of the following is true:
     * <ul>
     * <li>The method {@link #isDigit(int) isDigit(codePoint)} is {@code true} of the character
     *     and the Unicode decimal digit value of the character (or its
     *     single-character decomposition) is less than the specified radix.
     *     In this case the decimal digit value is returned.
     * <li>The character is one of the uppercase Latin letters
     *     {@code 'A'} through {@code 'Z'} and its code is less than
     *     {@code radix + 'A' - 10}.
     *     In this case, {@code codePoint - 'A' + 10}
     *     is returned.
     * <li>The character is one of the lowercase Latin letters
     *     {@code 'a'} through {@code 'z'} and its code is less than
     *     {@code radix + 'a' - 10}.
     *     In this case, {@code codePoint - 'a' + 10}
     *     is returned.
     * <li>The character is one of the fullwidth uppercase Latin letters A
     *     ({@code '\u005CuFF21'}) through Z ({@code '\u005CuFF3A'})
     *     and its code is less than
     *     {@code radix + '\u005CuFF21' - 10}.
     *     In this case,
     *     {@code codePoint - '\u005CuFF21' + 10}
     *     is returned.
     * <li>The character is one of the fullwidth lowercase Latin letters a
     *     ({@code '\u005CuFF41'}) through z ({@code '\u005CuFF5A'})
     *     and its code is less than
     *     {@code radix + '\u005CuFF41'- 10}.
     *     In this case,
     *     {@code codePoint - '\u005CuFF41' + 10}
     *     is returned.
     * </ul>
     *
     * @param   codePoint the character (Unicode code point) to be converted.
     * @param   radix   the radix.
     * @return  the numeric value represented by the character in the
     *          specified radix.
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(int)
     * @since   1.5
     */
    @JavaScriptBody(args = { "codePoint", "radix" }, body=
        "var x = parseInt(String.fromCharCode(codePoint), radix);\n"
      + "return isNaN(x) ? -1 : x;"
    )
    public static int digit(int codePoint, int radix) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code int} value that the specified Unicode
     * character represents. For example, the character
     * {@code '\u005Cu216C'} (the roman numeral fifty) will return
     * an int with a value of 50.
     * <p>
     * The letters A-Z in their uppercase ({@code '\u005Cu0041'} through
     * {@code '\u005Cu005A'}), lowercase
     * ({@code '\u005Cu0061'} through {@code '\u005Cu007A'}), and
     * full width variant ({@code '\u005CuFF21'} through
     * {@code '\u005CuFF3A'} and {@code '\u005CuFF41'} through
     * {@code '\u005CuFF5A'}) forms have numeric values from 10
     * through 35. This is independent of the Unicode specification,
     * which does not assign numeric values to these {@code char}
     * values.
     * <p>
     * If the character does not have a numeric value, then -1 is returned.
     * If the character has a numeric value that cannot be represented as a
     * nonnegative integer (for example, a fractional value), then -2
     * is returned.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #getNumericValue(int)} method.
     *
     * @param   ch      the character to be converted.
     * @return  the numeric value of the character, as a nonnegative {@code int}
     *           value; -2 if the character has a numeric value that is not a
     *          nonnegative integer; -1 if the character has no numeric value.
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(char)
     * @since   1.1
     */
    public static int getNumericValue(char ch) {
        return getNumericValue((int)ch);
    }

    /**
     * Returns the {@code int} value that the specified
     * character (Unicode code point) represents. For example, the character
     * {@code '\u005Cu216C'} (the Roman numeral fifty) will return
     * an {@code int} with a value of 50.
     * <p>
     * The letters A-Z in their uppercase ({@code '\u005Cu0041'} through
     * {@code '\u005Cu005A'}), lowercase
     * ({@code '\u005Cu0061'} through {@code '\u005Cu007A'}), and
     * full width variant ({@code '\u005CuFF21'} through
     * {@code '\u005CuFF3A'} and {@code '\u005CuFF41'} through
     * {@code '\u005CuFF5A'}) forms have numeric values from 10
     * through 35. This is independent of the Unicode specification,
     * which does not assign numeric values to these {@code char}
     * values.
     * <p>
     * If the character does not have a numeric value, then -1 is returned.
     * If the character has a numeric value that cannot be represented as a
     * nonnegative integer (for example, a fractional value), then -2
     * is returned.
     *
     * @param   codePoint the character (Unicode code point) to be converted.
     * @return  the numeric value of the character, as a nonnegative {@code int}
     *          value; -2 if the character has a numeric value that is not a
     *          nonnegative integer; -1 if the character has no numeric value.
     * @see     Character#forDigit(int, int)
     * @see     Character#isDigit(int)
     * @since   1.5
     */
    public static int getNumericValue(int codePoint) {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if the specified character is ISO-LATIN-1 white space.
     * This method returns {@code true} for the following five
     * characters only:
     * <table>
     * <tr><td>{@code '\t'}</td>            <td>{@code U+0009}</td>
     *     <td>{@code HORIZONTAL TABULATION}</td></tr>
     * <tr><td>{@code '\n'}</td>            <td>{@code U+000A}</td>
     *     <td>{@code NEW LINE}</td></tr>
     * <tr><td>{@code '\f'}</td>            <td>{@code U+000C}</td>
     *     <td>{@code FORM FEED}</td></tr>
     * <tr><td>{@code '\r'}</td>            <td>{@code U+000D}</td>
     *     <td>{@code CARRIAGE RETURN}</td></tr>
     * <tr><td>{@code '&nbsp;'}</td>  <td>{@code U+0020}</td>
     *     <td>{@code SPACE}</td></tr>
     * </table>
     *
     * @param      ch   the character to be tested.
     * @return     {@code true} if the character is ISO-LATIN-1 white
     *             space; {@code false} otherwise.
     * @see        Character#isSpaceChar(char)
     * @see        Character#isWhitespace(char)
     * @deprecated Replaced by isWhitespace(char).
     */
    @Deprecated
    public static boolean isSpace(char ch) {
        return (ch <= 0x0020) &&
            (((((1L << 0x0009) |
            (1L << 0x000A) |
            (1L << 0x000C) |
            (1L << 0x000D) |
            (1L << 0x0020)) >> ch) & 1L) != 0);
    }



    /**
     * Determines if the specified character is white space according to Java.
     * A character is a Java whitespace character if and only if it satisfies
     * one of the following criteria:
     * <ul>
     * <li> It is a Unicode space character ({@code SPACE_SEPARATOR},
     *      {@code LINE_SEPARATOR}, or {@code PARAGRAPH_SEPARATOR})
     *      but is not also a non-breaking space ({@code '\u005Cu00A0'},
     *      {@code '\u005Cu2007'}, {@code '\u005Cu202F'}).
     * <li> It is {@code '\u005Ct'}, U+0009 HORIZONTAL TABULATION.
     * <li> It is {@code '\u005Cn'}, U+000A LINE FEED.
     * <li> It is {@code '\u005Cu000B'}, U+000B VERTICAL TABULATION.
     * <li> It is {@code '\u005Cf'}, U+000C FORM FEED.
     * <li> It is {@code '\u005Cr'}, U+000D CARRIAGE RETURN.
     * <li> It is {@code '\u005Cu001C'}, U+001C FILE SEPARATOR.
     * <li> It is {@code '\u005Cu001D'}, U+001D GROUP SEPARATOR.
     * <li> It is {@code '\u005Cu001E'}, U+001E RECORD SEPARATOR.
     * <li> It is {@code '\u005Cu001F'}, U+001F UNIT SEPARATOR.
     * </ul>
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isWhitespace(int)} method.
     *
     * @param   ch the character to be tested.
     * @return  {@code true} if the character is a Java whitespace
     *          character; {@code false} otherwise.
     * @see     Character#isSpaceChar(char)
     * @since   1.1
     */
    public static boolean isWhitespace(char ch) {
        return isWhitespace((int)ch);
    }

    /**
     * Determines if the specified character (Unicode code point) is
     * white space according to Java.  A character is a Java
     * whitespace character if and only if it satisfies one of the
     * following criteria:
     * <ul>
     * <li> It is a Unicode space character ({@link #SPACE_SEPARATOR},
     *      {@link #LINE_SEPARATOR}, or {@link #PARAGRAPH_SEPARATOR})
     *      but is not also a non-breaking space ({@code '\u005Cu00A0'},
     *      {@code '\u005Cu2007'}, {@code '\u005Cu202F'}).
     * <li> It is {@code '\u005Ct'}, U+0009 HORIZONTAL TABULATION.
     * <li> It is {@code '\u005Cn'}, U+000A LINE FEED.
     * <li> It is {@code '\u005Cu000B'}, U+000B VERTICAL TABULATION.
     * <li> It is {@code '\u005Cf'}, U+000C FORM FEED.
     * <li> It is {@code '\u005Cr'}, U+000D CARRIAGE RETURN.
     * <li> It is {@code '\u005Cu001C'}, U+001C FILE SEPARATOR.
     * <li> It is {@code '\u005Cu001D'}, U+001D GROUP SEPARATOR.
     * <li> It is {@code '\u005Cu001E'}, U+001E RECORD SEPARATOR.
     * <li> It is {@code '\u005Cu001F'}, U+001F UNIT SEPARATOR.
     * </ul>
     * <p>
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is a Java whitespace
     *          character; {@code false} otherwise.
     * @see     Character#isSpaceChar(int)
     * @since   1.5
     */
    public static boolean isWhitespace(int codePoint) {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if the specified character is an ISO control
     * character.  A character is considered to be an ISO control
     * character if its code is in the range {@code '\u005Cu0000'}
     * through {@code '\u005Cu001F'} or in the range
     * {@code '\u005Cu007F'} through {@code '\u005Cu009F'}.
     *
     * <p><b>Note:</b> This method cannot handle <a
     * href="#supplementary"> supplementary characters</a>. To support
     * all Unicode characters, including supplementary characters, use
     * the {@link #isISOControl(int)} method.
     *
     * @param   ch      the character to be tested.
     * @return  {@code true} if the character is an ISO control character;
     *          {@code false} otherwise.
     *
     * @see     Character#isSpaceChar(char)
     * @see     Character#isWhitespace(char)
     * @since   1.1
     */
    public static boolean isISOControl(char ch) {
        return isISOControl((int)ch);
    }

    /**
     * Determines if the referenced character (Unicode code point) is an ISO control
     * character.  A character is considered to be an ISO control
     * character if its code is in the range {@code '\u005Cu0000'}
     * through {@code '\u005Cu001F'} or in the range
     * {@code '\u005Cu007F'} through {@code '\u005Cu009F'}.
     *
     * @param   codePoint the character (Unicode code point) to be tested.
     * @return  {@code true} if the character is an ISO control character;
     *          {@code false} otherwise.
     * @see     Character#isSpaceChar(int)
     * @see     Character#isWhitespace(int)
     * @since   1.5
     */
    public static boolean isISOControl(int codePoint) {
        // Optimized form of:
        //     (codePoint >= 0x00 && codePoint <= 0x1F) ||
        //     (codePoint >= 0x7F && codePoint <= 0x9F);
        return codePoint <= 0x9F &&
            (codePoint >= 0x7F || (codePoint >>> 5 == 0));
    }

    /**
     * Determines the character representation for a specific digit in
     * the specified radix. If the value of {@code radix} is not a
     * valid radix, or the value of {@code digit} is not a valid
     * digit in the specified radix, the null character
     * ({@code '\u005Cu0000'}) is returned.
     * <p>
     * The {@code radix} argument is valid if it is greater than or
     * equal to {@code MIN_RADIX} and less than or equal to
     * {@code MAX_RADIX}. The {@code digit} argument is valid if
     * {@code 0 <= digit < radix}.
     * <p>
     * If the digit is less than 10, then
     * {@code '0' + digit} is returned. Otherwise, the value
     * {@code 'a' + digit - 10} is returned.
     *
     * @param   digit   the number to convert to a character.
     * @param   radix   the radix.
     * @return  the {@code char} representation of the specified digit
     *          in the specified radix.
     * @see     Character#MIN_RADIX
     * @see     Character#MAX_RADIX
     * @see     Character#digit(char, int)
     */
    public static char forDigit(int digit, int radix) {
        if ((digit >= radix) || (digit < 0)) {
            return '\0';
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            return '\0';
        }
        if (digit < 10) {
            return (char)('0' + digit);
        }
        return (char)('a' - 10 + digit);
    }

    /**
     * Compares two {@code Character} objects numerically.
     *
     * @param   anotherCharacter   the {@code Character} to be compared.

     * @return  the value {@code 0} if the argument {@code Character}
     *          is equal to this {@code Character}; a value less than
     *          {@code 0} if this {@code Character} is numerically less
     *          than the {@code Character} argument; and a value greater than
     *          {@code 0} if this {@code Character} is numerically greater
     *          than the {@code Character} argument (unsigned comparison).
     *          Note that this is strictly a numerical comparison; it is not
     *          locale-dependent.
     * @since   1.2
     */
    public int compareTo(Character anotherCharacter) {
        return compare(this.value, anotherCharacter.value);
    }

    /**
     * Compares two {@code char} values numerically.
     * The value returned is identical to what would be returned by:
     * <pre>
     *    Character.valueOf(x).compareTo(Character.valueOf(y))
     * </pre>
     *
     * @param  x the first {@code char} to compare
     * @param  y the second {@code char} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x < y}; and
     *         a value greater than {@code 0} if {@code x > y}
     * @since 1.7
     */
    public static int compare(char x, char y) {
        return x - y;
    }


    /**
     * The number of bits used to represent a <tt>char</tt> value in unsigned
     * binary form, constant {@code 16}.
     *
     * @since 1.5
     */
    public static final int SIZE = 16;

    /**
     * Returns the value obtained by reversing the order of the bytes in the
     * specified <tt>char</tt> value.
     *
     * @return the value obtained by reversing (or, equivalently, swapping)
     *     the bytes in the specified <tt>char</tt> value.
     * @since 1.5
     */
    public static char reverseBytes(char ch) {
        return (char) (((ch & 0xFF00) >> 8) | (ch << 8));
    }

    static {
        // as last step of initialization, initialize valueOf method
        initValueOf();
    }
    @JavaScriptBody(args = {}, body = 
        "vm.java_lang_Character(false)." +
        "valueOf = function() { return this._value(); };"
    )
    private native static void initValueOf();
    
}
