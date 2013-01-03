// initialize methods on String constants
vm.java_lang_String(false);

// we need initialized arrays
Array.prototype.fillNulls = function() {
  for(var i = 0; i < this.length; i++) this[i] = null;
  return this;
};
Array.prototype.clone__Ljava_lang_Object_2 = function() {
  var s = this.length;
  var ret = new Array(s);
  for (var i = 0; i < s; i++) {
      ret[i] = this[i];
  }
  return ret;
};
