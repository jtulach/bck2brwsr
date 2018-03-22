// empty line needed here

(function(numberPrototype) {
'use strict';
/* Scala.js runtime support
 * Copyright 2013 LAMP/EPFL
 * Author: Sébastien Doeraene
 */

/* ---------------------------------- *
 * The top-level Scala.js environment *
 * ---------------------------------- */

var $imul = Math["imul"] || (function(a, b) {
  // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/imul
  var ah = (a >>> 16) & 0xffff;
  var al = a & 0xffff;
  var bh = (b >>> 16) & 0xffff;
  var bl = b & 0xffff;
  // the shift by 0 fixes the sign on the high part
  // the final |0 converts the unsigned value into a signed value
  return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0) | 0);
});

var $fround = Math["fround"] ||













































  (function(v) {
    return +v;
  });


var $clz32 = Math["clz32"] || (function(i) {
  // See Hacker's Delight, Section 5-3
  if (i === 0) return 32;
  var r = 1;
  if ((i & 0xffff0000) === 0) { i <<= 16; r += 16; };
  if ((i & 0xff000000) === 0) { i <<= 8; r += 8; };
  if ((i & 0xf0000000) === 0) { i <<= 4; r += 4; };
  if ((i & 0xc0000000) === 0) { i <<= 2; r += 2; };
  return r + (i >> 31);
});


// Cached instance of RuntimeLong for 0L
var $L0;

// identityHashCode support
var $lastIDHash = 0; // last value attributed to an id hash code



var $idHashCodeMap = typeof WeakMap !== "undefined" ? new WeakMap() : null;


// Core mechanism

function $makeIsArrayOfPrimitive(primitiveData) {
  return function(obj, depth) {
    return !!(obj && obj.$classData &&
      (obj.$classData.arrayDepth === depth) &&
      (obj.$classData.arrayBase === primitiveData));
  }
};












/** Encode a property name for runtime manipulation
  *  Usage:
  *    env.propertyName({someProp:0})
  *  Returns:
  *    "someProp"
  *  Useful when the property is renamed by a global optimizer (like Closure)
  *  but we must still get hold of a string of that name for runtime
  * reflection.
  */
function $propertyName(obj) {
  for (var prop in obj)
    return prop;
};

// Boxed Char











function $Char(c) {
  this.c = c;
};
$Char.prototype.toString = (function() {
  return String["fromCharCode"](this.c);
});


// Runtime functions

function $isScalaJSObject(obj) {
  return !!(obj && obj.$classData);
};
































function $noIsInstance(instance) {
  throw new TypeError(
    "Cannot call isInstance() on a Class representing a raw JS trait/object");
};

function $makeNativeArrayWrapper(arrayClassData, nativeArray) {
  return new arrayClassData.constr(nativeArray);
};

function $newArrayObject(arrayClassData, lengths) {
  return $newArrayObjectInternal(arrayClassData, lengths, 0);
};

function $newArrayObjectInternal(arrayClassData, lengths, lengthIndex) {
  var result = new arrayClassData.constr(lengths[lengthIndex]);

  if (lengthIndex < lengths.length-1) {
    var subArrayClassData = arrayClassData.componentData;
    var subLengthIndex = lengthIndex+1;
    var underlying = result.u;
    for (var i = 0; i < underlying.length; i++) {
      underlying[i] = $newArrayObjectInternal(
        subArrayClassData, lengths, subLengthIndex);
    }
  }

  return result;
};

function $objectGetClass(instance) {
  switch (typeof instance) {
    case "string":
      return $d_T.getClassOf();
    case "number": {
      var v = instance | 0;
      if (v === instance) { // is the value integral?
        if ($isByte(v))
          return $d_jl_Byte.getClassOf();
        else if ($isShort(v))
          return $d_jl_Short.getClassOf();
        else
          return $d_jl_Integer.getClassOf();
      } else {
        if ($isFloat(instance))
          return $d_jl_Float.getClassOf();
        else
          return $d_jl_Double.getClassOf();
      }
    }
    case "boolean":
      return $d_jl_Boolean.getClassOf();
    case "undefined":
      return $d_sr_BoxedUnit.getClassOf();
    default:
      if (instance === null)
        return instance.getClass__jl_Class();
      else if ($is_sjsr_RuntimeLong(instance))
        return $d_jl_Long.getClassOf();
      else if ($isChar(instance))
        return $d_jl_Character.getClassOf();
      else if ($isScalaJSObject(instance))
        return instance.$classData.getClassOf();
      else
        return null; // Exception?
  }
};

function $dp_toString__T(instance) {
  if (instance === void 0)
    return "undefined";
  else
    return instance.toString();
};

function $dp_getClass__jl_Class(instance) {
  return $objectGetClass(instance);
};

function $dp_clone__O(instance) {
  if ($isScalaJSObject(instance) || (instance === null))
    return instance.clone__O();
  else
    throw new $c_jl_CloneNotSupportedException().init___();
};

function $dp_notify__V(instance) {
  // final and no-op in java.lang.Object
  if (instance === null)
    instance.notify__V();
};

function $dp_notifyAll__V(instance) {
  // final and no-op in java.lang.Object
  if (instance === null)
    instance.notifyAll__V();
};

function $dp_finalize__V(instance) {
  if ($isScalaJSObject(instance) || (instance === null))
    instance.finalize__V();
  // else no-op
};

function $dp_equals__O__Z(instance, rhs) {
  if ($isScalaJSObject(instance) || (instance === null))
    return instance.equals__O__Z(rhs);
  else if (typeof instance === "number")
    return $f_jl_Double__equals__O__Z(instance, rhs);
  else if ($isChar(instance))
    return $f_jl_Character__equals__O__Z(instance, rhs);
  else
    return instance === rhs;
};

function $dp_hashCode__I(instance) {
  switch (typeof instance) {
    case "string":
      return $f_T__hashCode__I(instance);
    case "number":
      return $f_jl_Double__hashCode__I(instance);
    case "boolean":
      return $f_jl_Boolean__hashCode__I(instance);
    case "undefined":
      return $f_sr_BoxedUnit__hashCode__I(instance);
    default:
      if ($isScalaJSObject(instance) || instance === null)
        return instance.hashCode__I();
      else if ($isChar(instance))
        return $f_jl_Character__hashCode__I(instance);
      else
        return $systemIdentityHashCode(instance);
  }
};

function $dp_compareTo__O__I(instance, rhs) {
  switch (typeof instance) {
    case "string":
      return $f_T__compareTo__O__I(instance, rhs);
    case "number":
      return $f_jl_Double__compareTo__O__I(instance, rhs);
    case "boolean":
      return $f_jl_Boolean__compareTo__O__I(instance, rhs);
    default:
      if ($isChar(instance))
        return $f_jl_Character__compareTo__O__I(instance, rhs);
      else
        return instance.compareTo__O__I(rhs);
  }
};

function $dp_length__I(instance) {
  if (typeof(instance) === "string")
    return $f_T__length__I(instance);
  else
    return instance.length__I();
};

function $dp_charAt__I__C(instance, index) {
  if (typeof(instance) === "string")
    return $f_T__charAt__I__C(instance, index);
  else
    return instance.charAt__I__C(index);
};

function $dp_subSequence__I__I__jl_CharSequence(instance, start, end) {
  if (typeof(instance) === "string")
    return $f_T__subSequence__I__I__jl_CharSequence(instance, start, end);
  else
    return instance.subSequence__I__I__jl_CharSequence(start, end);
};

function $dp_byteValue__B(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__byteValue__B(instance);
  else
    return instance.byteValue__B();
};
function $dp_shortValue__S(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__shortValue__S(instance);
  else
    return instance.shortValue__S();
};
function $dp_intValue__I(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__intValue__I(instance);
  else
    return instance.intValue__I();
};
function $dp_longValue__J(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__longValue__J(instance);
  else
    return instance.longValue__J();
};
function $dp_floatValue__F(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__floatValue__F(instance);
  else
    return instance.floatValue__F();
};
function $dp_doubleValue__D(instance) {
  if (typeof instance === "number")
    return $f_jl_Double__doubleValue__D(instance);
  else
    return instance.doubleValue__D();
};

function $doubleToInt(x) {
  return (x > 2147483647) ? (2147483647) : ((x < -2147483648) ? -2147483648 : (x | 0));
};

/** Instantiates a JS object with variadic arguments to the constructor. */
function $newJSObjectWithVarargs(ctor, args) {
  // This basically emulates the ECMAScript specification for 'new'.
  var instance = Object["create"](ctor.prototype);
  var result = ctor["apply"](instance, args);
  switch (typeof result) {
    case "string": case "number": case "boolean": case "undefined": case "symbol":
      return instance;
    default:
      return result === null ? instance : result;
  }
};

function $resolveSuperRef(superClass, propName) {
  var getPrototypeOf = Object["getPrototypeOf"];
  var getOwnPropertyDescriptor = Object["getOwnPropertyDescriptor"];

  var superProto = superClass.prototype;
  while (superProto !== null) {
    var desc = getOwnPropertyDescriptor(superProto, propName);
    if (desc !== void 0)
      return desc;
    superProto = getPrototypeOf(superProto);
  }

  return void 0;
};

function $superGet(superClass, self, propName) {
  var desc = $resolveSuperRef(superClass, propName);
  if (desc !== void 0) {
    var getter = desc["get"];
    if (getter !== void 0)
      return getter["call"](self);
    else
      return desc["value"];
  }
  return void 0;
};

function $superSet(superClass, self, propName, value) {
  var desc = $resolveSuperRef(superClass, propName);
  if (desc !== void 0) {
    var setter = desc["set"];
    if (setter !== void 0) {
      setter["call"](self, value);
      return void 0;
    }
  }
  throw new TypeError("super has no setter '" + propName + "'.");
};







function $systemArraycopy(src, srcPos, dest, destPos, length) {
  var srcu = src.u;
  var destu = dest.u;









  if (srcu !== destu || destPos < srcPos || (((srcPos + length) | 0) < destPos)) {
    for (var i = 0; i < length; i = (i + 1) | 0)
      destu[(destPos + i) | 0] = srcu[(srcPos + i) | 0];
  } else {
    for (var i = (length - 1) | 0; i >= 0; i = (i - 1) | 0)
      destu[(destPos + i) | 0] = srcu[(srcPos + i) | 0];
  }
};

