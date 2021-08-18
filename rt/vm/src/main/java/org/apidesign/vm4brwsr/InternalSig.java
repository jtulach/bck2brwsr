/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

final class InternalSig {
    private final String jniName;
    private final String type;
    private final String[] parameters;

    private InternalSig(String jniName, String type, String[] parameters) {
        this.jniName = jniName;
        this.type = type;
        this.parameters = parameters;
    }

    static InternalSig find(MethodData m) {
        return find(m.getName(), m.getInternalSig());
    }

    static InternalSig find(String methodName, String internalSig) {
        StringBuilder name = new StringBuilder();
        if (methodName != null) {
            if ("<init>".equals(methodName)) {
                // NOI18N
                name.append("cons"); // NOI18N
            } else if ("<clinit>".equals(methodName)) {
                // NOI18N
                name.append("class"); // NOI18N
            } else {
                name.append(mangleMethodName(methodName));
            }
            name.append("__");
        }
        StringArray params = new StringArray();
        String[] retType = { null };
        countArgs(internalSig, retType, name, null, params);
        return new InternalSig(name.toString(), "" + retType[0], params.toArray());
    }

    static String findMethodName(MethodData m, StringBuilder cnt) {
        return findMethodName(m.getName(), m.getInternalSig(), cnt, null);
    }

    static String findMethodName(MethodData m, StringBuilder cnt, char[] retType) {
        return findMethodName(new String[] { null, m.getName(), m.getInternalSig() }, cnt, retType);
    }

    static String findMethodName(String[] mi, StringBuilder cnt, char[] retType) {
        return findMethodName(mi[1], mi[2], cnt, retType);
    }

    private static String findMethodName(String methodName, String internalSig, StringBuilder cnt, char[] retType) {
        InternalSig sig = find(methodName, internalSig);
        if (cnt != null) {
            for (int i = 0; i < sig.parameters.length; i++) {
                if ("J".equals(sig.parameters[i]) || "D".equals(sig.parameters[i])) {
                    cnt.append("1");
                } else {
                    cnt.append('0');
                }
            }
        }
        if (retType != null) {
            retType[0] = sig.type.charAt(0);
        }
        return sig.jniName;
    }

    static String mangleMethodName(String name) {
        return name == null ? null : mangle(name, 0, name.length(), false);
    }

