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
package java.util;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

final class BrowserCalendar extends Calendar {
    private static long[] MILLIS;
    static {
        final int minute = 1000 * 60;
        final int hour = 60 * minute;
        final long day = 24L * hour;
        final long week = 7 * day;

        MILLIS = new long[FIELD_COUNT];
        MILLIS[ERA] = -1;
        MILLIS[YEAR] = 365 * day;
        MILLIS[MONTH] = 31 * day;
        MILLIS[WEEK_OF_YEAR] = week;
        MILLIS[WEEK_OF_MONTH] = week;
        MILLIS[DAY_OF_MONTH] = day;
        MILLIS[DAY_OF_YEAR] = day;
        MILLIS[DAY_OF_WEEK] = day;
        MILLIS[DAY_OF_WEEK_IN_MONTH] = day;
        MILLIS[AM_PM] = -1;
        MILLIS[HOUR] = 60 * 1000 * 60;
        MILLIS[HOUR_OF_DAY] = -1;
        MILLIS[MINUTE] = 1000 * 60;
        MILLIS[SECOND] = 1000;
        MILLIS[MILLISECOND] = 1;
        MILLIS[ZONE_OFFSET] = -1;
        MILLIS[DST_OFFSET] = -1;
    }

    BrowserCalendar() {
    }

    @Override
    protected void computeTime() {
        throw new UnsupportedOperationException("computeTime");
    }

    @Override
    protected void computeFields() {
        Object[] values = computeFromDate(this.time);
        boolean all = true;
        for (int i = 0; i < FIELD_COUNT; i++) {
            if (this.isSet[i] = values[i] != null) {
                this.fields[i] = ((Number)values[i]).intValue();
            } else {
                all = false;
            }
        }
        this.areAllFieldsSet = all;
    }

    @Override
    public void add(int field, int amount) {
        long add = MILLIS[field];
        this.time += add * amount;
    }

    @Override
    public void roll(int field, boolean up) {
        long add = MILLIS[field];
        if (up) {
            this.time += add;
        } else {
            this.time -= add;
        }
    }

    @Override
    public int getMinimum(int field) {
        throw new UnsupportedOperationException("getMinimum " + field);
    }

    @Override
    public int getMaximum(int field) {
        throw new UnsupportedOperationException("getMaximum " + field);
    }

    @Override
    public int getGreatestMinimum(int field) {
        throw new UnsupportedOperationException("getGreatestMinimum " + field);
    }

    @Override
    public int getLeastMaximum(int field) {
        throw new UnsupportedOperationException("getLeastMaximum  " + field);
    }

    @JavaScriptBody(args = { "time" }, body = ""
            + "var d = new Date(time);\n"
            + "return [\n"
            + "  1,\n" // ERA
            + "  d.getFullYear(),\n" // YEAR
            + "  d.getMonth(),\n" // MONTH
            + "  null,\n" // WEEK_OF_YEAR
            + "  null,\n" // WEEK_OF_MONTH
            + "  null,\n" // DAY_OF_MONTH
            + "  null,\n" // DAY_OF_YEAR
            + "  d.getDay(),\n" // DAY_OF_WEEK
            + "  null,\n" // DAY_OF_WEEK_IN_MONTH
            + "  d.getHours() > 12 ? 1 : 0,\n" // AM_PM
            + "  d.getHours(),\n" // HOUR
            + "  null,\n" // HOUR_OF_DAY
            + "  d.getMinutes(),\n" // MINUTE
            + "  d.getSeconds(),\n" // SECOND
            + "  d.getMilliseconds(),\n" // MILLISECOND
            + "  d.getTimezoneOffset() * 60 * 1000,\n" // ZONE_OFFSET
            + "  null\n" // DST_OFFSET
            + "];\n"
    )
    private static native Object[] computeFromDate(double time);
}
