/*
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
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
package org.apidesign.vm4brwsr;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.core.JavaScriptPrototype;

/** This is a byte code parser heavily based on original code of JavaP utility.
 * As such I decided to keep the original Oracle's GPLv2 header.
 *
 * @author Jaroslav Tulach
 */
final class ByteCodeParser {
    private ByteCodeParser() {
    }

    /* Class File Constants */
    public static final int JAVA_MAGIC                   = 0xcafebabe;
    public static final int JAVA_VERSION                 = 45;
    public static final int JAVA_MINOR_VERSION           = 3;

    /* Constant table */
    public static final int CONSTANT_UTF8                = 1;
    public static final int CONSTANT_UNICODE             = 2;
    public static final int CONSTANT_INTEGER             = 3;
    public static final int CONSTANT_FLOAT               = 4;
    public static final int CONSTANT_LONG                = 5;
    public static final int CONSTANT_DOUBLE              = 6;
    public static final int CONSTANT_CLASS               = 7;
    public static final int CONSTANT_STRING              = 8;
    public static final int CONSTANT_FIELD               = 9;
    public static final int CONSTANT_METHOD              = 10;
    public static final int CONSTANT_INTERFACEMETHOD     = 11;
    public static final int CONSTANT_NAMEANDTYPE         = 12;
    public static final int CONSTANT_METHODHANDLE     = 15;
    public static final int CONSTANT_METHODTYPE     = 16;
    public static final int CONSTANT_INVOKEDYNAMIC     = 18;

    /* Access Flags */
    public static final int ACC_PUBLIC                   = 0x00000001;
    public static final int ACC_PRIVATE                  = 0x00000002;
    public static final int ACC_PROTECTED                = 0x00000004;
    public static final int ACC_STATIC                   = 0x00000008;
    public static final int ACC_FINAL                    = 0x00000010;
    public static final int ACC_SYNCHRONIZED             = 0x00000020;
    public static final int ACC_SUPER                        = 0x00000020;
    public static final int ACC_VOLATILE                 = 0x00000040;
    public static final int ACC_TRANSIENT                = 0x00000080;
    public static final int ACC_NATIVE                   = 0x00000100;
    public static final int ACC_INTERFACE                = 0x00000200;
    public static final int ACC_ABSTRACT                 = 0x00000400;
    public static final int ACC_STRICT                   = 0x00000800;
    public static final int ACC_EXPLICIT                 = 0x00001000;
    public static final int ACC_SYNTHETIC                = 0x00010000; // actually, this is an attribute
    private static final int ACC_ANNOTATION              = 0x00020000;

    /* Type codes for StackMap attribute */
    public static final int ITEM_Bogus      =0; // an unknown or uninitialized value
    public static final int ITEM_Integer    =1; // a 32-bit integer
    public static final int ITEM_Float      =2; // not used
    public static final int ITEM_Double     =3; // not used
    public static final int ITEM_Long       =4; // a 64-bit integer
    public static final int ITEM_Null       =5; // the type of null
    public static final int ITEM_InitObject =6; // "this" in constructor
    public static final int ITEM_Object     =7; // followed by 2-byte index of class name
    public static final int ITEM_NewObject  =8; // followed by 2-byte ref to "new"

    /* Constants used in StackMapTable attribute */
    public static final int SAME_FRAME_BOUND                  = 64;
    public static final int SAME_LOCALS_1_STACK_ITEM_BOUND    = 128;
    public static final int SAME_LOCALS_1_STACK_ITEM_EXTENDED = 247;
    public static final int SAME_FRAME_EXTENDED               = 251;
    public static final int FULL_FRAME                        = 255;

    /* Opcodes */
    public static final int opc_dead                     = -2;
    public static final int opc_label                    = -1;
    public static final int opc_nop                      = 0;
    public static final int opc_aconst_null              = 1;
    public static final int opc_iconst_m1                = 2;
    public static final int opc_iconst_0                 = 3;
    public static final int opc_iconst_1                 = 4;
    public static final int opc_iconst_2                 = 5;
    public static final int opc_iconst_3                 = 6;
    public static final int opc_iconst_4                 = 7;
    public static final int opc_iconst_5                 = 8;
    public static final int opc_lconst_0                 = 9;
    public static final int opc_lconst_1                 = 10;
    public static final int opc_fconst_0                 = 11;
    public static final int opc_fconst_1                 = 12;
    public static final int opc_fconst_2                 = 13;
    public static final int opc_dconst_0                 = 14;
    public static final int opc_dconst_1                 = 15;
    public static final int opc_bipush                   = 16;
    public static final int opc_sipush                   = 17;
    public static final int opc_ldc                      = 18;
    public static final int opc_ldc_w                    = 19;
    public static final int opc_ldc2_w                   = 20;
    public static final int opc_iload                    = 21;
    public static final int opc_lload                    = 22;
    public static final int opc_fload                    = 23;
    public static final int opc_dload                    = 24;
    public static final int opc_aload                    = 25;
    public static final int opc_iload_0                  = 26;
    public static final int opc_iload_1                  = 27;
    public static final int opc_iload_2                  = 28;
    public static final int opc_iload_3                  = 29;
    public static final int opc_lload_0                  = 30;
    public static final int opc_lload_1                  = 31;
    public static final int opc_lload_2                  = 32;
    public static final int opc_lload_3                  = 33;
    public static final int opc_fload_0                  = 34;
    public static final int opc_fload_1                  = 35;
    public static final int opc_fload_2                  = 36;
    public static final int opc_fload_3                  = 37;
    public static final int opc_dload_0                  = 38;
    public static final int opc_dload_1                  = 39;
    public static final int opc_dload_2                  = 40;
    public static final int opc_dload_3                  = 41;
    public static final int opc_aload_0                  = 42;
    public static final int opc_aload_1                  = 43;
    public static final int opc_aload_2                  = 44;
    public static final int opc_aload_3                  = 45;
    public static final int opc_iaload                   = 46;
    public static final int opc_laload                   = 47;
    public static final int opc_faload                   = 48;
    public static final int opc_daload                   = 49;
    public static final int opc_aaload                   = 50;
    public static final int opc_baload                   = 51;
    public static final int opc_caload                   = 52;
    public static final int opc_saload                   = 53;
    public static final int opc_istore                   = 54;
    public static final int opc_lstore                   = 55;
    public static final int opc_fstore                   = 56;
    public static final int opc_dstore                   = 57;
    public static final int opc_astore                   = 58;
    public static final int opc_istore_0                 = 59;
    public static final int opc_istore_1                 = 60;
    public static final int opc_istore_2                 = 61;
    public static final int opc_istore_3                 = 62;
    public static final int opc_lstore_0                 = 63;
    public static final int opc_lstore_1                 = 64;
    public static final int opc_lstore_2                 = 65;
    public static final int opc_lstore_3                 = 66;
    public static final int opc_fstore_0                 = 67;
    public static final int opc_fstore_1                 = 68;
    public static final int opc_fstore_2                 = 69;
    public static final int opc_fstore_3                 = 70;
    public static final int opc_dstore_0                 = 71;
    public static final int opc_dstore_1                 = 72;
    public static final int opc_dstore_2                 = 73;
    public static final int opc_dstore_3                 = 74;
    public static final int opc_astore_0                 = 75;
    public static final int opc_astore_1                 = 76;
    public static final int opc_astore_2                 = 77;
    public static final int opc_astore_3                 = 78;
    public static final int opc_iastore                  = 79;
    public static final int opc_lastore                  = 80;
    public static final int opc_fastore                  = 81;
    public static final int opc_dastore                  = 82;
    public static final int opc_aastore                  = 83;
    public static final int opc_bastore                  = 84;
    public static final int opc_castore                  = 85;
    public static final int opc_sastore                  = 86;
    public static final int opc_pop                      = 87;
    public static final int opc_pop2                     = 88;
    public static final int opc_dup                      = 89;
    public static final int opc_dup_x1                   = 90;
    public static final int opc_dup_x2                   = 91;
    public static final int opc_dup2                     = 92;
    public static final int opc_dup2_x1                  = 93;
    public static final int opc_dup2_x2                  = 94;
    public static final int opc_swap                     = 95;
    public static final int opc_iadd                     = 96;
    public static final int opc_ladd                     = 97;
    public static final int opc_fadd                     = 98;
    public static final int opc_dadd                     = 99;
    public static final int opc_isub                     = 100;
    public static final int opc_lsub                     = 101;
    public static final int opc_fsub                     = 102;
    public static final int opc_dsub                     = 103;
    public static final int opc_imul                     = 104;
    public static final int opc_lmul                     = 105;
    public static final int opc_fmul                     = 106;
    public static final int opc_dmul                     = 107;
    public static final int opc_idiv                     = 108;
    public static final int opc_ldiv                     = 109;
    public static final int opc_fdiv                     = 110;
    public static final int opc_ddiv                     = 111;
    public static final int opc_irem                     = 112;
    public static final int opc_lrem                     = 113;
    public static final int opc_frem                     = 114;
    public static final int opc_drem                     = 115;
    public static final int opc_ineg                     = 116;
    public static final int opc_lneg                     = 117;
    public static final int opc_fneg                     = 118;
    public static final int opc_dneg                     = 119;
    public static final int opc_ishl                     = 120;
    public static final int opc_lshl                     = 121;
    public static final int opc_ishr                     = 122;
    public static final int opc_lshr                     = 123;
    public static final int opc_iushr                    = 124;
    public static final int opc_lushr                    = 125;
    public static final int opc_iand                     = 126;
    public static final int opc_land                     = 127;
    public static final int opc_ior                      = 128;
    public static final int opc_lor                      = 129;
    public static final int opc_ixor                     = 130;
    public static final int opc_lxor                     = 131;
    public static final int opc_iinc                     = 132;
    public static final int opc_i2l                      = 133;
    public static final int opc_i2f                      = 134;
    public static final int opc_i2d                      = 135;
    public static final int opc_l2i                      = 136;
    public static final int opc_l2f                      = 137;
    public static final int opc_l2d                      = 138;
    public static final int opc_f2i                      = 139;
    public static final int opc_f2l                      = 140;
    public static final int opc_f2d                      = 141;
    public static final int opc_d2i                      = 142;
    public static final int opc_d2l                      = 143;
    public static final int opc_d2f                      = 144;
    public static final int opc_i2b                      = 145;
    public static final int opc_int2byte                 = 145;
    public static final int opc_i2c                      = 146;
    public static final int opc_int2char                 = 146;
    public static final int opc_i2s                      = 147;
    public static final int opc_int2short                = 147;
    public static final int opc_lcmp                     = 148;
    public static final int opc_fcmpl                    = 149;
    public static final int opc_fcmpg                    = 150;
    public static final int opc_dcmpl                    = 151;
    public static final int opc_dcmpg                    = 152;
    public static final int opc_ifeq                     = 153;
    public static final int opc_ifne                     = 154;
    public static final int opc_iflt                     = 155;
    public static final int opc_ifge                     = 156;
    public static final int opc_ifgt                     = 157;
    public static final int opc_ifle                     = 158;
    public static final int opc_if_icmpeq                = 159;
    public static final int opc_if_icmpne                = 160;
    public static final int opc_if_icmplt                = 161;
    public static final int opc_if_icmpge                = 162;
    public static final int opc_if_icmpgt                = 163;
    public static final int opc_if_icmple                = 164;
    public static final int opc_if_acmpeq                = 165;
    public static final int opc_if_acmpne                = 166;
    public static final int opc_goto                     = 167;
    public static final int opc_jsr                      = 168;
    public static final int opc_ret                      = 169;
    public static final int opc_tableswitch              = 170;
    public static final int opc_lookupswitch             = 171;
    public static final int opc_ireturn                  = 172;
    public static final int opc_lreturn                  = 173;
    public static final int opc_freturn                  = 174;
    public static final int opc_dreturn                  = 175;
    public static final int opc_areturn                  = 176;
    public static final int opc_return                   = 177;
    public static final int opc_getstatic                = 178;
    public static final int opc_putstatic                = 179;
    public static final int opc_getfield                 = 180;
    public static final int opc_putfield                 = 181;
    public static final int opc_invokevirtual            = 182;
    public static final int opc_invokenonvirtual         = 183;
    public static final int opc_invokespecial            = 183;
    public static final int opc_invokestatic             = 184;
    public static final int opc_invokeinterface          = 185;
    public static final int opc_invokedynamic            = 186;
    public static final int opc_new                      = 187;
    public static final int opc_newarray                 = 188;
    public static final int opc_anewarray                = 189;
    public static final int opc_arraylength              = 190;
    public static final int opc_athrow                   = 191;
    public static final int opc_checkcast                = 192;
    public static final int opc_instanceof               = 193;
    public static final int opc_monitorenter             = 194;
    public static final int opc_monitorexit              = 195;
    public static final int opc_wide                     = 196;
    public static final int opc_multianewarray           = 197;
    public static final int opc_ifnull                   = 198;
    public static final int opc_ifnonnull                = 199;
    public static final int opc_goto_w                   = 200;
    public static final int opc_jsr_w                    = 201;
        /* Pseudo-instructions */
    public static final int opc_bytecode                 = 203;
    public static final int opc_try                      = 204;
    public static final int opc_endtry                   = 205;
    public static final int opc_catch                    = 206;
    public static final int opc_var                      = 207;
    public static final int opc_endvar                   = 208;
    public static final int opc_localsmap                = 209;
    public static final int opc_stackmap                 = 210;
        /* PicoJava prefixes */
    public static final int opc_nonpriv                  = 254;
    public static final int opc_priv                     = 255;

