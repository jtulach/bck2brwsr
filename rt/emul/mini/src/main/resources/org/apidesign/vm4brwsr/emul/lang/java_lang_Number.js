// empty line needed here
Number.prototype.add32 = function(x) { return (this + x) | 0; };
Number.prototype.sub32 = function(x) { return (this - x) | 0; };
Number.prototype.mul32 = function(x) { 
    return (((this * (x >> 16)) << 16) + this * (x & 0xFFFF)) | 0;
};
Number.prototype.neg32 = function() { return (-this) | 0; };

Number.prototype.toInt8 = function()  { return (this << 24) >> 24; };
Number.prototype.toInt16 = function() { return (this << 16) >> 16; };

var __m32 = 0xFFFFFFFF;

Number.prototype.next32 = function(low) {
  if (this === 0) {
    return low;
  }
  var l = new Number(low);
  l.hi = this | 0;
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
    var hi = (this / (__m32+1)) | 0;
    var low = (this % (__m32+1)) | 0;
    if (low < 0) {
        low += __m32+1;
    }
        
    if (this < 0) {
        hi -= 1;
    }

    return hi.next32(low);
};

Number.prototype.toExactString = function() {
    if (this.hi) {
        // check for Long.MIN_VALUE
        if ((this.hi == (0x80000000 | 0)) && (this == 0)) {
            return '-9223372036854775808';
        }
        var res = 0;
        var a = [ 6,9,2,7,6,9,4,9,2,4 ];
        var s = '';
        var digit;
        var neg = this.hi < 0;
        if (neg) {
            var x = this.neg64();
            var hi = x.hi;
            var low = x;
        } else {
            var hi = this.hi;
            var low = this;
        }
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
        s = String(res).concat(s).replace(/^0+/, '');
        return (neg ? '-' : '').concat(s);
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

Number.prototype.sub64 = function(x) {
    var low = this - x;
    carry = 0;
    if (low < 0) {
        carry = 1;
        low += (__m32+1);
    }
    var hi = (this.high32() - x.high32() - carry) | 0;
    return hi.next32(low);
};

Number.prototype.mul64 = function(x) {
    var low = this.mul32(x);
    low += (low < 0) ? (__m32+1) : 0;
    // first count upper 32 bits of (this.low * x.low)
    var hi_hi = 0;
    var hi_low = 0;
    var m = 1;
    for (var i = 0; i < 32; i++) {
        if (x & m) {
            hi_hi += this >>> 16;
            hi_low += this & 0xFFFF
        }
        hi_low >>= 1;
        hi_low += (hi_hi & 1) ? 0x8000 : 0;
        hi_hi >>= 1;
        m <<= 1;
    }
    var hi = (hi_hi << 16) + hi_low;
    
    var m1 = this.high32().mul32(x);
    var m2 = this.mul32(x.high32());
    hi = hi.add32(m1).add32(m2);
    
    return hi.next32(low);
};

Number.prototype.and64 = function(x) {
    var low = this & x;
    low += (low < 0) ? (__m32+1) : 0;
    if (this.hi && x.hi) {
        var hi = this.hi & x.hi;
        return hi.next32(low);
    };
    return low;
};

Number.prototype.or64 = function(x) {
    var low = this | x;
    low += (low < 0) ? (__m32+1) : 0;
    if (this.hi || x.hi) {
        var hi = this.hi | x.hi;
        return hi.next32(low);
    };
    return low;
};

Number.prototype.xor64 = function(x) {
    var low = this ^ x;
    low += (low < 0) ? (__m32+1) : 0;
    if (this.hi || x.hi) {
        var hi = this.hi ^ x.hi;
        return hi.next32(low);
    };
    return low;
};

Number.prototype.shl64 = function(x) {
    if (x >= 32) {
        var hi = this << (x - 32);
        return hi.next32(0);
    } else {
        var hi = this.high32() << x;
        var low_reminder = this >> (32 - x);
        hi |= low_reminder;
        var low = this << x;
        low += (low < 0) ? (__m32+1) : 0;
        return hi.next32(low);
    }
};

Number.prototype.shr64 = function(x) {
    if (x >= 32) {
        var low = this.high32() >> (x - 32);
        low += (low < 0) ? (__m32+1) : 0;
        return low;
    } else {
        var low = this >> x;
        var hi_reminder = this.high32() << (32 - x);
        low |= hi_reminder;
        low += (low < 0) ? (__m32+1) : 0;
        var hi = this.high32() >> x;
        return hi.next32(low);
    }
};

Number.prototype.ushr64 = function(x) {
    if (x >= 32) {
        var low = this.high32() >>> (x - 32);
        low += (low < 0) ? (__m32+1) : 0;
        return low;
    } else {
        var low = this >>> x;
        var hi_reminder = this.high32() << (32 - x);
        low |= hi_reminder;
        low += (low < 0) ? (__m32+1) : 0;
        var hi = this.high32() >>> x;
        return hi.next32(low);
    }
};

Number.prototype.compare64 = function(x) {
    if (this.high32() === x.high32()) {
        return (this < x) ? -1 : ((this > x) ? 1 : 0);
    }
    return (this.high32() < x.high32()) ? -1 : 1;
};

Number.prototype.neg64 = function() {
    var hi = this.high32();
    var low = this;
    if ((hi === 0) && (low < 0)) { return -low; }
    hi = ~hi;
    low = ~low;
    low += (low < 0) ? (__m32+1) : 0;
    var ret = hi.next32(low);
    return ret.add64(1);
};

(function(numberPrototype) {
    function __handleDivByZero() {
        var exception = new vm.java_lang_ArithmeticException;
        vm.java_lang_ArithmeticException(false).constructor
          .cons__VLjava_lang_String_2.call(exception, "/ by zero");

        throw exception;
    }

    function __Int64(hi32, lo32) {
        this.hi32 = hi32 | 0;
        this.lo32 = lo32 | 0;

        this.get32 = function(bitIndex) {
            var v0;
            var v1;
            bitIndex += 32;
            var selector = bitIndex >>> 5;
            switch (selector) {
                case 0:
                    v0 = 0;
                    v1 = this.lo32;
                    break;
                case 1:
                    v0 = this.lo32;
                    v1 = this.hi32;
                    break;
                case 2:
                    v0 = this.hi32;
                    v1 = 0;
                    break
                default:
                    return 0;
            }

            var shift = bitIndex & 31;
            if (shift === 0) {
                return v0;
            }

            return (v1 << (32 - shift)) | (v0 >>> shift);
        }

        this.get16 = function(bitIndex) {
            return this.get32(bitIndex) & 0xffff;
        }

        this.set16 = function(bitIndex, value) {
            bitIndex += 32;
            var shift = bitIndex & 15;
            var svalue = (value & 0xffff) << shift; 
            var smask = 0xffff << shift;
            var selector = bitIndex >>> 4;
            switch (selector) {
                case 0:
                    break;
                case 1:
                    this.lo32 = (this.lo32 & ~(smask >>> 16))
                                    | (svalue >>> 16);
                    break;
                case 2:
                    this.lo32 = (this.lo32 & ~smask) | svalue;
                    break;
                case 3:
                    this.lo32 = (this.lo32 & ~(smask << 16))
                                    | (svalue << 16);
                    this.hi32 = (this.hi32 & ~(smask >>> 16))
                                    | (svalue >>> 16);
                    break;
                case 4:
                    this.hi32 = (this.hi32 & ~smask) | svalue;
                    break;
                case 5:
                    this.hi32 = (this.hi32 & ~(smask << 16))
                                    | (svalue << 16);
                    break;
            }
        }

        this.getDigit = function(index, shift) {
            return this.get16((index << 4) - shift);
        }

        this.getTwoDigits = function(index, shift) {
            return this.get32(((index - 1) << 4) - shift);
        }

        this.setDigit = function(index, shift, value) {
            this.set16((index << 4) - shift, value);
        }

        this.countSignificantDigits = function() {
            var sd;
            var remaining;

            if (this.hi32 === 0) {
                if (this.lo32 === 0) {
                    return 0;
                }

                sd = 2;
                remaining = this.lo32;
            } else {
                sd = 4;
                remaining = this.hi32;
            }

            if (remaining < 0) {
                return sd;
            }

            return (remaining < 65536) ? sd - 1 : sd;
        }
        
        this.toNumber = function() {
            var lo32 = this.lo32;
            if (lo32 < 0) {
                lo32 += 0x100000000;
            }

            return this.hi32.next32(lo32);
        }
    }

    function __countLeadingZeroes16(number) {
        var nlz = 0;

        if (number < 256) {
            nlz += 8;
            number <<= 8;
        }

        if (number < 4096) {
            nlz += 4;
            number <<= 4;
        }

        if (number < 16384) {
            nlz += 2;
            number <<= 2;
        }

        return (number < 32768) ? nlz + 1 : nlz;
    }
    
    // q = u / v; r = u - q * v;
    // v != 0
    function __div64(q, r, u, v) {
        var m = u.countSignificantDigits();
        var n = v.countSignificantDigits();

        q.hi32 = q.lo32 = 0;

        if (n === 1) {
            // v has single digit
            var vd = v.getDigit(0, 0);
            var carry = 0;
            for (var i = m - 1; i >= 0; --i) {
                var ui = (carry << 16) | u.getDigit(i, 0);
                if (ui < 0) {
                    ui += 0x100000000;
                }
                var qi = (ui / vd) | 0;
                q.setDigit(i, 0, qi);
                carry = ui - qi * vd;
            }

            r.hi32 = 0;
            r.lo32 = carry;
            return;
        }

        r.hi32 = u.hi32;  
        r.lo32 = u.lo32;

        if (m < n) {
            return;
        }

        // Normalize
        var nrm = __countLeadingZeroes16(v.getDigit(n - 1, 0));

        var vd1 = v.getDigit(n - 1, nrm);                
        var vd0 = v.getDigit(n - 2, nrm);
        for (var j = m - n; j >= 0; --j) {
            // Calculate qj estimate
            var ud21 = r.getTwoDigits(j + n, nrm);
            var ud2 = ud21 >>> 16;
            if (ud21 < 0) {
                ud21 += 0x100000000;
            }

            var qest = (ud2 === vd1) ? 0xFFFF : ((ud21 / vd1) | 0);
            var rest = ud21 - qest * vd1;

            // 0 <= (qest - qj) <= 2

            // Refine qj estimate
            var ud0 = r.getDigit(j + n - 2, nrm);
            while ((qest * vd0) > ((rest * 0x10000) + ud0)) {
                --qest;
                rest += vd1;
            }

            // 0 <= (qest - qj) <= 1
            
            // Multiply and subtract
            var carry = 0;
            for (var i = 0; i < n; ++i) {
                var vi = qest * v.getDigit(i, nrm);
                var ui = r.getDigit(i + j, nrm) - carry - (vi & 0xffff);
                r.setDigit(i + j, nrm, ui);
                carry = (vi >>> 16) - (ui >> 16);
            }
            var uj = ud2 - carry;

            if (uj < 0) {
                // qest - qj = 1

                // Add back
                --qest;
                var carry = 0;
                for (var i = 0; i < n; ++i) {
                    var ui = r.getDigit(i + j, nrm) + v.getDigit(i, nrm)
                                 + carry;
                    r.setDigit(i + j, nrm, ui);
                    carry = ui >> 16;
                }
                uj += carry;
            }

            q.setDigit(j, 0, qest);
            r.setDigit(j + n, nrm, uj);
        }
    }

    numberPrototype.div32 = function(x) {
        if (x === 0) {
            __handleDivByZero();
        }

        return (this / x) | 0;
    }

    numberPrototype.mod32 = function(x) {
        if (x === 0) {
            __handleDivByZero();
        }

        return (this % x);
    }

    numberPrototype.div64 = function(x) {
        var negateResult = false;
        var u, v;

        if ((this.high32() & 0x80000000) != 0) {
            u = this.neg64();
            negateResult = !negateResult;
        } else {
            u = this;        
        }

        if ((x.high32() & 0x80000000) != 0) {
            v = x.neg64();
            negateResult = !negateResult;
        } else {
            v = x;
        }

        if ((v == 0) && (v.high32() === 0)) {
            __handleDivByZero();
        }

        if (u.high32() === 0) {
            if (v.high32() === 0) {
                var result = (u / v) | 0;
                return negateResult ? result.neg64() : result; 
            }

            return 0;
        }

        var u64 = new __Int64(u.high32(), u);
        var v64 = new __Int64(v.high32(), v);
        var q64 = new __Int64(0, 0);
        var r64 = new __Int64(0, 0);

        __div64(q64, r64, u64, v64);

        var result = q64.toNumber();
        return negateResult ? result.neg64() : result; 
    }

    numberPrototype.mod64 = function(x) {
        var negateResult = false;
        var u, v;
        
        if ((this.high32() & 0x80000000) != 0) {
            u = this.neg64();
            negateResult = !negateResult;
        } else {
            u = this;        
        }

        if ((x.high32() & 0x80000000) != 0) {
            v = x.neg64();
        } else {
            v = x;
        }

        if ((v == 0) && (v.high32() === 0)) {
            __handleDivByZero();
        }

        if (u.high32() === 0) {
            var result = (v.high32() === 0) ? (u % v) : u;
            return negateResult ? result.neg64() : result; 
        }

        var u64 = new __Int64(u.high32(), u);
        var v64 = new __Int64(v.high32(), v);
        var q64 = new __Int64(0, 0);
        var r64 = new __Int64(0, 0);

        __div64(q64, r64, u64, v64);

        var result = r64.toNumber();
        return negateResult ? result.neg64() : result; 
    }
})(Number.prototype);

vm.java_lang_Number(false);