var $systemIdentityHashCode =

  ($idHashCodeMap !== null) ?

  (function(obj) {
    switch (typeof obj) {
      case "string": case "number": case "boolean": case "undefined":
        return $dp_hashCode__I(obj);
      default:
        if (obj === null) {
          return 0;
        } else {
          var hash = $idHashCodeMap["get"](obj);
          if (hash === void 0) {
            hash = ($lastIDHash + 1) | 0;
            $lastIDHash = hash;
            $idHashCodeMap["set"](obj, hash);
          }
          return hash;
        }
    }

  }) :
  (function(obj) {
    switch (typeof obj) {
      case "string": case "number": case "boolean": case "undefined":
        return $dp_hashCode__I(obj);
      default:
        if ($isScalaJSObject(obj)) {
          var hash = obj["$idHashCode$0"];
          if (hash !== void 0) {
            return hash;
          } else if (!Object["isSealed"](obj)) {
            hash = ($lastIDHash + 1) | 0;
            $lastIDHash = hash;
            obj["$idHashCode$0"] = hash;
            return hash;
          } else {
            return 42;
          }
        } else if (obj === null) {
          return 0;
        } else {
          return 42;
        }
    }

  });

// is/as for hijacked boxed classes (the non-trivial ones)

function $isChar(v) {
  return v instanceof $Char;
};

function $isByte(v) {
  return typeof v === "number" && (v << 24 >> 24) === v && 1/v !== 1/-0;
};

function $isShort(v) {
  return typeof v === "number" && (v << 16 >> 16) === v && 1/v !== 1/-0;
};

function $isInt(v) {
  return typeof v === "number" && (v | 0) === v && 1/v !== 1/-0;
};

function $isFloat(v) {



  return typeof v === "number";

};



























































// Boxes

function $bC(c) {
  return new $Char(c);
}
var $bC0 = $bC(0);

// Unboxes






























function $uC(value) {
  return null === value ? 0 : value.c;
}
function $uJ(value) {
  return null === value ? $L0 : value;
};


// TypeArray conversions

function $byteArray2TypedArray(value) { return new Int8Array(value.u); };
function $shortArray2TypedArray(value) { return new Int16Array(value.u); };
function $charArray2TypedArray(value) { return new Uint16Array(value.u); };
function $intArray2TypedArray(value) { return new Int32Array(value.u); };
function $floatArray2TypedArray(value) { return new Float32Array(value.u); };
function $doubleArray2TypedArray(value) { return new Float64Array(value.u); };

function $typedArray2ByteArray(value) {
  var arrayClassData = $d_B.getArrayOf();
  return new arrayClassData.constr(new Int8Array(value));
};
function $typedArray2ShortArray(value) {
  var arrayClassData = $d_S.getArrayOf();
  return new arrayClassData.constr(new Int16Array(value));
};
function $typedArray2CharArray(value) {
  var arrayClassData = $d_C.getArrayOf();
  return new arrayClassData.constr(new Uint16Array(value));
};
function $typedArray2IntArray(value) {
  var arrayClassData = $d_I.getArrayOf();
  return new arrayClassData.constr(new Int32Array(value));
};
function $typedArray2FloatArray(value) {
  var arrayClassData = $d_F.getArrayOf();
  return new arrayClassData.constr(new Float32Array(value));
};
function $typedArray2DoubleArray(value) {
  var arrayClassData = $d_D.getArrayOf();
  return new arrayClassData.constr(new Float64Array(value));
};

// TypeData class


/** @constructor */
function $TypeData() {




  // Runtime support
  this.constr = void 0;
  this.parentData = void 0;
  this.ancestors = null;
  this.componentData = null;
  this.arrayBase = null;
  this.arrayDepth = 0;
  this.zero = null;
  this.arrayEncodedName = "";
  this._classOf = void 0;
  this._arrayOf = void 0;
  this.isArrayOf = void 0;

  // java.lang.Class support
  this["name"] = "";
  this["isPrimitive"] = false;
  this["isInterface"] = false;
  this["isArrayClass"] = false;
  this["isRawJSType"] = false;
  this["isInstance"] = void 0;
};


$TypeData.prototype.initPrim = function(



    zero, arrayEncodedName, displayName) {
  // Runtime support
  this.ancestors = {};
  this.componentData = null;
  this.zero = zero;
  this.arrayEncodedName = arrayEncodedName;
  this.isArrayOf = function(obj, depth) { return false; };

  // java.lang.Class support
  this["name"] = displayName;
  this["isPrimitive"] = true;
  this["isInstance"] = function(obj) { return false; };

  return this;
};


$TypeData.prototype.initClass = function(



    internalNameObj, isInterface, fullName,
    ancestors, isRawJSType, parentData, isInstance, isArrayOf) {
  var internalName = $propertyName(internalNameObj);

  isInstance = isInstance || function(obj) {
    return !!(obj && obj.$classData && obj.$classData.ancestors[internalName]);
  };

  isArrayOf = isArrayOf || function(obj, depth) {
    return !!(obj && obj.$classData && (obj.$classData.arrayDepth === depth)
      && obj.$classData.arrayBase.ancestors[internalName])
  };

  // Runtime support
  this.parentData = parentData;
  this.ancestors = ancestors;
  this.arrayEncodedName = "L"+fullName+";";
  this.isArrayOf = isArrayOf;

  // java.lang.Class support
  this["name"] = fullName;
  this["isInterface"] = isInterface;
  this["isRawJSType"] = !!isRawJSType;
  this["isInstance"] = isInstance;

  return this;
};


$TypeData.prototype.initArray = function(



    componentData) {
  // The constructor

  var componentZero0 = componentData.zero;

  // The zero for the Long runtime representation
  // is a special case here, since the class has not
  // been defined yet when this constructor is called.
  var componentZero = (componentZero0 == "longZero") ? $L0 : componentZero0;


  /** @constructor */
  var ArrayClass = function(arg) {
    if (typeof(arg) === "number") {
      // arg is the length of the array
      this.u = new Array(arg);
      for (var i = 0; i < arg; i++)
        this.u[i] = componentZero;
    } else {
      // arg is a native array that we wrap
      this.u = arg;
    }
  }
  ArrayClass.prototype = new $h_O;
  ArrayClass.prototype.constructor = ArrayClass;














  ArrayClass.prototype.clone__O = function() {
    if (this.u instanceof Array)
      return new ArrayClass(this.u["slice"](0));
    else
      // The underlying Array is a TypedArray
      return new ArrayClass(new this.u.constructor(this.u));
  };






































  ArrayClass.prototype.$classData = this;

  // Don't generate reflective call proxies. The compiler special cases
  // reflective calls to methods on scala.Array

  // The data

  var encodedName = "[" + componentData.arrayEncodedName;
  var componentBase = componentData.arrayBase || componentData;
  var arrayDepth = componentData.arrayDepth + 1;

  var isInstance = function(obj) {
    return componentBase.isArrayOf(obj, arrayDepth);
  }

  // Runtime support
  this.constr = ArrayClass;
  this.parentData = $d_O;
  this.ancestors = {O: 1, jl_Cloneable: 1, Ljava_io_Serializable: 1};
  this.componentData = componentData;
  this.arrayBase = componentBase;
  this.arrayDepth = arrayDepth;
  this.zero = null;
  this.arrayEncodedName = encodedName;
  this._classOf = undefined;
  this._arrayOf = undefined;
  this.isArrayOf = undefined;

  // java.lang.Class support
  this["name"] = encodedName;
  this["isPrimitive"] = false;
  this["isInterface"] = false;
  this["isArrayClass"] = true;
  this["isInstance"] = isInstance;

  return this;
};


$TypeData.prototype.getClassOf = function() {



  if (!this._classOf)
    this._classOf = new $c_jl_Class(this);
  return this._classOf;
};


$TypeData.prototype.getArrayOf = function() {



  if (!this._arrayOf)
    this._arrayOf = new $TypeData().initArray(this);
  return this._arrayOf;
};

// java.lang.Class support


$TypeData.prototype["isAssignableFrom"] = function(that) {



  if (this["isPrimitive"] || that["isPrimitive"]) {
    return this === that;
  } else {
    var thatFakeInstance;
    if (that === $d_T)
      thatFakeInstance = "some string";
    else if (that === $d_jl_Boolean)
      thatFakeInstance = false;
    else if (that === $d_jl_Byte ||
             that === $d_jl_Short ||
             that === $d_jl_Integer ||
             that === $d_jl_Float ||
             that === $d_jl_Double)
      thatFakeInstance = 0;
    else if (that === $d_jl_Long)
      thatFakeInstance = $L0;
    else if (that === $d_sr_BoxedUnit)
      thatFakeInstance = void 0;
    else
      thatFakeInstance = {$classData: that};
    return this["isInstance"](thatFakeInstance);
  }
};


$TypeData.prototype["getSuperclass"] = function() {



  return this.parentData ? this.parentData.getClassOf() : null;
};


$TypeData.prototype["getComponentType"] = function() {



  return this.componentData ? this.componentData.getClassOf() : null;
};


$TypeData.prototype["newArrayOfThisClass"] = function(lengths) {



  var arrayClassData = this;
  for (var i = 0; i < lengths.length; i++)
    arrayClassData = arrayClassData.getArrayOf();
  return $newArrayObject(arrayClassData, lengths);
};




// Create primitive types

var $d_V = new $TypeData().initPrim(undefined, "V", "void");
var $d_Z = new $TypeData().initPrim(false, "Z", "boolean");
var $d_C = new $TypeData().initPrim(0, "C", "char");
var $d_B = new $TypeData().initPrim(0, "B", "byte");
var $d_S = new $TypeData().initPrim(0, "S", "short");
var $d_I = new $TypeData().initPrim(0, "I", "int");
var $d_J = new $TypeData().initPrim("longZero", "J", "long");
var $d_F = new $TypeData().initPrim(0.0, "F", "float");
var $d_D = new $TypeData().initPrim(0.0, "D", "double");

// Instance tests for array of primitives

var $isArrayOf_Z = $makeIsArrayOfPrimitive($d_Z);
$d_Z.isArrayOf = $isArrayOf_Z;

var $isArrayOf_C = $makeIsArrayOfPrimitive($d_C);
$d_C.isArrayOf = $isArrayOf_C;

var $isArrayOf_B = $makeIsArrayOfPrimitive($d_B);
$d_B.isArrayOf = $isArrayOf_B;

var $isArrayOf_S = $makeIsArrayOfPrimitive($d_S);
$d_S.isArrayOf = $isArrayOf_S;

var $isArrayOf_I = $makeIsArrayOfPrimitive($d_I);
$d_I.isArrayOf = $isArrayOf_I;

var $isArrayOf_J = $makeIsArrayOfPrimitive($d_J);
$d_J.isArrayOf = $isArrayOf_J;

var $isArrayOf_F = $makeIsArrayOfPrimitive($d_F);
$d_F.isArrayOf = $isArrayOf_F;

