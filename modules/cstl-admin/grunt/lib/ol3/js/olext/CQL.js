/**
 * Script inspired by ol2 format CQL
 * This script is a legacy cql parser that read a string cql query and returns a structure json that represents a tree.
 * it is used for SLD editor when restoring cql form inputs.
 */
var olext = {};
olext.Class = function() {
    var len = arguments.length;
    var P = arguments[0];
    var F = arguments[len-1];

    var C = typeof F.initialize == "function" ?
        F.initialize :
        function(){ P.prototype.initialize.apply(this, arguments); };

    if (len > 1) {
        var newArgs = [C, P].concat(
            Array.prototype.slice.call(arguments).slice(1, len-1), F);
        olext.inherit.apply(null, newArgs);
    } else {
        C.prototype = F;
    }
    return C;
};

olext.inherit = function(C, P) {
    var F = function() {};
    F.prototype = P.prototype;
    C.prototype = new F;
    var i, l, o;
    for(i=2, l=arguments.length; i<l; i++) {
        o = arguments[i];
        if(typeof o === "function") {
            o = o.prototype;
        }
        olext.Util.extend(C.prototype, o);
    }
};

olext.Util = olext.Util || {};
olext.Util.extend = function(destination, source) {
    destination = destination || {};
    if (source) {
        for (var property in source) {
            var value = source[property];
            if (value !== undefined) {
                destination[property] = value;
            }
        }
        var sourceIsEvt = typeof window.Event == "function"
            && source instanceof window.Event;

        if (!sourceIsEvt
            && source.hasOwnProperty && source.hasOwnProperty("toString")) {
            destination.toString = source.toString;
        }
    }
    return destination;
};

/********************************/

olext.Format = olext.Class({
    options: null,
    externalProjection: null,
    internalProjection: null,
    data: null,
    keepData: false,
    initialize: function(options) {
     olext.Util.extend(this, options);
     this.options = options;
     },
    CLASS_NAME: "olext.Format"
});

/********************************/

olext.Filter = olext.Class({
    initialize: function(options) {
        olext.Util.extend(this, options);
    },
    CLASS_NAME: "olext.Filter"
});

/********************************/

olext.Filter.Comparison = olext.Class(olext.Filter, {
    type: null,
    property: null,
    value: null,
    matchCase: true,
    lowerBoundary: null,
    upperBoundary: null,
    CLASS_NAME: "olext.Filter.Comparison"
});
olext.Filter.Comparison.EQUAL_TO                 = "==";
olext.Filter.Comparison.NOT_EQUAL_TO             = "!=";
olext.Filter.Comparison.LESS_THAN                = "<";
olext.Filter.Comparison.GREATER_THAN             = ">";
olext.Filter.Comparison.LESS_THAN_OR_EQUAL_TO    = "<=";
olext.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO = ">=";
olext.Filter.Comparison.BETWEEN                  = "..";
olext.Filter.Comparison.LIKE                     = "~";
olext.Filter.Comparison.ILIKE                    = "ILIKE";
olext.Filter.Comparison.IS_NULL                  = "NULL";

/********************************/

olext.Filter.Logical = olext.Class(olext.Filter, {
    filters: null,
    type: null,
    CLASS_NAME: "olext.Filter.Logical"
});
olext.Filter.Logical.AND = "&&";
olext.Filter.Logical.OR  = "||";
olext.Filter.Logical.NOT = "!";

/********************************/

