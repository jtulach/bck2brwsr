/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.IOException;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;

class LoopCode implements Runnable {
    private final Appendable out;
    private final ByteCodeToJavaScript byteCodeToJavaScript;
    private final NumberOperations numbers;
    private final ClassData jc;
    private boolean modified;

    LoopCode(ByteCodeToJavaScript b, Appendable out, NumberOperations numbers, ClassData jc) {
        this.out = new TrackingAppendable(out, this);
        this.jc = jc;
        this.numbers = numbers;
        this.byteCodeToJavaScript = b;
    }

    @Override
    public void run() {
        this.modified = true;
    }

    void loopCode(
        final ByteCodeParser.StackMapIterator stackMapIterator,
        final byte[] byteCodes, ByteCodeParser.TrapDataIterator trap,
        final StackMapper smapper,
        final LocalsMapper lmapper
    ) throws IllegalStateException, NumberFormatException, IOException {
        int lastStackFrame;
        ByteCodeParser.TrapData[] previousTrap = null;
        boolean wide = false;
        boolean didBranches;
        if (stackMapIterator.isEmpty()) {
            didBranches = false;
            lastStackFrame = 0;
        } else {
            didBranches = true;
            lastStackFrame = -1;
            out.append("\n  var gt = 0;\n");
        }
        int openBraces = 0;
        int topMostLabel = 0;
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            modified = false;
            stackMapIterator.advanceTo(i);
            boolean changeInCatch = trap.advanceTo(i);
            if (changeInCatch || lastStackFrame != stackMapIterator.getFrameIndex()) {
                if (previousTrap != null) {
                    byteCodeToJavaScript.generateCatch(out, previousTrap, i, topMostLabel);
                    previousTrap = null;
                }
            }
            if (lastStackFrame != stackMapIterator.getFrameIndex()) {
                smapper.flush(out);
                if (i != 0) {
                    out.append("    }\n");
                }
                if (openBraces > 64) {
                    for (int c = 0; c < 64; c++) {
                        out.append("break;}\n");
                    }
                    openBraces = 1;
                    topMostLabel = i;
                }
                lastStackFrame = stackMapIterator.getFrameIndex();
                lmapper.syncWithFrameLocals(stackMapIterator.getFrameLocals());
                smapper.syncWithFrameStack(stackMapIterator.getFrameStack());
                out.append("    X_" + i).append(": for (;;) { IF: if (gt <= " + i + ") {\n");
                openBraces++;
                changeInCatch = true;
            } else {
                byteCodeToJavaScript.debug(out, "    /* " + i + " */ ");
            }
            if (changeInCatch && trap.useTry()) {
                out.append("try {");
                previousTrap = trap.current();
            }
            final int c = ByteCodeToJavaScript.readUByte(byteCodes, i);
            switch (c) {
                case ByteCodeParser.opc_aload_0:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(0));
                    break;
                case ByteCodeParser.opc_iload_0:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(0));
                    break;
                case ByteCodeParser.opc_lload_0:
                    smapper.assign(out, VarType.LONG, lmapper.getL(0));
                    break;
                case ByteCodeParser.opc_fload_0:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(0));
                    break;
                case ByteCodeParser.opc_dload_0:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(0));
                    break;
                case ByteCodeParser.opc_aload_1:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(1));
                    break;
                case ByteCodeParser.opc_iload_1:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(1));
                    break;
                case ByteCodeParser.opc_lload_1:
                    smapper.assign(out, VarType.LONG, lmapper.getL(1));
                    break;
                case ByteCodeParser.opc_fload_1:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(1));
                    break;
                case ByteCodeParser.opc_dload_1:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(1));
                    break;
                case ByteCodeParser.opc_aload_2:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(2));
                    break;
                case ByteCodeParser.opc_iload_2:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(2));
                    break;
                case ByteCodeParser.opc_lload_2:
                    smapper.assign(out, VarType.LONG, lmapper.getL(2));
                    break;
                case ByteCodeParser.opc_fload_2:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(2));
                    break;
                case ByteCodeParser.opc_dload_2:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(2));
                    break;
                case ByteCodeParser.opc_aload_3:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(3));
                    break;
                case ByteCodeParser.opc_iload_3:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(3));
                    break;
                case ByteCodeParser.opc_lload_3:
                    smapper.assign(out, VarType.LONG, lmapper.getL(3));
                    break;
                case ByteCodeParser.opc_fload_3:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(3));
                    break;
                case ByteCodeParser.opc_dload_3:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(3));
                    break;
                case ByteCodeParser.opc_iload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(out, VarType.INTEGER, lmapper.getI(indx));
                        break;
                    }
                case ByteCodeParser.opc_lload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(out, VarType.LONG, lmapper.getL(indx));
                        break;
                    }
                case ByteCodeParser.opc_fload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(out, VarType.FLOAT, lmapper.getF(indx));
                        break;
                    }
                case ByteCodeParser.opc_dload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(out, VarType.DOUBLE, lmapper.getD(indx));
                        break;
                    }
                case ByteCodeParser.opc_aload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(out, VarType.REFERENCE, lmapper.getA(indx));
                        break;
                    }
                case ByteCodeParser.opc_istore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setI(indx), smapper.popI());
                        break;
                    }
                case ByteCodeParser.opc_lstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setL(indx), smapper.popL());
                        break;
                    }
                case ByteCodeParser.opc_fstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setF(indx), smapper.popF());
                        break;
                    }
                case ByteCodeParser.opc_dstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setD(indx), smapper.popD());
                        break;
                    }
                case ByteCodeParser.opc_astore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setA(indx), smapper.popA());
                        break;
                    }
                case ByteCodeParser.opc_astore_0:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setA(0), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_0:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setI(0), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_0:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setL(0), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_0:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setF(0), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_0:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setD(0), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_1:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setA(1), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_1:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_1:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_1:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_1:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_2:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setA(2), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_2:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setI(2), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_2:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setL(2), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_2:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setF(2), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_2:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setD(2), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_3:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setA(3), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_3:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setI(3), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_3:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setL(3), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_3:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setF(3), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_3:
                    ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", lmapper.setD(3), smapper.popD());
                    break;
                case ByteCodeParser.opc_iadd:
                    smapper.replace(out, VarType.INTEGER, "(((@1) + (@2)) | 0)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ladd:
                    smapper.replace(out, VarType.LONG, numbers.add64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fadd:
                    smapper.replace(out, VarType.FLOAT, "(@1 + @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dadd:
                    smapper.replace(out, VarType.DOUBLE, "(@1 + @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_isub:
                    smapper.replace(out, VarType.INTEGER, "(((@1) - (@2)) | 0)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lsub:
                    smapper.replace(out, VarType.LONG, numbers.sub64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fsub:
                    smapper.replace(out, VarType.FLOAT, "(@1 - @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dsub:
                    smapper.replace(out, VarType.DOUBLE, "(@1 - @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_imul:
                    smapper.replace(out, VarType.INTEGER, numbers.mul32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lmul:
                    smapper.replace(out, VarType.LONG, numbers.mul64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fmul:
                    smapper.replace(out, VarType.FLOAT, "(@1 * @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dmul:
                    smapper.replace(out, VarType.DOUBLE, "(@1 * @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_idiv:
                    smapper.replace(out, VarType.INTEGER, numbers.div32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ldiv:
                    smapper.replace(out, VarType.LONG, numbers.div64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fdiv:
                    smapper.replace(out, VarType.FLOAT, "(@1 / @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_ddiv:
                    smapper.replace(out, VarType.DOUBLE, "(@1 / @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_irem:
                    smapper.replace(out, VarType.INTEGER, numbers.mod32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lrem:
                    smapper.replace(out, VarType.LONG, numbers.mod64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_frem:
                    smapper.replace(out, VarType.FLOAT, "(@1 % @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_drem:
                    smapper.replace(out, VarType.DOUBLE, "(@1 % @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_iand:
                    smapper.replace(out, VarType.INTEGER, "(@1 & @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_land:
                    smapper.replace(out, VarType.LONG, numbers.and64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ior:
                    smapper.replace(out, VarType.INTEGER, "(@1 | @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lor:
                    smapper.replace(out, VarType.LONG, numbers.or64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ixor:
                    smapper.replace(out, VarType.INTEGER, "(@1 ^ @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lxor:
                    smapper.replace(out, VarType.LONG, numbers.xor64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ineg:
                    smapper.replace(out, VarType.INTEGER, "(-(@1) | 0)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_lneg:
                    smapper.replace(out, VarType.LONG, numbers.neg64(), smapper.getL(0));
                    break;
                case ByteCodeParser.opc_fneg:
                    smapper.replace(out, VarType.FLOAT, "(-@1)", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_dneg:
                    smapper.replace(out, VarType.DOUBLE, "(-@1)", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_ishl:
                    smapper.replace(out, VarType.INTEGER, "(@1 << @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lshl:
                    smapper.replace(out, VarType.LONG, numbers.shl64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ishr:
                    smapper.replace(out, VarType.INTEGER, "(@1 >> @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lshr:
                    smapper.replace(out, VarType.LONG, numbers.shr64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_iushr:
                    smapper.replace(out, VarType.INTEGER, "(@1 >>> @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lushr:
                    smapper.replace(out, VarType.LONG, numbers.ushr64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_iinc:
                    {
                        ++i;
                        final int varIndx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        ++i;
                        final int incrBy = wide ? ByteCodeToJavaScript.readShort(byteCodes, i++) : byteCodes[i];
                        wide = false;
                        if (incrBy == 1) {
                            ByteCodeToJavaScript.emit(out, smapper, "@1++;", lmapper.getI(varIndx));
                        } else {
                            ByteCodeToJavaScript.emit(out, smapper, "@1 += @2;", lmapper.getI(varIndx), Integer.toString(incrBy));
                        }
                        break;
                    }
                case ByteCodeParser.opc_return:
                    ByteCodeToJavaScript.emit(out, smapper, "return;");
                    break;
                case ByteCodeParser.opc_ireturn:
                    ByteCodeToJavaScript.emit(out, smapper, "return @1;", smapper.popI());
                    break;
                case ByteCodeParser.opc_lreturn:
                    ByteCodeToJavaScript.emit(out, smapper, "return @1;", smapper.popL());
                    break;
                case ByteCodeParser.opc_freturn:
                    ByteCodeToJavaScript.emit(out, smapper, "return @1;", smapper.popF());
                    break;
                case ByteCodeParser.opc_dreturn:
                    ByteCodeToJavaScript.emit(out, smapper, "return @1;", smapper.popD());
                    break;
                case ByteCodeParser.opc_areturn:
                    ByteCodeToJavaScript.emit(out, smapper, "return @1;", smapper.popA());
                    break;
                case ByteCodeParser.opc_i2l:
                    smapper.replace(out, VarType.LONG, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2f:
                    smapper.replace(out, VarType.FLOAT, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2d:
                    smapper.replace(out, VarType.DOUBLE, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_l2i:
                    smapper.replace(out, VarType.INTEGER, "((@1) | 0)", smapper.getL(0));
                    break;
            // max int check?
                case ByteCodeParser.opc_l2f:
                    smapper.replace(out, VarType.FLOAT, "(@1).toFP()", smapper.getL(0));
                    break;
                case ByteCodeParser.opc_l2d:
                    smapper.replace(out, VarType.DOUBLE, "(@1).toFP()", smapper.getL(0));
                    break;
                case ByteCodeParser.opc_f2d:
                    smapper.replace(out, VarType.DOUBLE, "@1", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_d2f:
                    smapper.replace(out, VarType.FLOAT, "@1", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_f2i:
                    smapper.replace(out, VarType.INTEGER, "((@1) | 0)", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_f2l:
                    smapper.replace(out, VarType.LONG, "(@1).toLong()", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_d2i:
                    smapper.replace(out, VarType.INTEGER, "((@1)| 0)", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_d2l:
                    smapper.replace(out, VarType.LONG, "(@1).toLong()", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_i2b:
                    smapper.replace(out, VarType.INTEGER, "(((@1) << 24) >> 24)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2c:
                case ByteCodeParser.opc_i2s:
                    smapper.replace(out, VarType.INTEGER, "(((@1) << 16) >> 16)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_aconst_null:
                    smapper.assign(out, VarType.REFERENCE, "null");
                    break;
                case ByteCodeParser.opc_iconst_m1:
                    smapper.assign(out, VarType.INTEGER, "-1");
                    break;
                case ByteCodeParser.opc_iconst_0:
                    smapper.assign(out, VarType.INTEGER, "0");
                    break;
                case ByteCodeParser.opc_dconst_0:
                    smapper.assign(out, VarType.DOUBLE, "0");
                    break;
                case ByteCodeParser.opc_lconst_0:
                    smapper.assign(out, VarType.LONG, "0");
                    break;
                case ByteCodeParser.opc_fconst_0:
                    smapper.assign(out, VarType.FLOAT, "0");
                    break;
                case ByteCodeParser.opc_iconst_1:
                    smapper.assign(out, VarType.INTEGER, "1");
                    break;
                case ByteCodeParser.opc_lconst_1:
                    smapper.assign(out, VarType.LONG, "1");
                    break;
                case ByteCodeParser.opc_fconst_1:
                    smapper.assign(out, VarType.FLOAT, "1");
                    break;
                case ByteCodeParser.opc_dconst_1:
                    smapper.assign(out, VarType.DOUBLE, "1");
                    break;
                case ByteCodeParser.opc_iconst_2:
                    smapper.assign(out, VarType.INTEGER, "2");
                    break;
                case ByteCodeParser.opc_fconst_2:
                    smapper.assign(out, VarType.FLOAT, "2");
                    break;
                case ByteCodeParser.opc_iconst_3:
                    smapper.assign(out, VarType.INTEGER, "3");
                    break;
                case ByteCodeParser.opc_iconst_4:
                    smapper.assign(out, VarType.INTEGER, "4");
                    break;
                case ByteCodeParser.opc_iconst_5:
                    smapper.assign(out, VarType.INTEGER, "5");
                    break;
                case ByteCodeParser.opc_ldc:
                    {
                        int indx = ByteCodeToJavaScript.readUByte(byteCodes, ++i);
                        String v = byteCodeToJavaScript.encodeConstant(out, indx);
                        int type = VarType.fromConstantType(jc.getTag(indx));
                        smapper.assign(out, type, v);
                        break;
                    }
                case ByteCodeParser.opc_ldc_w:
                case ByteCodeParser.opc_ldc2_w:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        String v = byteCodeToJavaScript.encodeConstant(out, indx);
                        int type = VarType.fromConstantType(jc.getTag(indx));
                        if (type == VarType.LONG) {
                            final Long lv = new Long(v);
                            final int low = (int) (lv.longValue() & -1);
                            final int hi = (int) (lv.longValue() >> 32);
                            if (hi == 0) {
                                smapper.assign(out, VarType.LONG, "0x" + Integer.toHexString(low));
                            } else {
                                smapper.assign(out, VarType.LONG, "0x" + Integer.toHexString(hi) + ".next32(0x" + Integer.toHexString(low) + ")");
                            }
                        } else {
                            smapper.assign(out, type, v);
                        }
                        break;
                    }
                case ByteCodeParser.opc_lcmp:
                    smapper.replace(out, VarType.INTEGER, numbers.compare64(), smapper.popL(), smapper.getL(0));
                    break;
                case ByteCodeParser.opc_fcmpl:
                case ByteCodeParser.opc_fcmpg:
                    ByteCodeToJavaScript.emit(out, smapper, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);", smapper.popF(), smapper.popF(), smapper.pushI());
                    break;
                case ByteCodeParser.opc_dcmpl:
                case ByteCodeParser.opc_dcmpg:
                    ByteCodeToJavaScript.emit(out, smapper, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);", smapper.popD(), smapper.popD(), smapper.pushI());
                    break;
                case ByteCodeParser.opc_if_acmpeq:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popA(), smapper.popA(), "===", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_acmpne:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popA(), smapper.popA(), "!==", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpeq:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), "==", topMostLabel);
                    break;
                case ByteCodeParser.opc_ifeq:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) == 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifne:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) != 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_iflt:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) < 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifle:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) <= 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifgt:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) > 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifge:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) >= 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifnonnull:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) !== null) ", smapper.popA(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifnull:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(out, smapper, "if ((@1) === null) ", smapper.popA(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_if_icmpne:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), "!=", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmplt:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), "<", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmple:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), "<=", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpgt:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), ">", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpge:
                    i = byteCodeToJavaScript.generateIf(out, smapper, byteCodes, i, smapper.popI(), smapper.popI(), ">=", topMostLabel);
                    break;
                case ByteCodeParser.opc_goto:
                    {
                        smapper.flush(out);
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.goTo(out, i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_lookupswitch:
                    {
                        i = byteCodeToJavaScript.generateLookupSwitch(out, i, byteCodes, smapper, topMostLabel);
                        break;
                    }
                case ByteCodeParser.opc_tableswitch:
                    {
                        i = byteCodeToJavaScript.generateTableSwitch(out, i, byteCodes, smapper, topMostLabel);
                        break;
                    }
                case ByteCodeParser.opc_invokeinterface:
                    {
                        i = invokeVirtualMethod(out, byteCodes, i, smapper) + 2;
                        break;
                    }
                case ByteCodeParser.opc_invokevirtual:
                    i = invokeVirtualMethod(out, byteCodes, i, smapper);
                    break;
                case ByteCodeParser.opc_invokespecial:
                    i = invokeStaticMethod(out, byteCodes, i, smapper, false);
                    break;
                case ByteCodeParser.opc_invokestatic:
                    i = invokeStaticMethod(out, byteCodes, i, smapper, true);
                    break;
                case ByteCodeParser.opc_invokedynamic:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        ByteCodeToJavaScript.println("invoke dynamic: " + indx);
                        ByteCodeParser.CPX2 c2 = jc.getCpoolEntry(indx);
                        ByteCodeParser.BootMethodData bm = jc.getBootMethod(c2.cpx1);
                        ByteCodeParser.CPX2 methodHandle = jc.getCpoolEntry(bm.method);
                        ByteCodeToJavaScript.println("  type: " + methodHandle.cpx1);
                        String[] mi = jc.getFieldInfoName(methodHandle.cpx2);
                        String mcn = ByteCodeToJavaScript.mangleClassName(mi[0]);
                        char[] returnType = {'V'};
                        StringBuilder cnt = new StringBuilder();
                        String mn = ByteCodeToJavaScript.findMethodName(mi, cnt, returnType);
                        StringBuilder sb = new StringBuilder();
                        sb.append("We don't handle invokedynamic, need to preprocess ahead-of-time:\n");
                        sb.append("  mi[0]: ").append(mi[0]).append("\n");
                        sb.append("  mi[1]: ").append(mi[1]).append("\n");
                        sb.append("  mi[2]: ").append(mi[2]).append("\n");
                        sb.append("  mn   : ").append(mn).append("\n");
                        sb.append("  name and type: ").append(jc.stringValue(c2.cpx2, true)).append("\n");
                        throw new IOException(sb.toString());
                        /*
                        CPX2 nameAndType = jc.getCpoolEntry(c2.cpx2);
                        String type = jc.StringValue(nameAndType.cpx2);
                        String object = accessClass(mcn) + "(false)";
                        if (mn.startsWith("cons_")) {
                        object += ".constructor";
                        }
                        append("var metHan = ");
                        append(accessStaticMethod(object, mn, mi));
                        append('(');
                        String lookup = accessClass("java_lang_invoke_MethodHandles") + "(false).findFor__Ljava_lang_invoke_MethodHandles$Lookup_2Ljava_lang_Class_2(CLS.$class)";
                        append(lookup);
                        append(", '").append(mi[1]).append("', ");
                        String methodType = accessClass("java_lang_invoke_MethodType") + "(false).fromMethodDescriptorString__Ljava_lang_invoke_MethodType_2Ljava_lang_String_2Ljava_lang_ClassLoader_2(";
                        append(methodType).append("'").append(type).append("', null)");
                        //                    if (numArguments > 0) {
                        //                        append(vars[0]);
                        //                        for (int j = 1; j < numArguments; ++j) {
                        //                            append(", ");
                        //                            append(vars[j]);
                        //                        }
                        //                    }
                        append(");");
                        emit(smapper, this, "throw 'Invoke dynamic: ' + @1 + ': ' + metHan;", "" + indx);
                        i += 4;
                        break;
                         */
                    }
                case ByteCodeParser.opc_new:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String ci = jc.getClassName(indx);
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = new @2;", smapper.pushA(), byteCodeToJavaScript.accessClass(ByteCodeToJavaScript.mangleClassName(ci)));
                        byteCodeToJavaScript.addReference(out, ci);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_newarray:
                    int atype = ByteCodeToJavaScript.readUByte(byteCodes, ++i);
                    byteCodeToJavaScript.generateNewArray(out, atype, smapper);
                    break;
                case ByteCodeParser.opc_anewarray:
                    {
                        int type = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        byteCodeToJavaScript.generateANewArray(out, type, smapper);
                        break;
                    }
                case ByteCodeParser.opc_multianewarray:
                    {
                        int type = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        i = byteCodeToJavaScript.generateMultiANewArray(out, type, byteCodes, i, smapper);
                        break;
                    }
                case ByteCodeParser.opc_arraylength:
                    smapper.replace(out, VarType.INTEGER, "(@1).length", smapper.getA(0));
                    break;
                case ByteCodeParser.opc_lastore:
                    ByteCodeToJavaScript.emit(out, smapper, "Array.at(@3, @2, @1);", smapper.popL(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_fastore:
                    ByteCodeToJavaScript.emit(out, smapper, "Array.at(@3, @2, @1);", smapper.popF(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_dastore:
                    ByteCodeToJavaScript.emit(out, smapper, "Array.at(@3, @2, @1);", smapper.popD(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_aastore:
                    ByteCodeToJavaScript.emit(out, smapper, "Array.at(@3, @2, @1);", smapper.popA(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_iastore:
                case ByteCodeParser.opc_bastore:
                case ByteCodeParser.opc_castore:
                case ByteCodeParser.opc_sastore:
                    ByteCodeToJavaScript.emit(out, smapper, "Array.at(@3, @2, @1);", smapper.popI(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_laload:
                    smapper.replace(out, VarType.LONG, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_faload:
                    smapper.replace(out, VarType.FLOAT, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_daload:
                    smapper.replace(out, VarType.DOUBLE, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_aaload:
                    smapper.replace(out, VarType.REFERENCE, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_iaload:
                case ByteCodeParser.opc_baload:
                case ByteCodeParser.opc_caload:
                case ByteCodeParser.opc_saload:
                    smapper.replace(out, VarType.INTEGER, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_pop:
                case ByteCodeParser.opc_pop2:
                    smapper.pop(1);
                    byteCodeToJavaScript.debug(out, "/* pop */");
                    break;
                case ByteCodeParser.opc_dup:
                    {
                        final Variable v = smapper.get(0);
                        if (smapper.isDirty()) {
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", smapper.pushT(v.getType()), v);
                        } else {
                            smapper.assign(out, v.getType(), v);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup2:
                    {
                        final Variable vi1 = smapper.get(0);
                        if (vi1.isCategory2()) {
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2;", smapper.pushT(vi1.getType()), vi1);
                        } else {
                            final Variable vi2 = smapper.get(1);
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4;", smapper.pushT(vi2.getType()), vi2, smapper.pushT(vi1.getType()), vi1);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup_x1:
                    {
                        final Variable vi1 = smapper.pop(out);
                        final Variable vi2 = smapper.pop(out);
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());
                        ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        break;
                    }
                case ByteCodeParser.opc_dup2_x1:
                    {
                        final Variable vi1 = smapper.pop(out);
                        final Variable vi2 = smapper.pop(out);
                        if (vi1.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop(out);
                            final Variable vo5 = smapper.pushT(vi2.getType());
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6,", vo1, vi1, vo2, vi2, vo3, vi3);
                            ByteCodeToJavaScript.emit(out, smapper, " @1 = @2, @3 = @4;", vo4, vo1, vo5, vo2);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup_x2:
                    {
                        final Variable vi1 = smapper.pop(out);
                        final Variable vi2 = smapper.pop(out);
                        if (vi2.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop(out);
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup2_x2:
                    {
                        final Variable vi1 = smapper.pop(out);
                        final Variable vi2 = smapper.pop(out);
                        if (vi1.isCategory2()) {
                            if (vi2.isCategory2()) {
                                final Variable vo3 = smapper.pushT(vi1.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                            } else {
                                final Variable vi3 = smapper.pop(out);
                                final Variable vo4 = smapper.pushT(vi1.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                            }
                        } else {
                            final Variable vi3 = smapper.pop(out);
                            if (vi3.isCategory2()) {
                                final Variable vo5 = smapper.pushT(vi2.getType());
                                final Variable vo4 = smapper.pushT(vi1.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6,", vo1, vi1, vo2, vi2, vo3, vi3);
                                ByteCodeToJavaScript.emit(out, smapper, " @1 = @2, @3 = @4;", vo4, vo1, vo5, vo2);
                            } else {
                                final Variable vi4 = smapper.pop(out);
                                final Variable vo6 = smapper.pushT(vi2.getType());
                                final Variable vo5 = smapper.pushT(vi1.getType());
                                final Variable vo4 = smapper.pushT(vi4.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8,", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vi4);
                                ByteCodeToJavaScript.emit(out, smapper, " @1 = @2, @3 = @4;", vo5, vo1, vo6, vo2);
                            }
                        }
                        break;
                    }
                case ByteCodeParser.opc_swap:
                    {
                        final Variable vi1 = smapper.get(0);
                        final Variable vi2 = smapper.get(1);
                        if (vi1.getType() == vi2.getType()) {
                            final Variable tmp = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(out, smapper, "var @1 = @2, @2 = @3, @3 = @1;", tmp, vi1, vi2);
                            smapper.pop(1);
                        } else {
                            smapper.pop(2);
                            smapper.pushT(vi1.getType());
                            smapper.pushT(vi2.getType());
                        }
                        break;
                    }
                case ByteCodeParser.opc_bipush:
                    smapper.assign(out, VarType.INTEGER, "(" + Integer.toString(byteCodes[++i]) + ")");
                    break;
                case ByteCodeParser.opc_sipush:
                    smapper.assign(out, VarType.INTEGER, "(" + Integer.toString(ByteCodeToJavaScript.readShortArg(byteCodes, i)) + ")");
                    i += 2;
                    break;
                case ByteCodeParser.opc_getfield:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String[] fi = jc.getFieldInfoName(indx);
                        final int type = VarType.fromFieldType(fi[2].charAt(0));
                        ByteCodeParser.FieldData field = byteCodeToJavaScript.findField(fi);
                        if (field == null) {
                            final String mangleClass = ByteCodeToJavaScript.mangleClassName(fi[0]);
                            final String mangleClassAccess = byteCodeToJavaScript.accessClassFalse(mangleClass);
                            smapper.replace(out, type, "@2.call(@1)", smapper.getA(0), byteCodeToJavaScript.accessField(mangleClassAccess, null, fi));
                        } else {
                            final String fieldOwner = ByteCodeToJavaScript.mangleClassName(field.cls.getClassName());
                            smapper.replace(out, type, "@1@2", smapper.getA(0), byteCodeToJavaScript.accessField(fieldOwner, field, fi));
                        }
                        i += 2;
                        byteCodeToJavaScript.addReference(out, fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_putfield:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String[] fi = jc.getFieldInfoName(indx);
                        final int type = VarType.fromFieldType(fi[2].charAt(0));
                        ByteCodeParser.FieldData field = byteCodeToJavaScript.findField(fi);
                        if (field == null) {
                            final String mangleClass = ByteCodeToJavaScript.mangleClassName(fi[0]);
                            final String mangleClassAccess = byteCodeToJavaScript.accessClassFalse(mangleClass);
                            ByteCodeToJavaScript.emit(out, smapper, "@3.call(@2, @1);", smapper.popT(type), smapper.popA(), byteCodeToJavaScript.accessField(mangleClassAccess, null, fi));
                        } else {
                            final String fieldOwner = ByteCodeToJavaScript.mangleClassName(field.cls.getClassName());
                            ByteCodeToJavaScript.emit(out, smapper, "@2@3 = @1;", smapper.popT(type), smapper.popA(), byteCodeToJavaScript.accessField(fieldOwner, field, fi));
                        }
                        i += 2;
                        byteCodeToJavaScript.addReference(out, fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_getstatic:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String[] fi = jc.getFieldInfoName(indx);
                        final int type = VarType.fromFieldType(fi[2].charAt(0));
                        String ac = byteCodeToJavaScript.accessClassFalse(ByteCodeToJavaScript.mangleClassName(fi[0]));
                        ByteCodeParser.FieldData field = byteCodeToJavaScript.findField(fi);
                        String af = byteCodeToJavaScript.accessField(ac, field, fi);
                        smapper.assign(out, type, af + "()");
                        i += 2;
                        byteCodeToJavaScript.addReference(out, fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_putstatic:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String[] fi = jc.getFieldInfoName(indx);
                        final int type = VarType.fromFieldType(fi[2].charAt(0));
                        ByteCodeToJavaScript.emit(out, smapper, "@1._@2(@3);", byteCodeToJavaScript.accessClassFalse(ByteCodeToJavaScript.mangleClassName(fi[0])), fi[1], smapper.popT(type));
                        i += 2;
                        byteCodeToJavaScript.addReference(out, fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_checkcast:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        byteCodeToJavaScript.generateCheckcast(out, indx, smapper);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_instanceof:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        byteCodeToJavaScript.generateInstanceOf(out, indx, smapper);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_athrow:
                    {
                        final CharSequence v = smapper.popA();
                        smapper.clear();
                        ByteCodeToJavaScript.emit(out, smapper, "{ var @1 = @2; throw @2; }", smapper.pushA(), v);
                        break;
                    }
                case ByteCodeParser.opc_monitorenter:
                    {
                        byteCodeToJavaScript.debug(null, "/* monitor enter */");
                        smapper.popA();
                        break;
                    }
                case ByteCodeParser.opc_monitorexit:
                    {
                        byteCodeToJavaScript.debug(null, "/* monitor exit */");
                        smapper.popA();
                        break;
                    }
                case ByteCodeParser.opc_wide:
                    wide = true;
                    break;
                default:
                    {
                        wide = false;
                        ByteCodeToJavaScript.emit(out, smapper, "throw 'unknown bytecode @1';", Integer.toString(c));
                    }
            }
            if (byteCodeToJavaScript.debug(out, " //")) {
                byteCodeToJavaScript.generateByteCodeComment(out, prev, i, byteCodes);
            }
            if (modified) {
                out.append("\n");
            }
        }
        if (previousTrap != null) {
            byteCodeToJavaScript.generateCatch(out, previousTrap, byteCodes.length, topMostLabel);
        }
        if (didBranches) {
            out.append("\n    }\n");
        }
        while (openBraces-- > 0) {
            out.append('}');
        }
    }

    int invokeVirtualMethod(Appendable out, byte[] byteCodes, int i, final StackMapper mapper) throws IOException {
        int methodIndex = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = {'V'};
        StringBuilder cnt = new StringBuilder();
        String mn = ByteCodeToJavaScript.findMethodName(mi, cnt, returnType);
        final int numArguments = cnt.length() + 1;
        final CharSequence[] vars = new CharSequence[numArguments];
        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.popValue();
        }
        if (returnType[0] != 'V') {
            mapper.flush(out);
            out.append("var ").append(mapper.pushT(VarType.fromFieldType(returnType[0]))).append(" = ");
        }

        boolean callbacksFinished = beginCall(mi, vars, false);
        out.append(byteCodeToJavaScript.accessVirtualMethod(vars[0].toString(), mn, mi, numArguments));
        String sep = "";
        for (int j = 1; j < numArguments; ++j) {
            out.append(sep);
            out.append(vars[j]);
            sep = ", ";
        }
        out.append(");");
        i += 2;
        endCall(callbacksFinished);
        return i;
    }

    int invokeStaticMethod(Appendable out, byte[] byteCodes, int i, final StackMapper mapper, boolean isStatic) throws IOException {
        int methodIndex = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = {'V'};
        StringBuilder cnt = new StringBuilder();
        String mn = ByteCodeToJavaScript.findMethodName(mi, cnt, returnType);
        final int numArguments = isStatic ? cnt.length() : cnt.length() + 1;
        final CharSequence[] vars = new CharSequence[numArguments];
        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.popValue();
        }
        if (("newUpdater__Ljava_util_concurrent_atomic_AtomicIntegerFieldUpdater_2Ljava_lang_Class_2Ljava_lang_String_2".equals(mn) && "java/util/concurrent/atomic/AtomicIntegerFieldUpdater".equals(mi[0])) || ("newUpdater__Ljava_util_concurrent_atomic_AtomicLongFieldUpdater_2Ljava_lang_Class_2Ljava_lang_String_2".equals(mn) && "java/util/concurrent/atomic/AtomicLongFieldUpdater".equals(mi[0]))) {
            if (vars[1] instanceof String) {
                String field = vars[1].toString();
                if (field.length() > 2 && field.charAt(0) == '"' && field.charAt(field.length() - 1) == '"') {
                    vars[1] = "c._" + field.substring(1, field.length() - 1);
                }
            }
        }
        if ("newUpdater__Ljava_util_concurrent_atomic_AtomicReferenceFieldUpdater_2Ljava_lang_Class_2Ljava_lang_Class_2Ljava_lang_String_2".equals(mn) && "java/util/concurrent/atomic/AtomicReferenceFieldUpdater".equals(mi[0])) {
            if (vars[1] instanceof String) {
                String field = vars[2].toString();
                if (field.length() > 2 && field.charAt(0) == '"' && field.charAt(field.length() - 1) == '"') {
                    vars[2] = "c._" + field.substring(1, field.length() - 1);
                }
            }
        }
        if (returnType[0] != 'V') {
            mapper.flush(out);
            out.append("var ").append(mapper.pushT(VarType.fromFieldType(returnType[0]))).append(" = ");
        }
        boolean callbacksFinished = beginCall(mi, vars, true);
        final String in = mi[0];
        String mcn = ByteCodeToJavaScript.mangleClassName(in);
        String object = byteCodeToJavaScript.accessClassFalse(mcn);
        if (mn.startsWith("cons_")) {
            object += ".constructor";
        }
        out.append(byteCodeToJavaScript.accessStaticMethod(object, mn, mi));
        if (isStatic) {
            out.append('(');
        } else {
            out.append(".call(");
        }
        if (numArguments > 0) {
            out.append(vars[0]);
            for (int j = 1; j < numArguments; ++j) {
                out.append(", ");
                out.append(vars[j]);
            }
        }
        out.append(");");
        i += 2;
        endCall(callbacksFinished);
        byteCodeToJavaScript.addReference(out, in);
        return i;
    }

    protected boolean beginCall(String[] mi, CharSequence[] vars, boolean isStatic) throws IOException {
        return false;
    }

    protected void endCall(boolean callbacksFinished) throws IOException {
    }
}
