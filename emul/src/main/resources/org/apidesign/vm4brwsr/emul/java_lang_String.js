/* */


function java_lang_String_consVAC(self,charArr) {
    for (var i = 0; i < charArr.length; i++) {
        if (typeof charArr[i] === 'number') charArr[i] = String.fromCharCode(charArr[i]);
    }
    self.r = charArr.join("");
}

function java_lang_String_consVACII(self, charArr, off, cnt) {
    var up = off + cnt;
    for (var i = off; i < up; i++) {
        if (typeof charArr[i] === 'number') charArr[i] = String.fromCharCode(charArr[i]);
    }
    self.r = charArr.slice(off, up).join("");
}

function java_lang_String_charAtCI(arg0,arg1) {
    return arg0.toString().charCodeAt(arg1);
}
function java_lang_String_lengthI(arg0) {
    return arg0.toString().length;
}
function java_lang_String_isEmptyZ(arg0) {
    return arg0.toString().length === 0;
}
function java_lang_String_valueOfLjava_lang_StringI(n) {
    return n.toString();
}

function java_lang_String_startsWithZLjava_lang_StringI(self,find,from) {
    find = find.toString();
    return self.toString().substring(from, find.length) === find;
}
function java_lang_String_startsWithZLjava_lang_String(self,find) {
    find = find.toString();
    return self.toString().substring(0, find.length) === find;
}
function java_lang_String_endsWithZLjava_lang_String(self,find) {
    self = self.toString();
    find = find.toString();
    if (find.length > self.length) {
        return false;
    }
    return self.substring(self.length - find.length) === find;
}

function java_lang_String_indexOfII(arg0,ch) {
    if (typeof ch === 'number') ch = String.fromCharCode(ch);
    return arg0.toString().indexOf(ch);
}
function java_lang_String_indexOfIII(arg0,ch,from) {
    if (typeof ch === 'number') ch = String.fromCharCode(ch);
    return arg0.toString().indexOf(ch, from);
}

function java_lang_String_getCharsVACI(self, arr, to) {
    var s = self.toString();
    for (var i = 0; i < s.length; i++) {
        arr[to++] = s[i];
    }
}

/*
function java_lang_String_codePointAtII(arg0,arg1) {
  var arg2;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 27
    case 1: if (stack.pop() < 0) { gt = 12; continue; } // 155 0 11
    case 4: stack.push(arg1); // 27
    case 5: stack.push(arg0); // 42
    case 6: stack.push(stack.pop().count); // 180 1 97
    case 9: if (stack.pop() > stack.pop()) { gt = 21; continue; } // 161 0 12
    case 12: stack.push(new java_lang_StringIndexOutOfBoundsException); // 187 0 206
    case 15: stack.push(stack[stack.length - 1]); // 89
    case 16: stack.push(arg1); // 27
    case 17: { var v0 = stack.pop(); java_lang_StringIndexOutOfBoundsException_consVI(stack.pop(), v0); } // 183 1 169
    case 20:  // 191
    case 21: stack.push(arg0); // 42
    case 22: stack.push(stack.pop().value); // 180 1 100
    case 25: stack.push(arg0); // 42
    case 26: stack.push(stack.pop().offset); // 180 1 99
    case 29: stack.push(arg1); // 27
    case 30: stack.push(stack.pop() + stack.pop()); // 96
    case 31: stack.push(arg0); // 42
    case 32: stack.push(stack.pop().offset); // 180 1 99
    case 35: stack.push(arg0); // 42
    case 36: stack.push(stack.pop().count); // 180 1 97
    case 39: stack.push(stack.pop() + stack.pop()); // 96
    case 40: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_codePointAtImplAIACAIAI(v0, v1, v2)); } // 184 1 113
    case 43: return stack.pop(); // 172
  }
}
function java_lang_String_codePointBeforeII(arg0,arg1) {
  var arg2;
  var arg3;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 27
    case 1: stack.push(1); // 4
    case 2: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 3: arg2 = stack.pop(); // 61
    case 4: stack.push(arg2); // 28
    case 5: if (stack.pop() < 0) { gt = 16; continue; } // 155 0 11
    case 8: stack.push(arg2); // 28
    case 9: stack.push(arg0); // 42
    case 10: stack.push(stack.pop().count); // 180 1 97
    case 13: if (stack.pop() > stack.pop()) { gt = 25; continue; } // 161 0 12
    case 16: stack.push(new java_lang_StringIndexOutOfBoundsException); // 187 0 206
    case 19: stack.push(stack[stack.length - 1]); // 89
    case 20: stack.push(arg1); // 27
    case 21: { var v0 = stack.pop(); java_lang_StringIndexOutOfBoundsException_consVI(stack.pop(), v0); } // 183 1 169
    case 24:  // 191
    case 25: stack.push(arg0); // 42
    case 26: stack.push(stack.pop().value); // 180 1 100
    case 29: stack.push(arg0); // 42
    case 30: stack.push(stack.pop().offset); // 180 1 99
    case 33: stack.push(arg1); // 27
    case 34: stack.push(stack.pop() + stack.pop()); // 96
    case 35: stack.push(arg0); // 42
    case 36: stack.push(stack.pop().offset); // 180 1 99
    case 39: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_codePointBeforeImplAIACAIAI(v0, v1, v2)); } // 184 1 114
    case 42: return stack.pop(); // 172
  }
}
function java_lang_String_codePointCountIII(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 27
    case 1: if (stack.pop() < 0) { gt = 17; continue; } // 155 0 16
    case 4: stack.push(arg2); // 28
    case 5: stack.push(arg0); // 42
    case 6: stack.push(stack.pop().count); // 180 1 97
    case 9: if (stack.pop() < stack.pop()) { gt = 17; continue; } // 163 0 8
    case 12: stack.push(arg1); // 27
    case 13: stack.push(arg2); // 28
    case 14: if (stack.pop() >= stack.pop()) { gt = 25; continue; } // 164 0 11
    case 17: stack.push(new java_lang_IndexOutOfBoundsException); // 187 0 194
    case 20: stack.push(stack[stack.length - 1]); // 89
    case 21: { java_lang_IndexOutOfBoundsException_consV(stack.pop()); } // 183 1 124
    case 24:  // 191
    case 25: stack.push(arg0); // 42
    case 26: stack.push(stack.pop().value); // 180 1 100
    case 29: stack.push(arg0); // 42
    case 30: stack.push(stack.pop().offset); // 180 1 99
    case 33: stack.push(arg1); // 27
    case 34: stack.push(stack.pop() + stack.pop()); // 96
    case 35: stack.push(arg2); // 28
    case 36: stack.push(arg1); // 27
    case 37: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 38: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_codePointCountImplAIACAIAI(v0, v1, v2)); } // 184 1 115
    case 41: return stack.pop(); // 172
  }
}
function java_lang_String_offsetByCodePointsIII(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(5);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 27
    case 1: if (stack.pop() < 0) { gt = 12; continue; } // 155 0 11
    case 4: stack.push(arg1); // 27
    case 5: stack.push(arg0); // 42
    case 6: stack.push(stack.pop().count); // 180 1 97
    case 9: if (stack.pop() >= stack.pop()) { gt = 20; continue; } // 164 0 11
    case 12: stack.push(new java_lang_IndexOutOfBoundsException); // 187 0 194
    case 15: stack.push(stack[stack.length - 1]); // 89
    case 16: { java_lang_IndexOutOfBoundsException_consV(stack.pop()); } // 183 1 124
    case 19:  // 191
    case 20: stack.push(arg0); // 42
    case 21: stack.push(stack.pop().value); // 180 1 100
    case 24: stack.push(arg0); // 42
    case 25: stack.push(stack.pop().offset); // 180 1 99
    case 28: stack.push(arg0); // 42
    case 29: stack.push(stack.pop().count); // 180 1 97
    case 32: stack.push(arg0); // 42
    case 33: stack.push(stack.pop().offset); // 180 1 99
    case 36: stack.push(arg1); // 27
    case 37: stack.push(stack.pop() + stack.pop()); // 96
    case 38: stack.push(arg2); // 28
    case 39: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_offsetByCodePointsImplAIACAIAIAIAI(v0, v1, v2, v3, v4)); } // 184 1 116
    case 42: stack.push(arg0); // 42
    case 43: stack.push(stack.pop().offset); // 180 1 99
    case 46: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 47: return stack.pop(); // 172
  }
}
*/

// public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
function java_lang_String_getCharsVIIACAI(arg0,arg1,arg2,arg3,arg4) {
    var s = arg0.toString();
    while (arg1 < arg2) {
        arg3[arg4++] = s[arg1++];
    }
}

/*
function java_lang_String_getBytesVIIABI(arg0,arg1,arg2,arg3,arg4) {
  var arg5;
  var arg6;
  var arg7;
  var arg8;
  var arg9;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 27
    case 1: if (stack.pop() >= 0) { gt = 13; continue; } // 156 0 12
    case 4: stack.push(new java_lang_StringIndexOutOfBoundsException); // 187 0 206
    case 7: stack.push(stack[stack.length - 1]); // 89
    case 8: stack.push(arg1); // 27
    case 9: { var v0 = stack.pop(); java_lang_StringIndexOutOfBoundsException_consVI(stack.pop(), v0); } // 183 1 169
    case 12:  // 191
    case 13: stack.push(arg2); // 28
    case 14: stack.push(arg0); // 42
    case 15: stack.push(stack.pop().count); // 180 1 97
    case 18: if (stack.pop() >= stack.pop()) { gt = 30; continue; } // 164 0 12
    case 21: stack.push(new java_lang_StringIndexOutOfBoundsException); // 187 0 206
    case 24: stack.push(stack[stack.length - 1]); // 89
    case 25: stack.push(arg2); // 28
    case 26: { var v0 = stack.pop(); java_lang_StringIndexOutOfBoundsException_consVI(stack.pop(), v0); } // 183 1 169
    case 29:  // 191
    case 30: stack.push(arg1); // 27
    case 31: stack.push(arg2); // 28
    case 32: if (stack.pop() >= stack.pop()) { gt = 46; continue; } // 164 0 14
    case 35: stack.push(new java_lang_StringIndexOutOfBoundsException); // 187 0 206
    case 38: stack.push(stack[stack.length - 1]); // 89
    case 39: stack.push(arg2); // 28
    case 40: stack.push(arg1); // 27
    case 41: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 42: { var v0 = stack.pop(); java_lang_StringIndexOutOfBoundsException_consVI(stack.pop(), v0); } // 183 1 169
    case 45:  // 191
    case 46: stack.push(arg4); // 21 4
    case 48: arg5 = stack.pop() // 54 5
    case 50: stack.push(arg0); // 42
    case 51: stack.push(stack.pop().offset); // 180 1 99
    case 54: stack.push(arg2); // 28
    case 55: stack.push(stack.pop() + stack.pop()); // 96
    case 56: arg6 = stack.pop() // 54 6
    case 58: stack.push(arg0); // 42
    case 59: stack.push(stack.pop().offset); // 180 1 99
    case 62: stack.push(arg1); // 27
    case 63: stack.push(stack.pop() + stack.pop()); // 96
    case 64: arg7 = stack.pop() // 54 7
    case 66: stack.push(arg0); // 42
    case 67: stack.push(stack.pop().value); // 180 1 100
    case 70: arg8 = stack.pop() // 58 8
    case 72: stack.push(arg7); // 21 7
    case 74: stack.push(arg6); // 21 6
    case 76: if (stack.pop() <= stack.pop()) { gt = 98; continue; } // 162 0 22
    case 79: stack.push(arg3); // 45
    case 80: stack.push(arg5); // 21 5
    case 82: arg5++; // 132 5 1
    case 85: stack.push(arg8); // 25 8
    case 87: stack.push(arg7); // 21 7
    case 89: arg7++; // 132 7 1
    case 92: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 93: // number conversion  // 145
    case 94: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 84
    case 95: gt = 72; continue; // 167 255 233
    case 98: return; // 177
  }
}
function java_lang_String_getBytesABLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: if (stack.pop()) { gt = 12; continue; } // 199 0 11
    case 4: stack.push(new java_lang_NullPointerException); // 187 0 198
    case 7: stack.push(stack[stack.length - 1]); // 89
    case 8: { java_lang_NullPointerException_consV(stack.pop()); } // 183 1 128
    case 11:  // 191
    case 12: stack.push(arg1); // 43
    case 13: stack.push(arg0); // 42
    case 14: stack.push(stack.pop().value); // 180 1 100
    case 17: stack.push(arg0); // 42
    case 18: stack.push(stack.pop().offset); // 180 1 99
    case 21: stack.push(arg0); // 42
    case 22: stack.push(stack.pop().count); // 180 1 97
    case 25: { var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_StringCoding_encodeABLjava_lang_StringACAIAI(v0, v1, v2, v3)); } // 184 1 166
    case 28: return stack.pop(); // 176
  }
}
function java_lang_String_getBytesABLjava_nio_charset_Charset(arg0,arg1) {
  var arg2;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: if (stack.pop()) { gt = 12; continue; } // 199 0 11
    case 4: stack.push(new java_lang_NullPointerException); // 187 0 198
    case 7: stack.push(stack[stack.length - 1]); // 89
    case 8: { java_lang_NullPointerException_consV(stack.pop()); } // 183 1 128
    case 11:  // 191
    case 12: stack.push(arg1); // 43
    case 13: stack.push(arg0); // 42
    case 14: stack.push(stack.pop().value); // 180 1 100
    case 17: stack.push(arg0); // 42
    case 18: stack.push(stack.pop().offset); // 180 1 99
    case 21: stack.push(arg0); // 42
    case 22: stack.push(stack.pop().count); // 180 1 97
    case 25: { var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_StringCoding_encodeABLjava_nio_charset_CharsetACAIAI(v0, v1, v2, v3)); } // 184 1 168
    case 28: return stack.pop(); // 176
  }
}
function java_lang_String_getBytesAB(arg0) {
  var arg1;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: stack.push(arg0); // 42
    case 5: stack.push(stack.pop().offset); // 180 1 99
    case 8: stack.push(arg0); // 42
    case 9: stack.push(stack.pop().count); // 180 1 97
    case 12: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_StringCoding_encodeABACAIAI(v0, v1, v2)); } // 184 1 164
    case 15: return stack.pop(); // 176
  }
}
*/
function java_lang_String_equalsZLjava_lang_Object(arg0,arg1) {
    return arg0.toString() === arg1.toString();
}