        /* Wide instructions *
    public static final int opc_iload_w         = (opc_wide<<8)|opc_iload;
    public static final int opc_lload_w         = (opc_wide<<8)|opc_lload;
    public static final int opc_fload_w         = (opc_wide<<8)|opc_fload;
    public static final int opc_dload_w         = (opc_wide<<8)|opc_dload;
    public static final int opc_aload_w         = (opc_wide<<8)|opc_aload;
    public static final int opc_istore_w        = (opc_wide<<8)|opc_istore;
    public static final int opc_lstore_w        = (opc_wide<<8)|opc_lstore;
    public static final int opc_fstore_w        = (opc_wide<<8)|opc_fstore;
    public static final int opc_dstore_w        = (opc_wide<<8)|opc_dstore;
    public static final int opc_astore_w        = (opc_wide<<8)|opc_astore;
    public static final int opc_ret_w           = (opc_wide<<8)|opc_ret;
    public static final int opc_iinc_w          = (opc_wide<<8)|opc_iinc;
*/
    static class AnnotationParser {

        private final boolean textual;
        private final boolean iterateArray;

        protected AnnotationParser(boolean textual, boolean iterateArray) {
            this.textual = textual;
            this.iterateArray = iterateArray;
        }

        protected void visitAnnotationStart(String type, boolean top) throws IOException {
        }

        protected void visitAnnotationEnd(String type, boolean top) throws IOException {
        }

        protected void visitValueStart(String attrName, char type) throws IOException {
        }

        protected void visitValueEnd(String attrName, char type) throws IOException {
        }

        protected void visitAttr(
            String annoType, String attr, String attrType, String value
        ) throws IOException {
        }

        protected void visitEnumAttr(
            String annoType, String attr, String attrType, String value
        ) throws IOException {
            visitAttr(annoType, attr, attrType, value);
        }

        protected void visitClassAttr(
            String annoType, String attr, String className
        ) throws IOException {
            visitAttr(annoType, attr, className, className);
        }

        /**
         * Initialize the parsing with constant pool from
         * <code>cd</code>.
         *
         * @param attr the attribute defining annotations
         * @param cd constant pool
         * @throws IOException in case I/O fails
         */
        public final void parse(byte[] attr, ClassData cd) throws IOException {
            ByteArrayInputStream is = new ByteArrayInputStream(attr);
            DataInputStream dis = new DataInputStream(is);
            try {
                read(dis, cd);
            } finally {
                is.close();
            }
        }

        private void read(DataInputStream dis, ClassData cd) throws IOException {
            int cnt = dis.readUnsignedShort();
            for (int i = 0; i < cnt; i++) {
                readAnno(dis, cd, true);
            }
        }

        private void readAnno(DataInputStream dis, ClassData cd, boolean top) throws IOException {
            int type = dis.readUnsignedShort();
            String typeName = cd.StringValue(type);
            visitAnnotationStart(typeName, top);
            int cnt = dis.readUnsignedShort();
            for (int i = 0; i < cnt; i++) {
                String attrName = cd.StringValue(dis.readUnsignedShort());
                readValue(dis, cd, typeName, attrName);
            }
            visitAnnotationEnd(typeName, top);
            if (cnt == 0) {
                visitAttr(typeName, null, null, null);
            }
        }

        public void parseDefault(byte[] defaultAttribute, ClassData cd) throws IOException {
            ByteArrayInputStream is = new ByteArrayInputStream(defaultAttribute);
            DataInputStream dis = new DataInputStream(is);
            try {
                readValue(dis, cd, null, null);
            } finally {
                is.close();
            }
        }

        private void readValue(
            DataInputStream dis, ClassData cd, String typeName, String attrName) throws IOException {
            char type = (char) dis.readByte();
            visitValueStart(attrName, type);
            if (type == '@') {
                readAnno(dis, cd, false);
            } else if ("CFJZsSIDB".indexOf(type) >= 0) { // NOI18N
                int primitive = dis.readUnsignedShort();
                String val = cd.stringValue(primitive, textual);
                String attrType;
                if (type == 's') {
                    attrType = "Ljava_lang_String_2";
                    if (textual) {
                        val = '"' + val + '"';
                    }
                } else {
                    attrType = "" + type;
                }
                visitAttr(typeName, attrName, attrType, val);
            } else if (type == 'c') {
                int cls = dis.readUnsignedShort();
                String attrType = cd.stringValue(cls, textual);
                visitClassAttr(typeName, attrName, attrType);
            } else if (type == '[') {
                int cnt = dis.readUnsignedShort();
                for (int i = 0; i < cnt; i++) {
                    readValue(dis, cd, typeName, iterateArray ? attrName : null);
                }
            } else if (type == 'e') {
                int enumT = dis.readUnsignedShort();
                String attrType = cd.stringValue(enumT, textual);
                int enumN = dis.readUnsignedShort();
                String val = cd.stringValue(enumN, textual);
                visitEnumAttr(typeName, attrName, attrType, val);
            } else {
                throw new IOException("Unknown type " + type);
            }
            visitValueEnd(attrName, type);
        }
    }
    
    /**
     * Reads and stores attribute information.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    private static class AttrData {

        ClassData cls;
        int name_cpx;
        int datalen;
        byte data[];

        public AttrData(ClassData cls) {
            this.cls = cls;
        }

        /**
         * Reads unknown attribute.
         */
        public void read(int name_cpx, DataInputStream in) throws IOException {
            this.name_cpx = name_cpx;
            datalen = in.readInt();
            data = new byte[datalen];
            in.readFully(data);
        }

        /**
         * Reads just the name of known attribute.
         */
        public void read(int name_cpx) {
            this.name_cpx = name_cpx;
        }

        /**
         * Returns attribute name.
         */
        public String getAttrName() {
            return cls.getString(name_cpx);
        }

