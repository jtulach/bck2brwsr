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

class LoopCode {
    private final ByteCodeToJavaScript out;
    private final NumberOperations numbers;
    private final ClassData jc;

    LoopCode(ByteCodeToJavaScript proccessor, NumberOperations numbers, ClassData jc) {
        this.out = proccessor;
        this.jc = jc;
        this.numbers = numbers;
    }

    void loopCode(final ByteCodeParser.StackMapIterator stackMapIterator, final byte[] byteCodes, ByteCodeParser.TrapDataIterator trap, final StackMapper smapper, final LocalsMapper lmapper, ByteCodeToJavaScript byteCodeToJavaScript) throws IllegalStateException, NumberFormatException, IOException {
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
            byteCodeToJavaScript.append("\n  var gt = 0;\n");
        }
        int openBraces = 0;
        int topMostLabel = 0;
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            byteCodeToJavaScript.outChanged = false;
            stackMapIterator.advanceTo(i);
            boolean changeInCatch = trap.advanceTo(i);
            if (changeInCatch || lastStackFrame != stackMapIterator.getFrameIndex()) {
                if (previousTrap != null) {
                    byteCodeToJavaScript.generateCatch(previousTrap, i, topMostLabel);
                    previousTrap = null;
                }
            }
            if (lastStackFrame != stackMapIterator.getFrameIndex()) {
                smapper.flush(byteCodeToJavaScript);
                if (i != 0) {
                    byteCodeToJavaScript.append("    }\n");
                }
                if (openBraces > 64) {
                    for (int c = 0; c < 64; c++) {
                        byteCodeToJavaScript.append("break;}\n");
                    }
                    openBraces = 1;
                    topMostLabel = i;
                }
                lastStackFrame = stackMapIterator.getFrameIndex();
                lmapper.syncWithFrameLocals(stackMapIterator.getFrameLocals());
                smapper.syncWithFrameStack(stackMapIterator.getFrameStack());
                byteCodeToJavaScript.append("    X_" + i).append(": for (;;) { IF: if (gt <= " + i + ") {\n");
                openBraces++;
                changeInCatch = true;
            } else {
                byteCodeToJavaScript.debug("    /* " + i + " */ ");
            }
            if (changeInCatch && trap.useTry()) {
                byteCodeToJavaScript.append("try {");
                previousTrap = trap.current();
            }
            final int c = ByteCodeToJavaScript.readUByte(byteCodes, i);
            switch (c) {
                case ByteCodeParser.opc_aload_0:
                    smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, lmapper.getA(0));
                    break;
                case ByteCodeParser.opc_iload_0:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, lmapper.getI(0));
                    break;
                case ByteCodeParser.opc_lload_0:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, lmapper.getL(0));
                    break;
                case ByteCodeParser.opc_fload_0:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, lmapper.getF(0));
                    break;
                case ByteCodeParser.opc_dload_0:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, lmapper.getD(0));
                    break;
                case ByteCodeParser.opc_aload_1:
                    smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, lmapper.getA(1));
                    break;
                case ByteCodeParser.opc_iload_1:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, lmapper.getI(1));
                    break;
                case ByteCodeParser.opc_lload_1:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, lmapper.getL(1));
                    break;
                case ByteCodeParser.opc_fload_1:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, lmapper.getF(1));
                    break;
                case ByteCodeParser.opc_dload_1:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, lmapper.getD(1));
                    break;
                case ByteCodeParser.opc_aload_2:
                    smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, lmapper.getA(2));
                    break;
                case ByteCodeParser.opc_iload_2:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, lmapper.getI(2));
                    break;
                case ByteCodeParser.opc_lload_2:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, lmapper.getL(2));
                    break;
                case ByteCodeParser.opc_fload_2:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, lmapper.getF(2));
                    break;
                case ByteCodeParser.opc_dload_2:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, lmapper.getD(2));
                    break;
                case ByteCodeParser.opc_aload_3:
                    smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, lmapper.getA(3));
                    break;
                case ByteCodeParser.opc_iload_3:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, lmapper.getI(3));
                    break;
                case ByteCodeParser.opc_lload_3:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, lmapper.getL(3));
                    break;
                case ByteCodeParser.opc_fload_3:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, lmapper.getF(3));
                    break;
                case ByteCodeParser.opc_dload_3:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, lmapper.getD(3));
                    break;
                case ByteCodeParser.opc_iload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(byteCodeToJavaScript, VarType.INTEGER, lmapper.getI(indx));
                        break;
                    }
                case ByteCodeParser.opc_lload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(byteCodeToJavaScript, VarType.LONG, lmapper.getL(indx));
                        break;
                    }
                case ByteCodeParser.opc_fload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(byteCodeToJavaScript, VarType.FLOAT, lmapper.getF(indx));
                        break;
                    }
                case ByteCodeParser.opc_dload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, lmapper.getD(indx));
                        break;
                    }
                case ByteCodeParser.opc_aload:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, lmapper.getA(indx));
                        break;
                    }
                case ByteCodeParser.opc_istore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setI(indx), smapper.popI());
                        break;
                    }
                case ByteCodeParser.opc_lstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setL(indx), smapper.popL());
                        break;
                    }
                case ByteCodeParser.opc_fstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setF(indx), smapper.popF());
                        break;
                    }
                case ByteCodeParser.opc_dstore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setD(indx), smapper.popD());
                        break;
                    }
                case ByteCodeParser.opc_astore:
                    {
                        ++i;
                        final int indx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setA(indx), smapper.popA());
                        break;
                    }
                case ByteCodeParser.opc_astore_0:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setA(0), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_0:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setI(0), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_0:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setL(0), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_0:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setF(0), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_0:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setD(0), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_1:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setA(1), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_1:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_1:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_1:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_1:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_2:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setA(2), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_2:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setI(2), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_2:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setL(2), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_2:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setF(2), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_2:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setD(2), smapper.popD());
                    break;
                case ByteCodeParser.opc_astore_3:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setA(3), smapper.popA());
                    break;
                case ByteCodeParser.opc_istore_3:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setI(3), smapper.popI());
                    break;
                case ByteCodeParser.opc_lstore_3:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setL(3), smapper.popL());
                    break;
                case ByteCodeParser.opc_fstore_3:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setF(3), smapper.popF());
                    break;
                case ByteCodeParser.opc_dstore_3:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", lmapper.setD(3), smapper.popD());
                    break;
                case ByteCodeParser.opc_iadd:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(((@1) + (@2)) | 0)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ladd:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.add64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fadd:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1 + @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dadd:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1 + @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_isub:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(((@1) - (@2)) | 0)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lsub:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.sub64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fsub:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1 - @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dsub:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1 - @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_imul:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, numbers.mul32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lmul:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.mul64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fmul:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1 * @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_dmul:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1 * @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_idiv:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, numbers.div32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ldiv:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.div64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_fdiv:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1 / @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_ddiv:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1 / @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_irem:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, numbers.mod32(), smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lrem:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.mod64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_frem:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1 % @2)", smapper.getF(1), smapper.popF());
                    break;
                case ByteCodeParser.opc_drem:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1 % @2)", smapper.getD(1), smapper.popD());
                    break;
                case ByteCodeParser.opc_iand:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 & @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_land:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.and64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ior:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 | @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lor:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.or64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ixor:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 ^ @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lxor:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.xor64(), smapper.getL(1), smapper.popL());
                    break;
                case ByteCodeParser.opc_ineg:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(-(@1) | 0)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_lneg:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.neg64(), smapper.getL(0));
                    break;
                case ByteCodeParser.opc_fneg:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(-@1)", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_dneg:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(-@1)", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_ishl:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 << @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lshl:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.shl64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_ishr:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 >> @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lshr:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.shr64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_iushr:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1 >>> @2)", smapper.getI(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_lushr:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, numbers.ushr64(), smapper.getL(1), smapper.popI());
                    break;
                case ByteCodeParser.opc_iinc:
                    {
                        ++i;
                        final int varIndx = wide ? ByteCodeToJavaScript.readUShort(byteCodes, i++) : ByteCodeToJavaScript.readUByte(byteCodes, i);
                        ++i;
                        final int incrBy = wide ? ByteCodeToJavaScript.readShort(byteCodes, i++) : byteCodes[i];
                        wide = false;
                        if (incrBy == 1) {
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "@1++;", lmapper.getI(varIndx));
                        } else {
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "@1 += @2;", lmapper.getI(varIndx), Integer.toString(incrBy));
                        }
                        break;
                    }
                case ByteCodeParser.opc_return:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return;");
                    break;
                case ByteCodeParser.opc_ireturn:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return @1;", smapper.popI());
                    break;
                case ByteCodeParser.opc_lreturn:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return @1;", smapper.popL());
                    break;
                case ByteCodeParser.opc_freturn:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return @1;", smapper.popF());
                    break;
                case ByteCodeParser.opc_dreturn:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return @1;", smapper.popD());
                    break;
                case ByteCodeParser.opc_areturn:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "return @1;", smapper.popA());
                    break;
                case ByteCodeParser.opc_i2l:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2f:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2d:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "@1", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_l2i:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "((@1) | 0)", smapper.getL(0));
                    break;
            // max int check?
                case ByteCodeParser.opc_l2f:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@1).toFP()", smapper.getL(0));
                    break;
                case ByteCodeParser.opc_l2d:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@1).toFP()", smapper.getL(0));
                    break;
                case ByteCodeParser.opc_f2d:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "@1", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_d2f:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "@1", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_f2i:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "((@1) | 0)", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_f2l:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, "(@1).toLong()", smapper.getF(0));
                    break;
                case ByteCodeParser.opc_d2i:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "((@1)| 0)", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_d2l:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, "(@1).toLong()", smapper.getD(0));
                    break;
                case ByteCodeParser.opc_i2b:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(((@1) << 24) >> 24)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_i2c:
                case ByteCodeParser.opc_i2s:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(((@1) << 16) >> 16)", smapper.getI(0));
                    break;
                case ByteCodeParser.opc_aconst_null:
                    smapper.assign(byteCodeToJavaScript, VarType.REFERENCE, "null");
                    break;
                case ByteCodeParser.opc_iconst_m1:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "-1");
                    break;
                case ByteCodeParser.opc_iconst_0:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "0");
                    break;
                case ByteCodeParser.opc_dconst_0:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, "0");
                    break;
                case ByteCodeParser.opc_lconst_0:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, "0");
                    break;
                case ByteCodeParser.opc_fconst_0:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, "0");
                    break;
                case ByteCodeParser.opc_iconst_1:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "1");
                    break;
                case ByteCodeParser.opc_lconst_1:
                    smapper.assign(byteCodeToJavaScript, VarType.LONG, "1");
                    break;
                case ByteCodeParser.opc_fconst_1:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, "1");
                    break;
                case ByteCodeParser.opc_dconst_1:
                    smapper.assign(byteCodeToJavaScript, VarType.DOUBLE, "1");
                    break;
                case ByteCodeParser.opc_iconst_2:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "2");
                    break;
                case ByteCodeParser.opc_fconst_2:
                    smapper.assign(byteCodeToJavaScript, VarType.FLOAT, "2");
                    break;
                case ByteCodeParser.opc_iconst_3:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "3");
                    break;
                case ByteCodeParser.opc_iconst_4:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "4");
                    break;
                case ByteCodeParser.opc_iconst_5:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "5");
                    break;
                case ByteCodeParser.opc_ldc:
                    {
                        int indx = ByteCodeToJavaScript.readUByte(byteCodes, ++i);
                        String v = byteCodeToJavaScript.encodeConstant(indx);
                        int type = VarType.fromConstantType(jc.getTag(indx));
                        smapper.assign(byteCodeToJavaScript, type, v);
                        break;
                    }
                case ByteCodeParser.opc_ldc_w:
                case ByteCodeParser.opc_ldc2_w:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        String v = byteCodeToJavaScript.encodeConstant(indx);
                        int type = VarType.fromConstantType(jc.getTag(indx));
                        if (type == VarType.LONG) {
                            final Long lv = new Long(v);
                            final int low = (int) (lv.longValue() & -1);
                            final int hi = (int) (lv.longValue() >> 32);
                            if (hi == 0) {
                                smapper.assign(byteCodeToJavaScript, VarType.LONG, "0x" + Integer.toHexString(low));
                            } else {
                                smapper.assign(byteCodeToJavaScript, VarType.LONG, "0x" + Integer.toHexString(hi) + ".next32(0x" + Integer.toHexString(low) + ")");
                            }
                        } else {
                            smapper.assign(byteCodeToJavaScript, type, v);
                        }
                        break;
                    }
                case ByteCodeParser.opc_lcmp:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, numbers.compare64(), smapper.popL(), smapper.getL(0));
                    break;
                case ByteCodeParser.opc_fcmpl:
                case ByteCodeParser.opc_fcmpg:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);", smapper.popF(), smapper.popF(), smapper.pushI());
                    break;
                case ByteCodeParser.opc_dcmpl:
                case ByteCodeParser.opc_dcmpg:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);", smapper.popD(), smapper.popD(), smapper.pushI());
                    break;
                case ByteCodeParser.opc_if_acmpeq:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popA(), smapper.popA(), "===", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_acmpne:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popA(), smapper.popA(), "!==", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpeq:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), "==", topMostLabel);
                    break;
                case ByteCodeParser.opc_ifeq:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) == 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifne:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) != 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_iflt:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) < 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifle:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) <= 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifgt:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) > 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifge:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) >= 0) ", smapper.popI(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifnonnull:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) !== null) ", smapper.popA(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_ifnull:
                    {
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.emitIf(smapper, byteCodeToJavaScript, "if ((@1) === null) ", smapper.popA(), i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_if_icmpne:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), "!=", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmplt:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), "<", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmple:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), "<=", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpgt:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), ">", topMostLabel);
                    break;
                case ByteCodeParser.opc_if_icmpge:
                    i = byteCodeToJavaScript.generateIf(smapper, byteCodes, i, smapper.popI(), smapper.popI(), ">=", topMostLabel);
                    break;
                case ByteCodeParser.opc_goto:
                    {
                        smapper.flush(byteCodeToJavaScript);
                        int indx = i + ByteCodeToJavaScript.readShortArg(byteCodes, i);
                        ByteCodeToJavaScript.goTo(byteCodeToJavaScript, i, indx, topMostLabel);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_lookupswitch:
                    {
                        i = byteCodeToJavaScript.generateLookupSwitch(i, byteCodes, smapper, topMostLabel);
                        break;
                    }
                case ByteCodeParser.opc_tableswitch:
                    {
                        i = byteCodeToJavaScript.generateTableSwitch(i, byteCodes, smapper, topMostLabel);
                        break;
                    }
                case ByteCodeParser.opc_invokeinterface:
                    {
                        i = byteCodeToJavaScript.invokeVirtualMethod(byteCodes, i, smapper) + 2;
                        break;
                    }
                case ByteCodeParser.opc_invokevirtual:
                    i = byteCodeToJavaScript.invokeVirtualMethod(byteCodes, i, smapper);
                    break;
                case ByteCodeParser.opc_invokespecial:
                    i = byteCodeToJavaScript.invokeStaticMethod(byteCodes, i, smapper, false);
                    break;
                case ByteCodeParser.opc_invokestatic:
                    i = byteCodeToJavaScript.invokeStaticMethod(byteCodes, i, smapper, true);
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
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = new @2;", smapper.pushA(), byteCodeToJavaScript.accessClass(ByteCodeToJavaScript.mangleClassName(ci)));
                        byteCodeToJavaScript.addReference(ci);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_newarray:
                    int atype = ByteCodeToJavaScript.readUByte(byteCodes, ++i);
                    byteCodeToJavaScript.generateNewArray(atype, smapper);
                    break;
                case ByteCodeParser.opc_anewarray:
                    {
                        int type = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        byteCodeToJavaScript.generateANewArray(type, smapper);
                        break;
                    }
                case ByteCodeParser.opc_multianewarray:
                    {
                        int type = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        i += 2;
                        i = byteCodeToJavaScript.generateMultiANewArray(type, byteCodes, i, smapper);
                        break;
                    }
                case ByteCodeParser.opc_arraylength:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@1).length", smapper.getA(0));
                    break;
                case ByteCodeParser.opc_lastore:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "Array.at(@3, @2, @1);", smapper.popL(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_fastore:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "Array.at(@3, @2, @1);", smapper.popF(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_dastore:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "Array.at(@3, @2, @1);", smapper.popD(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_aastore:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "Array.at(@3, @2, @1);", smapper.popA(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_iastore:
                case ByteCodeParser.opc_bastore:
                case ByteCodeParser.opc_castore:
                case ByteCodeParser.opc_sastore:
                    ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "Array.at(@3, @2, @1);", smapper.popI(), smapper.popI(), smapper.popA());
                    break;
                case ByteCodeParser.opc_laload:
                    smapper.replace(byteCodeToJavaScript, VarType.LONG, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_faload:
                    smapper.replace(byteCodeToJavaScript, VarType.FLOAT, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_daload:
                    smapper.replace(byteCodeToJavaScript, VarType.DOUBLE, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_aaload:
                    smapper.replace(byteCodeToJavaScript, VarType.REFERENCE, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_iaload:
                case ByteCodeParser.opc_baload:
                case ByteCodeParser.opc_caload:
                case ByteCodeParser.opc_saload:
                    smapper.replace(byteCodeToJavaScript, VarType.INTEGER, "(@2[@1] || Array.at(@2, @1))", smapper.popI(), smapper.getA(0));
                    break;
                case ByteCodeParser.opc_pop:
                case ByteCodeParser.opc_pop2:
                    smapper.pop(1);
                    byteCodeToJavaScript.debug("/* pop */");
                    break;
                case ByteCodeParser.opc_dup:
                    {
                        final Variable v = smapper.get(0);
                        if (smapper.isDirty()) {
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", smapper.pushT(v.getType()), v);
                        } else {
                            smapper.assign(byteCodeToJavaScript, v.getType(), v);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup2:
                    {
                        final Variable vi1 = smapper.get(0);
                        if (vi1.isCategory2()) {
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2;", smapper.pushT(vi1.getType()), vi1);
                        } else {
                            final Variable vi2 = smapper.get(1);
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4;", smapper.pushT(vi2.getType()), vi2, smapper.pushT(vi1.getType()), vi1);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup_x1:
                    {
                        final Variable vi1 = smapper.pop(byteCodeToJavaScript);
                        final Variable vi2 = smapper.pop(byteCodeToJavaScript);
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        break;
                    }
                case ByteCodeParser.opc_dup2_x1:
                    {
                        final Variable vi1 = smapper.pop(byteCodeToJavaScript);
                        final Variable vi2 = smapper.pop(byteCodeToJavaScript);
                        if (vi1.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop(byteCodeToJavaScript);
                            final Variable vo5 = smapper.pushT(vi2.getType());
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6,", vo1, vi1, vo2, vi2, vo3, vi3);
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, " @1 = @2, @3 = @4;", vo4, vo1, vo5, vo2);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup_x2:
                    {
                        final Variable vi1 = smapper.pop(byteCodeToJavaScript);
                        final Variable vi2 = smapper.pop(byteCodeToJavaScript);
                        if (vi2.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop(byteCodeToJavaScript);
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                        }
                        break;
                    }
                case ByteCodeParser.opc_dup2_x2:
                    {
                        final Variable vi1 = smapper.pop(byteCodeToJavaScript);
                        final Variable vi2 = smapper.pop(byteCodeToJavaScript);
                        if (vi1.isCategory2()) {
                            if (vi2.isCategory2()) {
                                final Variable vo3 = smapper.pushT(vi1.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6;", vo1, vi1, vo2, vi2, vo3, vo1);
                            } else {
                                final Variable vi3 = smapper.pop(byteCodeToJavaScript);
                                final Variable vo4 = smapper.pushT(vi1.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                            }
                        } else {
                            final Variable vi3 = smapper.pop(byteCodeToJavaScript);
                            if (vi3.isCategory2()) {
                                final Variable vo5 = smapper.pushT(vi2.getType());
                                final Variable vo4 = smapper.pushT(vi1.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6,", vo1, vi1, vo2, vi2, vo3, vi3);
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, " @1 = @2, @3 = @4;", vo4, vo1, vo5, vo2);
                            } else {
                                final Variable vi4 = smapper.pop(byteCodeToJavaScript);
                                final Variable vo6 = smapper.pushT(vi2.getType());
                                final Variable vo5 = smapper.pushT(vi1.getType());
                                final Variable vo4 = smapper.pushT(vi4.getType());
                                final Variable vo3 = smapper.pushT(vi3.getType());
                                final Variable vo2 = smapper.pushT(vi2.getType());
                                final Variable vo1 = smapper.pushT(vi1.getType());
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8,", vo1, vi1, vo2, vi2, vo3, vi3, vo4, vi4);
                                ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, " @1 = @2, @3 = @4;", vo5, vo1, vo6, vo2);
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
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "var @1 = @2, @2 = @3, @3 = @1;", tmp, vi1, vi2);
                            smapper.pop(1);
                        } else {
                            smapper.pop(2);
                            smapper.pushT(vi1.getType());
                            smapper.pushT(vi2.getType());
                        }
                        break;
                    }
                case ByteCodeParser.opc_bipush:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "(" + Integer.toString(byteCodes[++i]) + ")");
                    break;
                case ByteCodeParser.opc_sipush:
                    smapper.assign(byteCodeToJavaScript, VarType.INTEGER, "(" + Integer.toString(ByteCodeToJavaScript.readShortArg(byteCodes, i)) + ")");
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
                            smapper.replace(byteCodeToJavaScript, type, "@2.call(@1)", smapper.getA(0), byteCodeToJavaScript.accessField(mangleClassAccess, null, fi));
                        } else {
                            final String fieldOwner = ByteCodeToJavaScript.mangleClassName(field.cls.getClassName());
                            smapper.replace(byteCodeToJavaScript, type, "@1@2", smapper.getA(0), byteCodeToJavaScript.accessField(fieldOwner, field, fi));
                        }
                        i += 2;
                        byteCodeToJavaScript.addReference(fi[0]);
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
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "@3.call(@2, @1);", smapper.popT(type), smapper.popA(), byteCodeToJavaScript.accessField(mangleClassAccess, null, fi));
                        } else {
                            final String fieldOwner = ByteCodeToJavaScript.mangleClassName(field.cls.getClassName());
                            ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "@2@3 = @1;", smapper.popT(type), smapper.popA(), byteCodeToJavaScript.accessField(fieldOwner, field, fi));
                        }
                        i += 2;
                        byteCodeToJavaScript.addReference(fi[0]);
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
                        smapper.assign(byteCodeToJavaScript, type, af + "()");
                        i += 2;
                        byteCodeToJavaScript.addReference(fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_putstatic:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        String[] fi = jc.getFieldInfoName(indx);
                        final int type = VarType.fromFieldType(fi[2].charAt(0));
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "@1._@2(@3);", byteCodeToJavaScript.accessClassFalse(ByteCodeToJavaScript.mangleClassName(fi[0])), fi[1], smapper.popT(type));
                        i += 2;
                        byteCodeToJavaScript.addReference(fi[0]);
                        break;
                    }
                case ByteCodeParser.opc_checkcast:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        byteCodeToJavaScript.generateCheckcast(indx, smapper);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_instanceof:
                    {
                        int indx = ByteCodeToJavaScript.readUShortArg(byteCodes, i);
                        byteCodeToJavaScript.generateInstanceOf(indx, smapper);
                        i += 2;
                        break;
                    }
                case ByteCodeParser.opc_athrow:
                    {
                        final CharSequence v = smapper.popA();
                        smapper.clear();
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "{ var @1 = @2; throw @2; }", smapper.pushA(), v);
                        break;
                    }
                case ByteCodeParser.opc_monitorenter:
                    {
                        byteCodeToJavaScript.debug("/* monitor enter */");
                        smapper.popA();
                        break;
                    }
                case ByteCodeParser.opc_monitorexit:
                    {
                        byteCodeToJavaScript.debug("/* monitor exit */");
                        smapper.popA();
                        break;
                    }
                case ByteCodeParser.opc_wide:
                    wide = true;
                    break;
                default:
                    {
                        wide = false;
                        ByteCodeToJavaScript.emit(smapper, byteCodeToJavaScript, "throw 'unknown bytecode @1';", Integer.toString(c));
                    }
            }
            if (byteCodeToJavaScript.debug(" //")) {
                byteCodeToJavaScript.generateByteCodeComment(prev, i, byteCodes);
            }
            if (byteCodeToJavaScript.outChanged) {
                byteCodeToJavaScript.append("\n");
            }
        }
        if (previousTrap != null) {
            byteCodeToJavaScript.generateCatch(previousTrap, byteCodes.length, topMostLabel);
        }
        if (didBranches) {
            byteCodeToJavaScript.append("\n    }\n");
        }
        while (openBraces-- > 0) {
            byteCodeToJavaScript.append('}');
        }
    }
    

}
