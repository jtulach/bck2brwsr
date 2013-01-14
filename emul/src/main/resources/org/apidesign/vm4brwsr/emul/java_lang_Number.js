// empty line needed here
__add32 = function(x,y) { return (x + y) | 0; };
__sub32 = function(x,y) { return (x - y) | 0; };
__mul32 = function(x,y) { 
    return (((x * (y >> 16)) << 16) + x * (y & 0xFFFF)) | 0;
};

__toInt8 = function(x)  { return (x << 24) >> 24; };
__toInt16 = function(x) { return (x << 16) >> 16; };