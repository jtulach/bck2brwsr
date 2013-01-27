// empty line needed here
Number.prototype.add32 = function(x) { return (this + x) | 0; };
Number.prototype.sub32 = function(x) { return (this - x) | 0; };
Number.prototype.mul32 = function(x) { 
    return (((this * (x >> 16)) << 16) + this * (x & 0xFFFF)) | 0;
};

Number.prototype.toInt8 = function()  { return (this << 24) >> 24; };
Number.prototype.toInt16 = function() { return (this << 16) >> 16; };

Number.prototype.next32 = function(low) {
  if (this === 0) {
    return low;
  }
  var l = new Number(low);
  l.hi = this;
  return l;
};

Number.prototype.high32 = function() { 
    return this.hi ? this.hi : (Math.floor(this / 0xFFFFFFFF)) | 0;
};
Number.prototype.toInt32 = function() { return this | 0; };
Number.prototype.toFP = function() {
    return this.hi ? this.hi * 0xFFFFFFFF + this : this;
};
Number.prototype.toLong = function() {
    var hi = (this > 0xFFFFFFFF) ? (Math.floor(this / 0xFFFFFFFF)) | 0 : 0;
    return hi.next32(this % 0xFFFFFFFF);
}

Number.prototype.add64 = function(x) {
    var low = this + x;
    carry = 0;
    if (low > 0xFFFFFFFF) {
        carry = 1;
        low -= 0xFFFFFFFF;  // should not here be also -1?
    }
    var hi = (this.high32() + x.high32() + carry) & 0xFFFFFFFF;
    return hi.next32(low);
};

Number.prototype.div64 = function(x) {
    var low = Math.floor(this.toFP() / x.toFP()); // TODO: not exact enough
    if (low > 0xFFFFFFFF) {
        var hi = Math.floor(low / 0xFFFFFFFF) | 0;
        return hi.next32(low % 0xFFFFFFFF);
    }
    return low;
};

Number.prototype.shl64 = function(x) {
    if (x > 32) {
        var hi = (this << (x - 32)) & 0xFFFFFFFF;
        return hi.next32(0);
    } else {
        var hi = (this.high32() << x) & 0xFFFFFFFF;
        var low_reminder = this >> x;
        hi |= low_reminder;
        var low = this << x;
        return hi.next32(low);
    }
};

Number.prototype.compare64 = function(x) {
    if (this.hi == x.hi) {
        return (this == x) ? 0 : ((this < x) ? -1 : 1);
    }
    return (this.hi < x.hi) ? -1 : 1;
};