var $isArrayOf_D = $makeIsArrayOfPrimitive($d_D);
$d_D.isArrayOf = $isArrayOf_D;












/** @constructor */
function $c_O() {
  /*<skip>*/
}
/** @constructor */
function $h_O() {
  /*<skip>*/
}
$h_O.prototype = $c_O.prototype;
$c_O.prototype.init___ = (function() {
  return this
});
$c_O.prototype.toString__T = (function() {
  var jsx$1 = $objectGetClass(this).getName__T();
  var i = this.hashCode__I();
  return ((jsx$1 + "@") + (+(i >>> 0)).toString(16))
});
$c_O.prototype.hashCode__I = (function() {
  return $systemIdentityHashCode(this)
});
$c_O.prototype.toString = (function() {
  return this.toString__T()
});
function $is_O(obj) {
  return (obj !== null)
}
function $isArrayOf_O(obj, depth) {
  var data = (obj && obj.$classData);
  if ((!data)) {
    return false
  } else {
    var arrayDepth = (data.arrayDepth || 0);
    return ((!(arrayDepth < depth)) && ((arrayDepth > depth) || (!data.arrayBase.isPrimitive)))
  }
}
var $d_O = new $TypeData().initClass({
  O: 0
}, false, "java.lang.Object", {
  O: 1
}, (void 0), (void 0), $is_O, $isArrayOf_O);
$c_O.prototype.$classData = $d_O;
/** @constructor */
function $c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$() {
  /*<skip>*/
}
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype = new $h_O();
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.constructor = $c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$;
/** @constructor */
function $h_Lorg_apidesign_bck2brwr_vm4brwsr_Module$() {
  /*<skip>*/
}
$h_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype = $c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype;
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$div64__J__J__O = (function(a, b) {
  return this.div64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$low32__sjsr_RuntimeLong__O = (function(a) {
  return a.lo$2
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$and64__J__J__O = (function(a, b) {
  return this.and64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$to64__I__I__O = (function(lo, hi) {
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.compare64__J__J__I = (function(a, b) {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$compare__I__I__I__I__I(a.lo$2, a.hi$2, b.lo$2, b.hi$2)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$sub64__J__J__O = (function(a, b) {
  return this.sub64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.div64__J__J__J = (function(a, b) {
  var this$1 = $m_sjsr_RuntimeLong$();
  var lo = this$1.divideImpl__I__I__I__I__I(a.lo$2, a.hi$2, b.lo$2, b.hi$2);
  var hi = this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.and64__J__J__J = (function(a, b) {
  var lo = (a.lo$2 & b.lo$2);
  var hi = (a.hi$2 & b.hi$2);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.or64__J__J__J = (function(a, b) {
  var lo = (a.lo$2 | b.lo$2);
  var hi = (a.hi$2 | b.hi$2);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$shr64__J__I__O = (function(a, n) {
  return this.shr64__J__I__J(a, n)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.sub64__J__J__J = (function(a, b) {
  var alo = a.lo$2;
  var ahi = a.hi$2;
  var bhi = b.hi$2;
  var lo = ((alo - b.lo$2) | 0);
  var hi = ((((-2147483648) ^ lo) > ((-2147483648) ^ alo)) ? (((-1) + ((ahi - bhi) | 0)) | 0) : ((ahi - bhi) | 0));
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.shr64__J__I__J = (function(a, n) {
  var lo = (((32 & n) === 0) ? (((a.lo$2 >>> n) | 0) | ((a.hi$2 << 1) << ((31 - n) | 0))) : (a.hi$2 >> n));
  var hi = (((32 & n) === 0) ? (a.hi$2 >> n) : (a.hi$2 >> 31));
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$add64__J__J__O = (function(a, b) {
  return this.add64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$neg64__J__O = (function(a) {
  return this.neg64__J__J(a)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.add64__J__J__J = (function(a, b) {
  var alo = a.lo$2;
  var ahi = a.hi$2;
  var bhi = b.hi$2;
  var lo = ((alo + b.lo$2) | 0);
  var hi = ((((-2147483648) ^ lo) < ((-2147483648) ^ alo)) ? ((1 + ((ahi + bhi) | 0)) | 0) : ((ahi + bhi) | 0));
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$mul64__J__J__O = (function(a, b) {
  return this.mul64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$compare64__J__J__O = (function(a, b) {
  return this.compare64__J__J__I(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$ushr64__J__I__O = (function(a, n) {
  return this.ushr64__J__I__J(a, n)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$mod64__J__J__O = (function(a, b) {
  return this.mod64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$toDouble__J__O = (function(a) {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D(a.lo$2, a.hi$2)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.ushr64__J__I__J = (function(a, n) {
  var lo = (((32 & n) === 0) ? (((a.lo$2 >>> n) | 0) | ((a.hi$2 << 1) << ((31 - n) | 0))) : ((a.hi$2 >>> n) | 0));
  var hi = (((32 & n) === 0) ? ((a.hi$2 >>> n) | 0) : 0);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.mod64__J__J__J = (function(a, b) {
  var this$1 = $m_sjsr_RuntimeLong$();
  var lo = this$1.remainderImpl__I__I__I__I__I(a.lo$2, a.hi$2, b.lo$2, b.hi$2);
  var hi = this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$shl64__J__I__O = (function(a, n) {
  return this.shl64__J__I__J(a, n)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.shl64__J__I__J = (function(a, n) {
  var lo = (((32 & n) === 0) ? (a.lo$2 << n) : 0);
  var hi = (((32 & n) === 0) ? (((((a.lo$2 >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (a.hi$2 << n)) : (a.lo$2 << n));
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$fromDouble__D__O = (function(a) {
  var this$1 = $m_sjsr_RuntimeLong$();
  var lo = this$1.scala$scalajs$runtime$RuntimeLong$$fromDoubleImpl__D__I(a);
  var hi = this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.mul64__J__J__J = (function(a, b) {
  var alo = a.lo$2;
  var blo = b.lo$2;
  var a0 = (65535 & alo);
  var a1 = ((alo >>> 16) | 0);
  var b0 = (65535 & blo);
  var b1 = ((blo >>> 16) | 0);
  var a0b0 = $imul(a0, b0);
  var a1b0 = $imul(a1, b0);
  var a0b1 = $imul(a0, b1);
  var lo = ((a0b0 + (((a1b0 + a0b1) | 0) << 16)) | 0);
  var c1part = ((((a0b0 >>> 16) | 0) + a0b1) | 0);
  var hi = (((((((($imul(alo, b.hi$2) + $imul(a.hi$2, blo)) | 0) + $imul(a1, b1)) | 0) + ((c1part >>> 16) | 0)) | 0) + (((((65535 & c1part) + a1b0) | 0) >>> 16) | 0)) | 0);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.xor64__J__J__J = (function(a, b) {
  var lo = (a.lo$2 ^ b.lo$2);
  var hi = (a.hi$2 ^ b.hi$2);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$or64__J__J__O = (function(a, b) {
  return this.or64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$xor64__J__J__O = (function(a, b) {
  return this.xor64__J__J__J(a, b)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.neg64__J__J = (function(a) {
  var lo = a.lo$2;
  var hi = a.hi$2;
  var lo$1 = ((-lo) | 0);
  var hi$1 = ((lo !== 0) ? (~hi) : ((-hi) | 0));
  return new $c_sjsr_RuntimeLong(lo$1, hi$1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$$js$exported$meth$high32__sjsr_RuntimeLong__O = (function(a) {
  return a.hi$2
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.compare64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$compare64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.ushr64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = (arg$2 | 0);
  return this.$$js$exported$meth$ushr64__J__I__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.shr64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = (arg$2 | 0);
  return this.$$js$exported$meth$shr64__J__I__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.shl64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = (arg$2 | 0);
  return this.$$js$exported$meth$shl64__J__I__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.neg64 = (function(arg$1) {
  var prep0 = $uJ(arg$1);
  return this.$$js$exported$meth$neg64__J__O(prep0)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.xor64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$xor64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.or64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$or64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.and64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$and64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.mod64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$mod64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.div64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$div64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.mul64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$mul64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.sub64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$sub64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.add64 = (function(arg$1, arg$2) {
  var prep0 = $uJ(arg$1);
  var prep1 = $uJ(arg$2);
  return this.$$js$exported$meth$add64__J__J__O(prep0, prep1)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.toDouble = (function(arg$1) {
  var prep0 = $uJ(arg$1);
  return this.$$js$exported$meth$toDouble__J__O(prep0)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.fromDouble = (function(arg$1) {
  var prep0 = (+arg$1);
  return this.$$js$exported$meth$fromDouble__D__O(prep0)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.low32 = (function(arg$1) {
  var prep0 = arg$1;
  return this.$$js$exported$meth$low32__sjsr_RuntimeLong__O(prep0)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.high32 = (function(arg$1) {
  var prep0 = arg$1;
  return this.$$js$exported$meth$high32__sjsr_RuntimeLong__O(prep0)
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.to64 = (function(arg$1, arg$2) {
  var prep0 = (arg$1 | 0);
  var prep1 = (arg$2 | 0);
  return this.$$js$exported$meth$to64__I__I__O(prep0, prep1)
});
var $d_Lorg_apidesign_bck2brwr_vm4brwsr_Module$ = new $TypeData().initClass({
  Lorg_apidesign_bck2brwr_vm4brwsr_Module$: 0
}, false, "org.apidesign.bck2brwr.vm4brwsr.Module$", {
  Lorg_apidesign_bck2brwr_vm4brwsr_Module$: 1,
  O: 1
});
$c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$.prototype.$classData = $d_Lorg_apidesign_bck2brwr_vm4brwsr_Module$;
var $n_Lorg_apidesign_bck2brwr_vm4brwsr_Module$ = (void 0);
function $m_Lorg_apidesign_bck2brwr_vm4brwsr_Module$() {
  if ((!$n_Lorg_apidesign_bck2brwr_vm4brwsr_Module$)) {
    $n_Lorg_apidesign_bck2brwr_vm4brwsr_Module$ = new $c_Lorg_apidesign_bck2brwr_vm4brwsr_Module$()
  };
  return $n_Lorg_apidesign_bck2brwr_vm4brwsr_Module$
}
/** @constructor */
function $c_jl_Class(data0) {
  this.data$1 = null;
  this.data$1 = data0
}
$c_jl_Class.prototype = new $h_O();
$c_jl_Class.prototype.constructor = $c_jl_Class;
/** @constructor */
function $h_jl_Class() {
  /*<skip>*/
}
$h_jl_Class.prototype = $c_jl_Class.prototype;
$c_jl_Class.prototype.getName__T = (function() {
  return this.data$1.name
});
$c_jl_Class.prototype.isPrimitive__Z = (function() {
  return (!(!this.data$1.isPrimitive))
});
$c_jl_Class.prototype.toString__T = (function() {
  return ((this.isInterface__Z() ? "interface " : (this.isPrimitive__Z() ? "" : "class ")) + this.getName__T())
});
$c_jl_Class.prototype.isInterface__Z = (function() {
  return (!(!this.data$1.isInterface))
});
var $d_jl_Class = new $TypeData().initClass({
  jl_Class: 0
}, false, "java.lang.Class", {
  jl_Class: 1,
  O: 1
});
$c_jl_Class.prototype.$classData = $d_jl_Class;
/** @constructor */
function $c_jl_FloatingPointBits$() {
  this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f = false;
  this.arrayBuffer$1 = null;
  this.int32Array$1 = null;
  this.float32Array$1 = null;
  this.float64Array$1 = null;
  this.areTypedArraysBigEndian$1 = false;
  this.highOffset$1 = 0;
  this.lowOffset$1 = 0;
  $n_jl_FloatingPointBits$ = this;
  this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f = (((((typeof ArrayBuffer) !== "undefined") && ((typeof Int32Array) !== "undefined")) && ((typeof Float32Array) !== "undefined")) && ((typeof Float64Array) !== "undefined"));
  this.arrayBuffer$1 = (this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f ? new ArrayBuffer(8) : null);
  this.int32Array$1 = (this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f ? new Int32Array(this.arrayBuffer$1, 0, 2) : null);
  this.float32Array$1 = (this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f ? new Float32Array(this.arrayBuffer$1, 0, 2) : null);
  this.float64Array$1 = (this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f ? new Float64Array(this.arrayBuffer$1, 0, 1) : null);
  if ((!this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f)) {
    var jsx$1 = true
  } else {
    this.int32Array$1[0] = 16909060;
    var jsx$1 = ((new Int8Array(this.arrayBuffer$1, 0, 8)[0] | 0) === 1)
  };
  this.areTypedArraysBigEndian$1 = jsx$1;
  this.highOffset$1 = (this.areTypedArraysBigEndian$1 ? 0 : 1);
  this.lowOffset$1 = (this.areTypedArraysBigEndian$1 ? 1 : 0)
}
$c_jl_FloatingPointBits$.prototype = new $h_O();
$c_jl_FloatingPointBits$.prototype.constructor = $c_jl_FloatingPointBits$;
/** @constructor */
function $h_jl_FloatingPointBits$() {
  /*<skip>*/
}
$h_jl_FloatingPointBits$.prototype = $c_jl_FloatingPointBits$.prototype;
$c_jl_FloatingPointBits$.prototype.numberHashCode__D__I = (function(value) {
  var iv = ((value | 0) | 0);
  if (((iv === value) && ((1.0 / value) !== (-Infinity)))) {
    return iv
  } else {
    var t = this.doubleToLongBits__D__J(value);
    var lo = t.lo$2;
    var hi = t.hi$2;
    return (lo ^ hi)
  }
});
$c_jl_FloatingPointBits$.prototype.doubleToLongBitsPolyfill__p1__D__J = (function(value) {
  if ((value !== value)) {
    var _3 = (+Math.pow(2.0, 51.0));
    var x1_$_$$und1$1 = false;
    var x1_$_$$und2$1 = 2047;
    var x1_$_$$und3$1 = _3
  } else if (((value === Infinity) || (value === (-Infinity)))) {
    var _1 = (value < 0.0);
    var x1_$_$$und1$1 = _1;
    var x1_$_$$und2$1 = 2047;
    var x1_$_$$und3$1 = 0.0
  } else if ((value === 0.0)) {
    var _1$1 = ((1.0 / value) === (-Infinity));
    var x1_$_$$und1$1 = _1$1;
    var x1_$_$$und2$1 = 0;
    var x1_$_$$und3$1 = 0.0
  } else {
    var s = (value < 0.0);
    var av = (s ? (-value) : value);
    if ((av >= (+Math.pow(2.0, (-1022.0))))) {
      var twoPowFbits = (+Math.pow(2.0, 52.0));
      var a = ((+Math.log(av)) / 0.6931471805599453);
      var x = (+Math.floor(a));
      var a$1 = ((x | 0) | 0);
      var e = ((a$1 < 1023) ? a$1 : 1023);
      var b = e;
      var twoPowE = (+Math.pow(2.0, b));
      if ((twoPowE > av)) {
        e = (((-1) + e) | 0);
        twoPowE = (twoPowE / 2.0)
      };
      var n = ((av / twoPowE) * twoPowFbits);
      var w = (+Math.floor(n));
      var f = (n - w);
      var f$1 = ((f < 0.5) ? w : ((f > 0.5) ? (1.0 + w) : (((w % 2.0) !== 0.0) ? (1.0 + w) : w)));
      if (((f$1 / twoPowFbits) >= 2.0)) {
        e = ((1 + e) | 0);
        f$1 = 1.0
      };
      if ((e > 1023)) {
        e = 2047;
        f$1 = 0.0
      } else {
        e = ((1023 + e) | 0);
        f$1 = (f$1 - twoPowFbits)
      };
      var _2 = e;
      var _3$1 = f$1;
      var x1_$_$$und1$1 = s;
      var x1_$_$$und2$1 = _2;
      var x1_$_$$und3$1 = _3$1
    } else {
      var n$1 = (av / (+Math.pow(2.0, (-1074.0))));
      var w$1 = (+Math.floor(n$1));
      var f$2 = (n$1 - w$1);
      var _3$2 = ((f$2 < 0.5) ? w$1 : ((f$2 > 0.5) ? (1.0 + w$1) : (((w$1 % 2.0) !== 0.0) ? (1.0 + w$1) : w$1)));
      var x1_$_$$und1$1 = s;
      var x1_$_$$und2$1 = 0;
      var x1_$_$$und3$1 = _3$2
    }
  };
  var s$1 = (!(!x1_$_$$und1$1));
  var e$1 = (x1_$_$$und2$1 | 0);
  var f$3 = (+x1_$_$$und3$1);
  var x$1 = (f$3 / 4.294967296E9);
  var hif = ((x$1 | 0) | 0);
  var hi = (((s$1 ? (-2147483648) : 0) | (e$1 << 20)) | hif);
  var lo = ((f$3 | 0) | 0);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_jl_FloatingPointBits$.prototype.doubleToLongBits__D__J = (function(value) {
  if (this.java$lang$FloatingPointBits$$$undareTypedArraysSupported$f) {
    this.float64Array$1[0] = value;
    var value$1 = (this.int32Array$1[this.highOffset$1] | 0);
    var value$2 = (this.int32Array$1[this.lowOffset$1] | 0);
    return new $c_sjsr_RuntimeLong(value$2, value$1)
  } else {
    return this.doubleToLongBitsPolyfill__p1__D__J(value)
  }
});
var $d_jl_FloatingPointBits$ = new $TypeData().initClass({
  jl_FloatingPointBits$: 0
}, false, "java.lang.FloatingPointBits$", {
  jl_FloatingPointBits$: 1,
  O: 1
});
$c_jl_FloatingPointBits$.prototype.$classData = $d_jl_FloatingPointBits$;
var $n_jl_FloatingPointBits$ = (void 0);
function $m_jl_FloatingPointBits$() {
  if ((!$n_jl_FloatingPointBits$)) {
    $n_jl_FloatingPointBits$ = new $c_jl_FloatingPointBits$()
  };
  return $n_jl_FloatingPointBits$
}
/** @constructor */
function $c_s_util_hashing_MurmurHash3() {
  /*<skip>*/
}
$c_s_util_hashing_MurmurHash3.prototype = new $h_O();
$c_s_util_hashing_MurmurHash3.prototype.constructor = $c_s_util_hashing_MurmurHash3;
/** @constructor */
function $h_s_util_hashing_MurmurHash3() {
  /*<skip>*/
}
$h_s_util_hashing_MurmurHash3.prototype = $c_s_util_hashing_MurmurHash3.prototype;
$c_s_util_hashing_MurmurHash3.prototype.mixLast__I__I__I = (function(hash, data) {
  var k = data;
  k = $imul((-862048943), k);
  var i = k;
  k = ((i << 15) | ((i >>> 17) | 0));
  k = $imul(461845907, k);
  return (hash ^ k)
});
$c_s_util_hashing_MurmurHash3.prototype.mix__I__I__I = (function(hash, data) {
  var h = this.mixLast__I__I__I(hash, data);
  var i = h;
  h = ((i << 13) | ((i >>> 19) | 0));
  return (((-430675100) + $imul(5, h)) | 0)
});
$c_s_util_hashing_MurmurHash3.prototype.avalanche__p1__I__I = (function(hash) {
  var h = hash;
  h = (h ^ ((h >>> 16) | 0));
  h = $imul((-2048144789), h);
  h = (h ^ ((h >>> 13) | 0));
  h = $imul((-1028477387), h);
  h = (h ^ ((h >>> 16) | 0));
  return h
});
$c_s_util_hashing_MurmurHash3.prototype.productHash__s_Product__I__I = (function(x, seed) {
  var arr = x.productArity__I();
  if ((arr === 0)) {
    return $f_T__hashCode__I(x.productPrefix__T())
  } else {
    var h = seed;
    var i = 0;
    while ((i < arr)) {
      h = this.mix__I__I__I(h, $m_sr_Statics$().anyHash__O__I(x.productElement__I__O(i)));
      i = ((1 + i) | 0)
    };
    return this.finalizeHash__I__I__I(h, arr)
  }
});
$c_s_util_hashing_MurmurHash3.prototype.finalizeHash__I__I__I = (function(hash, length) {
  return this.avalanche__p1__I__I((hash ^ length))
});
/** @constructor */
function $c_sjsr_package$() {
  /*<skip>*/
}
$c_sjsr_package$.prototype = new $h_O();
$c_sjsr_package$.prototype.constructor = $c_sjsr_package$;
/** @constructor */
function $h_sjsr_package$() {
  /*<skip>*/
}
$h_sjsr_package$.prototype = $c_sjsr_package$.prototype;
$c_sjsr_package$.prototype.unwrapJavaScriptException__jl_Throwable__O = (function(th) {
  if ($is_sjs_js_JavaScriptException(th)) {
    var x2 = th;
    var e = x2.exception$4;
    return e
  } else {
    return th
  }
});
$c_sjsr_package$.prototype.wrapJavaScriptException__O__jl_Throwable = (function(e) {
  if ($is_jl_Throwable(e)) {
    var x2 = e;
    return x2
  } else {
    return new $c_sjs_js_JavaScriptException(e)
  }
});
var $d_sjsr_package$ = new $TypeData().initClass({
  sjsr_package$: 0
}, false, "scala.scalajs.runtime.package$", {
  sjsr_package$: 1,
  O: 1
});
$c_sjsr_package$.prototype.$classData = $d_sjsr_package$;
var $n_sjsr_package$ = (void 0);
function $m_sjsr_package$() {
  if ((!$n_sjsr_package$)) {
    $n_sjsr_package$ = new $c_sjsr_package$()
  };
  return $n_sjsr_package$
}
/** @constructor */
function $c_sr_Statics$() {
  /*<skip>*/
}
$c_sr_Statics$.prototype = new $h_O();
$c_sr_Statics$.prototype.constructor = $c_sr_Statics$;
/** @constructor */
function $h_sr_Statics$() {
  /*<skip>*/
}
$h_sr_Statics$.prototype = $c_sr_Statics$.prototype;
$c_sr_Statics$.prototype.doubleHash__D__I = (function(dv) {
  var iv = $doubleToInt(dv);
  if ((iv === dv)) {
    return iv
  } else {
    var this$1 = $m_sjsr_RuntimeLong$();
    var lo = this$1.scala$scalajs$runtime$RuntimeLong$$fromDoubleImpl__D__I(dv);
    var hi = this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
    return (($m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi) === dv) ? (lo ^ hi) : $m_jl_FloatingPointBits$().numberHashCode__D__I(dv))
  }
});
$c_sr_Statics$.prototype.anyHash__O__I = (function(x) {
  if ((x === null)) {
    return 0
  } else if (((typeof x) === "number")) {
    var x3 = (+x);
    return this.doubleHash__D__I(x3)
  } else if ($is_sjsr_RuntimeLong(x)) {
    var t = $uJ(x);
    var lo = t.lo$2;
    var hi = t.hi$2;
    return this.longHash__J__I(new $c_sjsr_RuntimeLong(lo, hi))
  } else {
    return $dp_hashCode__I(x)
  }
});
$c_sr_Statics$.prototype.longHash__J__I = (function(lv) {
  var lo = lv.lo$2;
  var lo$1 = lv.hi$2;
  return ((lo$1 === (lo >> 31)) ? lo : (lo ^ lo$1))
});
var $d_sr_Statics$ = new $TypeData().initClass({
  sr_Statics$: 0
}, false, "scala.runtime.Statics$", {
  sr_Statics$: 1,
  O: 1
});
$c_sr_Statics$.prototype.$classData = $d_sr_Statics$;
var $n_sr_Statics$ = (void 0);
function $m_sr_Statics$() {
  if ((!$n_sr_Statics$)) {
    $n_sr_Statics$ = new $c_sr_Statics$()
  };
  return $n_sr_Statics$
}
/** @constructor */
function $c_jl_Number() {
  /*<skip>*/
}
$c_jl_Number.prototype = new $h_O();
$c_jl_Number.prototype.constructor = $c_jl_Number;
/** @constructor */
function $h_jl_Number() {
  /*<skip>*/
}
$h_jl_Number.prototype = $c_jl_Number.prototype;
/** @constructor */
function $c_jl_Throwable() {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null
}
$c_jl_Throwable.prototype = new $h_O();
$c_jl_Throwable.prototype.constructor = $c_jl_Throwable;
/** @constructor */
function $h_jl_Throwable() {
  /*<skip>*/
}
$h_jl_Throwable.prototype = $c_jl_Throwable.prototype;
$c_jl_Throwable.prototype.fillInStackTrace__jl_Throwable = (function() {
  var v = Error.captureStackTrace;
  if ((v === (void 0))) {
    try {
      var e$1 = {}.undef()
    } catch (e) {
      var e$2 = $m_sjsr_package$().wrapJavaScriptException__O__jl_Throwable(e);
      if ((e$2 !== null)) {
        if ($is_sjs_js_JavaScriptException(e$2)) {
          var x5 = e$2;
          var e$3 = x5.exception$4;
          var e$1 = e$3
        } else {
          var e$1;
          throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(e$2)
        }
      } else {
        var e$1;
        throw e
      }
    };
    this.stackTraceStateInternal$1 = e$1
  } else {
    Error.captureStackTrace(this);
    this.stackTraceStateInternal$1 = this
  };
  return this
});
$c_jl_Throwable.prototype.getMessage__T = (function() {
  return this.s$1
});
$c_jl_Throwable.prototype.toString__T = (function() {
  var className = $objectGetClass(this).getName__T();
  var message = this.getMessage__T();
  return ((message === null) ? className : ((className + ": ") + message))
});
$c_jl_Throwable.prototype.init___T__jl_Throwable = (function(s, e) {
  this.s$1 = s;
  this.e$1 = e;
  this.fillInStackTrace__jl_Throwable();
  return this
});
function $is_jl_Throwable(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.jl_Throwable)))
}
function $isArrayOf_jl_Throwable(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Throwable)))
}
/** @constructor */
function $c_s_util_hashing_MurmurHash3$() {
  this.seqSeed$2 = 0;
  this.mapSeed$2 = 0;
  this.setSeed$2 = 0;
  $n_s_util_hashing_MurmurHash3$ = this;
  this.seqSeed$2 = $f_T__hashCode__I("Seq");
  this.mapSeed$2 = $f_T__hashCode__I("Map");
  this.setSeed$2 = $f_T__hashCode__I("Set")
}
$c_s_util_hashing_MurmurHash3$.prototype = new $h_s_util_hashing_MurmurHash3();
$c_s_util_hashing_MurmurHash3$.prototype.constructor = $c_s_util_hashing_MurmurHash3$;
/** @constructor */
function $h_s_util_hashing_MurmurHash3$() {
  /*<skip>*/
}
$h_s_util_hashing_MurmurHash3$.prototype = $c_s_util_hashing_MurmurHash3$.prototype;
var $d_s_util_hashing_MurmurHash3$ = new $TypeData().initClass({
  s_util_hashing_MurmurHash3$: 0
}, false, "scala.util.hashing.MurmurHash3$", {
  s_util_hashing_MurmurHash3$: 1,
  s_util_hashing_MurmurHash3: 1,
  O: 1
});
$c_s_util_hashing_MurmurHash3$.prototype.$classData = $d_s_util_hashing_MurmurHash3$;
var $n_s_util_hashing_MurmurHash3$ = (void 0);
function $m_s_util_hashing_MurmurHash3$() {
  if ((!$n_s_util_hashing_MurmurHash3$)) {
    $n_s_util_hashing_MurmurHash3$ = new $c_s_util_hashing_MurmurHash3$()
  };
  return $n_s_util_hashing_MurmurHash3$
}
function $f_sr_BoxedUnit__toString__T($thiz) {
  return "undefined"
}
function $f_sr_BoxedUnit__hashCode__I($thiz) {
  return 0
}
var $d_sr_BoxedUnit = new $TypeData().initClass({
  sr_BoxedUnit: 0
}, false, "scala.runtime.BoxedUnit", {
  sr_BoxedUnit: 1,
  O: 1,
  Ljava_io_Serializable: 1
}, (void 0), (void 0), (function(x) {
  return (x === (void 0))
}));
function $f_jl_Boolean__toString__T($thiz) {
  var b = (!(!$thiz));
  return ("" + b)
}
function $f_jl_Boolean__hashCode__I($thiz) {
  return ((!(!$thiz)) ? 1231 : 1237)
}
var $d_jl_Boolean = new $TypeData().initClass({
  jl_Boolean: 0
}, false, "java.lang.Boolean", {
  jl_Boolean: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return ((typeof x) === "boolean")
}));
function $f_jl_Character__toString__T($thiz) {
  var c = $uC($thiz);
  return String.fromCharCode(c)
}
function $f_jl_Character__hashCode__I($thiz) {
  return $uC($thiz)
}
var $d_jl_Character = new $TypeData().initClass({
  jl_Character: 0
}, false, "java.lang.Character", {
  jl_Character: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $isChar(x)
}));
/** @constructor */
function $c_jl_Exception() {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null
}
$c_jl_Exception.prototype = new $h_jl_Throwable();
$c_jl_Exception.prototype.constructor = $c_jl_Exception;
/** @constructor */
function $h_jl_Exception() {
  /*<skip>*/
}
$h_jl_Exception.prototype = $c_jl_Exception.prototype;
/** @constructor */
function $c_sjsr_RuntimeLong$() {
  this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0
}
$c_sjsr_RuntimeLong$.prototype = new $h_O();
$c_sjsr_RuntimeLong$.prototype.constructor = $c_sjsr_RuntimeLong$;
/** @constructor */
function $h_sjsr_RuntimeLong$() {
  /*<skip>*/
}
$h_sjsr_RuntimeLong$.prototype = $c_sjsr_RuntimeLong$.prototype;
$c_sjsr_RuntimeLong$.prototype.toUnsignedString__p1__I__I__T = (function(lo, hi) {
  if ((((-2097152) & hi) === 0)) {
    var this$3 = ((4.294967296E9 * hi) + (+(lo >>> 0)));
    return ("" + this$3)
  } else {
    return this.unsignedDivModHelper__p1__I__I__I__I__I__O(lo, hi, 1000000000, 0, 2)
  }
});
$c_sjsr_RuntimeLong$.prototype.divideImpl__I__I__I__I__I = (function(alo, ahi, blo, bhi) {
  if (((blo | bhi) === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  };
  if ((ahi === (alo >> 31))) {
    if ((bhi === (blo >> 31))) {
      if (((alo === (-2147483648)) && (blo === (-1)))) {
        this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
        return (-2147483648)
      } else {
        var lo = ((alo / blo) | 0);
        this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (lo >> 31);
        return lo
      }
    } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (-1);
      return (-1)
    } else {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
      return 0
    }
  } else {
    if ((ahi < 0)) {
      var lo$1 = ((-alo) | 0);
      var hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
      var aAbs_$_lo$2 = lo$1;
      var aAbs_$_hi$2 = hi
    } else {
      var aAbs_$_lo$2 = alo;
      var aAbs_$_hi$2 = ahi
    };
    if ((bhi < 0)) {
      var lo$2 = ((-blo) | 0);
      var hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
      var bAbs_$_lo$2 = lo$2;
      var bAbs_$_hi$2 = hi$1
    } else {
      var bAbs_$_lo$2 = blo;
      var bAbs_$_hi$2 = bhi
    };
    var absRLo = this.unsigned$und$div__p1__I__I__I__I__I(aAbs_$_lo$2, aAbs_$_hi$2, bAbs_$_lo$2, bAbs_$_hi$2);
    if (((ahi ^ bhi) >= 0)) {
      return absRLo
    } else {
      var hi$2 = this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
      return ((-absRLo) | 0)
    }
  }
});
$c_sjsr_RuntimeLong$.prototype.scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D = (function(lo, hi) {
  if ((hi < 0)) {
    var x = ((lo !== 0) ? (~hi) : ((-hi) | 0));
    var x$1 = ((-lo) | 0);
    return (-((4.294967296E9 * (+(x >>> 0))) + (+(x$1 >>> 0))))
  } else {
    return ((4.294967296E9 * hi) + (+(lo >>> 0)))
  }
});
$c_sjsr_RuntimeLong$.prototype.fromDouble__D__sjsr_RuntimeLong = (function(value) {
  var lo = this.scala$scalajs$runtime$RuntimeLong$$fromDoubleImpl__D__I(value);
  return new $c_sjsr_RuntimeLong(lo, this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f)
});
$c_sjsr_RuntimeLong$.prototype.scala$scalajs$runtime$RuntimeLong$$fromDoubleImpl__D__I = (function(value) {
  if ((value < (-9.223372036854776E18))) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (-2147483648);
    return 0
  } else if ((value >= 9.223372036854776E18)) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 2147483647;
    return (-1)
  } else {
    var rawLo = ((value | 0) | 0);
    var x = (value / 4.294967296E9);
    var rawHi = ((x | 0) | 0);
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (((value < 0.0) && (rawLo !== 0)) ? (((-1) + rawHi) | 0) : rawHi);
    return rawLo
  }
});
$c_sjsr_RuntimeLong$.prototype.unsigned$und$div__p1__I__I__I__I__I = (function(alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      var aDouble = ((4.294967296E9 * ahi) + (+(alo >>> 0)));
      var bDouble = ((4.294967296E9 * bhi) + (+(blo >>> 0)));
      var rDouble = (aDouble / bDouble);
      var x = (rDouble / 4.294967296E9);
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ((x | 0) | 0);
      return ((rDouble | 0) | 0)
    } else {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
      return 0
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    var pow = ((31 - $clz32(blo)) | 0);
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ((ahi >>> pow) | 0);
    return (((alo >>> pow) | 0) | ((ahi << 1) << ((31 - pow) | 0)))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    var pow$2 = ((31 - $clz32(bhi)) | 0);
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
    return ((ahi >>> pow$2) | 0)
  } else {
    return (this.unsignedDivModHelper__p1__I__I__I__I__I__O(alo, ahi, blo, bhi, 0) | 0)
  }
});
$c_sjsr_RuntimeLong$.prototype.scala$scalajs$runtime$RuntimeLong$$toString__I__I__T = (function(lo, hi) {
  return ((hi === (lo >> 31)) ? ("" + lo) : ((hi < 0) ? ("-" + this.toUnsignedString__p1__I__I__T(((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))) : this.toUnsignedString__p1__I__I__T(lo, hi)))
});
$c_sjsr_RuntimeLong$.prototype.fromInt__I__sjsr_RuntimeLong = (function(value) {
  return new $c_sjsr_RuntimeLong(value, (value >> 31))
});
$c_sjsr_RuntimeLong$.prototype.scala$scalajs$runtime$RuntimeLong$$compare__I__I__I__I__I = (function(alo, ahi, blo, bhi) {
  return ((ahi === bhi) ? ((alo === blo) ? 0 : ((((-2147483648) ^ alo) < ((-2147483648) ^ blo)) ? (-1) : 1)) : ((ahi < bhi) ? (-1) : 1))
});
$c_sjsr_RuntimeLong$.prototype.unsignedDivModHelper__p1__I__I__I__I__I__O = (function(alo, ahi, blo, bhi, ask) {
  var shift = ((((bhi !== 0) ? $clz32(bhi) : ((32 + $clz32(blo)) | 0)) - ((ahi !== 0) ? $clz32(ahi) : ((32 + $clz32(alo)) | 0))) | 0);
  var n = shift;
  var lo = (((32 & n) === 0) ? (blo << n) : 0);
  var hi = (((32 & n) === 0) ? (((((blo >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (bhi << n)) : (blo << n));
  var bShiftLo = lo;
  var bShiftHi = hi;
  var remLo = alo;
  var remHi = ahi;
  var quotLo = 0;
  var quotHi = 0;
  while (((shift >= 0) && (((-2097152) & remHi) !== 0))) {
    var alo$1 = remLo;
    var ahi$1 = remHi;
    var blo$1 = bShiftLo;
    var bhi$1 = bShiftHi;
    if (((ahi$1 === bhi$1) ? (((-2147483648) ^ alo$1) >= ((-2147483648) ^ blo$1)) : (((-2147483648) ^ ahi$1) >= ((-2147483648) ^ bhi$1)))) {
      var lo$1 = remLo;
      var hi$1 = remHi;
      var lo$2 = bShiftLo;
      var hi$2 = bShiftHi;
      var lo$3 = ((lo$1 - lo$2) | 0);
      var hi$3 = ((((-2147483648) ^ lo$3) > ((-2147483648) ^ lo$1)) ? (((-1) + ((hi$1 - hi$2) | 0)) | 0) : ((hi$1 - hi$2) | 0));
      remLo = lo$3;
      remHi = hi$3;
      if ((shift < 32)) {
        quotLo = (quotLo | (1 << shift))
      } else {
        quotHi = (quotHi | (1 << shift))
      }
    };
    shift = (((-1) + shift) | 0);
    var lo$4 = bShiftLo;
    var hi$4 = bShiftHi;
    var lo$5 = (((lo$4 >>> 1) | 0) | (hi$4 << 31));
    var hi$5 = ((hi$4 >>> 1) | 0);
    bShiftLo = lo$5;
    bShiftHi = hi$5
  };
  var alo$2 = remLo;
  var ahi$2 = remHi;
  if (((ahi$2 === bhi) ? (((-2147483648) ^ alo$2) >= ((-2147483648) ^ blo)) : (((-2147483648) ^ ahi$2) >= ((-2147483648) ^ bhi)))) {
    var lo$6 = remLo;
    var hi$6 = remHi;
    var remDouble = ((4.294967296E9 * hi$6) + (+(lo$6 >>> 0)));
    var bDouble = ((4.294967296E9 * bhi) + (+(blo >>> 0)));
    if ((ask !== 1)) {
      var x = (remDouble / bDouble);
      var lo$7 = ((x | 0) | 0);
      var x$1 = (x / 4.294967296E9);
      var hi$7 = ((x$1 | 0) | 0);
      var lo$8 = quotLo;
      var hi$8 = quotHi;
      var lo$9 = ((lo$8 + lo$7) | 0);
      var hi$9 = ((((-2147483648) ^ lo$9) < ((-2147483648) ^ lo$8)) ? ((1 + ((hi$8 + hi$7) | 0)) | 0) : ((hi$8 + hi$7) | 0));
      quotLo = lo$9;
      quotHi = hi$9
    };
    if ((ask !== 0)) {
      var rem_mod_bDouble = (remDouble % bDouble);
      remLo = ((rem_mod_bDouble | 0) | 0);
      var x$2 = (rem_mod_bDouble / 4.294967296E9);
      remHi = ((x$2 | 0) | 0)
    }
  };
  if ((ask === 0)) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = quotHi;
    return quotLo
  } else if ((ask === 1)) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = remHi;
    return remLo
  } else {
    var lo$10 = quotLo;
    var hi$10 = quotHi;
    var quot = ((4.294967296E9 * hi$10) + (+(lo$10 >>> 0)));
    var this$13 = remLo;
    var remStr = ("" + this$13);
    var start = (remStr.length | 0);
    return ((("" + quot) + "000000000".substring(start)) + remStr)
  }
});
$c_sjsr_RuntimeLong$.prototype.remainderImpl__I__I__I__I__I = (function(alo, ahi, blo, bhi) {
  if (((blo | bhi) === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  };
  if ((ahi === (alo >> 31))) {
    if ((bhi === (blo >> 31))) {
      if ((blo !== (-1))) {
        var lo = ((alo % blo) | 0);
        this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (lo >> 31);
        return lo
      } else {
        this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
        return 0
      }
    } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
      return 0
    } else {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ahi;
      return alo
    }
  } else {
    if ((ahi < 0)) {
      var lo$1 = ((-alo) | 0);
      var hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
      var aAbs_$_lo$2 = lo$1;
      var aAbs_$_hi$2 = hi
    } else {
      var aAbs_$_lo$2 = alo;
      var aAbs_$_hi$2 = ahi
    };
    if ((bhi < 0)) {
      var lo$2 = ((-blo) | 0);
      var hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
      var bAbs_$_lo$2 = lo$2;
      var bAbs_$_hi$2 = hi$1
    } else {
      var bAbs_$_lo$2 = blo;
      var bAbs_$_hi$2 = bhi
    };
    var absRLo = this.unsigned$und$percent__p1__I__I__I__I__I(aAbs_$_lo$2, aAbs_$_hi$2, bAbs_$_lo$2, bAbs_$_hi$2);
    if ((ahi < 0)) {
      var hi$2 = this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f;
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
      return ((-absRLo) | 0)
    } else {
      return absRLo
    }
  }
});
$c_sjsr_RuntimeLong$.prototype.unsigned$und$percent__p1__I__I__I__I__I = (function(alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      var aDouble = ((4.294967296E9 * ahi) + (+(alo >>> 0)));
      var bDouble = ((4.294967296E9 * bhi) + (+(blo >>> 0)));
      var rDouble = (aDouble % bDouble);
      var x = (rDouble / 4.294967296E9);
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ((x | 0) | 0);
      return ((rDouble | 0) | 0)
    } else {
      this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = ahi;
      return alo
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = 0;
    return (alo & (((-1) + blo) | 0))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    this.scala$scalajs$runtime$RuntimeLong$$hiReturn$f = (ahi & (((-1) + bhi) | 0));
    return alo
  } else {
    return (this.unsignedDivModHelper__p1__I__I__I__I__I__O(alo, ahi, blo, bhi, 1) | 0)
  }
});
var $d_sjsr_RuntimeLong$ = new $TypeData().initClass({
  sjsr_RuntimeLong$: 0
}, false, "scala.scalajs.runtime.RuntimeLong$", {
  sjsr_RuntimeLong$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sjsr_RuntimeLong$.prototype.$classData = $d_sjsr_RuntimeLong$;
var $n_sjsr_RuntimeLong$ = (void 0);
function $m_sjsr_RuntimeLong$() {
  if ((!$n_sjsr_RuntimeLong$)) {
    $n_sjsr_RuntimeLong$ = new $c_sjsr_RuntimeLong$()
  };
  return $n_sjsr_RuntimeLong$
}
function $f_T__toString__T($thiz) {
  return $thiz
}
function $f_T__hashCode__I($thiz) {
  var res = 0;
  var mul = 1;
  var i = (((-1) + ($thiz.length | 0)) | 0);
  while ((i >= 0)) {
    var jsx$1 = res;
    var index = i;
    res = ((jsx$1 + $imul((65535 & ($thiz.charCodeAt(index) | 0)), mul)) | 0);
    mul = $imul(31, mul);
    i = (((-1) + i) | 0)
  };
  return res
}
function $is_T(obj) {
  return ((typeof obj) === "string")
}
function $isArrayOf_T(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.T)))
}
var $d_T = new $TypeData().initClass({
  T: 0
}, false, "java.lang.String", {
  T: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1,
  jl_CharSequence: 1
}, (void 0), (void 0), $is_T);
function $f_jl_Byte__toString__T($thiz) {
  var b = ($thiz | 0);
  return ("" + b)
}
function $f_jl_Byte__hashCode__I($thiz) {
  return ($thiz | 0)
}
var $d_jl_Byte = new $TypeData().initClass({
  jl_Byte: 0
}, false, "java.lang.Byte", {
  jl_Byte: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $isByte(x)
}));
/** @constructor */
function $c_jl_CloneNotSupportedException() {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null;
  $c_jl_Throwable.prototype.init___T__jl_Throwable.call(this, null, null)
}
$c_jl_CloneNotSupportedException.prototype = new $h_jl_Exception();
$c_jl_CloneNotSupportedException.prototype.constructor = $c_jl_CloneNotSupportedException;
/** @constructor */
function $h_jl_CloneNotSupportedException() {
  /*<skip>*/
}
$h_jl_CloneNotSupportedException.prototype = $c_jl_CloneNotSupportedException.prototype;
var $d_jl_CloneNotSupportedException = new $TypeData().initClass({
  jl_CloneNotSupportedException: 0
}, false, "java.lang.CloneNotSupportedException", {
  jl_CloneNotSupportedException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_CloneNotSupportedException.prototype.$classData = $d_jl_CloneNotSupportedException;
function $f_jl_Double__toString__T($thiz) {
  var d = (+$thiz);
  return ("" + d)
}
function $f_jl_Double__hashCode__I($thiz) {
  var value = (+$thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
}
function $isArrayOf_jl_Double(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Double)))
}
var $d_jl_Double = new $TypeData().initClass({
  jl_Double: 0
}, false, "java.lang.Double", {
  jl_Double: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return ((typeof x) === "number")
}));
function $f_jl_Float__toString__T($thiz) {
  var f = (+$thiz);
  return ("" + f)
}
function $f_jl_Float__hashCode__I($thiz) {
  var value = (+$thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
}
var $d_jl_Float = new $TypeData().initClass({
  jl_Float: 0
}, false, "java.lang.Float", {
  jl_Float: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $isFloat(x)
}));
function $f_jl_Integer__toString__T($thiz) {
  var i = ($thiz | 0);
  return ("" + i)
}
function $f_jl_Integer__hashCode__I($thiz) {
  return ($thiz | 0)
}
var $d_jl_Integer = new $TypeData().initClass({
  jl_Integer: 0
}, false, "java.lang.Integer", {
  jl_Integer: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $isInt(x)
}));
function $f_jl_Long__toString__T($thiz) {
  var t = $uJ($thiz);
  var lo = t.lo$2;
  var hi = t.hi$2;
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toString__I__I__T(lo, hi)
}
function $f_jl_Long__hashCode__I($thiz) {
  var t = $uJ($thiz);
  var lo = t.lo$2;
  var hi = t.hi$2;
  return (lo ^ hi)
}
function $isArrayOf_jl_Long(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Long)))
}
var $d_jl_Long = new $TypeData().initClass({
  jl_Long: 0
}, false, "java.lang.Long", {
  jl_Long: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $is_sjsr_RuntimeLong(x)
}));
/** @constructor */
function $c_jl_RuntimeException() {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null
}
$c_jl_RuntimeException.prototype = new $h_jl_Exception();
$c_jl_RuntimeException.prototype.constructor = $c_jl_RuntimeException;
/** @constructor */
function $h_jl_RuntimeException() {
  /*<skip>*/
}
$h_jl_RuntimeException.prototype = $c_jl_RuntimeException.prototype;
function $f_jl_Short__toString__T($thiz) {
  var s = ($thiz | 0);
  return ("" + s)
}
function $f_jl_Short__hashCode__I($thiz) {
  return ($thiz | 0)
}
var $d_jl_Short = new $TypeData().initClass({
  jl_Short: 0
}, false, "java.lang.Short", {
  jl_Short: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), (function(x) {
  return $isShort(x)
}));
/** @constructor */
function $c_sjsr_RuntimeLong(lo, hi) {
  this.lo$2 = 0;
  this.hi$2 = 0;
  this.lo$2 = lo;
  this.hi$2 = hi
}
$c_sjsr_RuntimeLong.prototype = new $h_jl_Number();
$c_sjsr_RuntimeLong.prototype.constructor = $c_sjsr_RuntimeLong;
/** @constructor */
function $h_sjsr_RuntimeLong() {
  /*<skip>*/
}
$h_sjsr_RuntimeLong.prototype = $c_sjsr_RuntimeLong.prototype;
$c_sjsr_RuntimeLong.prototype.longValue__J = (function() {
  return $uJ(this)
});
$c_sjsr_RuntimeLong.prototype.$$bar__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  return new $c_sjsr_RuntimeLong((this.lo$2 | b.lo$2), (this.hi$2 | b.hi$2))
});
$c_sjsr_RuntimeLong.prototype.$$greater$eq__sjsr_RuntimeLong__Z = (function(b) {
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  return ((ahi === bhi) ? (((-2147483648) ^ this.lo$2) >= ((-2147483648) ^ b.lo$2)) : (ahi > bhi))
});
$c_sjsr_RuntimeLong.prototype.byteValue__B = (function() {
  return ((this.lo$2 << 24) >> 24)
});
$c_sjsr_RuntimeLong.prototype.equals__O__Z = (function(that) {
  if ($is_sjsr_RuntimeLong(that)) {
    var x2 = that;
    return ((this.lo$2 === x2.lo$2) && (this.hi$2 === x2.hi$2))
  } else {
    return false
  }
});
$c_sjsr_RuntimeLong.prototype.$$less__sjsr_RuntimeLong__Z = (function(b) {
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  return ((ahi === bhi) ? (((-2147483648) ^ this.lo$2) < ((-2147483648) ^ b.lo$2)) : (ahi < bhi))
});
$c_sjsr_RuntimeLong.prototype.$$times__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  var alo = this.lo$2;
  var blo = b.lo$2;
  var a0 = (65535 & alo);
  var a1 = ((alo >>> 16) | 0);
  var b0 = (65535 & blo);
  var b1 = ((blo >>> 16) | 0);
  var a0b0 = $imul(a0, b0);
  var a1b0 = $imul(a1, b0);
  var a0b1 = $imul(a0, b1);
  var lo = ((a0b0 + (((a1b0 + a0b1) | 0) << 16)) | 0);
  var c1part = ((((a0b0 >>> 16) | 0) + a0b1) | 0);
  var hi = (((((((($imul(alo, b.hi$2) + $imul(this.hi$2, blo)) | 0) + $imul(a1, b1)) | 0) + ((c1part >>> 16) | 0)) | 0) + (((((65535 & c1part) + a1b0) | 0) >>> 16) | 0)) | 0);
  return new $c_sjsr_RuntimeLong(lo, hi)
});
$c_sjsr_RuntimeLong.prototype.$$percent__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  var this$1 = $m_sjsr_RuntimeLong$();
  var lo = this$1.remainderImpl__I__I__I__I__I(this.lo$2, this.hi$2, b.lo$2, b.hi$2);
  return new $c_sjsr_RuntimeLong(lo, this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f)
});
$c_sjsr_RuntimeLong.prototype.toString__T = (function() {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toString__I__I__T(this.lo$2, this.hi$2)
});
$c_sjsr_RuntimeLong.prototype.compareTo__O__I = (function(x$1) {
  var that = x$1;
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$compare__I__I__I__I__I(this.lo$2, this.hi$2, that.lo$2, that.hi$2)
});
$c_sjsr_RuntimeLong.prototype.$$less$eq__sjsr_RuntimeLong__Z = (function(b) {
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  return ((ahi === bhi) ? (((-2147483648) ^ this.lo$2) <= ((-2147483648) ^ b.lo$2)) : (ahi < bhi))
});
$c_sjsr_RuntimeLong.prototype.$$amp__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  return new $c_sjsr_RuntimeLong((this.lo$2 & b.lo$2), (this.hi$2 & b.hi$2))
});
$c_sjsr_RuntimeLong.prototype.$$greater$greater$greater__I__sjsr_RuntimeLong = (function(n) {
  return new $c_sjsr_RuntimeLong((((32 & n) === 0) ? (((this.lo$2 >>> n) | 0) | ((this.hi$2 << 1) << ((31 - n) | 0))) : ((this.hi$2 >>> n) | 0)), (((32 & n) === 0) ? ((this.hi$2 >>> n) | 0) : 0))
});
$c_sjsr_RuntimeLong.prototype.$$greater__sjsr_RuntimeLong__Z = (function(b) {
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  return ((ahi === bhi) ? (((-2147483648) ^ this.lo$2) > ((-2147483648) ^ b.lo$2)) : (ahi > bhi))
});
$c_sjsr_RuntimeLong.prototype.$$less$less__I__sjsr_RuntimeLong = (function(n) {
  return new $c_sjsr_RuntimeLong((((32 & n) === 0) ? (this.lo$2 << n) : 0), (((32 & n) === 0) ? (((((this.lo$2 >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (this.hi$2 << n)) : (this.lo$2 << n)))
});
$c_sjsr_RuntimeLong.prototype.toInt__I = (function() {
  return this.lo$2
});
$c_sjsr_RuntimeLong.prototype.notEquals__sjsr_RuntimeLong__Z = (function(b) {
  return (!((this.lo$2 === b.lo$2) && (this.hi$2 === b.hi$2)))
});
$c_sjsr_RuntimeLong.prototype.unary$und$minus__sjsr_RuntimeLong = (function() {
  var lo = this.lo$2;
  var hi = this.hi$2;
  return new $c_sjsr_RuntimeLong(((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))
});
$c_sjsr_RuntimeLong.prototype.$$plus__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  var alo = this.lo$2;
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  var lo = ((alo + b.lo$2) | 0);
  return new $c_sjsr_RuntimeLong(lo, ((((-2147483648) ^ lo) < ((-2147483648) ^ alo)) ? ((1 + ((ahi + bhi) | 0)) | 0) : ((ahi + bhi) | 0)))
});
$c_sjsr_RuntimeLong.prototype.shortValue__S = (function() {
  return ((this.lo$2 << 16) >> 16)
});
$c_sjsr_RuntimeLong.prototype.$$greater$greater__I__sjsr_RuntimeLong = (function(n) {
  return new $c_sjsr_RuntimeLong((((32 & n) === 0) ? (((this.lo$2 >>> n) | 0) | ((this.hi$2 << 1) << ((31 - n) | 0))) : (this.hi$2 >> n)), (((32 & n) === 0) ? (this.hi$2 >> n) : (this.hi$2 >> 31)))
});
$c_sjsr_RuntimeLong.prototype.toDouble__D = (function() {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D(this.lo$2, this.hi$2)
});
$c_sjsr_RuntimeLong.prototype.$$div__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  var this$1 = $m_sjsr_RuntimeLong$();
  var lo = this$1.divideImpl__I__I__I__I__I(this.lo$2, this.hi$2, b.lo$2, b.hi$2);
  return new $c_sjsr_RuntimeLong(lo, this$1.scala$scalajs$runtime$RuntimeLong$$hiReturn$f)
});
$c_sjsr_RuntimeLong.prototype.doubleValue__D = (function() {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D(this.lo$2, this.hi$2)
});
$c_sjsr_RuntimeLong.prototype.hashCode__I = (function() {
  return (this.lo$2 ^ this.hi$2)
});
$c_sjsr_RuntimeLong.prototype.intValue__I = (function() {
  return this.lo$2
});
$c_sjsr_RuntimeLong.prototype.unary$und$tilde__sjsr_RuntimeLong = (function() {
  return new $c_sjsr_RuntimeLong((~this.lo$2), (~this.hi$2))
});
$c_sjsr_RuntimeLong.prototype.compareTo__jl_Long__I = (function(that) {
  return $m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$compare__I__I__I__I__I(this.lo$2, this.hi$2, that.lo$2, that.hi$2)
});
$c_sjsr_RuntimeLong.prototype.floatValue__F = (function() {
  return $fround($m_sjsr_RuntimeLong$().scala$scalajs$runtime$RuntimeLong$$toDouble__I__I__D(this.lo$2, this.hi$2))
});
$c_sjsr_RuntimeLong.prototype.$$minus__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  var alo = this.lo$2;
  var ahi = this.hi$2;
  var bhi = b.hi$2;
  var lo = ((alo - b.lo$2) | 0);
  return new $c_sjsr_RuntimeLong(lo, ((((-2147483648) ^ lo) > ((-2147483648) ^ alo)) ? (((-1) + ((ahi - bhi) | 0)) | 0) : ((ahi - bhi) | 0)))
});
$c_sjsr_RuntimeLong.prototype.$$up__sjsr_RuntimeLong__sjsr_RuntimeLong = (function(b) {
  return new $c_sjsr_RuntimeLong((this.lo$2 ^ b.lo$2), (this.hi$2 ^ b.hi$2))
});
$c_sjsr_RuntimeLong.prototype.equals__sjsr_RuntimeLong__Z = (function(b) {
  return ((this.lo$2 === b.lo$2) && (this.hi$2 === b.hi$2))
});
function $is_sjsr_RuntimeLong(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sjsr_RuntimeLong)))
}
function $isArrayOf_sjsr_RuntimeLong(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sjsr_RuntimeLong)))
}
var $d_sjsr_RuntimeLong = new $TypeData().initClass({
  sjsr_RuntimeLong: 0
}, false, "scala.scalajs.runtime.RuntimeLong", {
  sjsr_RuntimeLong: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
});
$c_sjsr_RuntimeLong.prototype.$classData = $d_sjsr_RuntimeLong;
/** @constructor */
function $c_jl_ArithmeticException(s) {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null;
  $c_jl_Throwable.prototype.init___T__jl_Throwable.call(this, s, null)
}
$c_jl_ArithmeticException.prototype = new $h_jl_RuntimeException();
$c_jl_ArithmeticException.prototype.constructor = $c_jl_ArithmeticException;
/** @constructor */
function $h_jl_ArithmeticException() {
  /*<skip>*/
}
$h_jl_ArithmeticException.prototype = $c_jl_ArithmeticException.prototype;
var $d_jl_ArithmeticException = new $TypeData().initClass({
  jl_ArithmeticException: 0
}, false, "java.lang.ArithmeticException", {
  jl_ArithmeticException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ArithmeticException.prototype.$classData = $d_jl_ArithmeticException;
/** @constructor */
function $c_jl_IndexOutOfBoundsException(s) {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null;
  $c_jl_Throwable.prototype.init___T__jl_Throwable.call(this, s, null)
}
$c_jl_IndexOutOfBoundsException.prototype = new $h_jl_RuntimeException();
$c_jl_IndexOutOfBoundsException.prototype.constructor = $c_jl_IndexOutOfBoundsException;
/** @constructor */
function $h_jl_IndexOutOfBoundsException() {
  /*<skip>*/
}
$h_jl_IndexOutOfBoundsException.prototype = $c_jl_IndexOutOfBoundsException.prototype;
var $d_jl_IndexOutOfBoundsException = new $TypeData().initClass({
  jl_IndexOutOfBoundsException: 0
}, false, "java.lang.IndexOutOfBoundsException", {
  jl_IndexOutOfBoundsException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_IndexOutOfBoundsException.prototype.$classData = $d_jl_IndexOutOfBoundsException;
/** @constructor */
function $c_sjs_js_JavaScriptException(exception) {
  this.s$1 = null;
  this.e$1 = null;
  this.stackTraceStateInternal$1 = null;
  this.stackTrace$1 = null;
  this.exception$4 = null;
  this.exception$4 = exception;
  $c_jl_Throwable.prototype.init___T__jl_Throwable.call(this, null, null)
}
$c_sjs_js_JavaScriptException.prototype = new $h_jl_RuntimeException();
$c_sjs_js_JavaScriptException.prototype.constructor = $c_sjs_js_JavaScriptException;
/** @constructor */
function $h_sjs_js_JavaScriptException() {
  /*<skip>*/
}
$h_sjs_js_JavaScriptException.prototype = $c_sjs_js_JavaScriptException.prototype;
$c_sjs_js_JavaScriptException.prototype.productPrefix__T = (function() {
  return "JavaScriptException"
});
$c_sjs_js_JavaScriptException.prototype.productArity__I = (function() {
  return 1
});
$c_sjs_js_JavaScriptException.prototype.fillInStackTrace__jl_Throwable = (function() {
  this.setStackTraceStateInternal__O__(this.exception$4);
  return this
});
$c_sjs_js_JavaScriptException.prototype.productElement__I__O = (function(x$1) {
  switch (x$1) {
    case 0: {
      return this.exception$4;
      break
    }
    default: {
      throw new $c_jl_IndexOutOfBoundsException(("" + x$1))
    }
  }
});
$c_sjs_js_JavaScriptException.prototype.getMessage__T = (function() {
  return $dp_toString__T(this.exception$4)
});
$c_sjs_js_JavaScriptException.prototype.hashCode__I = (function() {
  var this$2 = $m_s_util_hashing_MurmurHash3$();
  return this$2.productHash__s_Product__I__I(this, (-889275714))
});
$c_sjs_js_JavaScriptException.prototype.setStackTraceStateInternal__O__ = (function(e) {
  this.stackTraceStateInternal$1 = e
});
function $is_sjs_js_JavaScriptException(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sjs_js_JavaScriptException)))
}
function $isArrayOf_sjs_js_JavaScriptException(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sjs_js_JavaScriptException)))
}
var $d_sjs_js_JavaScriptException = new $TypeData().initClass({
  sjs_js_JavaScriptException: 0
}, false, "scala.scalajs.js.JavaScriptException", {
  sjs_js_JavaScriptException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1
});
$c_sjs_js_JavaScriptException.prototype.$classData = $d_sjs_js_JavaScriptException;
$L0 = new $c_sjsr_RuntimeLong(0, 0);
var Module = $m_Lorg_apidesign_bck2brwr_vm4brwsr_Module$();

$uJ = function(value) {
  if (value === null) {
    return $L0;
  }
  if ($is_sjsr_RuntimeLong(value)) {
    return value;
  } else {
    return Module.$$js$exported$meth$fromDouble__D__O(value)
  }
};

$c_jl_ArithmeticException = function(msg) {
        var exception = new vm.java_lang_ArithmeticException;
        vm.java_lang_ArithmeticException(false).constructor
          .cons__VLjava_lang_String_2.call(exception, msg);
        return exception;
};


    function add32(x, y) {
        return (x + y) | 0;
    };
    function mul32(x, y) {
        return (((x * (y >> 16)) << 16) + x * (y & 0xFFFF)) | 0;
    };
    var __m32 = 0xFFFFFFFF;

    numberPrototype.next32 = function(low) {
        if (this === 0) {
            return low;
        }
        return Module.to64(low | 0, this | 0);
    };

    numberPrototype.toFP = function() {
        return Module.toDouble(this);
    };
    numberPrototype.toLong = function() {
        return Module.fromDouble(this.valueOf());
    };

    var b = numberPrototype['__bit64'] = {};
    b.add64 = function(x, y) { return Module.add64(x, y); };
    b.sub64 = function(x, y) { return Module.sub64(x, y); };
    b.mul64 = function(x, y) { return Module.mul64(x, y); };
    b.div64 = function(x, y) { return Module.div64(x, y); };
    b.mod64 = function(x, y) { return Module.mod64(x, y); };
    b.and64 = function(x, y) { return Module.and64(x, y); };
    b.or64 = function(x, y) { return Module.or64(x, y); };
    b.xor64 = function(x, y) { return Module.xor64(x, y); };
    b.neg64 = function(x) { return Module.neg64(x); };
    b.shl64 = function(x, y) { return Module.shl64(x, y); };
    b.shr64 = function(x, y) { return Module.shr64(x, y); };
    b.ushr64 = function(x, y) { return Module.ushr64(x, y); };
    b.compare64 = function(x, y) { return Module.compare64(x, y); };
    b.high32 = function(x) { return Module.high32(x); }
    b.low32 = function(x) { return Module.low32(x); }
    b.toFP = function(x) { return Module.toDouble(x); }
})(Number.prototype);

vm.java_lang_Number(false);