function java_lang_String_hashCodeI(self) {
    var h = 0;
    var s = self.toString();
    for (var i = 0; i < s.length; i++) {
        h = 31 * h + s.charCodeAt(i);
    }
    return h;
}

/*
function java_lang_String_contentEqualsZLjava_lang_StringBuffer(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: stack.push(stack[stack.length - 1]); // 89
    case 2: arg2 = stack.pop(); // 77
    case 3:  // 194
    case 4: stack.push(arg0); // 42
    case 5: stack.push(arg1); // 43
    case 6: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.contentEqualsZLjava_lang_CharSequence(self, v0)); } // 182 1 146
    case 9: stack.push(arg2); // 44
    case 10:  // 195
    case 11: return stack.pop(); // 172
    case 12: arg3 = stack.pop(); // 78
    case 13: stack.push(arg2); // 44
    case 14:  // 195
    case 15: stack.push(arg3); // 45
    case 16:  // 191
  }
}
function java_lang_String_contentEqualsZLjava_lang_CharSequence(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().count); // 180 1 97
    case 4: stack.push(arg1); // 43
    case 5: { var self = stack.pop(); stack.push(self.lengthI(self)); } // 185 1 188
    case 8:  // 1
    case 9:  // 0
    case 10: if (stack.pop() == stack.pop()) { gt = 15; continue; } // 159 0 5
    case 13: stack.push(0); // 3
    case 14: return stack.pop(); // 172
    case 15: stack.push(arg1); // 43
    case 16: stack.push(stack.pop().$instOf_java_lang_AbstractStringBuilder ? 1 : 0); // 193 0 186
    case 19: if (stack.pop() == 0) { gt = 77; continue; } // 153 0 58
    case 22: stack.push(arg0); // 42
    case 23: stack.push(stack.pop().value); // 180 1 100
    case 26: arg2 = stack.pop(); // 77
    case 27: stack.push(arg1); // 43
    case 28: if(stack[stack.length - 1].$instOf_java_lang_AbstractStringBuilder != 1) throw {}; // 192 0 186
    case 31: { var self = stack.pop(); stack.push(self.getValueAC(self)); } // 182 1 103
    case 34: arg3 = stack.pop(); // 78
    case 35: stack.push(arg0); // 42
    case 36: stack.push(stack.pop().offset); // 180 1 99
    case 39: arg4 = stack.pop() // 54 4
    case 41: stack.push(0); // 3
    case 42: arg5 = stack.pop() // 54 5
    case 44: stack.push(arg0); // 42
    case 45: stack.push(stack.pop().count); // 180 1 97
    case 48: arg6 = stack.pop() // 54 6
    case 50: stack.push(arg6); // 21 6
    case 52: arg6 += 255; // 132 6 255
    case 55: if (stack.pop() == 0) { gt = 77; continue; } // 153 0 22
    case 58: stack.push(arg2); // 44
    case 59: stack.push(arg4); // 21 4
    case 61: arg4++; // 132 4 1
    case 64: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 65: stack.push(arg3); // 45
    case 66: stack.push(arg5); // 21 5
    case 68: arg5++; // 132 5 1
    case 71: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 72: if (stack.pop() == stack.pop()) { gt = 50; continue; } // 159 255 234
    case 75: stack.push(0); // 3
    case 76: return stack.pop(); // 172
    case 77: stack.push(arg1); // 43
    case 78: stack.push(arg0); // 42
    case 79: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.equalsZLjava_lang_Object(self, v0)); } // 182 1 131
    case 82: if (stack.pop() == 0) { gt = 87; continue; } // 153 0 5
    case 85: stack.push(1); // 4
    case 86: return stack.pop(); // 172
    case 87: stack.push(arg0); // 42
    case 88: stack.push(stack.pop().value); // 180 1 100
    case 91: arg2 = stack.pop(); // 77
    case 92: stack.push(arg0); // 42
    case 93: stack.push(stack.pop().offset); // 180 1 99
    case 96: arg3 = stack.pop(); // 62
    case 97: stack.push(0); // 3
    case 98: arg4 = stack.pop() // 54 4
    case 100: stack.push(arg0); // 42
    case 101: stack.push(stack.pop().count); // 180 1 97
    case 104: arg5 = stack.pop() // 54 5
    case 106: stack.push(arg5); // 21 5
    case 108: arg5 += 255; // 132 5 255
    case 111: if (stack.pop() == 0) { gt = 136; continue; } // 153 0 25
    case 114: stack.push(arg2); // 44
    case 115: stack.push(arg3); // 29
    case 116: arg3++; // 132 3 1
    case 119: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 120: stack.push(arg1); // 43
    case 121: stack.push(arg4); // 21 4
    case 123: arg4++; // 132 4 1
    case 126: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.charAtCI(self, v0)); } // 185 1 189
    case 129:  // 2
    case 130:  // 0
    case 131: if (stack.pop() == stack.pop()) { gt = 106; continue; } // 159 255 231
    case 134: stack.push(0); // 3
    case 135: return stack.pop(); // 172
    case 136: stack.push(1); // 4
    case 137: return stack.pop(); // 172
  }
}
function java_lang_String_equalsIgnoreCaseZLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(6);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2:  // 166
    case 3:  // 0
    case 4: stack.push(4); // 7
    case 5: stack.push(1); // 4
    case 6: gt = 44; continue; // 167 0 38
    case 9: stack.push(arg1); // 43
    case 10: if (!stack.pop()) { gt = 43; continue; } // 198 0 33
    case 13: stack.push(arg1); // 43
    case 14: stack.push(stack.pop().count); // 180 1 97
    case 17: stack.push(arg0); // 42
    case 18: stack.push(stack.pop().count); // 180 1 97
    case 21: if (stack.pop() != stack.pop()) { gt = 43; continue; } // 160 0 22
    case 24: stack.push(arg0); // 42
    case 25: stack.push(1); // 4
    case 26: stack.push(0); // 3
    case 27: stack.push(arg1); // 43
    case 28: stack.push(0); // 3
    case 29: stack.push(arg0); // 42
    case 30: stack.push(stack.pop().count); // 180 1 97
    case 33: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.regionMatchesZZILjava_lang_StringII(self, v0, v1, v2, v3, v4)); } // 182 1 153
    case 36: if (stack.pop() == 0) { gt = 43; continue; } // 153 0 7
    case 39: stack.push(1); // 4
    case 40: gt = 44; continue; // 167 0 4
    case 43: stack.push(0); // 3
    case 44: return stack.pop(); // 172
  }
}
function java_lang_String_compareToILjava_lang_String(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var arg13;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().count); // 180 1 97
    case 4: arg2 = stack.pop(); // 61
    case 5: stack.push(arg1); // 43
    case 6: stack.push(stack.pop().count); // 180 1 97
    case 9: arg3 = stack.pop(); // 62
    case 10: stack.push(arg2); // 28
    case 11: stack.push(arg3); // 29
    case 12: { var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Math_minIII(v0, v1)); } // 184 1 127
    case 15: arg4 = stack.pop() // 54 4
    case 17: stack.push(arg0); // 42
    case 18: stack.push(stack.pop().value); // 180 1 100
    case 21: arg5 = stack.pop() // 58 5
    case 23: stack.push(arg1); // 43
    case 24: stack.push(stack.pop().value); // 180 1 100
    case 27: arg6 = stack.pop() // 58 6
    case 29: stack.push(arg0); // 42
    case 30: stack.push(stack.pop().offset); // 180 1 99
    case 33: arg7 = stack.pop() // 54 7
    case 35: stack.push(arg1); // 43
    case 36: stack.push(stack.pop().offset); // 180 1 99
    case 39: arg8 = stack.pop() // 54 8
    case 41: stack.push(arg7); // 21 7
    case 43: stack.push(arg8); // 21 8
    case 45: if (stack.pop() != stack.pop()) { gt = 102; continue; } // 160 0 57
    case 48: stack.push(arg7); // 21 7
    case 50: arg9 = stack.pop() // 54 9
    case 52: stack.push(arg4); // 21 4
    case 54: stack.push(arg7); // 21 7
    case 56: stack.push(stack.pop() + stack.pop()); // 96
    case 57: arg10 = stack.pop() // 54 10
    case 59: stack.push(arg9); // 21 9
    case 61: stack.push(arg10); // 21 10
    case 63: if (stack.pop() <= stack.pop()) { gt = 99; continue; } // 162 0 36
    case 66: stack.push(arg5); // 25 5
    case 68: stack.push(arg9); // 21 9
    case 70: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 71: arg11 = stack.pop() // 54 11
    case 73: stack.push(arg6); // 25 6
    case 75: stack.push(arg9); // 21 9
    case 77: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 78: arg12 = stack.pop() // 54 12
    case 80: stack.push(arg11); // 21 11
    case 82: stack.push(arg12); // 21 12
    case 84: if (stack.pop() == stack.pop()) { gt = 93; continue; } // 159 0 9
    case 87: stack.push(arg11); // 21 11
    case 89: stack.push(arg12); // 21 12
    case 91: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 92: return stack.pop(); // 172
    case 93: arg9++; // 132 9 1
    case 96: gt = 59; continue; // 167 255 219
    case 99: gt = 146; continue; // 167 0 47
    case 102: stack.push(arg4); // 21 4
    case 104: arg4 += 255; // 132 4 255
    case 107: if (stack.pop() == 0) { gt = 146; continue; } // 153 0 39
    case 110: stack.push(arg5); // 25 5
    case 112: stack.push(arg7); // 21 7
    case 114: arg7++; // 132 7 1
    case 117: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 118: arg9 = stack.pop() // 54 9
    case 120: stack.push(arg6); // 25 6
    case 122: stack.push(arg8); // 21 8
    case 124: arg8++; // 132 8 1
    case 127: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 128: arg10 = stack.pop() // 54 10
    case 130: stack.push(arg9); // 21 9
    case 132: stack.push(arg10); // 21 10
    case 134: if (stack.pop() == stack.pop()) { gt = 143; continue; } // 159 0 9
    case 137: stack.push(arg9); // 21 9
    case 139: stack.push(arg10); // 21 10
    case 141: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 142: return stack.pop(); // 172
    case 143: gt = 102; continue; // 167 255 215
    case 146: stack.push(arg2); // 28
    case 147: stack.push(arg3); // 29
    case 148: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 149: return stack.pop(); // 172
  }
}
function java_lang_String_compareToIgnoreCaseILjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(java_lang_String_CASE_INSENSITIVE_ORDER); // 178 1 102
    case 3: stack.push(arg0); // 42
    case 4: stack.push(arg1); // 43
    case 5: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.compareILjava_lang_ObjectLjava_lang_Object(self, v0, v1)); } // 185 1 190
    case 8: stack.push(0); // 3
    case 9:  // 0
    case 10: return stack.pop(); // 172
  }
}
function java_lang_String_regionMatchesZILjava_lang_StringII(arg0,arg1,arg2,arg3,arg4) {
  var arg5;
  var arg6;
  var arg7;
  var arg8;
  var arg9;
;
  var stack = new Array(6);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: arg5 = stack.pop() // 58 5
    case 6: stack.push(arg0); // 42
    case 7: stack.push(stack.pop().offset); // 180 1 99
    case 10: stack.push(arg1); // 27
    case 11: stack.push(stack.pop() + stack.pop()); // 96
    case 12: arg6 = stack.pop() // 54 6
    case 14: stack.push(arg2); // 44
    case 15: stack.push(stack.pop().value); // 180 1 100
    case 18: arg7 = stack.pop() // 58 7
    case 20: stack.push(arg2); // 44
    case 21: stack.push(stack.pop().offset); // 180 1 99
    case 24: stack.push(arg3); // 29
    case 25: stack.push(stack.pop() + stack.pop()); // 96
    case 26: arg8 = stack.pop() // 54 8
    case 28: stack.push(arg3); // 29
    case 29: if (stack.pop() < 0) { gt = 66; continue; } // 155 0 37
    case 32: stack.push(arg1); // 27
    case 33: if (stack.pop() < 0) { gt = 66; continue; } // 155 0 33
    case 36: stack.push(arg1); // 27
    case 37: // number conversion  // 133
    case 38: stack.push(arg0); // 42
    case 39: stack.push(stack.pop().count); // 180 1 97
    case 42: // number conversion  // 133
    case 43: stack.push(arg4); // 21 4
    case 45: // number conversion  // 133
    case 46: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 101
    case 47: { var delta = stack.pop() - stack.pop(); stack.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); } // 148
    case 48: if (stack.pop() > 0) { gt = 66; continue; } // 157 0 18
    case 51: stack.push(arg3); // 29
    case 52: // number conversion  // 133
    case 53: stack.push(arg2); // 44
    case 54: stack.push(stack.pop().count); // 180 1 97
    case 57: // number conversion  // 133
    case 58: stack.push(arg4); // 21 4
    case 60: // number conversion  // 133
    case 61: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 101
    case 62: { var delta = stack.pop() - stack.pop(); stack.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); } // 148
    case 63: if (stack.pop() <= 0) { gt = 68; continue; } // 158 0 5
    case 66: stack.push(0); // 3
    case 67: return stack.pop(); // 172
    case 68: stack.push(arg4); // 21 4
    case 70: arg4 += 255; // 132 4 255
    case 73: if (stack.pop() <= 0) { gt = 97; continue; } // 158 0 24
    case 76: stack.push(arg5); // 25 5
    case 78: stack.push(arg6); // 21 6
    case 80: arg6++; // 132 6 1
    case 83: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 84: stack.push(arg7); // 25 7
    case 86: stack.push(arg8); // 21 8
    case 88: arg8++; // 132 8 1
    case 91: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 92: if (stack.pop() == stack.pop()) { gt = 68; continue; } // 159 255 232
    case 95: stack.push(0); // 3
    case 96: return stack.pop(); // 172
    case 97: stack.push(1); // 4
    case 98: return stack.pop(); // 172
  }
}
function java_lang_String_regionMatchesZZILjava_lang_StringII(arg0,arg1,arg2,arg3,arg4,arg5) {
  var arg6;
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var arg13;
  var arg14;
;
  var stack = new Array(6);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: arg6 = stack.pop() // 58 6
    case 6: stack.push(arg0); // 42
    case 7: stack.push(stack.pop().offset); // 180 1 99
    case 10: stack.push(arg2); // 28
    case 11: stack.push(stack.pop() + stack.pop()); // 96
    case 12: arg7 = stack.pop() // 54 7
    case 14: stack.push(arg3); // 45
    case 15: stack.push(stack.pop().value); // 180 1 100
    case 18: arg8 = stack.pop() // 58 8
    case 20: stack.push(arg3); // 45
    case 21: stack.push(stack.pop().offset); // 180 1 99
    case 24: stack.push(arg4); // 21 4
    case 26: stack.push(stack.pop() + stack.pop()); // 96
    case 27: arg9 = stack.pop() // 54 9
    case 29: stack.push(arg4); // 21 4
    case 31: if (stack.pop() < 0) { gt = 69; continue; } // 155 0 38
    case 34: stack.push(arg2); // 28
    case 35: if (stack.pop() < 0) { gt = 69; continue; } // 155 0 34
    case 38: stack.push(arg2); // 28
    case 39: // number conversion  // 133
    case 40: stack.push(arg0); // 42
    case 41: stack.push(stack.pop().count); // 180 1 97
    case 44: // number conversion  // 133
    case 45: stack.push(arg5); // 21 5
    case 47: // number conversion  // 133
    case 48: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 101
    case 49: { var delta = stack.pop() - stack.pop(); stack.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); } // 148
    case 50: if (stack.pop() > 0) { gt = 69; continue; } // 157 0 19
    case 53: stack.push(arg4); // 21 4
    case 55: // number conversion  // 133
    case 56: stack.push(arg3); // 45
    case 57: stack.push(stack.pop().count); // 180 1 97
    case 60: // number conversion  // 133
    case 61: stack.push(arg5); // 21 5
    case 63: // number conversion  // 133
    case 64: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 101
    case 65: { var delta = stack.pop() - stack.pop(); stack.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); } // 148
    case 66: if (stack.pop() <= 0) { gt = 71; continue; } // 158 0 5
    case 69: stack.push(0); // 3
    case 70: return stack.pop(); // 172
    case 71: stack.push(arg5); // 21 5
    case 73: arg5 += 255; // 132 5 255
    case 76: if (stack.pop() <= 0) { gt = 155; continue; } // 158 0 79
    case 79: stack.push(arg6); // 25 6
    case 81: stack.push(arg7); // 21 7
    case 83: arg7++; // 132 7 1
    case 86: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 87: arg10 = stack.pop() // 54 10
    case 89: stack.push(arg8); // 25 8
    case 91: stack.push(arg9); // 21 9
    case 93: arg9++; // 132 9 1
    case 96: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 97: arg11 = stack.pop() // 54 11
    case 99: stack.push(arg10); // 21 10
    case 101: stack.push(arg11); // 21 11
    case 103: if (stack.pop() != stack.pop()) { gt = 109; continue; } // 160 0 6
    case 106: gt = 71; continue; // 167 255 221
    case 109: stack.push(arg1); // 27
    case 110: if (stack.pop() == 0) { gt = 153; continue; } // 153 0 43
    case 113: stack.push(arg10); // 21 10
    case 115: { var v0 = stack.pop(); stack.push(java_lang_Character_toUpperCaseCC(v0)); } // 184 1 105
    case 118: arg12 = stack.pop() // 54 12
    case 120: stack.push(arg11); // 21 11
    case 122: { var v0 = stack.pop(); stack.push(java_lang_Character_toUpperCaseCC(v0)); } // 184 1 105
    case 125: arg13 = stack.pop() // 54 13
    case 127: stack.push(arg12); // 21 12
    case 129: stack.push(arg13); // 21 13
    case 131: if (stack.pop() != stack.pop()) { gt = 137; continue; } // 160 0 6
    case 134: gt = 71; continue; // 167 255 193
    case 137: stack.push(arg12); // 21 12
    case 139: { var v0 = stack.pop(); stack.push(java_lang_Character_toLowerCaseCC(v0)); } // 184 1 104
    case 142: stack.push(arg13); // 21 13
    case 144: { var v0 = stack.pop(); stack.push(java_lang_Character_toLowerCaseCC(v0)); } // 184 1 104
    case 147: if (stack.pop() != stack.pop()) { gt = 153; continue; } // 160 0 6
    case 150: gt = 71; continue; // 167 255 177
    case 153: stack.push(0); // 3
    case 154: return stack.pop(); // 172
    case 155: stack.push(1); // 4
    case 156: return stack.pop(); // 172
  }
}
function java_lang_String_startsWithZLjava_lang_StringI(arg0,arg1,arg2) {
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
  var arg8;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: arg3 = stack.pop(); // 78
    case 5: stack.push(arg0); // 42
    case 6: stack.push(stack.pop().offset); // 180 1 99
    case 9: stack.push(arg2); // 28
    case 10: stack.push(stack.pop() + stack.pop()); // 96
    case 11: arg4 = stack.pop() // 54 4
    case 13: stack.push(arg1); // 43
    case 14: stack.push(stack.pop().value); // 180 1 100
    case 17: arg5 = stack.pop() // 58 5
    case 19: stack.push(arg1); // 43
    case 20: stack.push(stack.pop().offset); // 180 1 99
    case 23: arg6 = stack.pop() // 54 6
    case 25: stack.push(arg1); // 43
    case 26: stack.push(stack.pop().count); // 180 1 97
    case 29: arg7 = stack.pop() // 54 7
    case 31: stack.push(arg2); // 28
    case 32: if (stack.pop() < 0) { gt = 46; continue; } // 155 0 14
    case 35: stack.push(arg2); // 28
    case 36: stack.push(arg0); // 42
    case 37: stack.push(stack.pop().count); // 180 1 97
    case 40: stack.push(arg7); // 21 7
    case 42: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 43: if (stack.pop() >= stack.pop()) { gt = 48; continue; } // 164 0 5
    case 46: stack.push(0); // 3
    case 47: return stack.pop(); // 172
    case 48: arg7 += 255; // 132 7 255
    case 51: stack.push(arg7); // 21 7
    case 53: if (stack.pop() < 0) { gt = 76; continue; } // 155 0 23
    case 56: stack.push(arg3); // 45
    case 57: stack.push(arg4); // 21 4
    case 59: arg4++; // 132 4 1
    case 62: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 63: stack.push(arg5); // 25 5
    case 65: stack.push(arg6); // 21 6
    case 67: arg6++; // 132 6 1
    case 70: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 71: if (stack.pop() == stack.pop()) { gt = 48; continue; } // 159 255 233
    case 74: stack.push(0); // 3
    case 75: return stack.pop(); // 172
    case 76: stack.push(1); // 4
    case 77: return stack.pop(); // 172
  }
}
function java_lang_String_startsWithZLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: stack.push(0); // 3
    case 3: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.startsWithZLjava_lang_StringI(self, v0, v1)); } // 182 1 152
    case 6: return stack.pop(); // 172
  }
}
function java_lang_String_endsWithZLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: stack.push(arg0); // 42
    case 3: stack.push(stack.pop().count); // 180 1 97
    case 6: stack.push(arg1); // 43
    case 7: stack.push(stack.pop().count); // 180 1 97
    case 10: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 11: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.startsWithZLjava_lang_StringI(self, v0, v1)); } // 182 1 152
    case 14: return stack.pop(); // 172
  }
}
function java_lang_String_lastIndexOfII(arg0,arg1) {
  var arg2;
;
  var stack = new Array(4);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 27
    case 2: stack.push(arg0); // 42
    case 3: stack.push(stack.pop().count); // 180 1 97
    case 6: stack.push(1); // 4
    case 7: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 8: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.lastIndexOfIII(self, v0, v1)); } // 182 1 136
    case 11: return stack.pop(); // 172
  }
}
function java_lang_String_lastIndexOfIII(arg0,arg1,arg2) {
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
  var arg8;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().offset); // 180 1 99
    case 4: arg3 = stack.pop(); // 62
    case 5: stack.push(arg0); // 42
    case 6: stack.push(stack.pop().value); // 180 1 100
    case 9: arg4 = stack.pop() // 58 4
    case 11: stack.push(arg0); // 42
    case 12: stack.push(stack.pop().offset); // 180 1 99
    case 15: stack.push(arg2); // 28
    case 16: stack.push(arg0); // 42
    case 17: stack.push(stack.pop().count); // 180 1 97
    case 20: if (stack.pop() > stack.pop()) { gt = 32; continue; } // 161 0 12
    case 23: stack.push(arg0); // 42
    case 24: stack.push(stack.pop().count); // 180 1 97
    case 27: stack.push(1); // 4
    case 28: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 29: gt = 33; continue; // 167 0 4
    case 32: stack.push(arg2); // 28
    case 33: stack.push(stack.pop() + stack.pop()); // 96
    case 34: arg5 = stack.pop() // 54 5
    case 36: stack.push(arg1); // 27
    case 37: stack.push(65536); // 18 3
    case 39: if (stack.pop() <= stack.pop()) { gt = 73; continue; } // 162 0 34
    case 42: stack.push(arg5); // 21 5
    case 44: stack.push(arg3); // 29
    case 45: if (stack.pop() > stack.pop()) { gt = 71; continue; } // 161 0 26
    case 48: stack.push(arg4); // 25 4
    case 50: stack.push(arg5); // 21 5
    case 52: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 53: stack.push(arg1); // 27
    case 54: if (stack.pop() != stack.pop()) { gt = 65; continue; } // 160 0 11
    case 57: stack.push(arg5); // 21 5
    case 59: stack.push(arg0); // 42
    case 60: stack.push(stack.pop().offset); // 180 1 99
    case 63: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 64: return stack.pop(); // 172
    case 65: arg5 += 255; // 132 5 255
    case 68: gt = 42; continue; // 167 255 230
    case 71:  // 2
    case 72: return stack.pop(); // 172
    case 73: stack.push(arg0); // 42
    case 74: stack.push(stack.pop().offset); // 180 1 99
    case 77: stack.push(arg0); // 42
    case 78: stack.push(stack.pop().count); // 180 1 97
    case 81: stack.push(stack.pop() + stack.pop()); // 96
    case 82: arg6 = stack.pop() // 54 6
    case 84: stack.push(arg1); // 27
    case 85: stack.push(1114111); // 18 4
    case 87: if (stack.pop() < stack.pop()) { gt = 154; continue; } // 163 0 67
    case 90: stack.push(arg1); // 27
    case 91: { var v0 = stack.pop(); stack.push(java_lang_Character_toCharsACI(v0)); } // 184 1 109
    case 94: arg7 = stack.pop() // 58 7
    case 96: stack.push(arg5); // 21 5
    case 98: stack.push(arg3); // 29
    case 99: if (stack.pop() > stack.pop()) { gt = 154; continue; } // 161 0 55
    case 102: stack.push(arg4); // 25 4
    case 104: stack.push(arg5); // 21 5
    case 106: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 107: stack.push(arg7); // 25 7
    case 109: stack.push(0); // 3
    case 110: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 111: if (stack.pop() != stack.pop()) { gt = 148; continue; } // 160 0 37
    case 114: stack.push(arg5); // 21 5
    case 116: stack.push(1); // 4
    case 117: stack.push(stack.pop() + stack.pop()); // 96
    case 118: stack.push(arg6); // 21 6
    case 120: if (stack.pop() != stack.pop()) { gt = 126; continue; } // 160 0 6
    case 123: gt = 154; continue; // 167 0 31
    case 126: stack.push(arg4); // 25 4
    case 128: stack.push(arg5); // 21 5
    case 130: stack.push(1); // 4
    case 131: stack.push(stack.pop() + stack.pop()); // 96
    case 132: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 133: stack.push(arg7); // 25 7
    case 135: stack.push(1); // 4
    case 136: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 137: if (stack.pop() != stack.pop()) { gt = 148; continue; } // 160 0 11
    case 140: stack.push(arg5); // 21 5
    case 142: stack.push(arg0); // 42
    case 143: stack.push(stack.pop().offset); // 180 1 99
    case 146: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 147: return stack.pop(); // 172
    case 148: arg5 += 255; // 132 5 255
    case 151: gt = 96; continue; // 167 255 201
    case 154:  // 2
    case 155: return stack.pop(); // 172
  }
}
function java_lang_String_indexOfILjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: stack.push(0); // 3
    case 3: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.indexOfILjava_lang_StringI(self, v0, v1)); } // 182 1 150
    case 6: return stack.pop(); // 172
  }
}
function java_lang_String_indexOfILjava_lang_StringI(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(7);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: stack.push(arg0); // 42
    case 5: stack.push(stack.pop().offset); // 180 1 99
    case 8: stack.push(arg0); // 42
    case 9: stack.push(stack.pop().count); // 180 1 97
    case 12: stack.push(arg1); // 43
    case 13: stack.push(stack.pop().value); // 180 1 100
    case 16: stack.push(arg1); // 43
    case 17: stack.push(stack.pop().offset); // 180 1 99
    case 20: stack.push(arg1); // 43
    case 21: stack.push(stack.pop().count); // 180 1 97
    case 24: stack.push(arg2); // 28
    case 25: { var v6 = stack.pop(); var v5 = stack.pop(); var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_String_indexOfAIACAIAIACAIAIAI(v0, v1, v2, v3, v4, v5, v6)); } // 184 1 144
    case 28: return stack.pop(); // 172
  }
}
function java_lang_String_indexOfIACIIACIII(arg0,arg1,arg2,arg3,arg4,arg5,arg6) {
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg6); // 21 6
    case 2: stack.push(arg2); // 28
    case 3: if (stack.pop() > stack.pop()) { gt = 17; continue; } // 161 0 14
    case 6: stack.push(arg5); // 21 5
    case 8: if (stack.pop() != 0) { gt = 15; continue; } // 154 0 7
    case 11: stack.push(arg2); // 28
    case 12: gt = 16; continue; // 167 0 4
    case 15:  // 2
    case 16: return stack.pop(); // 172
    case 17: stack.push(arg6); // 21 6
    case 19: if (stack.pop() >= 0) { gt = 25; continue; } // 156 0 6
    case 22: stack.push(0); // 3
    case 23: arg6 = stack.pop() // 54 6
    case 25: stack.push(arg5); // 21 5
    case 27: if (stack.pop() != 0) { gt = 33; continue; } // 154 0 6
    case 30: stack.push(arg6); // 21 6
    case 32: return stack.pop(); // 172
    case 33: stack.push(arg3); // 45
    case 34: stack.push(arg4); // 21 4
    case 36: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 37: arg7 = stack.pop() // 54 7
    case 39: stack.push(arg1); // 27
    case 40: stack.push(arg2); // 28
    case 41: stack.push(arg5); // 21 5
    case 43: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 44: stack.push(stack.pop() + stack.pop()); // 96
    case 45: arg8 = stack.pop() // 54 8
    case 47: stack.push(arg1); // 27
    case 48: stack.push(arg6); // 21 6
    case 50: stack.push(stack.pop() + stack.pop()); // 96
    case 51: arg9 = stack.pop() // 54 9
    case 53: stack.push(arg9); // 21 9
    case 55: stack.push(arg8); // 21 8
    case 57: if (stack.pop() < stack.pop()) { gt = 164; continue; } // 163 0 107
    case 60: stack.push(arg0); // 42
    case 61: stack.push(arg9); // 21 9
    case 63: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 64: stack.push(arg7); // 21 7
    case 66: if (stack.pop() == stack.pop()) { gt = 91; continue; } // 159 0 25
    case 69: arg9++; // 132 9 1
    case 72: stack.push(arg9); // 21 9
    case 74: stack.push(arg8); // 21 8
    case 76: if (stack.pop() < stack.pop()) { gt = 91; continue; } // 163 0 15
    case 79: stack.push(arg0); // 42
    case 80: stack.push(arg9); // 21 9
    case 82: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 83: stack.push(arg7); // 21 7
    case 85: if (stack.pop() == stack.pop()) { gt = 91; continue; } // 159 0 6
    case 88: gt = 69; continue; // 167 255 237
    case 91: stack.push(arg9); // 21 9
    case 93: stack.push(arg8); // 21 8
    case 95: if (stack.pop() < stack.pop()) { gt = 158; continue; } // 163 0 63
    case 98: stack.push(arg9); // 21 9
    case 100: stack.push(1); // 4
    case 101: stack.push(stack.pop() + stack.pop()); // 96
    case 102: arg10 = stack.pop() // 54 10
    case 104: stack.push(arg10); // 21 10
    case 106: stack.push(arg5); // 21 5
    case 108: stack.push(stack.pop() + stack.pop()); // 96
    case 109: stack.push(1); // 4
    case 110: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 111: arg11 = stack.pop() // 54 11
    case 113: stack.push(arg4); // 21 4
    case 115: stack.push(1); // 4
    case 116: stack.push(stack.pop() + stack.pop()); // 96
    case 117: arg12 = stack.pop() // 54 12
    case 119: stack.push(arg10); // 21 10
    case 121: stack.push(arg11); // 21 11
    case 123: if (stack.pop() <= stack.pop()) { gt = 146; continue; } // 162 0 23
    case 126: stack.push(arg0); // 42
    case 127: stack.push(arg10); // 21 10
    case 129: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 130: stack.push(arg3); // 45
    case 131: stack.push(arg12); // 21 12
    case 133: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 134: if (stack.pop() != stack.pop()) { gt = 146; continue; } // 160 0 12
    case 137: arg10++; // 132 10 1
    case 140: arg12++; // 132 12 1
    case 143: gt = 119; continue; // 167 255 232
    case 146: stack.push(arg10); // 21 10
    case 148: stack.push(arg11); // 21 11
    case 150: if (stack.pop() != stack.pop()) { gt = 158; continue; } // 160 0 8
    case 153: stack.push(arg9); // 21 9
    case 155: stack.push(arg1); // 27
    case 156: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 157: return stack.pop(); // 172
    case 158: arg9++; // 132 9 1
    case 161: gt = 53; continue; // 167 255 148
    case 164:  // 2
    case 165: return stack.pop(); // 172
  }
}
function java_lang_String_lastIndexOfILjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: stack.push(arg0); // 42
    case 3: stack.push(stack.pop().count); // 180 1 97
    case 6: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.lastIndexOfILjava_lang_StringI(self, v0, v1)); } // 182 1 151
    case 9: return stack.pop(); // 172
  }
}
function java_lang_String_lastIndexOfILjava_lang_StringI(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(7);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().value); // 180 1 100
    case 4: stack.push(arg0); // 42
    case 5: stack.push(stack.pop().offset); // 180 1 99
    case 8: stack.push(arg0); // 42
    case 9: stack.push(stack.pop().count); // 180 1 97
    case 12: stack.push(arg1); // 43
    case 13: stack.push(stack.pop().value); // 180 1 100
    case 16: stack.push(arg1); // 43
    case 17: stack.push(stack.pop().offset); // 180 1 99
    case 20: stack.push(arg1); // 43
    case 21: stack.push(stack.pop().count); // 180 1 97
    case 24: stack.push(arg2); // 28
    case 25: { var v6 = stack.pop(); var v5 = stack.pop(); var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_String_lastIndexOfAIACAIAIACAIAIAI(v0, v1, v2, v3, v4, v5, v6)); } // 184 1 145
    case 28: return stack.pop(); // 172
  }
}
function java_lang_String_lastIndexOfIACIIACIII(arg0,arg1,arg2,arg3,arg4,arg5,arg6) {
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var arg13;
  var arg14;
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg2); // 28
    case 1: stack.push(arg5); // 21 5
    case 3: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 4: arg7 = stack.pop() // 54 7
    case 6: stack.push(arg6); // 21 6
    case 8: if (stack.pop() >= 0) { gt = 13; continue; } // 156 0 5
    case 11:  // 2
    case 12: return stack.pop(); // 172
    case 13: stack.push(arg6); // 21 6
    case 15: stack.push(arg7); // 21 7
    case 17: if (stack.pop() >= stack.pop()) { gt = 24; continue; } // 164 0 7
    case 20: stack.push(arg7); // 21 7
    case 22: arg6 = stack.pop() // 54 6
    case 24: stack.push(arg5); // 21 5
    case 26: if (stack.pop() != 0) { gt = 32; continue; } // 154 0 6
    case 29: stack.push(arg6); // 21 6
    case 31: return stack.pop(); // 172
    case 32: stack.push(arg4); // 21 4
    case 34: stack.push(arg5); // 21 5
    case 36: stack.push(stack.pop() + stack.pop()); // 96
    case 37: stack.push(1); // 4
    case 38: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 39: arg8 = stack.pop() // 54 8
    case 41: stack.push(arg3); // 45
    case 42: stack.push(arg8); // 21 8
    case 44: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 45: arg9 = stack.pop() // 54 9
    case 47: stack.push(arg1); // 27
    case 48: stack.push(arg5); // 21 5
    case 50: stack.push(stack.pop() + stack.pop()); // 96
    case 51: stack.push(1); // 4
    case 52: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 53: arg10 = stack.pop() // 54 10
    case 55: stack.push(arg10); // 21 10
    case 57: stack.push(arg6); // 21 6
    case 59: stack.push(stack.pop() + stack.pop()); // 96
    case 60: arg11 = stack.pop() // 54 11
    case 62: stack.push(arg11); // 21 11
    case 64: stack.push(arg10); // 21 10
    case 66: if (stack.pop() > stack.pop()) { gt = 84; continue; } // 161 0 18
    case 69: stack.push(arg0); // 42
    case 70: stack.push(arg11); // 21 11
    case 72: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 73: stack.push(arg9); // 21 9
    case 75: if (stack.pop() == stack.pop()) { gt = 84; continue; } // 159 0 9
    case 78: arg11 += 255; // 132 11 255
    case 81: gt = 62; continue; // 167 255 237
    case 84: stack.push(arg11); // 21 11
    case 86: stack.push(arg10); // 21 10
    case 88: if (stack.pop() <= stack.pop()) { gt = 93; continue; } // 162 0 5
    case 91:  // 2
    case 92: return stack.pop(); // 172
    case 93: stack.push(arg11); // 21 11
    case 95: stack.push(1); // 4
    case 96: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 97: arg12 = stack.pop() // 54 12
    case 99: stack.push(arg12); // 21 12
    case 101: stack.push(arg5); // 21 5
    case 103: stack.push(1); // 4
    case 104: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 105: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 106: arg13 = stack.pop() // 54 13
    case 108: stack.push(arg8); // 21 8
    case 110: stack.push(1); // 4
    case 111: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 112: arg14 = stack.pop() // 54 14
    case 114: stack.push(arg12); // 21 12
    case 116: stack.push(arg13); // 21 13
    case 118: if (stack.pop() >= stack.pop()) { gt = 144; continue; } // 164 0 26
    case 121: stack.push(arg0); // 42
    case 122: stack.push(arg12); // 21 12
    case 124: arg12 += 255; // 132 12 255
    case 127: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 128: stack.push(arg3); // 45
    case 129: stack.push(arg14); // 21 14
    case 131: arg14 += 255; // 132 14 255
    case 134: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 135: if (stack.pop() == stack.pop()) { gt = 114; continue; } // 159 255 235
    case 138: arg11 += 255; // 132 11 255
    case 141: gt = 62; continue; // 167 255 177
    case 144: stack.push(arg13); // 21 13
    case 146: stack.push(arg1); // 27
    case 147: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 148: stack.push(1); // 4
    case 149: stack.push(stack.pop() + stack.pop()); // 96
    case 150: return stack.pop(); // 172
  }
}
*/
function java_lang_String_substringLjava_lang_StringI(arg0,arg1) {
    return arg0.toString().substring(arg1);
}
function java_lang_String_substringLjava_lang_StringII(arg0,arg1,arg2) {
    return arg0.toString().substring(arg1, arg2);
}

