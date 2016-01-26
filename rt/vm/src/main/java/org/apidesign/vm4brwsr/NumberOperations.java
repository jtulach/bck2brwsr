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
    private static final int BIT64 = 8;

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
        used |= BIT64;
        return "__add64(@1,@2)";
    }

    public String sub64() {
        used |= BIT64;
        return "__sub64(@1,@2)";
    }

    public String mul64() {
        used |= BIT64;
        return "__mul64(@1,@2)";
    }

    public String div64() {
        used |= BIT64;
        return "__div64(@1,@2)";
    }

    public String mod64() {
        used |= BIT64;
        return "__mod64(@1,@2)";
    }

    public String and64() {
        used |= BIT64;
        return "__and64(@1,@2)";
    }

    public String or64() {
        used |= BIT64;
        return "__or64(@1,@2)";
    }

    public String xor64() {
        used |= BIT64;
        return "__xor64(@1,@2)";
    }

    public String neg64() {
        used |= BIT64;
        return "__neg64(@1)";
    }

    public String shl64() {
        used |= BIT64;
        return "__shl64(@1,@2)";
    }

    public String shr64() {
        used |= BIT64;
        return "__shr64(@1,@2)";
    }

    public String ushr64() {
        used |= BIT64;
        return "__ushr64(@1,@2)";
    }

    public String compare64() {
        used |= BIT64;
        return "__compare64(@1,@2)";
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
        if ((used & BIT64) != 0) {
            sb.append(
                "    var __add64 = function(x,y) { return x.add64(y); };\n" +
                "    var __sub64 = function(x,y) { return x.sub64(y); };\n" +
                "    var __mul64 = function(x,y) { return x.mul64(y); };\n" +
                "    var __div64 = function(x,y) { return x.div64(y); };\n" +
                "    var __mod64 = function(x,y) { return x.mod64(y); };\n" +
                "    var __and64 = function(x,y) { return x.and64(y); };\n" +
                "    var __or64 = function(x,y) { return x.or64(y); };\n" +
                "    var __xor64 = function(x,y) { return x.xor64(y); };\n" +
                "    var __neg64 = function(x) { return x.neg64(); };\n" +
                "    var __shl64 = function(x,y) { return x.shl64(y); };\n" +
                "    var __shr64 = function(x,y) { return x.shr64(y); };\n" +
                "    var __ushr64 = function(x,y) { return x.ushr64(y); };\n" +
                "    var __compare64 = function(x,y) { return y.compare64(x); };\n" +
                ""
            );
        }
        return sb.toString();
    }
}