        /**
         * Returns attribute data.
         */
        public byte[] getData() {
            return data;
        }
    }

    /**
     * Stores constant pool entry information with one field.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static class CPX {

        final int cpx;

        CPX(int cpx) {
            this.cpx = cpx;
        }
    }

    /**
     * Stores constant pool entry information with two fields.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static class CPX2 {

        final int cpx1, cpx2;

        CPX2(int cpx1, int cpx2) {
            this.cpx1 = cpx1;
            this.cpx2 = cpx2;
        }
    }

    /**
     * Central data repository of the Java Disassembler. Stores all the
     * information in java class file.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static final class ClassData {

        private int magic;
        private int minor_version;
        private int major_version;
        private int cpool_count;
        private Object cpool[];
        private int access;
        private int this_class = 0;
        private int super_class;
        private int interfaces_count;
        private int[] interfaces = new int[0];
        private FieldData[] fields;
        private MethodData[] methods;
        private InnerClassData[] innerClasses;
        private BootMethodData[] bootMethods;
        private int attributes_count;
        private AttrData[] attrs;
        private int source_cpx = 0;
        private byte tags[];
        private Hashtable indexHashAscii = new Hashtable();
        private String pkgPrefix = "";
        private int pkgPrefixLen = 0;
        private boolean hasEnclosingMethod;

        /**
         * Read classfile to disassemble.
         */
        public ClassData(InputStream infile) throws IOException {
            this.read(new DataInputStream(infile));
        }

        /**
         * Reads and stores class file information.
         */
        public void read(DataInputStream in) throws IOException {
            // Read the header
            magic = in.readInt();
            if (magic != JAVA_MAGIC) {
                throw new ClassFormatError("wrong magic: "
                    + toHex(magic) + ", expected "
                    + toHex(JAVA_MAGIC));
            }
            minor_version = in.readShort();
            major_version = in.readShort();
            if (major_version != JAVA_VERSION) {
            }

            // Read the constant pool
            readCP(in);
            access = in.readUnsignedShort();
            this_class = in.readUnsignedShort();
            super_class = in.readUnsignedShort();

            //Read interfaces.
            interfaces_count = in.readUnsignedShort();
            if (interfaces_count > 0) {
                interfaces = new int[interfaces_count];
            }
            for (int i = 0; i < interfaces_count; i++) {
                interfaces[i] = in.readShort();
            }

            // Read the fields
            readFields(in);

            // Read the methods
            readMethods(in);

            // Read the attributes
            attributes_count = in.readUnsignedShort();
            attrs = new AttrData[attributes_count];
            for (int k = 0; k < attributes_count; k++) {
                int name_cpx = in.readUnsignedShort();
                if (getTag(name_cpx) == CONSTANT_UTF8) {
                    final String attrName = getString(name_cpx);
                    if (attrName.equals("SourceFile")) {
                        if (in.readInt() != 2) {
                            throw new ClassFormatError("invalid attr length");
                        }
                        source_cpx = in.readUnsignedShort();
                        AttrData attr = new AttrData(this);
                        attr.read(name_cpx);
                        attrs[k] = attr;

                    } else if (attrName.equals("InnerClasses")) {
                        int length = in.readInt();
                        int num = in.readUnsignedShort();
                        if (2 + num * 8 != length) {
                            throw new ClassFormatError("invalid attr length");
                        }
                        innerClasses = new InnerClassData[num];
                        for (int j = 0; j < num; j++) {
                            InnerClassData innerClass = new InnerClassData(this);
                            innerClass.read(in);
                            innerClasses[j] = innerClass;
                        }
                        AttrData attr = new AttrData(this);
                        attr.read(name_cpx);
                        attrs[k] = attr;
                    } else if (attrName.equals("BootstrapMethods")) {
                        AttrData attr = new AttrData(this);
                        bootMethods = readBootstrapMethods(in);
                        attr.read(name_cpx);
                        attrs[k] = attr;
                    } else {
                        if (attrName.equals("EnclosingMethod")) {
                            hasEnclosingMethod = true;
                        }
                        AttrData attr = new AttrData(this);
                        attr.read(name_cpx, in);
                        attrs[k] = attr;
                    }
                }
            }
            in.close();
        } // end ClassData.read()

        BootMethodData[] readBootstrapMethods(DataInputStream in) throws IOException {
            int attr_len = in.readInt();  //attr_lengt
            int number = in.readShort();
            BootMethodData[] arr = new BootMethodData[number];
            for (int i = 0; i < number; i++) {
                int ref = in.readShort();
                int len = in.readShort();
                int[] args = new int[len];
                for (int j = 0; j < len; j++) {
                    args[j] = in.readShort();
                }
                arr[i] = new BootMethodData(this, ref, args);
            }
            return arr;
        }
        
        /**
         * Reads and stores constant pool info.
         */
        void readCP(DataInputStream in) throws IOException {
            cpool_count = in.readUnsignedShort();
            tags = new byte[cpool_count];
            cpool = new Object[cpool_count];
            for (int i = 1; i < cpool_count; i++) {
                byte tag = in.readByte();

                switch (tags[i] = tag) {
                    case CONSTANT_UTF8:
                        String str = in.readUTF();
                        indexHashAscii.put(cpool[i] = str, i);
                        break;
                    case CONSTANT_INTEGER:
                        cpool[i] = in.readInt();
                        break;
                    case CONSTANT_FLOAT:
                        cpool[i] = in.readFloat();
                        break;
                    case CONSTANT_LONG:
                        cpool[i++] = in.readLong();
                        break;
                    case CONSTANT_DOUBLE:
                        cpool[i++] = in.readDouble();
                        break;
                    case CONSTANT_CLASS:
                    case CONSTANT_STRING:
                        cpool[i] = new CPX(in.readUnsignedShort());
                        break;

                    case CONSTANT_FIELD:
                    case CONSTANT_METHOD:
                    case CONSTANT_INTERFACEMETHOD:
                    case CONSTANT_NAMEANDTYPE:
                        cpool[i] = new CPX2(in.readUnsignedShort(), in.readUnsignedShort());
                        break;
                    case CONSTANT_METHODHANDLE:
                        cpool[i] = new CPX2(in.readByte(), in.readUnsignedShort());
                        break;
                    case CONSTANT_METHODTYPE:
                        cpool[i] = new CPX(in.readUnsignedShort());
                        break;
                    case CONSTANT_INVOKEDYNAMIC:
                        cpool[i] = new CPX2(in.readUnsignedShort(), in.readUnsignedShort());
                        break;
                    case 0:
                    default:
                        throw new IOException("invalid constant type: " + (int) tags[i]);
                }
            }
        }

        /**
         * Reads and strores field info.
         */
        protected void readFields(DataInputStream in) throws IOException {
            int fields_count = in.readUnsignedShort();
            fields = new FieldData[fields_count];
            for (int k = 0; k < fields_count; k++) {
                FieldData field = new FieldData(this);
                field.read(in);
                fields[k] = field;
            }
        }

        /**
         * Reads and strores Method info.
         */
        protected void readMethods(DataInputStream in) throws IOException {
            int methods_count = in.readUnsignedShort();
            methods = new MethodData[methods_count];
            for (int k = 0; k < methods_count; k++) {
                MethodData method = new MethodData(this);
                method.read(in);
                methods[k] = method;
            }
        }

        /**
         * get a string
         */
        public String getString(int n) {
            if (n == 0) {
                return null;
            } else {
                return (String) cpool[n];
            }
        }

        /**
         * get the type of constant given an index
         */
        public byte getTag(int n) {
            try {
                return tags[n];
            } catch (ArrayIndexOutOfBoundsException e) {
                return (byte) 100;
            }
        }
        static final String hexString = "0123456789ABCDEF";
        public static char hexTable[] = hexString.toCharArray();

        static String toHex(long val, int width) {
            StringBuffer s = new StringBuffer();
            for (int i = width - 1; i >= 0; i--) {
                s.append(hexTable[((int) (val >> (4 * i))) & 0xF]);
            }
            return "0x" + s.toString();
        }

        static String toHex(long val) {
            int width;
            for (width = 16; width > 0; width--) {
                if ((val >> (width - 1) * 4) != 0) {
                    break;
                }
            }
            return toHex(val, width);
        }

        static String toHex(int val) {
            int width;
            for (width = 8; width > 0; width--) {
                if ((val >> (width - 1) * 4) != 0) {
                    break;
                }
            }
            return toHex(val, width);
        }

        /**
         * Returns the name of this class.
         */
        public String getClassName() {
            String res = null;
            if (this_class == 0) {
                return res;
            }
            int tcpx;
            try {
                if (tags[this_class] != CONSTANT_CLASS) {
                    return res; //"<CP["+cpx+"] is not a Class> ";
                }
                tcpx = ((CPX) cpool[this_class]).cpx;
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "#"+cpx+"// invalid constant pool index";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }

            try {
                return (String) (cpool[tcpx]);
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "class #"+scpx+"// invalid constant pool index";
            } catch (ClassCastException e) {
                return res; // "class #"+scpx+"// invalid constant pool reference";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }

        }

        /**
         * Returns the name of class at perticular index.
         */
        public String getClassName(int cpx) {
            String res = "#" + cpx;
            if (cpx == 0) {
                return res;
            }
            int scpx;
            try {
                if (tags[cpx] != CONSTANT_CLASS) {
                    return res; //"<CP["+cpx+"] is not a Class> ";
                }
                scpx = ((CPX) cpool[cpx]).cpx;
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "#"+cpx+"// invalid constant pool index";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }
            res = "#" + scpx;
            try {
                return (String) (cpool[scpx]);
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "class #"+scpx+"// invalid constant pool index";
            } catch (ClassCastException e) {
                return res; // "class #"+scpx+"// invalid constant pool reference";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }
        }

        public int getAccessFlags() {
            return access;
        }

        public boolean hasEnclosingMethod() {
            return hasEnclosingMethod;
        }

        /**
         * Returns true if it is a class
         */
        public boolean isClass() {
            if ((access & ACC_INTERFACE) == 0) {
                return true;
            }
            return false;
        }

        /**
         * Returns true if it is a interface.
         */
        public boolean isInterface() {
            if ((access & ACC_INTERFACE) != 0) {
                return true;
            }
            return false;
        }

        public boolean isAnnotation() {
            return (access & ACC_ANNOTATION) != 0;
        }

        /**
         * Returns true if this member is public, false otherwise.
         */
        public boolean isPublic() {
            return (access & ACC_PUBLIC) != 0;
        }

        /**
         * Returns the access of this class or interface.
         */
        public String[] getAccess() {
            Vector v = new Vector();
            if ((access & ACC_PUBLIC) != 0) {
                v.addElement("public");
            }
            if ((access & ACC_FINAL) != 0) {
                v.addElement("final");
            }
            if ((access & ACC_ABSTRACT) != 0) {
                v.addElement("abstract");
            }
            String[] accflags = new String[v.size()];
            v.copyInto(accflags);
            return accflags;
        }

        /**
         * Returns list of innerclasses.
         */
        public InnerClassData[] getInnerClasses() {
            return innerClasses;
        }

        /**
         * Returns list of attributes.
         */
        final AttrData[] getAttributes() {
            return attrs;
        }

        public byte[] findAnnotationData(boolean classRetention) {
            String n = classRetention
                ? "RuntimeInvisibleAnnotations" : // NOI18N
                "RuntimeVisibleAnnotations"; // NOI18N
            return findAttr(n, attrs);
        }

        /**
         * Returns true if superbit is set.
         */
        public boolean isSuperSet() {
            if ((access & ACC_SUPER) != 0) {
                return true;
            }
            return false;
        }

        /**
         * Returns super class name.
         */
        public String getSuperClassName() {
            String res = null;
            if (super_class == 0) {
                return res;
            }
            int scpx;
            try {
                if (tags[super_class] != CONSTANT_CLASS) {
                    return res; //"<CP["+cpx+"] is not a Class> ";
                }
                scpx = ((CPX) cpool[super_class]).cpx;
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "#"+cpx+"// invalid constant pool index";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }

            try {
                return (String) (cpool[scpx]);
            } catch (ArrayIndexOutOfBoundsException e) {
                return res; // "class #"+scpx+"// invalid constant pool index";
            } catch (ClassCastException e) {
                return res; // "class #"+scpx+"// invalid constant pool reference";
            } catch (Throwable e) {
                return res; // "#"+cpx+"// ERROR IN DISASSEMBLER";
            }
        }

        /**
         * Returns list of super interfaces.
         */
        public String[] getSuperInterfaces() {
            String interfacenames[] = new String[interfaces.length];
            int interfacecpx = -1;
            for (int i = 0; i < interfaces.length; i++) {
                interfacecpx = ((CPX) cpool[interfaces[i]]).cpx;
                interfacenames[i] = (String) (cpool[interfacecpx]);
            }
            return interfacenames;
        }

        /**
         * Returns string at prticular constant pool index.
         */
        public String getStringValue(int cpoolx) {
            try {
                return ((String) cpool[cpoolx]);
            } catch (ArrayIndexOutOfBoundsException e) {
                return "//invalid constant pool index:" + cpoolx;
            } catch (ClassCastException e) {
                return "//invalid constant pool ref:" + cpoolx;
            }
        }

        /**
         * Returns list of field info.
         */
        public FieldData[] getFields() {
            return fields;
        }

        /**
         * Returns list of method info.
         */
        public MethodData[] getMethods() {
            return methods;
        }

        /**
         * Returns constant pool entry at that index.
         */
        public CPX2 getCpoolEntry(int cpx) {
            return ((CPX2) (cpool[cpx]));
        }

        public Object getCpoolEntryobj(int cpx) {
            return (cpool[cpx]);
        }

        /**
         * Returns index of this class.
         */
        public int getthis_cpx() {
            return this_class;
        }

        /**
         * Returns string at that index.
         */
        public String StringValue(int cpx) {
            return stringValue(cpx, false);
        }

        public String stringValue(int cpx, boolean textual) {
            return stringValue(cpx, textual, null);
        }

        public String stringValue(int cpx, String[] classRefs) {
            return stringValue(cpx, true, classRefs);
        }

        private String stringValue(int cpx, boolean textual, String[] refs) {
            if (cpx == 0) {
                return "#0";
            }
            int tag;
            Object x;
            String suffix = "";
            try {
                tag = tags[cpx];
                x = cpool[cpx];
            } catch (IndexOutOfBoundsException e) {
                return "<Incorrect CP index:" + cpx + ">";
            }

            if (x == null) {
                return "<NULL>";
            }
            switch (tag) {
                case CONSTANT_UTF8: {
                    if (!textual) {
                        return (String) x;
                    }
                    StringBuilder sb = new StringBuilder();
                    String s = (String) x;
                    for (int k = 0; k < s.length(); k++) {
                        char c = s.charAt(k);
                        switch (c) {
                            case '\\':
                                sb.append('\\').append('\\');
                                break;
                            case '\t':
                                sb.append('\\').append('t');
                                break;
                            case '\n':
                                sb.append('\\').append('n');
                                break;
                            case '\r':
                                sb.append('\\').append('r');
                                break;
                            case '\"':
                                sb.append('\\').append('\"');
                                break;
                            case '\u2028':
                                sb.append("\\u2028");
                                break;
                            case '\u2029':
                                sb.append("\\u2029");
                                break;
                            default:
                                sb.append(c);
                        }
                    }
                    return sb.toString();
                }
                case CONSTANT_DOUBLE: {
                    Double d = (Double) x;
                    String sd = d.toString();
                    if (textual) {
                        return sd;
                    }
                    return sd + "d";
                }
                case CONSTANT_FLOAT: {
                    Float f = (Float) x;
                    String sf = (f).toString();
                    if (textual) {
                        return sf;
                    }
                    return sf + "f";
                }
                case CONSTANT_LONG: {
                    Long ln = (Long) x;
                    if (textual) {
                        return ln.toString();
                    }
                    return ln.toString() + 'l';
                }
                case CONSTANT_INTEGER: {
                    Integer in = (Integer) x;
                    return in.toString();
                }
                case CONSTANT_CLASS:
                    String jn = getClassName(cpx);
                    if (textual) {
                        if (refs != null) {
                            refs[0] = jn;
                        }
                        return jn;
                    }
                    return javaName(jn);
                case CONSTANT_STRING:
                    String sv = stringValue(((CPX) x).cpx, textual);
                    if (textual) {
                        return '"' + sv + '"';
                    } else {
                        return sv;
                    }
                case CONSTANT_FIELD:
                case CONSTANT_METHOD:
                case CONSTANT_INTERFACEMETHOD:
                    //return getShortClassName(((CPX2)x).cpx1)+"."+StringValue(((CPX2)x).cpx2);
                    return javaName(getClassName(((CPX2) x).cpx1)) + "." + StringValue(((CPX2) x).cpx2);

                case CONSTANT_NAMEANDTYPE:
                    return getName(((CPX2) x).cpx1) + ":" + StringValue(((CPX2) x).cpx2);
                case CONSTANT_METHODHANDLE:
                    return "K" + ((CPX2)x).cpx1 + "@" + stringValue(((CPX2)x).cpx2, textual);
                case CONSTANT_METHODTYPE:
                    return stringValue(((CPX)x).cpx, true);
                default:
                    return "UnknownTag" + tag; //TBD
            }
        }

        /**
         * Returns resolved java type name.
         */
        public String javaName(String name) {
            if (name == null) {
                return "null";
            }
            int len = name.length();
            if (len == 0) {
                return "\"\"";
            }
            int cc = '/';
            fullname:
            { // xxx/yyy/zzz
                int cp;
                for (int k = 0; k < len; k += Character.charCount(cp)) {
                    cp = name.codePointAt(k);
                    if (cc == '/') {
                        if (!isJavaIdentifierStart(cp)) {
                            break fullname;
                        }
                    } else if (cp != '/') {
                        if (!isJavaIdentifierPart(cp)) {
                            break fullname;
                        }
                    }
                    cc = cp;
                }
                return name;
            }
            return "\"" + name + "\"";
        }

        public String getName(int cpx) {
            String res;
            try {
                return javaName((String) cpool[cpx]); //.replace('/','.');
            } catch (ArrayIndexOutOfBoundsException e) {
                return "<invalid constant pool index:" + cpx + ">";
            } catch (ClassCastException e) {
                return "<invalid constant pool ref:" + cpx + ">";
            }
        }

        /**
         * Returns unqualified class name.
         */
        public String getShortClassName(int cpx) {
            String classname = javaName(getClassName(cpx));
            pkgPrefixLen = classname.lastIndexOf("/") + 1;
            if (pkgPrefixLen != 0) {
                pkgPrefix = classname.substring(0, pkgPrefixLen);
                if (classname.startsWith(pkgPrefix)) {
                    return classname.substring(pkgPrefixLen);
                }
            }
            return classname;
        }

        /**
         * Returns source file name.
         */
        public String getSourceName() {
            return getName(source_cpx);
        }

        /**
         * Returns package name.
         */
        public String getPkgName() {
            String classname = getClassName(this_class);
            pkgPrefixLen = classname.lastIndexOf("/") + 1;
            if (pkgPrefixLen != 0) {
                pkgPrefix = classname.substring(0, pkgPrefixLen);
                return /* ("package  " + */ pkgPrefix.substring(0, pkgPrefixLen - 1) /* + ";\n") */;
            } else {
                return null;
            }
        }
        
        public BootMethodData getBootMethod(int indx) {
            return bootMethods != null ? bootMethods[indx] : null;
        }

        /**
         * Returns total constant pool entry count.
         */
        public int getCpoolCount() {
            return cpool_count;
        }

        /**
         * Returns minor version of class file.
         */
        public int getMinor_version() {
            return minor_version;
        }

        /**
         * Returns major version of class file.
         */
        public int getMajor_version() {
            return major_version;
        }

        private boolean isJavaIdentifierStart(int cp) {
            return ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z');
        }

        private boolean isJavaIdentifierPart(int cp) {
            return isJavaIdentifierStart(cp) || ('0' <= cp && cp <= '9');
        }

        public String[] getNameAndType(int indx) {
            return getNameAndType(indx, 0, new String[2]);
        }

        private String[] getNameAndType(int indx, int at, String[] arr) {
            CPX2 c2 = getCpoolEntry(indx);
            arr[at] = StringValue(c2.cpx1);
            arr[at + 1] = StringValue(c2.cpx2);
            return arr;
        }

        public String[] getFieldInfoName(int indx) {
            CPX2 c2 = getCpoolEntry(indx);
            String[] arr = new String[3];
            arr[0] = getClassName(c2.cpx1);
            return getNameAndType(c2.cpx2, 1, arr);
        }

        public MethodData findMethod(String name, String signature) {
            for (MethodData md: methods) {
                if (md.getName().equals(name)
                        && md.getInternalSig().equals(signature)) {
                    return md;
                }
            }

            // not found
            return null;
        }

        public FieldData findField(String name, String signature) {
            for (FieldData fd: fields) {
                if (fd.getName().equals(name)
                        && fd.getInternalSig().equals(signature)) {
                    return fd;
                }
            }

            // not found
            return null;
        }

        static byte[] findAttr(String n, AttrData[] attrs) {
            for (AttrData ad : attrs) {
                if (n.equals(ad.getAttrName())) {
                    return ad.getData();
                }
            }
            return null;
        }
    }

    /**
     * Strores field data informastion.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static class FieldData {

        ClassData cls;
        int access;
        int name_index;
        int descriptor_index;
        int attributes_count;
        int value_cpx = -1;
        boolean isSynthetic = false;
        boolean isDeprecated = false;
        Vector attrs;

        public FieldData(ClassData cls) {
            this.cls = cls;
        }

        /**
         * Read and store field info.
         */
        public void read(DataInputStream in) throws IOException {
            access = in.readUnsignedShort();
            name_index = in.readUnsignedShort();
            descriptor_index = in.readUnsignedShort();
            // Read the attributes
            int attributes_count = in.readUnsignedShort();
            attrs = new Vector(attributes_count);
            for (int i = 0; i < attributes_count; i++) {
                int attr_name_index = in.readUnsignedShort();
                if (cls.getTag(attr_name_index) != CONSTANT_UTF8) {
                    continue;
                }
                String attr_name = cls.getString(attr_name_index);
                if (attr_name.equals("ConstantValue")) {
                    if (in.readInt() != 2) {
                        throw new ClassFormatError("invalid ConstantValue attr length");
                    }
                    value_cpx = in.readUnsignedShort();
                    AttrData attr = new AttrData(cls);
                    attr.read(attr_name_index);
                    attrs.addElement(attr);
                } else if (attr_name.equals("Synthetic")) {
                    if (in.readInt() != 0) {
                        throw new ClassFormatError("invalid Synthetic attr length");
                    }
                    isSynthetic = true;
                    AttrData attr = new AttrData(cls);
                    attr.read(attr_name_index);
                    attrs.addElement(attr);
                } else if (attr_name.equals("Deprecated")) {
                    if (in.readInt() != 0) {
                        throw new ClassFormatError("invalid Synthetic attr length");
                    }
                    isDeprecated = true;
                    AttrData attr = new AttrData(cls);
                    attr.read(attr_name_index);
                    attrs.addElement(attr);
                } else {
                    AttrData attr = new AttrData(cls);
                    attr.read(attr_name_index, in);
                    attrs.addElement(attr);
                }
            }

        }  // end read

        public boolean isStatic() {
            return (access & ACC_STATIC) != 0;
        }

        /**
         * Returns access of a field.
         */
        public String[] getAccess() {
            Vector v = new Vector();
            if ((access & ACC_PUBLIC) != 0) {
                v.addElement("public");
            }
            if ((access & ACC_PRIVATE) != 0) {
                v.addElement("private");
            }
            if ((access & ACC_PROTECTED) != 0) {
                v.addElement("protected");
            }
            if ((access & ACC_STATIC) != 0) {
                v.addElement("static");
            }
            if ((access & ACC_FINAL) != 0) {
                v.addElement("final");
            }
            if ((access & ACC_VOLATILE) != 0) {
                v.addElement("volatile");
            }
            if ((access & ACC_TRANSIENT) != 0) {
                v.addElement("transient");
            }
            String[] accflags = new String[v.size()];
            v.copyInto(accflags);
            return accflags;
        }

        /**
         * Returns name of a field.
         */
        public String getName() {
            return cls.getStringValue(name_index);
        }

        /**
         * Returns internal signature of a field
         */
        public String getInternalSig() {
            return cls.getStringValue(descriptor_index);
        }

        /**
         * Returns true if field is synthetic.
         */
        public boolean isSynthetic() {
            return isSynthetic;
        }

        /**
         * Returns true if field is deprecated.
         */
        public boolean isDeprecated() {
            return isDeprecated;
        }

        public boolean hasConstantValue() {
            return value_cpx != -1;
        }

        /**
         * Returns list of attributes of field.
         */
        public Vector getAttributes() {
            return attrs;
        }

        public byte[] findAnnotationData(boolean classRetention) {
            String n = classRetention
                ? "RuntimeInvisibleAnnotations" : // NOI18N
                "RuntimeVisibleAnnotations"; // NOI18N
            AttrData[] arr = new AttrData[attrs.size()];
            attrs.copyInto(arr);
            return ClassData.findAttr(n, arr);
        }
    }

    /**
     * A JavaScript optimized replacement for Hashtable.
     *
     * @author Jaroslav Tulach
     */
    private static final class Hashtable {

        private Object[] keys;
        private Object[] values;

        Hashtable(int i) {
            this();
        }

        Hashtable(int i, double d) {
            this();
        }

        Hashtable() {
        }

        synchronized void put(Object key, Object val) {
            int[] where = {-1, -1};
            Object found = get(key, where);
            if (where[0] != -1) {
                // key exists
                values[where[0]] = val;
            } else {
                if (where[1] != -1) {
                    // null found
                    keys[where[1]] = key;
                    values[where[1]] = val;
                } else {
                    if (keys == null) {
                        keys = new Object[11];
                        values = new Object[11];
                        keys[0] = key;
                        values[0] = val;
                    } else {
                        Object[] newKeys = new Object[keys.length * 2];
                        Object[] newValues = new Object[values.length * 2];
                        for (int i = 0; i < keys.length; i++) {
                            newKeys[i] = keys[i];
                            newValues[i] = values[i];
                        }
                        newKeys[keys.length] = key;
                        newValues[keys.length] = val;
                        keys = newKeys;
                        values = newValues;
                    }
                }
            }
        }

        Object get(Object key) {
            return get(key, null);
        }

        private synchronized Object get(Object key, int[] foundAndNull) {
            if (keys == null) {
                return null;
            }
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == null) {
                    if (foundAndNull != null) {
                        foundAndNull[1] = i;
                    }
                } else if (keys[i].equals(key)) {
                    if (foundAndNull != null) {
                        foundAndNull[0] = i;
                    }
                    return values[i];
                }
            }
            return null;
        }
    }

    /**
     * Strores InnerClass data informastion.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    private static class InnerClassData {

        ClassData cls;
        int inner_class_info_index, outer_class_info_index, inner_name_index, access;

        public InnerClassData(ClassData cls) {
            this.cls = cls;

        }

        /**
         * Read Innerclass attribute data.
         */
        public void read(DataInputStream in) throws IOException {
            inner_class_info_index = in.readUnsignedShort();
            outer_class_info_index = in.readUnsignedShort();
            inner_name_index = in.readUnsignedShort();
            access = in.readUnsignedShort();
        }  // end read

        /**
         * Returns the access of this class or interface.
         */
        public String[] getAccess() {
            Vector v = new Vector();
            if ((access & ACC_PUBLIC) != 0) {
                v.addElement("public");
            }
            if ((access & ACC_FINAL) != 0) {
                v.addElement("final");
            }
            if ((access & ACC_ABSTRACT) != 0) {
                v.addElement("abstract");
            }
            String[] accflags = new String[v.size()];
            v.copyInto(accflags);
            return accflags;
        }
    } // end InnerClassData
    
    static class BootMethodData {
        final ClassData clazz;
        final int method;
        final int[] args;

        private BootMethodData(ClassData clazz, int method, int[] args) {
            this.clazz = clazz;
            this.method = method;
            this.args = args;
        }

        int getArguments() {
            return args.length;
        }

        String getArgument(int index) {
            return clazz.stringValue(args[index], false);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(clazz.stringValue(method, true));
            sb.append('(');
            for (int i = 0; i < getArguments(); i++) {
                sb.append("\n  ");
                sb.append(getArgument(i));
            }
            sb.append(')');
            return sb.toString();
        }
    }

    /**
     * Strores LineNumberTable data information.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    private static class LineNumData {

        short start_pc, line_number;

        public LineNumData() {
        }

        /**
         * Read LineNumberTable attribute.
         */
        public LineNumData(DataInputStream in) throws IOException {
            start_pc = in.readShort();
            line_number = in.readShort();

        }
    }

    /**
     * Strores LocalVariableTable data information.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    private static class LocVarData {

        short start_pc, length, name_cpx, sig_cpx, slot;

        public LocVarData() {
        }

        /**
         * Read LocalVariableTable attribute.
         */
        public LocVarData(DataInputStream in) throws IOException {
            start_pc = in.readShort();
            length = in.readShort();
            name_cpx = in.readShort();
            sig_cpx = in.readShort();
            slot = in.readShort();

        }
    }
    /**
     * Strores method data informastion.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static class MethodData {

        ClassData cls;
        int access;
        int name_index;
        int descriptor_index;
        int attributes_count;
        byte[] code;
        Vector exception_table = new Vector(0);
        Vector lin_num_tb = new Vector(0);
        Vector loc_var_tb = new Vector(0);
        StackMapTableData[] stackMapTable;
        StackMapData[] stackMap;
        int[] exc_index_table = null;
        Vector attrs = new Vector(0);
        Vector code_attrs = new Vector(0);
        int max_stack, max_locals;
        boolean isSynthetic = false;
        boolean isDeprecated = false;
        private AttrData annotationDefault;

        public MethodData(ClassData cls) {
            this.cls = cls;
        }

        /**
         * Read method info.
         */
        public void read(DataInputStream in) throws IOException {
            access = in.readUnsignedShort();
            name_index = in.readUnsignedShort();
            descriptor_index = in.readUnsignedShort();
            int attributes_count = in.readUnsignedShort();
            for (int i = 0; i < attributes_count; i++) {
                int attr_name_index = in.readUnsignedShort();

                readAttr:
                {
                    if (cls.getTag(attr_name_index) == CONSTANT_UTF8) {
                        String attr_name = cls.getString(attr_name_index);
                        if (attr_name.equals("Code")) {
                            readCode(in);
                            AttrData attr = new AttrData(cls);
                            attr.read(attr_name_index);
                            attrs.addElement(attr);
                            break readAttr;
                        } else if (attr_name.equals("Exceptions")) {
                            readExceptions(in);
                            AttrData attr = new AttrData(cls);
                            attr.read(attr_name_index);
                            attrs.addElement(attr);
                            break readAttr;
                        } else if (attr_name.equals("Synthetic")) {
                            if (in.readInt() != 0) {
                                throw new ClassFormatError("invalid Synthetic attr length");
                            }
                            isSynthetic = true;
                            AttrData attr = new AttrData(cls);
                            attr.read(attr_name_index);
                            attrs.addElement(attr);
                            break readAttr;
                        } else if (attr_name.equals("Deprecated")) {
                            if (in.readInt() != 0) {
                                throw new ClassFormatError("invalid Synthetic attr length");
                            }
                            isDeprecated = true;
                            AttrData attr = new AttrData(cls);
                            attr.read(attr_name_index);
                            attrs.addElement(attr);
                            break readAttr;
                        } else if (attr_name.equals("AnnotationDefault")) {
                            AttrData attr = new AttrData(cls);
                            attr.read(attr_name_index, in);
                            attrs.addElement(attr);
                            annotationDefault = attr;
                            break readAttr;
                        }
                    }
                    AttrData attr = new AttrData(cls);
                    attr.read(attr_name_index, in);
                    attrs.addElement(attr);
                }
            }
        }

        /**
         * Read code attribute info.
         */
        public void readCode(DataInputStream in) throws IOException {

            int attr_length = in.readInt();
            max_stack = in.readUnsignedShort();
            max_locals = in.readUnsignedShort();
            int codelen = in.readInt();

            code = new byte[codelen];
            int totalread = 0;
            while (totalread < codelen) {
                totalread += in.read(code, totalread, codelen - totalread);
            }
            //      in.read(code, 0, codelen);
            int clen = 0;
            readExceptionTable(in);
            int code_attributes_count = in.readUnsignedShort();

            for (int k = 0; k < code_attributes_count; k++) {
                int table_name_index = in.readUnsignedShort();
                int table_name_tag = cls.getTag(table_name_index);
                AttrData attr = new AttrData(cls);
                if (table_name_tag == CONSTANT_UTF8) {
                    String table_name_tstr = cls.getString(table_name_index);
                    if (table_name_tstr.equals("LineNumberTable")) {
                        readLineNumTable(in);
                        attr.read(table_name_index);
                    } else if (table_name_tstr.equals("LocalVariableTable")) {
                        readLocVarTable(in);
                        attr.read(table_name_index);
                    } else if (table_name_tstr.equals("StackMapTable")) {
                        readStackMapTable(in);
                        attr.read(table_name_index);
                    } else if (table_name_tstr.equals("StackMap")) {
                        readStackMap(in);
                        attr.read(table_name_index);
                    } else {
                        attr.read(table_name_index, in);
                    }
                    code_attrs.addElement(attr);
                    continue;
                }

                attr.read(table_name_index, in);
                code_attrs.addElement(attr);
            }
        }

        /**
         * Read exception table info.
         */
        void readExceptionTable(DataInputStream in) throws IOException {
            int exception_table_len = in.readUnsignedShort();
            exception_table = new Vector(exception_table_len);
            for (int l = 0; l < exception_table_len; l++) {
                exception_table.addElement(new TrapData(in, l));
            }
        }

        /**
         * Read LineNumberTable attribute info.
         */
        void readLineNumTable(DataInputStream in) throws IOException {
            int attr_len = in.readInt(); // attr_length
            int lin_num_tb_len = in.readUnsignedShort();
            lin_num_tb = new Vector(lin_num_tb_len);
            for (int l = 0; l < lin_num_tb_len; l++) {
                lin_num_tb.addElement(new LineNumData(in));
            }
        }

        /**
         * Read LocalVariableTable attribute info.
         */
        void readLocVarTable(DataInputStream in) throws IOException {
            int attr_len = in.readInt(); // attr_length
            int loc_var_tb_len = in.readUnsignedShort();
            loc_var_tb = new Vector(loc_var_tb_len);
            for (int l = 0; l < loc_var_tb_len; l++) {
                loc_var_tb.addElement(new LocVarData(in));
            }
        }

        /**
         * Read Exception attribute info.
         */
        public void readExceptions(DataInputStream in) throws IOException {
            int attr_len = in.readInt(); // attr_length in prog
            int num_exceptions = in.readUnsignedShort();
            exc_index_table = new int[num_exceptions];
            for (int l = 0; l < num_exceptions; l++) {
                int exc = in.readShort();
                exc_index_table[l] = exc;
            }
        }

        /**
         * Read StackMapTable attribute info.
         */
        void readStackMapTable(DataInputStream in) throws IOException {
            int attr_len = in.readInt();  //attr_length
            int stack_map_tb_len = in.readUnsignedShort();
            stackMapTable = new StackMapTableData[stack_map_tb_len];
            for (int i = 0; i < stack_map_tb_len; i++) {
                stackMapTable[i] = StackMapTableData.getInstance(in, this);
            }
        }

        /**
         * Read StackMap attribute info.
         */
        void readStackMap(DataInputStream in) throws IOException {
            int attr_len = in.readInt();  //attr_length
            int stack_map_len = in.readUnsignedShort();
            stackMap = new StackMapData[stack_map_len];
            for (int i = 0; i < stack_map_len; i++) {
                stackMap[i] = new StackMapData(in, this);
            }
        }
        
        /**
         * Return access of the method.
         */
        public int getAccess() {
            return access;
        }

        /**
         * Return name of the method.
         */
        public String getName() {
            return cls.getStringValue(name_index);
        }

        /**
         * Return internal siganature of the method.
         */
        public String getInternalSig() {
            return cls.getStringValue(descriptor_index);
        }

        /**
         * Return code attribute data of a method.
         */
        public byte[] getCode() {
            return code;
        }

        /**
         * Return LineNumberTable size.
         */
        public int getnumlines() {
            return lin_num_tb.size();
        }

        /**
         * Return LineNumberTable
         */
        public Vector getlin_num_tb() {
            return lin_num_tb;
        }

        /**
         * Return LocalVariableTable size.
         */
        public int getloc_var_tbsize() {
            return loc_var_tb.size();
        }

        /**
         * Return LocalVariableTable.
         */
        public Vector getloc_var_tb() {
            return loc_var_tb;
        }

        /**
         * Return StackMap.
         */
        public StackMapData[] getStackMap() {
            return stackMap;
        }

        /**
         * Return StackMapTable.
         */
        public StackMapTableData[] getStackMapTable() {
            return stackMapTable;
        }

        public StackMapIterator createStackMapIterator() {
            return new StackMapIterator(this);
        }

        /**
         * Return true if method is static
         */
        public boolean isStatic() {
            if ((access & ACC_STATIC) != 0) {
                return true;
            }
            return false;
        }

        /**
         * Return max depth of operand stack.
         */
        public int getMaxStack() {
            return max_stack;
        }

        /**
         * Return number of local variables.
         */
        public int getMaxLocals() {
            return max_locals;
        }

        /**
         * Return exception index table in Exception attribute.
         */
        public int[] get_exc_index_table() {
            return exc_index_table;
        }

        /**
         * Return exception table in code attributre.
         */
        public TrapDataIterator getTrapDataIterator() {
            return new TrapDataIterator(exception_table);
        }

        /**
         * Return method attributes.
         */
        public Vector getAttributes() {
            return attrs;
        }

        /**
         * Return code attributes.
         */
        public Vector getCodeAttributes() {
            return code_attrs;
        }

        byte[] getDefaultAttribute() {
            return annotationDefault == null ? null : annotationDefault.getData();
        }

        /**
         * Return true if method id synthetic.
         */
        public boolean isSynthetic() {
            return isSynthetic;
        }

        /**
         * Return true if method is deprecated.
         */
        public boolean isDeprecated() {
            return isDeprecated;
        }

        public byte[] findAnnotationData(boolean classRetention) {
            String n = classRetention
                ? "RuntimeInvisibleAnnotations" : // NOI18N
                "RuntimeVisibleAnnotations"; // NOI18N
            AttrData[] arr = new AttrData[attrs.size()];
            attrs.copyInto(arr);
            return ClassData.findAttr(n, arr);
        }

        public boolean isConstructor() {
            return "<init>".equals(getName());
        }

        int getLineNumber() {
            Vector tab = lin_num_tb;
            if (tab.size() == 0)
                return 0;
            LineNumData e = (LineNumData) tab.elementAt(0);
            return Math.max(e.line_number - 2, 0);
        }

        long[] getLineNumberTable() {
            Vector tab = lin_num_tb;
            int size = tab.size();
            long[] result = new long[size];
            for (int i = 0; i < size; i++) {
                LineNumData e = (LineNumData) tab.elementAt(i);
                long startPC = (int) e.start_pc & 0xffff;
                long lineNumber = (int) e.line_number & 0xffff;
                result[i] = startPC << 32 | lineNumber;
            }
            return result;
        }

        long[] getLocalVariableTableKeys() {
            Vector tab = loc_var_tb;
            int size = tab.size();
            long[] result = new long[size];
            for (int i = 0; i < size; i++) {
                LocVarData e = (LocVarData) tab.elementAt(i);
                long startPC = (int) e.start_pc & 0xffff;
                long length = (int) e.length & 0xffff;
                long slot = (int) e.slot & 0xffff;
                result[i] = startPC << 32 | length << 16 | slot;
            }
            return result;
        }

        String[] getLocalVariableTableValues() {
            Vector tab = loc_var_tb;
            int size = tab.size();
            String[] result = new String[size];
            for (int i = 0; i < size; i++) {
                LocVarData e = (LocVarData) tab.elementAt(i);
                result[i] = cls.getString(e.name_cpx);
            }
            return result;
        }
    }

    /* represents one entry of StackMap attribute
     */
    private static class StackMapData {

        final int offset;
        final int[] locals;
        final int[] stack;

        StackMapData(int offset, int[] locals, int[] stack) {
            this.offset = offset;
            this.locals = locals;
            this.stack = stack;
        }

        StackMapData(DataInputStream in, MethodData method) throws IOException {
            offset = in.readUnsignedShort();
            int local_size = in.readUnsignedShort();
            locals = readTypeArray(in, local_size, method);
            int stack_size = in.readUnsignedShort();
            stack = readTypeArray(in, stack_size, method);
        }

        static final int[] readTypeArray(DataInputStream in, int length, MethodData method) throws IOException {
            int[] types = new int[length];
            for (int i = 0; i < length; i++) {
                types[i] = readType(in, method);
            }
            return types;
        }

        static final int readType(DataInputStream in, MethodData method) throws IOException {
            int type = in.readUnsignedByte();
            if (type == ITEM_Object || type == ITEM_NewObject) {
                type = type | (in.readUnsignedShort() << 8);
            }
            return type;
        }
    }

    static final class StackMapIterator {

        private final StackMapTableData[] stackMapTable;
        private final TypeArray argTypes;
        private final TypeArray localTypes;
        private final TypeArray stackTypes;
        private int nextFrameIndex;
        private int lastFrameByteCodeOffset;
        private int byteCodeOffset;

        StackMapIterator(final MethodData methodData) {
            this(methodData.getStackMapTable(),
                methodData.getInternalSig(),
                methodData.isStatic());
        }

        StackMapIterator(final StackMapTableData[] stackMapTable,
            final String methodSignature,
            final boolean isStaticMethod) {
            this.stackMapTable = (stackMapTable != null)
                ? stackMapTable
                : new StackMapTableData[0];

            argTypes = getArgTypes(methodSignature, isStaticMethod);
            localTypes = new TypeArray();
            stackTypes = new TypeArray();

            localTypes.addAll(argTypes);

            lastFrameByteCodeOffset = -1;
            advanceBy(0);
        }
        
        public boolean isEmpty() {
            return stackMapTable.length == 0;
        }

        public String getFrameAsString() {
            return (nextFrameIndex == 0)
                ? StackMapTableData.toString("INITIAL", 0, null, null)
                : stackMapTable[nextFrameIndex - 1].toString();
        }

        public int getFrameIndex() {
            return nextFrameIndex;
        }

        public TypeArray getFrameStack() {
            return stackTypes;
        }

        public TypeArray getFrameLocals() {
            return localTypes;
        }

        public TypeArray getArguments() {
            return argTypes;
        }

        public void advanceBy(final int numByteCodes) {
            if (numByteCodes < 0) {
                throw new IllegalStateException("Forward only iterator");
            }

            byteCodeOffset += numByteCodes;
            while ((nextFrameIndex < stackMapTable.length)
                && ((byteCodeOffset - lastFrameByteCodeOffset)
                >= (stackMapTable[nextFrameIndex].offsetDelta
                + 1))) {
                final StackMapTableData nextFrame = stackMapTable[nextFrameIndex];

                lastFrameByteCodeOffset += nextFrame.offsetDelta + 1;
                nextFrame.applyTo(localTypes, stackTypes);

                ++nextFrameIndex;
            }
        }

        public void advanceTo(final int nextByteCodeOffset) {
            advanceBy(nextByteCodeOffset - byteCodeOffset);
        }

        private static TypeArray getArgTypes(final String methodSignature,
            final boolean isStaticMethod) {
            final TypeArray argTypes = new TypeArray();

            if (!isStaticMethod) {
                argTypes.add(ITEM_Object);
            }

            if (methodSignature.charAt(0) != '(') {
                throw new IllegalArgumentException("Invalid method signature");
            }

            final int length = methodSignature.length();
            boolean skipType = false;
            int argType;
            for (int i = 1; i < length; ++i) {
                switch (methodSignature.charAt(i)) {
                    case 'B':
                    case 'C':
                    case 'S':
                    case 'Z':
                    case 'I':
                        argType = ITEM_Integer;
                        break;
                    case 'J':
                        argType = ITEM_Long;
                        break;
                    case 'F':
                        argType = ITEM_Float;
                        break;
                    case 'D':
                        argType = ITEM_Double;
                        break;
                    case 'L': {
                        i = methodSignature.indexOf(';', i + 1);
                        if (i == -1) {
                            throw new IllegalArgumentException(
                                "Invalid method signature");
                        }
                        argType = ITEM_Object;
                        break;
                    }
                    case ')':
                        // not interested in the return value type
                        return argTypes;
                    case '[':
                        if (!skipType) {
                            argTypes.add(ITEM_Object);
                            skipType = true;
                        }
                        continue;

                    default:
                        throw new IllegalArgumentException(
                            "Invalid method signature");
                }

                if (!skipType) {
                    argTypes.add(argType);
                } else {
                    skipType = false;
                }
            }

            return argTypes;
        }
    }
    /* represents one entry of StackMapTable attribute
     */

    private static abstract class StackMapTableData {

        final int frameType;
        int offsetDelta;

        StackMapTableData(int frameType) {
            this.frameType = frameType;
        }

        abstract void applyTo(TypeArray localTypes, TypeArray stackTypes);

        protected static String toString(
            final String frameType,
            final int offset,
            final int[] localTypes,
            final int[] stackTypes) {
            final StringBuilder sb = new StringBuilder(frameType);

            sb.append("(off: +").append(offset);
            if (localTypes != null) {
                sb.append(", locals: ");
                appendTypes(sb, localTypes);
            }
            if (stackTypes != null) {
                sb.append(", stack: ");
                appendTypes(sb, stackTypes);
            }
            sb.append(')');

            return sb.toString();
        }

        private static void appendTypes(final StringBuilder sb, final int[] types) {
            sb.append('[');
            if (types.length > 0) {
                sb.append(TypeArray.typeString(types[0]));
                for (int i = 1; i < types.length; ++i) {
                    sb.append(", ");
                    sb.append(TypeArray.typeString(types[i]));
                }
            }
            sb.append(']');
        }

        private static class SameFrame extends StackMapTableData {

            SameFrame(int frameType, int offsetDelta) {
                super(frameType);
                this.offsetDelta = offsetDelta;
            }

            @Override
            void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                stackTypes.clear();
            }

            @Override
            public String toString() {
                return toString("SAME" + ((frameType == SAME_FRAME_EXTENDED)
                    ? "_FRAME_EXTENDED" : ""),
                    offsetDelta,
                    null, null);
            }
        }

        private static class SameLocals1StackItem extends StackMapTableData {

            final int[] stack;

            SameLocals1StackItem(int frameType, int offsetDelta, int[] stack) {
                super(frameType);
                this.offsetDelta = offsetDelta;
                this.stack = stack;
            }

            @Override
            void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                stackTypes.setAll(stack);
            }

            @Override
            public String toString() {
                return toString(
                    "SAME_LOCALS_1_STACK_ITEM"
                    + ((frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED)
                    ? "_EXTENDED" : ""),
                    offsetDelta,
                    null, stack);
            }
        }

        private static class ChopFrame extends StackMapTableData {

            ChopFrame(int frameType, int offsetDelta) {
                super(frameType);
                this.offsetDelta = offsetDelta;
            }

            @Override
            void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                localTypes.setSize(localTypes.getSize()
                    - (SAME_FRAME_EXTENDED - frameType));
                stackTypes.clear();
            }

            @Override
            public String toString() {
                return toString("CHOP", offsetDelta, null, null);
            }
        }

        private static class AppendFrame extends StackMapTableData {

            final int[] locals;

            AppendFrame(int frameType, int offsetDelta, int[] locals) {
                super(frameType);
                this.offsetDelta = offsetDelta;
                this.locals = locals;
            }

            @Override
            void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                localTypes.addAll(locals);
                stackTypes.clear();
            }

            @Override
            public String toString() {
                return toString("APPEND", offsetDelta, locals, null);
            }
        }

        private static class FullFrame extends StackMapTableData {

            final int[] locals;
            final int[] stack;

            FullFrame(int offsetDelta, int[] locals, int[] stack) {
                super(FULL_FRAME);
                this.offsetDelta = offsetDelta;
                this.locals = locals;
                this.stack = stack;
            }

            @Override
            void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                localTypes.setAll(locals);
                stackTypes.setAll(stack);
            }

            @Override
            public String toString() {
                return toString("FULL", offsetDelta, locals, stack);
            }
        }

        static StackMapTableData getInstance(DataInputStream in, MethodData method)
            throws IOException {
            int frameType = in.readUnsignedByte();

            if (frameType < SAME_FRAME_BOUND) {
                // same_frame
                return new SameFrame(frameType, frameType);
            } else if (SAME_FRAME_BOUND <= frameType && frameType < SAME_LOCALS_1_STACK_ITEM_BOUND) {
                // same_locals_1_stack_item_frame
                // read additional single stack element
                return new SameLocals1StackItem(frameType,
                    (frameType - SAME_FRAME_BOUND),
                    StackMapData.readTypeArray(in, 1, method));
            } else if (frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED) {
                // same_locals_1_stack_item_extended
                return new SameLocals1StackItem(frameType,
                    in.readUnsignedShort(),
                    StackMapData.readTypeArray(in, 1, method));
            } else if (SAME_LOCALS_1_STACK_ITEM_EXTENDED < frameType && frameType < SAME_FRAME_EXTENDED) {
                // chop_frame or same_frame_extended
                return new ChopFrame(frameType, in.readUnsignedShort());
            } else if (frameType == SAME_FRAME_EXTENDED) {
                // chop_frame or same_frame_extended
                return new SameFrame(frameType, in.readUnsignedShort());
            } else if (SAME_FRAME_EXTENDED < frameType && frameType < FULL_FRAME) {
                // append_frame
                return new AppendFrame(frameType, in.readUnsignedShort(),
                    StackMapData.readTypeArray(in, frameType - SAME_FRAME_EXTENDED, method));
            } else if (frameType == FULL_FRAME) {
                // full_frame
                int offsetDelta = in.readUnsignedShort();
                int locals_size = in.readUnsignedShort();
                int[] locals = StackMapData.readTypeArray(in, locals_size, method);
                int stack_size = in.readUnsignedShort();
                int[] stack = StackMapData.readTypeArray(in, stack_size, method);
                return new FullFrame(offsetDelta, locals, stack);
            } else {
                throw new ClassFormatError("unrecognized frame_type in StackMapTable");
            }
        }
    }

    /**
     * Stores exception table data in code attribute.
     *
     * @author Sucheta Dambalkar (Adopted code from jdis)
     */
    static final class TrapData {

        public final short start_pc;
        public final short end_pc;
        public final short handler_pc;
        public final short catch_cpx;
        final int num;

        /**
         * Read and store exception table data in code attribute.
         */
        TrapData(DataInputStream in, int num) throws IOException {
            this.num = num;
            start_pc = in.readShort();
            end_pc = in.readShort();
            handler_pc = in.readShort();
            catch_cpx = in.readShort();
        }

        /**
         * returns recommended identifier
         */
        public String ident() {
            return "t" + num;
        }
    }
    /**
     *
     * @author Jaroslav Tulach
     */
    static final class TrapDataIterator {

        private final Hashtable exStart = new Hashtable();
        private final Hashtable exStop = new Hashtable();
        private TrapData[] current = new TrapData[10];
        private int currentCount;

        TrapDataIterator(Vector exceptionTable) {
            for (int i = 0; i < exceptionTable.size(); i++) {
                final TrapData td = (TrapData) exceptionTable.elementAt(i);
                put(exStart, td.start_pc, td);
                put(exStop, td.end_pc, td);
            }
        }

        private static void put(Hashtable h, short key, TrapData td) {
            Short s = Short.valueOf((short) key);
            Vector v = (Vector) h.get(s);
            if (v == null) {
                v = new Vector(1);
                h.put(s, v);
            }
            v.add(td);
        }

        private boolean processAll(Hashtable h, Short key, boolean add) {
            boolean change = false;
            Vector v = (Vector) h.get(key);
            if (v != null) {
                int s = v.size();
                for (int i = 0; i < s; i++) {
                    TrapData td = (TrapData) v.elementAt(i);
                    if (add) {
                        add(td);
                        change = true;
                    } else {
                        remove(td);
                        change = true;
                    }
                }
            }
            return change;
        }

        public boolean advanceTo(int i) {
            Short s = Short.valueOf((short) i);
            boolean ch1 = processAll(exStart, s, true);
            boolean ch2 = processAll(exStop, s, false);
            return ch1 || ch2;
        }

        public boolean useTry() {
            return currentCount > 0;
        }

        public TrapData[] current() {
            TrapData[] copy = new TrapData[currentCount];
            for (int i = 0; i < currentCount; i++) {
                copy[i] = current[i];
            }
            return copy;
        }

        private void add(TrapData e) {
            if (currentCount == current.length) {
                TrapData[] data = new TrapData[currentCount * 2];
                for (int i = 0; i < currentCount; i++) {
                    data[i] = current[i];
                }
                current = data;
            }
            current[currentCount++] = e;
        }

        private void remove(TrapData e) {
            if (currentCount == 0) {
                return;
            }
            int from = 0;
            while (from < currentCount) {
                if (e == current[from++]) {
                    break;
                }
            }
            while (from < currentCount) {
                current[from - 1] = current[from];
                current[from] = null;
                from++;
            }
            currentCount--;
        }
    }
    static final class TypeArray {

        private static final int CAPACITY_INCREMENT = 16;
        private int[] types;
        private int size;

        public TypeArray() {
        }

        public TypeArray(final TypeArray initialTypes) {
            setAll(initialTypes);
        }

        public void add(final int newType) {
            ensureCapacity(size + 1);
            types[size++] = newType;
        }

        public void addAll(final TypeArray newTypes) {
            addAll(newTypes.types, 0, newTypes.size);
        }

        public void addAll(final int[] newTypes) {
            addAll(newTypes, 0, newTypes.length);
        }

        public void addAll(final int[] newTypes,
            final int offset,
            final int count) {
            if (count > 0) {
                ensureCapacity(size + count);
                arraycopy(newTypes, offset, types, size, count);
                size += count;
            }
        }

        public void set(final int index, final int newType) {
            types[index] = newType;
        }

        public void setAll(final TypeArray newTypes) {
            setAll(newTypes.types, 0, newTypes.size);
        }

        public void setAll(final int[] newTypes) {
            setAll(newTypes, 0, newTypes.length);
        }

        public void setAll(final int[] newTypes,
            final int offset,
            final int count) {
            if (count > 0) {
                ensureCapacity(count);
                arraycopy(newTypes, offset, types, 0, count);
                size = count;
            } else {
                clear();
            }
        }

        public void setSize(final int newSize) {
            if (size != newSize) {
                ensureCapacity(newSize);

                for (int i = size; i < newSize; ++i) {
                    types[i] = 0;
                }
                size = newSize;
            }
        }

        public void clear() {
            size = 0;
        }

        public int getSize() {
            return size;
        }

        public int get(final int index) {
            return types[index];
        }

        public static String typeString(final int type) {
            switch (type & 0xff) {
                case ITEM_Bogus:
                    return "_top_";
                case ITEM_Integer:
                    return "_int_";
                case ITEM_Float:
                    return "_float_";
                case ITEM_Double:
                    return "_double_";
                case ITEM_Long:
                    return "_long_";
                case ITEM_Null:
                    return "_null_";
                case ITEM_InitObject: // UninitializedThis
                    return "_init_";
                case ITEM_Object:
                    return "_object_";
                case ITEM_NewObject: // Uninitialized
                    return "_new_";
                default:
                    throw new IllegalArgumentException("Unknown type");
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            String sep = "";
            for (int i = 0; i < size; ++i) {
                sb.append(sep).append(VarType.toString(types[i] & 0xff));
                sep = ", ";
            }
            return sb.append(']').toString();
        }

        private void ensureCapacity(final int minCapacity) {
            if ((minCapacity == 0)
                || (types != null) && (minCapacity <= types.length)) {
                return;
            }

            final int newCapacity =
                ((minCapacity + CAPACITY_INCREMENT - 1) / CAPACITY_INCREMENT)
                * CAPACITY_INCREMENT;
            final int[] newTypes = new int[newCapacity];

            if (size > 0) {
                arraycopy(types, 0, newTypes, 0, size);
            }

            types = newTypes;
        }

        // no System.arraycopy
        private void arraycopy(final int[] src, final int srcPos,
            final int[] dest, final int destPos,
            final int length) {
            for (int i = 0; i < length; ++i) {
                dest[destPos + i] = src[srcPos + i];
            }
        }
    }
    /**
     * A JavaScript ready replacement for java.util.Vector
     *
     * @author Jaroslav Tulach
     */
    @JavaScriptPrototype(prototype = "new Array")
    private static final class Vector {

        private Object[] arr;

        Vector() {
        }

        Vector(int i) {
        }

        void add(Object objectType) {
            addElement(objectType);
        }

        @JavaScriptBody(args = {"obj"}, body =
            "this.push(obj);")
        void addElement(Object obj) {
            final int s = size();
            setSize(s + 1);
            setElementAt(obj, s);
        }

        @JavaScriptBody(args = {}, body =
            "return this.length;")
        int size() {
            return arr == null ? 0 : arr.length;
        }

        @JavaScriptBody(args = {"newArr"}, body =
            "for (var i = 0; i < this.length; i++) {\n"
            + "  newArr[i] = this[i];\n"
            + "}\n")
        void copyInto(Object[] newArr) {
            if (arr == null) {
                return;
            }
            int min = Math.min(newArr.length, arr.length);
            for (int i = 0; i < min; i++) {
                newArr[i] = arr[i];
            }
        }

        @JavaScriptBody(args = {"index"}, body =
            "return this[index];")
        Object elementAt(int index) {
            return arr[index];
        }

        private void setSize(int len) {
            Object[] newArr = new Object[len];
            copyInto(newArr);
            arr = newArr;
        }

        @JavaScriptBody(args = {"val", "index"}, body =
            "this[index] = val;")
        void setElementAt(Object val, int index) {
            arr[index] = val;
        }
    }
    
}
