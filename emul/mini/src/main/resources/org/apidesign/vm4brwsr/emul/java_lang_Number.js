// empty line needed here
Number.prototype.add32 = function(x) { return (this + x) | 0; };
Number.prototype.sub32 = function(x) { return (this - x) | 0; };
Number.prototype.mul32 = function(x) { 
    return (((this * (x >> 16)) << 16) + this * (x & 0xFFFF)) | 0;
};

Number.prototype.toInt8 = function()  { return (this << 24) >> 24; };
Number.prototype.toInt16 = function() { return (this << 16) >> 16; };