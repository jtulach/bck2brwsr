// initialize methods on String constants
vm.java_lang_String(false);

// we need initialized arrays
Array.prototype.fillNulls = function() {
  for(var i = 0; i < this.length; i++) this[i] = null;
  return this;
};
Array.prototype.arrtype = function(sig) {
  this.jvmName = sig;
  return this;
};
Array.prototype.getClass__Ljava_lang_Class_2 = function() {
  var c = Array[this.jvmName];
  if (c) return c;
  c = vm.java_lang_Class(true);
  c.jvmName = this.jvmName;
  c.superclass = vm.java_lang_Object(false).$class;
  c.array = true;
  Array[this.jvmName] = c;
  return c;
};
Array.prototype.clone__Ljava_lang_Object_2 = function() {
  var s = this.length;
  var ret = new Array(s);
  for (var i = 0; i < s; i++) {
      ret[i] = this[i];
  }
  return ret;
};
