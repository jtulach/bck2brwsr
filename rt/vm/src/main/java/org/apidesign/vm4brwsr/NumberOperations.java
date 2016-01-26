/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.vm4brwsr;

final class NumberOperations {
    private static final int DIV32 = 1;
    private static final int MOD32 = 2;
    private static final int MUL32 = 4;

    private int used;

    public String mul32() {
        used |= MUL32;
        return "__mul32(@1,@2)";
    }
    public String div32() {
        used |= DIV32;
        return "__div32(@1,@2)";
    }

    public String mod32() {
        used |= MOD32;
        return "__mod32(@1,@2)";
    }

    public String add64() {
        return "(@1).add64(@2)";
    }

    public String sub64() {
        return "(@1).sub64(@2)";
    }

    public String mul64() {
        return "(@1).mul64(@2)";
    }

    public String div64() {
        return "(@1).div64(@2)";
    }

    public String mod64() {
        return "(@1).mod64(@2)";
    }

    public String and64() {
        return "(@1).and64(@2)";
    }

    public String or64() {
        return "(@1).or64(@2)";
    }

    public String xor64() {
        return "(@1).xor64(@2)";
    }

    public String neg64() {
        return "(@1).neg64()";
    }

    public String shl64() {
        return "(@1).shl64(@2)";
    }

    public String shr64() {
        return "(@1).shr64(@2)";
    }

    public String ushr64() {
        return "(@1).ushr64(@2)";
    }

    public String compare64() {
        return "(@2).compare64(@1)";
    }

    public String generate() {
        if (used == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if ((used & MUL32) != 0) {
            sb.append(
                "    __mul32 = function(x, y) {\n" +
                "        return (((x * (y >> 16)) << 16) + x * (y & 0xFFFF)) | 0;\n" +
                "    };\n" +
                ""
            );
        }
        if ((used & (MOD32 | DIV32)) != 0) {
            sb.append(
                "    function __handleDivByZero() {\n" +
                "        var exception = new vm.java_lang_ArithmeticException;\n" +
                "        vm.java_lang_ArithmeticException(false).constructor\n" +
                "          .cons__VLjava_lang_String_2.call(exception, \"/ by zero\");\n" +
                "\n" +
                "        throw exception;\n" +
                "    }\n" +
                ""
            );
        }
        if ((used & MOD32) != 0) {
            sb.append(
                "    function __mod32(x, y) {\n" +
                "        if (y === 0) __handleDivByZero();\n" +
                "        return (x % y) | 0;\n" +
                "    }\n" +
                ""
            );
        }
        if ((used & DIV32) != 0) {
            sb.append(
                "    function __div32(x, y) {\n" +
                "        if (y === 0) __handleDivByZero();\n" +
                "        return (x / y) | 0;\n" +
                "    }\n" +
                ""
            );
        }
        return sb.toString();
    }
}
