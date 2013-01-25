// empty line needed here
Number.prototype.add32 = function(x) { return (this + x) | 0; };
Number.prototype.sub32 = function(x) { return (this - x) | 0; };
Number.prototype.mul32 = function(x) { 
    return (((this * (x >> 16)) << 16) + this * (x & 0xFFFF)) | 0;
};

Number.prototype.toInt8 = function()  { return (this << 24) >> 24; };
Number.prototype.toInt16 = function() { return (this << 16) >> 16; };

var Long = function(low, hi) {
    this.low = low;
    this.hi = hi;
};

function LongFromNumber(x) {
    return new Long(x % 0xFFFFFFFF, Math.floor(x / 0xFFFFFFFF));
};

function MakeLong(x) {
    if ((x.hi == undefined) && (x.low == undefined)) {
        return LongFromNumber(x);
    }
    return x;
};

Long.prototype.toInt32 = function() { return this.low | 0; };
Long.prototype.toFP = function() { return this.hi * 0xFFFFFFFF + this.low; };

Long.prototype.toString = function() {
    return String(this.toFP());
};

Long.prototype.valueOf = function() {
    return this.toFP();
};

Long.prototype.compare64 = function(x) {
    if (this.hi == x.hi) {
        return (this.low == x.low) ? 0 : ((this.low < x.low) ? -1 : 1);
    }
    return (this.hi < x.hi) ? -1 : 1;
};

Long.prototype.add64 = function(x) {
    low = this.low + x.low;
    carry = 0;
    if (low > 0xFFFFFFFF) {
        carry = 1;
        low -= 0xFFFFFFFF;
    }
    hi = (this.hi + x.hi + carry) & 0xFFFFFFFF;
    return new Long(low, hi);
};

Long.prototype.div64 = function(x) {
    return LongFromNumber(Math.floor(this.toFP() / x.toFP()));
};

Long.prototype.shl64 = function(x) {
    if (x > 32) {
        hi = (this.low << (x - 32)) & 0xFFFFFFFF;
        low = 0;
    } else {
        hi = (this.hi << x) & 0xFFFFFFFF;
        low_reminder = this.low >> x;
        hi |= low_reminder;
        low = this.low << x;
    }
    return new Long(low, hi);
};