olext.Format.CQL = (function() {

    var tokens = [
            "PROPERTY", "COMPARISON", "VALUE", "LOGICAL"
        ],

        patterns = {
            PROPERTY: /^[_a-zA-Z]\w*/,
            COMPARISON: /^(=|<>|<=|<|>=|>|LIKE|ILIKE)/i,
            IS_NULL: /^IS NULL/i,
            COMMA: /^,/,
            LOGICAL: /^(AND|OR)/i,
            VALUE: /^('([^']|'')*'|\d+(\.\d*)?|\.\d+)/,
            LPAREN: /^\(/,
            RPAREN: /^\)/,
            SPATIAL: /^(BBOX|INTERSECTS|DWITHIN|WITHIN|CONTAINS)/i,
            NOT: /^NOT/i,
            BETWEEN: /^BETWEEN/i,
            GEOMETRY: function(text) {
                var type = /^(POINT|LINESTRING|POLYGON|MULTIPOINT|MULTILINESTRING|MULTIPOLYGON|GEOMETRYCOLLECTION)/.exec(text);
                if (type) {
                    var len = text.length;
                    var idx = text.indexOf("(", type[0].length);
                    if (idx > -1) {
                        var depth = 1;
                        while (idx < len && depth > 0) {
                            idx++;
                            switch(text.charAt(idx)) {
                                case '(':
                                    depth++;
                                    break;
                                case ')':
                                    depth--;
                                    break;
                                default:
                                // in default case, do nothing
                            }
                        }
                    }
                    return [text.substr(0, idx+1)];
                }
            },
            END: /^$/
        },

        follows = {
            LPAREN: ['GEOMETRY', 'SPATIAL', 'PROPERTY', 'VALUE', 'LPAREN'],
            RPAREN: ['NOT', 'LOGICAL', 'END', 'RPAREN'],
            PROPERTY: ['COMPARISON', 'BETWEEN', 'COMMA', 'IS_NULL'],
            BETWEEN: ['VALUE'],
            IS_NULL: ['END'],
            COMPARISON: ['VALUE'],
            COMMA: ['GEOMETRY', 'VALUE', 'PROPERTY'],
            VALUE: ['LOGICAL', 'COMMA', 'RPAREN', 'END'],
            SPATIAL: ['LPAREN'],
            LOGICAL: ['NOT', 'VALUE', 'SPATIAL', 'PROPERTY', 'LPAREN'],
            NOT: ['PROPERTY', 'LPAREN'],
            GEOMETRY: ['COMMA', 'RPAREN']
        },

        operators = {
            '=': olext.Filter.Comparison.EQUAL_TO,
            '<>': olext.Filter.Comparison.NOT_EQUAL_TO,
            '<': olext.Filter.Comparison.LESS_THAN,
            '<=': olext.Filter.Comparison.LESS_THAN_OR_EQUAL_TO,
            '>': olext.Filter.Comparison.GREATER_THAN,
            '>=': olext.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
            'LIKE': olext.Filter.Comparison.LIKE,
            'ILIKE': olext.Filter.Comparison.ILIKE,
            'BETWEEN': olext.Filter.Comparison.BETWEEN,
            'IS NULL': olext.Filter.Comparison.IS_NULL
        },

        operatorReverse = {},

        logicals = {
            'AND': olext.Filter.Logical.AND,
            'OR': olext.Filter.Logical.OR
        },

        logicalReverse = {},

        precedence = {
            'RPAREN': 3,
            'LOGICAL': 2,
            'COMPARISON': 1
        };

    var i;
    for (i in operators) {
        if (operators.hasOwnProperty(i)) {
            operatorReverse[operators[i]] = i;
        }
    }

    for (i in logicals) {
        if (logicals.hasOwnProperty(i)) {
            logicalReverse[logicals[i]] = i;
        }
    }

    function tryToken(text, pattern) {
        if (pattern instanceof RegExp) {
            return pattern.exec(text);
        } else {
            return pattern(text);
        }
    }

    function nextToken(text, tokens) {
        var i, token, len = tokens.length;
        for (i=0; i<len; i++) {
            token = tokens[i];
            var pat = patterns[token];
            var matches = tryToken(text, pat);
            if (matches) {
                var match = matches[0];
                var remainder = text.substr(match.length).replace(/^\s*/, "");
                return {
                    type: token,
                    text: match,
                    remainder: remainder
                };
            }
        }

        var msg = "ERROR: In parsing: [" + text + "], expected one of: ";
        for (i=0; i<len; i++) {
            token = tokens[i];
            msg += "\n    " + token + ": " + patterns[token];
        }

        throw new Error(msg);
    }

    function tokenize(text) {
        var results = [];
        var token, expect = ["NOT", "GEOMETRY", "SPATIAL", "PROPERTY", "LPAREN"];

        do {
            token = nextToken(text, expect);
            text = token.remainder;
            expect = follows[token.type];
            if (token.type != "END" && !expect) {
                throw new Error("No follows list for " + token.type);
            }
            results.push(token);
        } while (token.type != "END");

        return results;
    }

    function buildAst(tokens) {
        var operatorStack = [],
            postfix = [];

        while (tokens.length) {
            var tok = tokens.shift();
            switch (tok.type) {
                case "PROPERTY":
                case "GEOMETRY":
                case "VALUE":
                    postfix.push(tok);
                    break;
                case "COMPARISON":
                case "BETWEEN":
                case "IS_NULL":
                case "LOGICAL":
                    var p = precedence[tok.type];

                    while (operatorStack.length > 0 &&
                        (precedence[operatorStack[operatorStack.length - 1].type] <= p)
                        ) {
                        postfix.push(operatorStack.pop());
                    }

                    operatorStack.push(tok);
                    break;
                case "SPATIAL":
                case "NOT":
                case "LPAREN":
                    operatorStack.push(tok);
                    break;
                case "RPAREN":
                    while (operatorStack.length > 0 &&
                        (operatorStack[operatorStack.length - 1].type != "LPAREN")
                        ) {
                        postfix.push(operatorStack.pop());
                    }
                    operatorStack.pop(); // toss out the LPAREN

                    if (operatorStack.length > 0 &&
                        operatorStack[operatorStack.length-1].type == "SPATIAL") {
                        postfix.push(operatorStack.pop());
                    }
                case "COMMA":
                case "END":
                    break;
                default:
                    throw new Error("Unknown token type " + tok.type);
            }
        }

        while (operatorStack.length > 0) {
            postfix.push(operatorStack.pop());
        }

        function buildTree() {
            var tok = postfix.pop();
            switch (tok.type) {
                case "LOGICAL":
                    var rhs = buildTree(),
                        lhs = buildTree();
                    return new olext.Filter.Logical({
                        filters: [lhs, rhs],
                        type: logicals[tok.text.toUpperCase()]
                    });
                case "NOT":
                    var operand = buildTree();
                    return new olext.Filter.Logical({
                        filters: [operand],
                        type: olext.Filter.Logical.NOT
                    });
                case "BETWEEN":
                    var min, max, property;
                    postfix.pop(); // unneeded AND token here
                    max = buildTree();
                    min = buildTree();
                    property = buildTree();
                    return new olext.Filter.Comparison({
                        property: property,
                        lowerBoundary: min,
                        upperBoundary: max,
                        type: olext.Filter.Comparison.BETWEEN
                    });
                case "COMPARISON":
                    var value = buildTree(),
                        property = buildTree();
                    return new olext.Filter.Comparison({
                        property: property,
                        value: value,
                        type: operators[tok.text.toUpperCase()]
                    });
                case "IS_NULL":
                    var property = buildTree();
                    return new olext.Filter.Comparison({
                        property: property,
                        type: operators[tok.text.toUpperCase()]
                    });
                case "VALUE":
                    var match = tok.text.match(/^'(.*)'$/);
                    if (match) {
                        return match[1].replace(/''/g, "'");
                    } else {
                        return Number(tok.text);
                    }
                default:
                    return tok.text;
            }
        }

        var result = buildTree();
        if (postfix.length > 0) {
            var msg = "Remaining tokens after building AST: \n";
            for (var i = postfix.length - 1; i >= 0; i--) {
                msg += postfix[i].type + ": " + postfix[i].text + "\n";
            }
            throw new Error(msg);
        }

        return result;
    }

    return olext.Class(olext.Format, {
        read: function(text) {
            var result = buildAst(tokenize(text));
            if (this.keepData) {
                this.data = result;
            }
            return result;
        },
        CLASS_NAME: "olext.Format.CQL"

    });
})();

