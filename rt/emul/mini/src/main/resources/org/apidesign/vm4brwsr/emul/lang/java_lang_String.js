// initialize methods on arrays and String constants
vm.java_lang_reflect_Array(false);
vm.java_lang_String(false);

Array.at = function(arr, indx, value) {
  var prev = arr[indx];
  if (typeof prev === 'undefined' && (indx < 0 || indx >= arr.length)) {
    var e = vm.java_lang_ArrayIndexOutOfBoundsException(true);
    e.constructor.cons__VLjava_lang_String_2.call(e, indx.toString());
    throw e;
  }
  if (arguments.length === 3) {
    arr[indx] = value;
  } else {
    return prev;
  }
};
Array.prototype.getClass__Ljava_lang_Class_2 = function() {
  return vm.java_lang_Class(false).defineArray__Ljava_lang_Class_2Ljava_lang_String_2Ljava_lang_Object_2(this.jvmName, this.fnc);
};
Array.prototype.clone__Ljava_lang_Object_2 = function() {
  var s = this.length;
  var ret = new Array(s);
  for (var i = 0; i < s; i++) {
      ret[i] = this[i];
  }
  ret.jvmName = this.jvmName;
  ret.fnc = this.fnc;
  return ret;
};
