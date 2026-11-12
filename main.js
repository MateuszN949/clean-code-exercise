const fs = require("node:fs");

class Everything {
    send(x) { throw new Error("Not implemented"); }
    save(x) { throw new Error("Not implemented"); }
    refund(x) { throw new Error("Not implemented"); }
}
class Thing extends Everything {
    send(x) { console.log("EMAIL " + x); }
    save(x) { fs.appendFileSync("orders.txt", x + "\n", "utf8"); }
    refund(x) { throw new Error("Not supported"); }
}
let mode = "production";
let counter = 0;
let total = 0;
const cache = new Map();
const futureCurrencies = ["EUR", "USD", "BTC", "POINTS"];

function doIt(s, action, verbose, send, save, unused) {
    try {
        if (action === 2) {
            let x = "REPORT|" + counter + "|" + total.toFixed(2);
            if (verbose) console.log(x);
            return x;
        }
        if (action !== 1) return "UNKNOWN";
        let a = s.split("|");
        let n = a[0], e = a[1], t = a[2];
        let b = a[3].split(",");
        let z = 0;
        let detail = "";
        for (let q of b) {
            let c = q.split(":");
            let p;
            if (c[0] === "BOOK") p = 40;
            else if (c[0] === "PEN") p = 5;
            else if (c[0] === "BAG") p = 80;
            else p = 0;
            if (!/^[+-]?\d+$/.test(c[1])) throw new Error("Bad quantity");
            z += p * Number(c[1]);
            detail += c[0] + "x" + c[1] + ";";
        }
        if (t === "VIP") z *= 0.9;
        else if (t === "STUDENT") z *= 0.95;
        if (a[4] === "SAVE10") z *= 0.9;
        if (z < 100) z += 12;
        z *= 1.23;
        let x = "ORDER|" + n + "|" + detail + "|" + z.toFixed(2);
        counter++;
        total += z;
        cache.set(e, x);
        let obj = new Thing();
        if (save) obj.save(x);
        if (send) obj.send(e + "|" + x);
        if (verbose) console.log(x);
        return x;
    } catch (ex) { return "ERROR"; }
}
if (require.main === module) {
    doIt("Alice|alice@example.test|VIP|BOOK:2,PEN:3|SAVE10", 1, true, true, true, 999);
    doIt("Bob|bob@example.test|REGULAR|BAG:2|NONE", 1, true, true, true, 999);
    doIt("", 2, true, false, false, 999);
}
module.exports = { doIt };