function java_lang_String_replaceLjava_lang_StringCC(arg0,arg1,arg2) {
    if (typeof arg1 === 'number') arg1 = String.fromCharCode(arg1);
    if (typeof arg2 === 'number') arg2 = String.fromCharCode(arg2);
    var s = arg0.toString();
    for (;;) {
        var ret = s.replace(arg1, arg2);
        if (ret === s) {
            return ret;
        }
        s = ret;
    }
}
function java_lang_String_containsZLjava_lang_CharSequence(arg0,arg1) {
    return arg0.toString().indexOf(arg1.toString()) >= 0;
}

/*
function java_lang_String_subSequenceLjava_lang_CharSequenceII(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 27
    case 2: stack.push(arg2); // 28
    case 3: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.substringLjava_lang_StringII(self, v0, v1)); } // 182 1 147
    case 6: return stack.pop(); // 176
  }
}
function java_lang_String_concatLjava_lang_StringLjava_lang_String(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
;
  var stack = new Array(5);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: { var self = stack.pop(); stack.push(self.lengthI(self)); } // 182 1 133
    case 4: arg2 = stack.pop(); // 61
    case 5: stack.push(arg2); // 28
    case 6: if (stack.pop() != 0) { gt = 11; continue; } // 154 0 5
    case 9: stack.push(arg0); // 42
    case 10: return stack.pop(); // 176
    case 11: stack.push(arg0); // 42
    case 12: stack.push(stack.pop().count); // 180 1 97
    case 15: stack.push(arg2); // 28
    case 16: stack.push(stack.pop() + stack.pop()); // 96
    case 17: stack.push(new Array(stack.pop())); // 188 5
    case 19: arg3 = stack.pop(); // 78
    case 20: stack.push(arg0); // 42
    case 21: stack.push(0); // 3
    case 22: stack.push(arg0); // 42
    case 23: stack.push(stack.pop().count); // 180 1 97
    case 26: stack.push(arg3); // 45
    case 27: stack.push(0); // 3
    case 28: { var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); self.getCharsVIIACAI(self, v0, v1, v2, v3); } // 182 1 138
    case 31: stack.push(arg1); // 43
    case 32: stack.push(0); // 3
    case 33: stack.push(arg2); // 28
    case 34: stack.push(arg3); // 45
    case 35: stack.push(arg0); // 42
    case 36: stack.push(stack.pop().count); // 180 1 97
    case 39: { var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); self.getCharsVIIACAI(self, v0, v1, v2, v3); } // 182 1 138
    case 42: stack.push(new java_lang_String); // 187 0 200
    case 45: stack.push(stack[stack.length - 1]); // 89
    case 46: stack.push(0); // 3
    case 47: stack.push(arg0); // 42
    case 48: stack.push(stack.pop().count); // 180 1 97
    case 51: stack.push(arg2); // 28
    case 52: stack.push(stack.pop() + stack.pop()); // 96
    case 53: stack.push(arg3); // 45
    case 54: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVIIAC(stack.pop(), v0, v1, v2); } // 183 1 137
    case 57: return stack.pop(); // 176
  }
}
function java_lang_String_matchesZLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: stack.push(arg0); // 42
    case 2: { var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_util_regex_Pattern_matchesZLjava_lang_StringLjava_lang_CharSequence(v0, v1)); } // 184 1 183
    case 5: return stack.pop(); // 172
  }
}
function java_lang_String_replaceFirstLjava_lang_StringLjava_lang_StringLjava_lang_String(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: { var v0 = stack.pop(); stack.push(java_util_regex_Pattern_compileLjava_util_regex_PatternLjava_lang_String(v0)); } // 184 1 186
    case 4: stack.push(arg0); // 42
    case 5: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.matcherLjava_util_regex_MatcherLjava_lang_CharSequence(self, v0)); } // 182 1 185
    case 8: stack.push(arg2); // 44
    case 9: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.replaceFirstLjava_lang_StringLjava_lang_String(self, v0)); } // 182 1 182
    case 12: return stack.pop(); // 176
  }
}
function java_lang_String_replaceAllLjava_lang_StringLjava_lang_StringLjava_lang_String(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: { var v0 = stack.pop(); stack.push(java_util_regex_Pattern_compileLjava_util_regex_PatternLjava_lang_String(v0)); } // 184 1 186
    case 4: stack.push(arg0); // 42
    case 5: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.matcherLjava_util_regex_MatcherLjava_lang_CharSequence(self, v0)); } // 182 1 185
    case 8: stack.push(arg2); // 44
    case 9: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.replaceAllLjava_lang_StringLjava_lang_String(self, v0)); } // 182 1 181
    case 12: return stack.pop(); // 176
  }
}
function java_lang_String_replaceLjava_lang_StringLjava_lang_CharSequenceLjava_lang_CharSequence(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: { var self = stack.pop(); stack.push(self.toStringLjava_lang_String(self)); } // 182 1 132
    case 4: stack.push(16); // 16 16
    case 6: { var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_util_regex_Pattern_compileLjava_util_regex_PatternLjava_lang_StringI(v0, v1)); } // 184 1 187
    case 9: stack.push(arg0); // 42
    case 10: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.matcherLjava_util_regex_MatcherLjava_lang_CharSequence(self, v0)); } // 182 1 185
    case 13: stack.push(arg2); // 44
    case 14: { var self = stack.pop(); stack.push(self.toStringLjava_lang_String(self)); } // 182 1 132
    case 17: { var v0 = stack.pop(); stack.push(java_util_regex_Matcher_quoteReplacementLjava_lang_StringLjava_lang_String(v0)); } // 184 1 180
    case 20: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.replaceAllLjava_lang_StringLjava_lang_String(self, v0)); } // 182 1 181
    case 23: return stack.pop(); // 176
  }
}
function java_lang_String_splitALjava_lang_StringLjava_lang_StringI(arg0,arg1,arg2) {
  var arg3;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: { var v0 = stack.pop(); stack.push(java_util_regex_Pattern_compileLjava_util_regex_PatternLjava_lang_String(v0)); } // 184 1 186
    case 4: stack.push(arg0); // 42
    case 5: stack.push(arg2); // 28
    case 6: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.splitALjava_lang_StringLjava_lang_CharSequenceI(self, v0, v1)); } // 182 1 184
    case 9: return stack.pop(); // 176
  }
}
function java_lang_String_splitALjava_lang_StringLjava_lang_String(arg0,arg1) {
  var arg2;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: stack.push(0); // 3
    case 3: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.splitALjava_lang_StringLjava_lang_StringI(self, v0, v1)); } // 182 1 157
    case 6: return stack.pop(); // 176
  }
}
function java_lang_String_toLowerCaseLjava_lang_StringLjava_util_Locale(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var arg13;
  var arg14;
;
  var stack = new Array(6);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: if (stack.pop()) { gt = 12; continue; } // 199 0 11
    case 4: stack.push(new java_lang_NullPointerException); // 187 0 198
    case 7: stack.push(stack[stack.length - 1]); // 89
    case 8: { java_lang_NullPointerException_consV(stack.pop()); } // 183 1 128
    case 11:  // 191
    case 12: stack.push(0); // 3
    case 13: arg2 = stack.pop(); // 61
    case 14: stack.push(arg2); // 28
    case 15: stack.push(arg0); // 42
    case 16: stack.push(stack.pop().count); // 180 1 97
    case 19: if (stack.pop() <= stack.pop()) { gt = 94; continue; } // 162 0 75
    case 22: stack.push(arg0); // 42
    case 23: stack.push(stack.pop().value); // 180 1 100
    case 26: stack.push(arg0); // 42
    case 27: stack.push(stack.pop().offset); // 180 1 99
    case 30: stack.push(arg2); // 28
    case 31: stack.push(stack.pop() + stack.pop()); // 96
    case 32: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 33: arg3 = stack.pop(); // 62
    case 34: stack.push(arg3); // 29
    case 35: stack.push(55296); // 18 1
    case 37: if (stack.pop() > stack.pop()) { gt = 77; continue; } // 161 0 40
    case 40: stack.push(arg3); // 29
    case 41: stack.push(56319); // 18 2
    case 43: if (stack.pop() < stack.pop()) { gt = 77; continue; } // 163 0 34
    case 46: stack.push(arg0); // 42
    case 47: stack.push(arg2); // 28
    case 48: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.codePointAtII(self, v0)); } // 182 1 134
    case 51: arg4 = stack.pop() // 54 4
    case 53: stack.push(arg4); // 21 4
    case 55: stack.push(arg4); // 21 4
    case 57: { var v0 = stack.pop(); stack.push(java_lang_Character_toLowerCaseII(v0)); } // 184 1 107
    case 60: if (stack.pop() == stack.pop()) { gt = 66; continue; } // 159 0 6
    case 63: gt = 96; continue; // 167 0 33
    case 66: stack.push(arg2); // 28
    case 67: stack.push(arg4); // 21 4
    case 69: { var v0 = stack.pop(); stack.push(java_lang_Character_charCountII(v0)); } // 184 1 106
    case 72: stack.push(stack.pop() + stack.pop()); // 96
    case 73: arg2 = stack.pop(); // 61
    case 74: gt = 91; continue; // 167 0 17
    case 77: stack.push(arg3); // 29
    case 78: stack.push(arg3); // 29
    case 79: { var v0 = stack.pop(); stack.push(java_lang_Character_toLowerCaseCC(v0)); } // 184 1 104
    case 82: if (stack.pop() == stack.pop()) { gt = 88; continue; } // 159 0 6
    case 85: gt = 96; continue; // 167 0 11
    case 88: arg2++; // 132 2 1
    case 91: gt = 14; continue; // 167 255 179
    case 94: stack.push(arg0); // 42
    case 95: return stack.pop(); // 176
    case 96: stack.push(arg0); // 42
    case 97: stack.push(stack.pop().count); // 180 1 97
    case 100: stack.push(new Array(stack.pop())); // 188 5
    case 102: arg3 = stack.pop(); // 78
    case 103: stack.push(0); // 3
    case 104: arg4 = stack.pop() // 54 4
    case 106: stack.push(arg0); // 42
    case 107: stack.push(stack.pop().value); // 180 1 100
    case 110: stack.push(arg0); // 42
    case 111: stack.push(stack.pop().offset); // 180 1 99
    case 114: stack.push(arg3); // 45
    case 115: stack.push(0); // 3
    case 116: stack.push(arg2); // 28
    case 117: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_System_arraycopyVLjava_lang_ObjectILjava_lang_ObjectII(v0, v1, v2, v3, v4); } // 184 1 171
    case 120: stack.push(arg1); // 43
    case 121: { var self = stack.pop(); stack.push(self.getLanguageLjava_lang_String(self)); } // 182 1 178
    case 124: arg5 = stack.pop() // 58 5
    case 126: stack.push(arg5); // 25 5
    case 128: stack.push("tr"); // 18 11
    case 130:  // 165
    case 131:  // 0
    case 132: stack.push(6405); // 17 25 5
    case 135: stack.push("az"); // 18 5
    case 137:  // 165
    case 138:  // 0
    case 139: stack.push(1); // 10
    case 140: stack.push(arg5); // 25 5
    case 142: stack.push("lt"); // 18 9
    case 144:  // 166
    case 145:  // 0
    case 146: stack.push(4); // 7
    case 147: stack.push(1); // 4
    case 148: gt = 152; continue; // 167 0 4
    case 151: stack.push(0); // 3
    case 152: arg6 = stack.pop() // 54 6
    case 154: stack.push(arg2); // 28
    case 155: arg11 = stack.pop() // 54 11
    case 157: stack.push(arg11); // 21 11
    case 159: stack.push(arg0); // 42
    case 160: stack.push(stack.pop().count); // 180 1 97
    case 163: if (stack.pop() <= stack.pop()) { gt = 419; continue; } // 162 1 0
    case 166: stack.push(arg0); // 42
    case 167: stack.push(stack.pop().value); // 180 1 100
    case 170: stack.push(arg0); // 42
    case 171: stack.push(stack.pop().offset); // 180 1 99
    case 174: stack.push(arg11); // 21 11
    case 176: stack.push(stack.pop() + stack.pop()); // 96
    case 177: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 178: arg9 = stack.pop() // 54 9
    case 180: stack.push(arg9); // 21 9
    case 182: // number conversion  // 146
    case 183: stack.push(55296); // 18 1
    case 185: if (stack.pop() > stack.pop()) { gt = 214; continue; } // 161 0 29
    case 188: stack.push(arg9); // 21 9
    case 190: // number conversion  // 146
    case 191: stack.push(56319); // 18 2
    case 193: if (stack.pop() < stack.pop()) { gt = 214; continue; } // 163 0 21
    case 196: stack.push(arg0); // 42
    case 197: stack.push(arg11); // 21 11
    case 199: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.codePointAtII(self, v0)); } // 182 1 134
    case 202: arg9 = stack.pop() // 54 9
    case 204: stack.push(arg9); // 21 9
    case 206: { var v0 = stack.pop(); stack.push(java_lang_Character_charCountII(v0)); } // 184 1 106
    case 209: arg10 = stack.pop() // 54 10
    case 211: gt = 217; continue; // 167 0 6
    case 214: stack.push(1); // 4
    case 215: arg10 = stack.pop() // 54 10
    case 217: stack.push(arg6); // 21 6
    case 219: if (stack.pop() != 0) { gt = 230; continue; } // 154 0 11
    case 222: stack.push(arg9); // 21 9
    case 224: stack.push(931); // 17 3 163
    case 227: if (stack.pop() != stack.pop()) { gt = 242; continue; } // 160 0 15
    case 230: stack.push(arg0); // 42
    case 231: stack.push(arg11); // 21 11
    case 233: stack.push(arg1); // 43
    case 234: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_ConditionalSpecialCasing_toLowerCaseExILjava_lang_StringILjava_util_Locale(v0, v1, v2)); } // 184 1 117
    case 237: arg8 = stack.pop() // 54 8
    case 239: gt = 249; continue; // 167 0 10
    case 242: stack.push(arg9); // 21 9
    case 244: { var v0 = stack.pop(); stack.push(java_lang_Character_toLowerCaseII(v0)); } // 184 1 107
    case 247: arg8 = stack.pop() // 54 8
    case 249: stack.push(arg8); // 21 8
    case 251:  // 2
    case 252: if (stack.pop() == stack.pop()) { gt = 262; continue; } // 159 0 10
    case 255: stack.push(arg8); // 21 8
    case 257: stack.push(65536); // 18 3
    case 259: if (stack.pop() > stack.pop()) { gt = 399; continue; } // 161 0 140
    case 262: stack.push(arg8); // 21 8
    case 264:  // 2
    case 265: if (stack.pop() != stack.pop()) { gt = 280; continue; } // 160 0 15
    case 268: stack.push(arg0); // 42
    case 269: stack.push(arg11); // 21 11
    case 271: stack.push(arg1); // 43
    case 272: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_ConditionalSpecialCasing_toLowerCaseCharArrayACLjava_lang_StringILjava_util_Locale(v0, v1, v2)); } // 184 1 119
    case 275: arg7 = stack.pop() // 58 7
    case 277: gt = 315; continue; // 167 0 38
    case 280: stack.push(arg10); // 21 10
    case 282: stack.push(2); // 5
    case 283: if (stack.pop() != stack.pop()) { gt = 308; continue; } // 160 0 25
    case 286: stack.push(arg4); // 21 4
    case 288: stack.push(arg8); // 21 8
    case 290: stack.push(arg3); // 45
    case 291: stack.push(arg11); // 21 11
    case 293: stack.push(arg4); // 21 4
    case 295: stack.push(stack.pop() + stack.pop()); // 96
    case 296: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_toCharsAIIACAI(v0, v1, v2)); } // 184 1 111
    case 299: stack.push(arg10); // 21 10
    case 301: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 302: stack.push(stack.pop() + stack.pop()); // 96
    case 303: arg4 = stack.pop() // 54 4
    case 305: gt = 409; continue; // 167 0 104
    case 308: stack.push(arg8); // 21 8
    case 310: { var v0 = stack.pop(); stack.push(java_lang_Character_toCharsACI(v0)); } // 184 1 109
    case 313: arg7 = stack.pop() // 58 7
    case 315: stack.push(arg7); // 25 7
    case 317: stack.push(stack.pop().length); // 190
    case 318: arg12 = stack.pop() // 54 12
    case 320: stack.push(arg12); // 21 12
    case 322: stack.push(arg10); // 21 10
    case 324: if (stack.pop() >= stack.pop()) { gt = 355; continue; } // 164 0 31
    case 327: stack.push(arg3); // 45
    case 328: stack.push(stack.pop().length); // 190
    case 329: stack.push(arg12); // 21 12
    case 331: stack.push(stack.pop() + stack.pop()); // 96
    case 332: stack.push(arg10); // 21 10
    case 334: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 335: stack.push(new Array(stack.pop())); // 188 5
    case 337: arg13 = stack.pop() // 58 13
    case 339: stack.push(arg3); // 45
    case 340: stack.push(0); // 3
    case 341: stack.push(arg13); // 25 13
    case 343: stack.push(0); // 3
    case 344: stack.push(arg11); // 21 11
    case 346: stack.push(arg4); // 21 4
    case 348: stack.push(stack.pop() + stack.pop()); // 96
    case 349: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_System_arraycopyVLjava_lang_ObjectILjava_lang_ObjectII(v0, v1, v2, v3, v4); } // 184 1 171
    case 352: stack.push(arg13); // 25 13
    case 354: arg3 = stack.pop(); // 78
    case 355: stack.push(0); // 3
    case 356: arg13 = stack.pop() // 54 13
    case 358: stack.push(arg13); // 21 13
    case 360: stack.push(arg12); // 21 12
    case 362: if (stack.pop() <= stack.pop()) { gt = 386; continue; } // 162 0 24
    case 365: stack.push(arg3); // 45
    case 366: stack.push(arg11); // 21 11
    case 368: stack.push(arg4); // 21 4
    case 370: stack.push(stack.pop() + stack.pop()); // 96
    case 371: stack.push(arg13); // 21 13
    case 373: stack.push(stack.pop() + stack.pop()); // 96
    case 374: stack.push(arg7); // 25 7
    case 376: stack.push(arg13); // 21 13
    case 378: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 379: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 85
    case 380: arg13++; // 132 13 1
    case 383: gt = 358; continue; // 167 255 231
    case 386: stack.push(arg4); // 21 4
    case 388: stack.push(arg12); // 21 12
    case 390: stack.push(arg10); // 21 10
    case 392: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 393: stack.push(stack.pop() + stack.pop()); // 96
    case 394: arg4 = stack.pop() // 54 4
    case 396: gt = 409; continue; // 167 0 13
    case 399: stack.push(arg3); // 45
    case 400: stack.push(arg11); // 21 11
    case 402: stack.push(arg4); // 21 4
    case 404: stack.push(stack.pop() + stack.pop()); // 96
    case 405: stack.push(arg8); // 21 8
    case 407: // number conversion  // 146
    case 408: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 85
    case 409: stack.push(arg11); // 21 11
    case 411: stack.push(arg10); // 21 10
    case 413: stack.push(stack.pop() + stack.pop()); // 96
    case 414: arg11 = stack.pop() // 54 11
    case 416: gt = 157; continue; // 167 254 253
    case 419: stack.push(new java_lang_String); // 187 0 200
    case 422: stack.push(stack[stack.length - 1]); // 89
    case 423: stack.push(0); // 3
    case 424: stack.push(arg0); // 42
    case 425: stack.push(stack.pop().count); // 180 1 97
    case 428: stack.push(arg4); // 21 4
    case 430: stack.push(stack.pop() + stack.pop()); // 96
    case 431: stack.push(arg3); // 45
    case 432: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVIIAC(stack.pop(), v0, v1, v2); } // 183 1 137
    case 435: return stack.pop(); // 176
  }
}
function java_lang_String_toLowerCaseLjava_lang_String(arg0) {
  var arg1;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: { stack.push(java_util_Locale_getDefaultLjava_util_Locale()); } // 184 1 179
    case 4: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.toLowerCaseLjava_lang_StringLjava_util_Locale(self, v0)); } // 182 1 158
    case 7: return stack.pop(); // 176
  }
}
function java_lang_String_toUpperCaseLjava_lang_StringLjava_util_Locale(arg0,arg1) {
  var arg2;
  var arg3;
  var arg4;
  var arg5;
  var arg6;
  var arg7;
  var arg8;
  var arg9;
  var arg10;
  var arg11;
  var arg12;
  var arg13;
  var arg14;
;
  var stack = new Array(6);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg1); // 43
    case 1: if (stack.pop()) { gt = 12; continue; } // 199 0 11
    case 4: stack.push(new java_lang_NullPointerException); // 187 0 198
    case 7: stack.push(stack[stack.length - 1]); // 89
    case 8: { java_lang_NullPointerException_consV(stack.pop()); } // 183 1 128
    case 11:  // 191
    case 12: stack.push(0); // 3
    case 13: arg2 = stack.pop(); // 61
    case 14: stack.push(arg2); // 28
    case 15: stack.push(arg0); // 42
    case 16: stack.push(stack.pop().count); // 180 1 97
    case 19: if (stack.pop() <= stack.pop()) { gt = 93; continue; } // 162 0 74
    case 22: stack.push(arg0); // 42
    case 23: stack.push(stack.pop().value); // 180 1 100
    case 26: stack.push(arg0); // 42
    case 27: stack.push(stack.pop().offset); // 180 1 99
    case 30: stack.push(arg2); // 28
    case 31: stack.push(stack.pop() + stack.pop()); // 96
    case 32: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 33: arg3 = stack.pop(); // 62
    case 34: stack.push(arg3); // 29
    case 35: stack.push(55296); // 18 1
    case 37: if (stack.pop() > stack.pop()) { gt = 61; continue; } // 161 0 24
    case 40: stack.push(arg3); // 29
    case 41: stack.push(56319); // 18 2
    case 43: if (stack.pop() < stack.pop()) { gt = 61; continue; } // 163 0 18
    case 46: stack.push(arg0); // 42
    case 47: stack.push(arg2); // 28
    case 48: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.codePointAtII(self, v0)); } // 182 1 134
    case 51: arg3 = stack.pop(); // 62
    case 52: stack.push(arg3); // 29
    case 53: { var v0 = stack.pop(); stack.push(java_lang_Character_charCountII(v0)); } // 184 1 106
    case 56: arg4 = stack.pop() // 54 4
    case 58: gt = 64; continue; // 167 0 6
    case 61: stack.push(1); // 4
    case 62: arg4 = stack.pop() // 54 4
    case 64: stack.push(arg3); // 29
    case 65: { var v0 = stack.pop(); stack.push(java_lang_Character_toUpperCaseExII(v0)); } // 184 1 108
    case 68: arg5 = stack.pop() // 54 5
    case 70: stack.push(arg5); // 21 5
    case 72:  // 2
    case 73: if (stack.pop() == stack.pop()) { gt = 95; continue; } // 159 0 22
    case 76: stack.push(arg3); // 29
    case 77: stack.push(arg5); // 21 5
    case 79: if (stack.pop() == stack.pop()) { gt = 85; continue; } // 159 0 6
    case 82: gt = 95; continue; // 167 0 13
    case 85: stack.push(arg2); // 28
    case 86: stack.push(arg4); // 21 4
    case 88: stack.push(stack.pop() + stack.pop()); // 96
    case 89: arg2 = stack.pop(); // 61
    case 90: gt = 14; continue; // 167 255 180
    case 93: stack.push(arg0); // 42
    case 94: return stack.pop(); // 176
    case 95: stack.push(arg0); // 42
    case 96: stack.push(stack.pop().count); // 180 1 97
    case 99: stack.push(new Array(stack.pop())); // 188 5
    case 101: arg3 = stack.pop(); // 78
    case 102: stack.push(0); // 3
    case 103: arg4 = stack.pop() // 54 4
    case 105: stack.push(arg0); // 42
    case 106: stack.push(stack.pop().value); // 180 1 100
    case 109: stack.push(arg0); // 42
    case 110: stack.push(stack.pop().offset); // 180 1 99
    case 113: stack.push(arg3); // 45
    case 114: stack.push(0); // 3
    case 115: stack.push(arg2); // 28
    case 116: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_System_arraycopyVLjava_lang_ObjectILjava_lang_ObjectII(v0, v1, v2, v3, v4); } // 184 1 171
    case 119: stack.push(arg1); // 43
    case 120: { var self = stack.pop(); stack.push(self.getLanguageLjava_lang_String(self)); } // 182 1 178
    case 123: arg5 = stack.pop() // 58 5
    case 125: stack.push(arg5); // 25 5
    case 127: stack.push("tr"); // 18 11
    case 129:  // 165
    case 130:  // 0
    case 131: stack.push(6405); // 17 25 5
    case 134: stack.push("az"); // 18 5
    case 136:  // 165
    case 137:  // 0
    case 138: stack.push(1); // 10
    case 139: stack.push(arg5); // 25 5
    case 141: stack.push("lt"); // 18 9
    case 143:  // 166
    case 144:  // 0
    case 145: stack.push(4); // 7
    case 146: stack.push(1); // 4
    case 147: gt = 151; continue; // 167 0 4
    case 150: stack.push(0); // 3
    case 151: arg6 = stack.pop() // 54 6
    case 153: stack.push(arg2); // 28
    case 154: arg11 = stack.pop() // 54 11
    case 156: stack.push(arg11); // 21 11
    case 158: stack.push(arg0); // 42
    case 159: stack.push(stack.pop().count); // 180 1 97
    case 162: if (stack.pop() <= stack.pop()) { gt = 425; continue; } // 162 1 7
    case 165: stack.push(arg0); // 42
    case 166: stack.push(stack.pop().value); // 180 1 100
    case 169: stack.push(arg0); // 42
    case 170: stack.push(stack.pop().offset); // 180 1 99
    case 173: stack.push(arg11); // 21 11
    case 175: stack.push(stack.pop() + stack.pop()); // 96
    case 176: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 177: arg9 = stack.pop() // 54 9
    case 179: stack.push(arg9); // 21 9
    case 181: // number conversion  // 146
    case 182: stack.push(55296); // 18 1
    case 184: if (stack.pop() > stack.pop()) { gt = 213; continue; } // 161 0 29
    case 187: stack.push(arg9); // 21 9
    case 189: // number conversion  // 146
    case 190: stack.push(56319); // 18 2
    case 192: if (stack.pop() < stack.pop()) { gt = 213; continue; } // 163 0 21
    case 195: stack.push(arg0); // 42
    case 196: stack.push(arg11); // 21 11
    case 198: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.codePointAtII(self, v0)); } // 182 1 134
    case 201: arg9 = stack.pop() // 54 9
    case 203: stack.push(arg9); // 21 9
    case 205: { var v0 = stack.pop(); stack.push(java_lang_Character_charCountII(v0)); } // 184 1 106
    case 208: arg10 = stack.pop() // 54 10
    case 210: gt = 216; continue; // 167 0 6
    case 213: stack.push(1); // 4
    case 214: arg10 = stack.pop() // 54 10
    case 216: stack.push(arg6); // 21 6
    case 218: if (stack.pop() == 0) { gt = 233; continue; } // 153 0 15
    case 221: stack.push(arg0); // 42
    case 222: stack.push(arg11); // 21 11
    case 224: stack.push(arg1); // 43
    case 225: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_ConditionalSpecialCasing_toUpperCaseExILjava_lang_StringILjava_util_Locale(v0, v1, v2)); } // 184 1 118
    case 228: arg8 = stack.pop() // 54 8
    case 230: gt = 240; continue; // 167 0 10
    case 233: stack.push(arg9); // 21 9
    case 235: { var v0 = stack.pop(); stack.push(java_lang_Character_toUpperCaseExII(v0)); } // 184 1 108
    case 238: arg8 = stack.pop() // 54 8
    case 240: stack.push(arg8); // 21 8
    case 242:  // 2
    case 243: if (stack.pop() == stack.pop()) { gt = 253; continue; } // 159 0 10
    case 246: stack.push(arg8); // 21 8
    case 248: stack.push(65536); // 18 3
    case 250: if (stack.pop() > stack.pop()) { gt = 405; continue; } // 161 0 155
    case 253: stack.push(arg8); // 21 8
    case 255:  // 2
    case 256: if (stack.pop() != stack.pop()) { gt = 286; continue; } // 160 0 30
    case 259: stack.push(arg6); // 21 6
    case 261: if (stack.pop() == 0) { gt = 276; continue; } // 153 0 15
    case 264: stack.push(arg0); // 42
    case 265: stack.push(arg11); // 21 11
    case 267: stack.push(arg1); // 43
    case 268: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_ConditionalSpecialCasing_toUpperCaseCharArrayACLjava_lang_StringILjava_util_Locale(v0, v1, v2)); } // 184 1 120
    case 271: arg7 = stack.pop() // 58 7
    case 273: gt = 321; continue; // 167 0 48
    case 276: stack.push(arg9); // 21 9
    case 278: { var v0 = stack.pop(); stack.push(java_lang_Character_toUpperCaseCharArrayACI(v0)); } // 184 1 110
    case 281: arg7 = stack.pop() // 58 7
    case 283: gt = 321; continue; // 167 0 38
    case 286: stack.push(arg10); // 21 10
    case 288: stack.push(2); // 5
    case 289: if (stack.pop() != stack.pop()) { gt = 314; continue; } // 160 0 25
    case 292: stack.push(arg4); // 21 4
    case 294: stack.push(arg8); // 21 8
    case 296: stack.push(arg3); // 45
    case 297: stack.push(arg11); // 21 11
    case 299: stack.push(arg4); // 21 4
    case 301: stack.push(stack.pop() + stack.pop()); // 96
    case 302: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Character_toCharsAIIACAI(v0, v1, v2)); } // 184 1 111
    case 305: stack.push(arg10); // 21 10
    case 307: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 308: stack.push(stack.pop() + stack.pop()); // 96
    case 309: arg4 = stack.pop() // 54 4
    case 311: gt = 415; continue; // 167 0 104
    case 314: stack.push(arg8); // 21 8
    case 316: { var v0 = stack.pop(); stack.push(java_lang_Character_toCharsACI(v0)); } // 184 1 109
    case 319: arg7 = stack.pop() // 58 7
    case 321: stack.push(arg7); // 25 7
    case 323: stack.push(stack.pop().length); // 190
    case 324: arg12 = stack.pop() // 54 12
    case 326: stack.push(arg12); // 21 12
    case 328: stack.push(arg10); // 21 10
    case 330: if (stack.pop() >= stack.pop()) { gt = 361; continue; } // 164 0 31
    case 333: stack.push(arg3); // 45
    case 334: stack.push(stack.pop().length); // 190
    case 335: stack.push(arg12); // 21 12
    case 337: stack.push(stack.pop() + stack.pop()); // 96
    case 338: stack.push(arg10); // 21 10
    case 340: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 341: stack.push(new Array(stack.pop())); // 188 5
    case 343: arg13 = stack.pop() // 58 13
    case 345: stack.push(arg3); // 45
    case 346: stack.push(0); // 3
    case 347: stack.push(arg13); // 25 13
    case 349: stack.push(0); // 3
    case 350: stack.push(arg11); // 21 11
    case 352: stack.push(arg4); // 21 4
    case 354: stack.push(stack.pop() + stack.pop()); // 96
    case 355: { var v4 = stack.pop(); var v3 = stack.pop(); var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_System_arraycopyVLjava_lang_ObjectILjava_lang_ObjectII(v0, v1, v2, v3, v4); } // 184 1 171
    case 358: stack.push(arg13); // 25 13
    case 360: arg3 = stack.pop(); // 78
    case 361: stack.push(0); // 3
    case 362: arg13 = stack.pop() // 54 13
    case 364: stack.push(arg13); // 21 13
    case 366: stack.push(arg12); // 21 12
    case 368: if (stack.pop() <= stack.pop()) { gt = 392; continue; } // 162 0 24
    case 371: stack.push(arg3); // 45
    case 372: stack.push(arg11); // 21 11
    case 374: stack.push(arg4); // 21 4
    case 376: stack.push(stack.pop() + stack.pop()); // 96
    case 377: stack.push(arg13); // 21 13
    case 379: stack.push(stack.pop() + stack.pop()); // 96
    case 380: stack.push(arg7); // 25 7
    case 382: stack.push(arg13); // 21 13
    case 384: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 385: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 85
    case 386: arg13++; // 132 13 1
    case 389: gt = 364; continue; // 167 255 231
    case 392: stack.push(arg4); // 21 4
    case 394: stack.push(arg12); // 21 12
    case 396: stack.push(arg10); // 21 10
    case 398: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 399: stack.push(stack.pop() + stack.pop()); // 96
    case 400: arg4 = stack.pop() // 54 4
    case 402: gt = 415; continue; // 167 0 13
    case 405: stack.push(arg3); // 45
    case 406: stack.push(arg11); // 21 11
    case 408: stack.push(arg4); // 21 4
    case 410: stack.push(stack.pop() + stack.pop()); // 96
    case 411: stack.push(arg8); // 21 8
    case 413: // number conversion  // 146
    case 414: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 85
    case 415: stack.push(arg11); // 21 11
    case 417: stack.push(arg10); // 21 10
    case 419: stack.push(stack.pop() + stack.pop()); // 96
    case 420: arg11 = stack.pop() // 54 11
    case 422: gt = 156; continue; // 167 254 246
    case 425: stack.push(new java_lang_String); // 187 0 200
    case 428: stack.push(stack[stack.length - 1]); // 89
    case 429: stack.push(0); // 3
    case 430: stack.push(arg0); // 42
    case 431: stack.push(stack.pop().count); // 180 1 97
    case 434: stack.push(arg4); // 21 4
    case 436: stack.push(stack.pop() + stack.pop()); // 96
    case 437: stack.push(arg3); // 45
    case 438: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVIIAC(stack.pop(), v0, v1, v2); } // 183 1 137
    case 441: return stack.pop(); // 176
  }
}
function java_lang_String_toUpperCaseLjava_lang_String(arg0) {
  var arg1;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: { stack.push(java_util_Locale_getDefaultLjava_util_Locale()); } // 184 1 179
    case 4: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.toUpperCaseLjava_lang_StringLjava_util_Locale(self, v0)); } // 182 1 159
    case 7: return stack.pop(); // 176
  }
}
function java_lang_String_trimLjava_lang_String(arg0) {
  var arg1;
  var arg2;
  var arg3;
  var arg4;
  var arg5;
;
  var stack = new Array(3);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(stack.pop().count); // 180 1 97
    case 4: arg1 = stack.pop(); // 60
    case 5: stack.push(0); // 3
    case 6: arg2 = stack.pop(); // 61
    case 7: stack.push(arg0); // 42
    case 8: stack.push(stack.pop().offset); // 180 1 99
    case 11: arg3 = stack.pop(); // 62
    case 12: stack.push(arg0); // 42
    case 13: stack.push(stack.pop().value); // 180 1 100
    case 16: arg4 = stack.pop() // 58 4
    case 18: stack.push(arg2); // 28
    case 19: stack.push(arg1); // 27
    case 20: if (stack.pop() <= stack.pop()) { gt = 40; continue; } // 162 0 20
    case 23: stack.push(arg4); // 25 4
    case 25: stack.push(arg3); // 29
    case 26: stack.push(arg2); // 28
    case 27: stack.push(stack.pop() + stack.pop()); // 96
    case 28: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 29: stack.push(32); // 16 32
    case 31: if (stack.pop() < stack.pop()) { gt = 40; continue; } // 163 0 9
    case 34: arg2++; // 132 2 1
    case 37: gt = 18; continue; // 167 255 237
    case 40: stack.push(arg2); // 28
    case 41: stack.push(arg1); // 27
    case 42: if (stack.pop() <= stack.pop()) { gt = 64; continue; } // 162 0 22
    case 45: stack.push(arg4); // 25 4
    case 47: stack.push(arg3); // 29
    case 48: stack.push(arg1); // 27
    case 49: stack.push(stack.pop() + stack.pop()); // 96
    case 50: stack.push(1); // 4
    case 51: { var tmp = stack.pop(); stack.push(stack.pop() - tmp); } // 100
    case 52: { var indx = stack.pop(); stack.push(stack.pop()[indx]); } // 52
    case 53: stack.push(32); // 16 32
    case 55: if (stack.pop() < stack.pop()) { gt = 64; continue; } // 163 0 9
    case 58: arg1 += 255; // 132 1 255
    case 61: gt = 40; continue; // 167 255 235
    case 64: stack.push(arg2); // 28
    case 65: if (stack.pop() > 0) { gt = 76; continue; } // 157 0 11
    case 68: stack.push(arg1); // 27
    case 69: stack.push(arg0); // 42
    case 70: stack.push(stack.pop().count); // 180 1 97
    case 73: if (stack.pop() <= stack.pop()) { gt = 85; continue; } // 162 0 12
    case 76: stack.push(arg0); // 42
    case 77: stack.push(arg2); // 28
    case 78: stack.push(arg1); // 27
    case 79: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.substringLjava_lang_StringII(self, v0, v1)); } // 182 1 147
    case 82: gt = 86; continue; // 167 0 4
    case 85: stack.push(arg0); // 42
    case 86: return stack.pop(); // 176
  }
}
*/
function java_lang_String_toStringLjava_lang_String(arg0) {
    return arg0.toString();
}
function java_lang_String_toCharArrayAC(arg0) {
    return arg0.toString().split('');
}
/*
function java_lang_String_formatLjava_lang_StringLjava_lang_StringLjava_lang_Object(arg0,arg1) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(new java_util_Formatter); // 187 0 211
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: { java_util_Formatter_consV(stack.pop()); } // 183 1 174
    case 7: stack.push(arg0); // 42
    case 8: stack.push(arg1); // 43
    case 9: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.formatALjava_util_FormatterLjava_lang_StringALjava_lang_Object(self, v0, v1)); } // 182 1 177
    case 12: { var self = stack.pop(); stack.push(self.toStringLjava_lang_String(self)); } // 182 1 175
    case 15: return stack.pop(); // 176
  }
}
function java_lang_String_formatLjava_lang_StringLjava_util_LocaleLjava_lang_StringLjava_lang_Object(arg0,arg1,arg2) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(new java_util_Formatter); // 187 0 211
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: stack.push(arg0); // 42
    case 5: { var v0 = stack.pop(); java_util_Formatter_consVLjava_util_Locale(stack.pop(), v0); } // 183 1 176
    case 8: stack.push(arg1); // 43
    case 9: stack.push(arg2); // 44
    case 10: { var v1 = stack.pop(); var v0 = stack.pop(); var self = stack.pop(); stack.push(self.formatALjava_util_FormatterLjava_lang_StringALjava_lang_Object(self, v0, v1)); } // 182 1 177
    case 13: { var self = stack.pop(); stack.push(self.toStringLjava_lang_String(self)); } // 182 1 175
    case 16: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringLjava_lang_Object(arg0) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: if (stack.pop()) { gt = 9; continue; } // 199 0 8
    case 4: stack.push("null"); // 18 10
    case 6: gt = 13; continue; // 167 0 7
    case 9: stack.push(arg0); // 42
    case 10: { var self = stack.pop(); stack.push(self.toStringLjava_lang_String(self)); } // 182 1 132
    case 13: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringAC(arg0) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(new java_lang_String); // 187 0 200
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: stack.push(arg0); // 42
    case 5: { var v0 = stack.pop(); java_lang_String_consVAC(stack.pop(), v0); } // 183 1 142
    case 8: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringACII(arg0,arg1,arg2) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(new java_lang_String); // 187 0 200
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: stack.push(arg0); // 42
    case 5: stack.push(arg1); // 27
    case 6: stack.push(arg2); // 28
    case 7: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVACAIAI(stack.pop(), v0, v1, v2); } // 183 1 143
    case 10: return stack.pop(); // 176
  }
}
function java_lang_String_copyValueOfLjava_lang_StringACII(arg0,arg1,arg2) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(new java_lang_String); // 187 0 200
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: stack.push(arg0); // 42
    case 5: stack.push(arg1); // 27
    case 6: stack.push(arg2); // 28
    case 7: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVACAIAI(stack.pop(), v0, v1, v2); } // 183 1 143
    case 10: return stack.pop(); // 176
  }
}
function java_lang_String_copyValueOfLjava_lang_StringAC(arg0) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(0); // 3
    case 2: stack.push(arg0); // 42
    case 3: stack.push(stack.pop().length); // 190
    case 4: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_String_copyValueOfALjava_lang_StringACAIAI(v0, v1, v2)); } // 184 1 155
    case 7: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringZ(arg0) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 26
    case 1: if (stack.pop() == 0) { gt = 9; continue; } // 153 0 8
    case 4: stack.push("true"); // 18 12
    case 6: gt = 11; continue; // 167 0 5
    case 9: stack.push("false"); // 18 8
    case 11: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringC(arg0) {
  var arg1;
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(1); // 4
    case 1: stack.push(new Array(stack.pop())); // 188 5
    case 3: stack.push(stack[stack.length - 1]); // 89
    case 4: stack.push(0); // 3
    case 5: stack.push(arg0); // 26
    case 6: { var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; } // 85
    case 7: arg1 = stack.pop(); // 76
    case 8: stack.push(new java_lang_String); // 187 0 200
    case 11: stack.push(stack[stack.length - 1]); // 89
    case 12: stack.push(0); // 3
    case 13: stack.push(1); // 4
    case 14: stack.push(arg1); // 43
    case 15: { var v2 = stack.pop(); var v1 = stack.pop(); var v0 = stack.pop(); java_lang_String_consVIIAC(stack.pop(), v0, v1, v2); } // 183 1 137
    case 18: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringJ(arg0) {
  var arg1;
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 30
    case 1: stack.push(10); // 16 10
    case 3: { var v1 = stack.pop(); var v0 = stack.pop(); stack.push(java_lang_Long_toStringLjava_lang_StringJI(v0, v1)); } // 184 1 126
    case 6: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringF(arg0) {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 34
    case 1: { var v0 = stack.pop(); stack.push(java_lang_Float_toStringLjava_lang_StringF(v0)); } // 184 1 122
    case 4: return stack.pop(); // 176
  }
}
function java_lang_String_valueOfLjava_lang_StringD(arg0) {
  var arg1;
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 38
    case 1: { var v0 = stack.pop(); stack.push(java_lang_Double_toStringLjava_lang_StringD(v0)); } // 184 1 121
    case 4: return stack.pop(); // 176
  }
}
function java_lang_String_internLjava_lang_String(arg0) {
  // no code found for null 
}
function java_lang_String_compareToILjava_lang_Object(arg0,arg1) {
  var arg2;
;
  var stack = new Array(2);
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(arg0); // 42
    case 1: stack.push(arg1); // 43
    case 2: if(stack[stack.length - 1].$instOf_java_lang_String != 1) throw {}; // 192 0 200
    case 5: { var v0 = stack.pop(); var self = stack.pop(); stack.push(self.compareToILjava_lang_String(self, v0)); } // 182 1 148
    case 8: return stack.pop(); // 172
  }
}
function java_lang_String_classV() {
  var stack = new Array();
  var gt = 0;
  for(;;) switch(gt) {
    case 0: stack.push(0); // 3
    case 1: stack.push(new Array(stack.pop())); // 189 0 183
    case 4: java_lang_String_serialPersistentFields = stack.pop(); // 179 1 101
    case 7: stack.push(new java_lang_String$CaseInsensitiveComparator); // 187 0 202
    case 10: stack.push(stack[stack.length - 1]); // 89
    case 11:  // 1
    case 12: { var v0 = stack.pop(); java_lang_String$CaseInsensitiveComparator_consVLjava_lang_String$1(stack.pop(), v0); } // 183 1 160
    case 15: java_lang_String_CASE_INSENSITIVE_ORDER = stack.pop(); // 179 1 102
    case 18: return; // 177
  }
}
*/
var java_lang_String_serialVersionUID = 0;
var java_lang_String_serialPersistentFields = 0;
var java_lang_String_CASE_INSENSITIVE_ORDER = 0;
function java_lang_String() {
  /** the real value of this 'string' we delegate to */
  this.r = '';
  
  var self = this;
    /*
  this.value = 0;
  this.offset = 0;
  this.count = 0;
  this.hash = 0;
  */
  this.toString = function() { return self.r; };
}
java_lang_String.prototype = new String;
//java_lang_String_classV();

