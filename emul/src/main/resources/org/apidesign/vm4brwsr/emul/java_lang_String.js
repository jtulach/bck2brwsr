// initialize methods on String constants
java_lang_String(false);

// we need initialized arrays
Array.prototype.fillNulls = function() {
  for(var i = 0; i < this.length; i++) this[i] = null;
  return this;
};

