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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

@ExtraJavaScript(processByteCode = false, resource="")
final class SourceMapGenerator {
    private final String sourceRoot;
    private final ArrayList<String> sources = new ArrayList<>();
    private final HashMap<String, Integer> sourcesIndices = new HashMap<>();
    private final ArrayList<String> names = new ArrayList<>();
    private final HashMap<String, Integer> namesIndices = new HashMap<>();
    private final StringBuilder mappings = new StringBuilder();
    private final LineCountingAppendable lineCounter;

    private int prevLine = 0, prevColumn = 0, prevSrcFileIndex = 0, prevSrcLine = 0, prevSrcColumn = 0, prevSrcNameIndex = 0;
    private int prevSize = 0;

    SourceMapGenerator(String sourceRoot, LineCountingAppendable lineCounter) {
        this.sourceRoot = sourceRoot;
        this.lineCounter = lineCounter;
    }

    private void setPos() {
        int line = lineCounter.getLineNumber();
        int column = lineCounter.getColumnNumber();

        if (line != prevLine) {
            for (int i = prevLine; i < line; i++)
                mappings.append(';');
            putInt(column);
            prevLine = line;
            prevColumn = column;
        } else if (prevSize == 0) {
            putInt(column);
            prevColumn = column;
        } else {
            mappings.append(',');
            putInt(column - prevColumn);
            prevColumn = column;
        }
    }

    void addItem() {
        setPos();
        prevSize = 1;
    }

    void addItem(String srcFile, int srcLine, int srcColumn) {
        setPos();
        int srcFileIndex = sourceFileToIndex(srcFile);
        putInt(srcFileIndex - prevSrcFileIndex);
        prevSrcFileIndex = srcFileIndex;
        putInt(srcLine - prevSrcLine);
        prevSrcLine = srcLine;
        putInt(srcColumn - prevSrcColumn);
        prevSrcColumn = srcColumn;
        prevSize = 4;
    }

    void extendLastItem(String srcName) {
        switch (prevSize) {
            // fall-throughs!
            case 5:
                mappings.append(',');
            case 0:
                putInt(0); // column
            case 1:
                putInt(0); // srcFile
                putInt(0); // srcLine
                putInt(0); // srcColumn
            case 4:
                break;
            default:
                throw new AssertionError();
        }
        int srcNameIndex = sourceNameToIndex(srcName);
        putInt(srcNameIndex - prevSrcNameIndex);
        prevSrcNameIndex = srcNameIndex;
        prevSize = 5;
    }

    private int sourceFileToIndex(String srcFile) {
        return sourcesIndices.computeIfAbsent(srcFile, sf -> {
            int index = sources.size();
            sources.add(sf);
            return index;
        });
    }

    private int sourceNameToIndex(String srcName) {
        return namesIndices.computeIfAbsent(srcName, sn -> {
            int index = names.size();
            names.add(sn);
            return index;
        });
    }

    private void putInt(int i) {
        final int BITS_PER_DIGIT = 5;
        final int DIGIT_MASK = (1 << BITS_PER_DIGIT) - 1;
        final int BITS_IN_FIRST_DIGIT = 4;
        final int FIRST_DIGIT_MASK = (1 << BITS_IN_FIRST_DIGIT) - 1;
        final int CONTINUATION = 1 << BITS_PER_DIGIT;

        assert i != Integer.MIN_VALUE;

        int signBit = -(i >> 31);
        i = Math.abs(i);

        int digit = (i & FIRST_DIGIT_MASK) << 1 | signBit;
        i >>= BITS_IN_FIRST_DIGIT;

        while (i != 0) {
            putDigit(digit | CONTINUATION);
            digit = i & DIGIT_MASK;
            i >>= BITS_PER_DIGIT;
        }

        putDigit(digit);
    }

    private void putDigit(int digitValue) {
        final String BASE64_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        mappings.append(BASE64_DIGITS.charAt(digitValue));
    }

    void generate(Appendable out) throws IOException {
        out.append("{\n");

        out.append("\t\"version\": 3,\n");

        if (sourceRoot != null) {
            out.append("\t\"sourceRoot\": \"").append(sourceRoot).append("\",\n");
        }

        out.append("\t\"sources\": [");
        generateList(sources, out);
        out.append("],\n");

        out.append("\t\"names\": [");
        generateList(names, out);
        out.append("],\n");

        out.append("\t\"mappings\": \"").append(mappings).append("\"\n");

        out.append("}\n");
    }

    private static void generateList(Iterable<String> list, Appendable out) throws IOException {
        boolean comma = false;
        for (String s : list) {
            if (comma)
                out.append(", ");
            else
                comma = true;
            out.append('"').append(s).append('"');
        }
    }
}