/* new method for JavaScript String */
String.prototype.consVAC = java_lang_String_consVAC;
String.prototype.consVACII = java_lang_String_consVACII;
String.prototype.charAtCI = java_lang_String_charAtCI;
String.prototype.lengthI = java_lang_String_lengthI;
String.prototype.isEmptyZ = java_lang_String_isEmptyZ;
String.prototype.getCharsVIIACI = java_lang_String_getCharsVIIACAI;
String.prototype.getCharsVACI = java_lang_String_getCharsVACI;
String.prototype.toStringLjava_lang_String = java_lang_String_toStringLjava_lang_String;
String.prototype.substringLjava_lang_StringI = java_lang_String_substringLjava_lang_StringI;
String.prototype.substringLjava_lang_StringII = java_lang_String_substringLjava_lang_StringII;
String.prototype.replaceLjava_lang_StringCC = java_lang_String_replaceLjava_lang_StringCC;
String.prototype.containsZLjava_lang_CharSequence = java_lang_String_containsZLjava_lang_CharSequence;
String.prototype.equalsZLjava_lang_Object = java_lang_String_equalsZLjava_lang_Object;
String.prototype.hashCodeI = java_lang_String_hashCodeI;
String.prototype.toCharArrayAC = java_lang_String_toCharArrayAC;
String.prototype.valueOfLjava_lang_StringI=java_lang_String_valueOfLjava_lang_StringI;
String.prototype.startsWithZLjava_lang_StringI = java_lang_String_startsWithZLjava_lang_StringI;
String.prototype.startsWithZLjava_lang_String=java_lang_String_startsWithZLjava_lang_String;
String.prototype.endsWithZLjava_lang_String=java_lang_String_endsWithZLjava_lang_String;
String.prototype.indexOfII=java_lang_String_indexOfII;
String.prototype.indexOfIII=java_lang_String_indexOfIII;