    static String mangleJsCallbacks(String fqn, String method, String params, boolean isStatic) {
        if (params.startsWith("(")) {
            params = params.substring(1);
        }
        if (params.endsWith(")")) {
            params = params.substring(0, params.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        final String fqnu = fqn.replace('.', '_');
        final String rfqn = mangleClassName(fqnu);
        final String rm = mangleMethodName(method);
        final String srp;
        {
            StringBuilder pb = new StringBuilder();
            int len = params.length();
            int indx = 0;
            while (indx < len) {
                char ch = params.charAt(indx);
                if (ch == '[' || ch == 'L') {
                    int column = params.indexOf(';', indx) + 1;
                    if (column > indx) {
                        String real = params.substring(indx, column);
                        if ("Ljava/lang/String;".equals(real)) {
                            pb.append("Ljava/lang/String;");
                            indx = column;
                            continue;
                        }
                    }
                    pb.append("Ljava/lang/Object;");
                    indx = column;
                } else {
                    pb.append(ch);
                    indx++;
                }
            }
            srp = mangleSig(pb.toString());
        }
        final String rp = mangleSig(params);
        final String mrp = mangleMethodName(rp);
        sb.append(rfqn).append("$").append(rm).append('$').append(mrp).append("__Ljava_lang_Object_2");
        if (!isStatic) {
            sb.append('L').append(fqnu).append("_2");
        }
        sb.append(srp);
        return sb.toString();
    }

    static String mangleSig(String sig) {
        return mangle(sig, 0, sig.length(), false);
    }

    static String mangleSig(String txt, int first, int last) {
        StringBuilder sb = new StringBuilder((last - first) * 2);
        for (int i = first; i < last; i++) {
            final char ch = txt.charAt(i);
            switch (ch) {
                case '/':
                    sb.append('_');
                    break;
                case '_':
                    sb.append("_1");
                    break;
                case ';':
                    sb.append("_2");
                    break;
                case '[':
                    sb.append("_3");
                    break;
                default:
                    if (Character.isJavaIdentifierPart(ch)) {
                        sb.append(ch);
                    } else {
                        sb.append("_0");
                        String hex = Integer.toHexString(ch).toLowerCase();
                        for (int m = hex.length(); m < 4; m++) {
                            sb.append("0");
                        }
                        sb.append(hex);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    static String mangleClassName(String name) {
        return mangleSig(name);
    }

    static String mangle(String originalName, int from, int till, boolean replaceDot) {
        final int bufferSize = Math.max((till - from) * 2, 32);
        char[] buf = new char[bufferSize];
        int at = 0;
        for (int i = from; i < till; i++) {
            if (at > buf.length - 10) {
                buf = copyDouble(buf);
            }
            final char ch = originalName.charAt(i);
            switch (ch) {
                case '/':
                    buf[at++] = '_';
                    break;
                case '_':
                    buf[at++] = '_';
                    buf[at++] = '1';
                    break;
                case ';':
                    buf[at++] = '_';
                    buf[at++] = '2';
                    break;
                case '[':
                    buf[at++] = '_';
                    buf[at++] = '3';
                    break;
                case '.':
                    if (replaceDot) {
                        buf[at++] = '_';
                        break;
                    }
            // fallhrough
                default:
                    boolean valid = i == 0 ? Character.isJavaIdentifierStart(ch) : Character.isJavaIdentifierPart(ch);
                    if (valid) {
                        buf[at++] = ch;
                    } else {
                        buf[at++] = '_';
                        buf[at++] = '0';
                        String hex = Integer.toHexString(ch).toLowerCase();
                        for (int m = hex.length(); m < 4; m++) {
                            buf[at++] = '0';
                        }
                        for (int r = 0; r < hex.length(); r++) {
                            buf[at++] = hex.charAt(r);
                        }
                    }
                    break;
            }
        }
        return new String(buf, 0, at);
    }

    private static void countArgs(String descriptor, String[] returnType, StringBuilder sig, StringBuilder cnt, StringArray arr) {
        int i = 0;
        Boolean count = null;
        String array = null;
        int firstPos = sig.length();
        while (i < descriptor.length()) {
            char ch = descriptor.charAt(i++);
            switch (ch) {
                case '(':
                    count = true;
                    continue;
                case ')':
                    count = false;
                    continue;
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    if (count) {
                        if (array != null) {
                            sig.append("_3");
                            if (cnt != null) {
                                cnt.append("0");
                            }
                            if (arr != null) {
                                arr.add(array + ch);
                            }
                        } else {
                            if (cnt != null) {
                                if (ch == 'J' || ch == 'D') {
                                    cnt.append('1');
                                } else {
                                    cnt.append('0');
                                }
                            }
                            if (arr != null) {
                                arr.add("" + ch);
                            }
                        }
                        sig.append(ch);
                    } else {
                        sig.insert(firstPos, ch);
                        if (array != null) {
                            returnType[0] = array + ch;
                            sig.insert(firstPos, "_3");
                        } else {
                            returnType[0] = "" + ch;
                        }
                    }
                    array = null;
                    continue;
                case 'V':
                    assert !count;
                    returnType[0] = "V";
                    sig.insert(firstPos, 'V');
                    continue;
                case 'L':
                    int next = descriptor.indexOf(';', i);
                    String realSig = InternalSig.mangleSig(descriptor, i - 1, next + 1);
                    if (count) {
                        if (array != null) {
                            sig.append("_3");
                            if (arr != null) {
                                arr.add(array + realSig);
                            }
                        } else {
                            if (arr != null) {
                                arr.add(realSig);
                            }
                        }
                        sig.append(realSig);
                        if (cnt != null) {
                            cnt.append('0');
                        }
                    } else {
                        sig.insert(firstPos, realSig);
                        if (array != null) {
                            sig.insert(firstPos, "_3");
                            returnType[0] = array + realSig;
                        } else {
                            returnType[0] = realSig;
                        }
                    }
                    i = next + 1;
                    array = null;
                    continue;
                case '[':
                    if (array == null) {
                        array = "[";
                    } else {
                        array += "[";
                    }
                    continue;
                default:
                    throw new IllegalStateException("Invalid char: " + ch);
            }
        }
    }

    private static char[] copyDouble(char[] buf) {
        char[] copy = new char[buf.length * 2];
        for (int i = 0; i < buf.length; i++) {
            copy[i] = buf[i];
        }
        return copy;
    }

    int getParametersLength() {
        return parameters.length;
    }

    String getType() {
        return type;
    }

    String getJniName() {
        return jniName;
    }
}
