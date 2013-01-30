// empty line needed here
Number.prototype.add32 = function(x) { return (this + x) | 0; };
Number.prototype.sub32 = function(x) { return (this - x) | 0; };
Number.prototype.mul32 = function(x) { 
    return (((this * (x >> 16)) << 16) + this * (x & 0xFFFF)) | 0;
};

Number.prototype.toInt8 = function()  { return (this << 24) >> 24; };
Number.prototype.toInt16 = function() { return (this << 16) >> 16; };

var __m32 = 0xFFFFFFFF;

Number.prototype.next32 = function(low) {
  if (this === 0) {
    return low;
  }
  var l = new Number(low);
  l.hi = this;
  return l;
};

Number.prototype.high32 = function() { 
    return this.hi ? this.hi : (Math.floor(this / (__m32+1))) | 0;
};
Number.prototype.toInt32 = function() { return this | 0; };
Number.prototype.toFP = function() {
    return this.hi ? this.hi * (__m32+1) + this : this;
};
Number.prototype.toLong = function() {
    var hi = (this > __m32) ? (Math.floor(this / (__m32+1))) | 0 : 0;
    return hi.next32(this % (__m32+1));
};

Number.prototype.toExactString = function() {
    if (this.hi) {
        var res = 0;
        var a = [ 6,9,2,7,6,9,4,9,2,4 ];
        var s = '';
        var digit;
        var hi = this.hi;
        var low = this;
        for (var i = 0; i < a.length; i++) {
            res += hi * a[i];
            var low_digit = low % 10;
            digit = (res % 10) + low_digit;

            low = Math.floor(low / 10);
            res = Math.floor(res / 10);

            if (digit >= 10) {
                digit -= 10;
                res++;
            }
            s = String(digit).concat(s);
        }
        return String(res).concat(s);
    }
    return String(this);
};

Number.prototype.add64 = function(x) {
    var low = this + x;
    carry = 0;
    if (low > __m32) {
        carry = 1;
        low -= (__m32+1);
    }
    var hi = (this.high32() + x.high32() + carry) | 0;
    return hi.next32(low);
};

Number.prototype.div64 = function(x) {
    var low = Math.floor(this.toFP() / x.toFP()); // TODO: not exact enough
    if (low > __m32) {
        var hi = Math.floor(low / (__m32+1)) | 0;
        return hi.next32(low % (__m32+1));
    }
    return low;
};

Number.prototype.and64 = function(x) {
    var low = this & x;
    if (this.hi && x.hi) {
        var hi = this.hi & x.hi;
        return hi.next32(low);
    };
    return low;
};

Number.prototype.shl64 = function(x) {
    if (x > 32) {
        var hi = (this << (x - 32)) & 0xFFFFFFFF;
        return hi.next32(0);
    } else {
        var hi = (this.high32() << x) & 0xFFFFFFFF;
        var low_reminder = this >> (32 - x);
        hi |= low_reminder;
        var low = this << x;
        return hi.next32(low);
    }
};

Number.prototype.shr64 = function(x) {
    if (x > 32) {
        var low = (this.high32() >> (x - 32)) & 0xFFFFFFFF;
        return low;
    } else {
        var low = (this >> x) & 0xFFFFFFFF;
        var hi_reminder = (this.high32() << (32 - x)) >> (32 - x);
        low |= hi_reminder;
        var hi = this.high32() >> x;
        return hi.next32(low);
    }
};

Number.prototype.compare64 = function(x) {
    if (this.hi == x.hi) {
        return (this == x) ? 0 : ((this < x) ? -1 : 1);
    }
    return (this.hi < x.hi) ? -1 : 1;
};