String.prototype.$instOf_java_lang_String = true;
String.prototype.$instOf_java_io_Serializable = true;
String.prototype.$instOf_java_lang_Comparable = true;
String.prototype.$instOf_java_lang_CharSequence = true;

/*
  this.lengthI = java_lang_String_lengthI;
  this.isEmptyZ = java_lang_String_isEmptyZ;
  this.charAtCI = java_lang_String_charAtCI;
  this.codePointAtII = java_lang_String_codePointAtII;
  this.codePointBeforeII = java_lang_String_codePointBeforeII;
  this.codePointCountIII = java_lang_String_codePointCountIII;
  this.offsetByCodePointsIII = java_lang_String_offsetByCodePointsIII;
  this.getCharsVACI = java_lang_String_getCharsVACI;
  this.getCharsVIIACI = java_lang_String_getCharsVIIACI;
  this.getBytesVIIABI = java_lang_String_getBytesVIIABI;
  this.getBytesABLjava_lang_String = java_lang_String_getBytesABLjava_lang_String;
  this.getBytesABLjava_nio_charset_Charset = java_lang_String_getBytesABLjava_nio_charset_Charset;
  this.getBytesAB = java_lang_String_getBytesAB;
  this.equalsZLjava_lang_Object = java_lang_String_equalsZLjava_lang_Object;
  this.contentEqualsZLjava_lang_StringBuffer = java_lang_String_contentEqualsZLjava_lang_StringBuffer;
  this.contentEqualsZLjava_lang_CharSequence = java_lang_String_contentEqualsZLjava_lang_CharSequence;
  this.equalsIgnoreCaseZLjava_lang_String = java_lang_String_equalsIgnoreCaseZLjava_lang_String;
  this.compareToILjava_lang_String = java_lang_String_compareToILjava_lang_String;
  this.compareToIgnoreCaseILjava_lang_String = java_lang_String_compareToIgnoreCaseILjava_lang_String;
  this.regionMatchesZILjava_lang_StringII = java_lang_String_regionMatchesZILjava_lang_StringII;
  this.regionMatchesZZILjava_lang_StringII = java_lang_String_regionMatchesZZILjava_lang_StringII;
  this.startsWithZLjava_lang_StringI = java_lang_String_startsWithZLjava_lang_StringI;
  this.startsWithZLjava_lang_String = java_lang_String_startsWithZLjava_lang_String;
  this.endsWithZLjava_lang_String = java_lang_String_endsWithZLjava_lang_String;
  this.hashCodeI = java_lang_String_hashCodeI;
  this.indexOfII = java_lang_String_indexOfII;
  this.indexOfIII = java_lang_String_indexOfIII;
  this.lastIndexOfII = java_lang_String_lastIndexOfII;
  this.lastIndexOfIII = java_lang_String_lastIndexOfIII;
  this.indexOfILjava_lang_String = java_lang_String_indexOfILjava_lang_String;
  this.indexOfILjava_lang_StringI = java_lang_String_indexOfILjava_lang_StringI;
  this.lastIndexOfILjava_lang_String = java_lang_String_lastIndexOfILjava_lang_String;
  this.lastIndexOfILjava_lang_StringI = java_lang_String_lastIndexOfILjava_lang_StringI;
  this.substringLjava_lang_StringI = java_lang_String_substringLjava_lang_StringI;
  this.substringLjava_lang_StringII = java_lang_String_substringLjava_lang_StringII;
  this.subSequenceLjava_lang_CharSequenceII = java_lang_String_subSequenceLjava_lang_CharSequenceII;
  this.concatLjava_lang_StringLjava_lang_String = java_lang_String_concatLjava_lang_StringLjava_lang_String;
  this.replaceLjava_lang_StringCC = java_lang_String_replaceLjava_lang_StringCC;
  this.matchesZLjava_lang_String = java_lang_String_matchesZLjava_lang_String;
  this.containsZLjava_lang_CharSequence = java_lang_String_containsZLjava_lang_CharSequence;
  this.replaceFirstLjava_lang_StringLjava_lang_StringLjava_lang_String = java_lang_String_replaceFirstLjava_lang_StringLjava_lang_StringLjava_lang_String;
  this.replaceAllLjava_lang_StringLjava_lang_StringLjava_lang_String = java_lang_String_replaceAllLjava_lang_StringLjava_lang_StringLjava_lang_String;
  this.replaceLjava_lang_StringLjava_lang_CharSequenceLjava_lang_CharSequence = java_lang_String_replaceLjava_lang_StringLjava_lang_CharSequenceLjava_lang_CharSequence;
  this.splitALjava_lang_StringLjava_lang_StringI = java_lang_String_splitALjava_lang_StringLjava_lang_StringI;
  this.splitALjava_lang_StringLjava_lang_String = java_lang_String_splitALjava_lang_StringLjava_lang_String;
  this.toLowerCaseLjava_lang_StringLjava_util_Locale = java_lang_String_toLowerCaseLjava_lang_StringLjava_util_Locale;
  this.toLowerCaseLjava_lang_String = java_lang_String_toLowerCaseLjava_lang_String;
  this.toUpperCaseLjava_lang_StringLjava_util_Locale = java_lang_String_toUpperCaseLjava_lang_StringLjava_util_Locale;
  this.toUpperCaseLjava_lang_String = java_lang_String_toUpperCaseLjava_lang_String;
  this.trimLjava_lang_String = java_lang_String_trimLjava_lang_String;
  this.toStringLjava_lang_String = java_lang_String_toStringLjava_lang_String;
  this.internLjava_lang_String = java_lang_String_internLjava_lang_String;
  this.compareToILjava_lang_Object = java_lang_String_compareToILjava_lang_Object;
 */



