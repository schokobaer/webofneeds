SystemJS.config({
  paths: {
    "github:": "jspm_packages/github/",
    "npm:": "jspm_packages/npm/",
    "bower:": "jspm_packages/bower/",
  },
  devConfig: {
    "map": {
      "babel-runtime": "npm:babel-runtime@5.8.35",
      "core-js": "npm:core-js@1.2.6",
      "plugin-babel": "npm:systemjs-plugin-babel@0.0.25"
    },
    "packages": {
      "npm:babel-runtime@5.8.35": {
        "map": {}
      },
      "npm:core-js@1.2.6": {
        "map": {
          "systemjs-json": "github:systemjs/plugin-json@0.1.2"
        }
      }
    }
  },
  transpiler: "plugin-babel",
  babelOptions: {
    "optional": [
      "runtime"
    ]
  },
  meta: {
    "rdfstore-js": {
      "format": "global",
      "exports": "rdfstore"
    }
  },
  map: {
    "angular-ui-router-shim": "npm:angular-ui-router@1.0.3/release/stateEvents.js",
    "babel": "npm:babel-core@5.8.35",
    "rdfstore-js": "scripts/rdfstore-js/rdf_store.js"
  },
});

SystemJS.config({
  defaultJSExtensions: true,
  packageConfigPaths: [
    "npm:@*/*.json",
    "npm:*.json",
    "bower:*.json",
    "github:*/*.json"
  ],
  map: {
    "@uirouter/angularjs": "npm:@uirouter/angularjs@1.0.6",
    "angular": "npm:angular@1.6.6",
    "angular-inview": "npm:angular-inview@2.1.0",
    "angular-route": "npm:angular-route@1.6.6",
    "angular-sanitize": "npm:angular-sanitize@1.6.6",
    "angular-ui-router": "npm:angular-ui-router@1.0.3",
    "assert": "npm:jspm-nodelibs-assert@0.2.1",
    "bcrypt-pbkdf": "npm:bcrypt-pbkdf@1.0.1",
    "buffer": "npm:jspm-nodelibs-buffer@0.2.3",
    "child_process": "npm:jspm-nodelibs-child_process@0.2.1",
    "constants": "npm:jspm-nodelibs-constants@0.2.1",
    "crypto": "npm:jspm-nodelibs-crypto@0.2.1",
    "dgram": "npm:jspm-nodelibs-dgram@0.2.1",
    "dns": "npm:jspm-nodelibs-dns@0.2.1",
    "ecc-jsbn": "npm:ecc-jsbn@0.1.1",
    "events": "npm:jspm-nodelibs-events@0.2.2",
    "fetch": "github:github/fetch@0.10.1",
    "fs": "npm:jspm-nodelibs-fs@0.2.1",
    "http": "npm:jspm-nodelibs-http@0.2.0",
    "https": "npm:jspm-nodelibs-https@0.2.2",
    "immutable": "npm:immutable@3.7.5",
    "jsbn": "npm:jsbn@0.1.1",
    "jsonld": "npm:jsonld@0.4.12",
    "leaflet": "npm:leaflet@0.7.7",
    "net": "npm:jspm-nodelibs-net@0.2.1",
    "ng-redux": "npm:ng-redux@3.4.1",
    "os": "npm:jspm-nodelibs-os@0.2.2",
    "path": "npm:jspm-nodelibs-path@0.2.3",
    "process": "npm:jspm-nodelibs-process@0.2.1",
    "querystring": "npm:jspm-nodelibs-querystring@0.2.2",
    "reduce-reducers": "npm:reduce-reducers@0.1.1",
    "redux": "npm:redux@3.7.2",
    "redux-immutablejs": "npm:redux-immutablejs@0.0.8",
    "redux-thunk": "npm:redux-thunk@1.0.0",
    "redux-ui-router": "npm:redux-ui-router@0.7.2",
    "reselect": "npm:reselect@2.0.2",
    "sockjs": "bower:sockjs@0.3.4",
    "stream": "npm:jspm-nodelibs-stream@0.2.1",
    "string_decoder": "npm:jspm-nodelibs-string_decoder@0.2.1",
    "tls": "npm:jspm-nodelibs-tls@0.2.1",
    "tty": "npm:jspm-nodelibs-tty@0.2.1",
    "tweetnacl": "npm:tweetnacl@0.14.5",
    "url": "npm:jspm-nodelibs-url@0.2.1",
    "util": "npm:jspm-nodelibs-util@0.2.2",
    "vm": "npm:jspm-nodelibs-vm@0.2.1",
    "zlib": "npm:jspm-nodelibs-zlib@0.2.3"
  },
  packages: {
    "npm:@uirouter/angularjs@1.0.6": {
      "map": {
        "@uirouter/core": "npm:@uirouter/core@5.0.6",
        "angular": "npm:angular@1.6.6",
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:@uirouter/core@5.0.3": {
      "map": {
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:@uirouter/core@5.0.6": {
      "map": {
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:angular-inview@2.1.0": {
      "map": {
        "angular": "npm:angular@1.6.6"
      }
    },
    "npm:angular-ui-router@1.0.3": {
      "map": {
        "@uirouter/core": "npm:@uirouter/core@5.0.3",
        "angular": "npm:angular@1.6.6",
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:buffer@5.0.7": {
      "map": {
        "base64-js": "npm:base64-js@1.2.1",
        "ieee754": "npm:ieee754@1.1.8"
      }
    },
    "npm:core-util-is@1.0.2": {
      "map": {}
    },
    "npm:es6-promise@2.0.1": {
      "map": {
        "systemjs-json": "github:systemjs/plugin-json@0.1.0"
      }
    },
    "npm:immutable@3.7.5": {
      "map": {
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:inherits@2.0.1": {
      "map": {}
    },
    "npm:invariant@2.2.2": {
      "map": {
        "loose-envify": "npm:loose-envify@1.3.1"
      }
    },
    "npm:isarray@1.0.0": {
      "map": {
        "systemjs-json": "github:systemjs/plugin-json@0.1.0"
      }
    },
    "npm:jsonld@0.4.12": {
      "map": {
        "es6-promise": "npm:es6-promise@2.0.1",
        "xmldom": "npm:xmldom@0.1.19",
        "node-xmldom": "npm:xmldom@0.1.19",
        "pkginfo": "npm:pkginfo@0.4.1",
        "node-pkginfo": "npm:pkginfo@0.4.1",
        "request": "npm:request@2.81.0",
        "node-request": "npm:request@2.81.0"
      }
    },
    "npm:lodash._baseassign@3.2.0": {
      "map": {
        "lodash._basecopy": "npm:lodash._basecopy@3.0.1",
        "lodash.keys": "npm:lodash.keys@3.1.2"
      }
    },
    "npm:lodash._createassigner@3.1.1": {
      "map": {
        "lodash._bindcallback": "npm:lodash._bindcallback@3.0.1",
        "lodash._isiterateecall": "npm:lodash._isiterateecall@3.0.9",
        "lodash.restparam": "npm:lodash.restparam@3.6.1"
      }
    },
    "npm:lodash.assign@3.2.0": {
      "map": {
        "lodash._baseassign": "npm:lodash._baseassign@3.2.0",
        "lodash._createassigner": "npm:lodash._createassigner@3.1.1",
        "lodash.keys": "npm:lodash.keys@3.1.2"
      }
    },
    "npm:lodash.curry@4.1.1": {
      "map": {}
    },
    "npm:lodash.isplainobject@3.2.0": {
      "map": {
        "lodash._basefor": "npm:lodash._basefor@3.0.3",
        "lodash.isarguments": "npm:lodash.isarguments@3.1.0",
        "lodash.keysin": "npm:lodash.keysin@3.0.8"
      }
    },
    "npm:lodash.keys@3.1.2": {
      "map": {
        "lodash._getnative": "npm:lodash._getnative@3.9.1",
        "lodash.isarguments": "npm:lodash.isarguments@3.1.0",
        "lodash.isarray": "npm:lodash.isarray@3.0.4"
      }
    },
    "npm:lodash.keysin@3.0.8": {
      "map": {
        "lodash.isarguments": "npm:lodash.isarguments@3.1.0",
        "lodash.isarray": "npm:lodash.isarray@3.0.4"
      }
    },
    "npm:lodash.map@4.6.0": {
      "map": {}
    },
    "npm:loose-envify@1.3.1": {
      "map": {
        "js-tokens": "npm:js-tokens@3.0.2",
        "systemjs-json": "github:systemjs/plugin-json@0.1.2"
      }
    },
    "npm:ng-redux@3.4.1": {
      "map": {
        "invariant": "npm:invariant@2.2.2",
        "lodash.assign": "npm:lodash.assign@3.2.0",
        "lodash.curry": "npm:lodash.curry@4.1.1",
        "lodash.isarray": "npm:lodash.isarray@3.0.4",
        "lodash.isfunction": "npm:lodash.isfunction@3.0.8",
        "lodash.isobject": "npm:lodash.isobject@3.0.2",
        "lodash.isplainobject": "npm:lodash.isplainobject@3.2.0",
        "lodash.map": "npm:lodash.map@4.6.0",
        "redux": "npm:redux@3.7.2"
      }
    },
    "npm:redux-immutablejs@0.0.8": {
      "map": {
        "immutable": "npm:immutable@3.7.5",
        "redux": "npm:redux@3.7.2"
      }
    },
    "npm:redux-ui-router@0.7.2": {
      "map": {
        "@uirouter/angularjs": "npm:@uirouter/angularjs@1.0.6",
        "angular": "npm:angular@1.6.6",
        "redux": "npm:redux@3.7.2"
      }
    },
    "npm:redux@3.7.2": {
      "map": {
        "lodash": "npm:lodash@4.17.4",
        "lodash-es": "npm:lodash-es@4.17.4",
        "loose-envify": "npm:loose-envify@1.3.1",
        "symbol-observable": "npm:symbol-observable@1.0.4"
      }
    },
    "npm:string_decoder@0.10.31": {
      "map": {}
    },
    "npm:request@2.81.0": {
      "map": {
        "extend": "npm:extend@3.0.1",
        "forever-agent": "npm:forever-agent@0.6.1",
        "aws-sign2": "npm:aws-sign2@0.6.0",
        "har-validator": "npm:har-validator@4.2.1",
        "aws4": "npm:aws4@1.6.0",
        "caseless": "npm:caseless@0.12.0",
        "combined-stream": "npm:combined-stream@1.0.5",
        "qs": "npm:qs@6.4.0",
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "performance-now": "npm:performance-now@0.2.0",
        "stringstream": "npm:stringstream@0.0.5",
        "tunnel-agent": "npm:tunnel-agent@0.6.0",
        "isstream": "npm:isstream@0.1.2",
        "is-typedarray": "npm:is-typedarray@1.0.0",
        "oauth-sign": "npm:oauth-sign@0.8.2",
        "uuid": "npm:uuid@3.1.0",
        "http-signature": "npm:http-signature@1.1.1",
        "form-data": "npm:form-data@2.1.4",
        "hawk": "npm:hawk@3.1.3",
        "json-stringify-safe": "npm:json-stringify-safe@5.0.1",
        "tough-cookie": "npm:tough-cookie@2.3.2",
        "mime-types": "npm:mime-types@2.1.17"
      }
    },
    "npm:jspm-nodelibs-crypto@0.2.1": {
      "map": {
        "crypto-browserify": "npm:crypto-browserify@3.11.1"
      }
    },
    "npm:jspm-nodelibs-http@0.2.0": {
      "map": {
        "http-browserify": "npm:stream-http@2.7.2"
      }
    },
    "npm:tunnel-agent@0.6.0": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1"
      }
    },
    "npm:stream-http@2.7.2": {
      "map": {
        "inherits": "npm:inherits@2.0.1",
        "to-arraybuffer": "npm:to-arraybuffer@1.0.1",
        "xtend": "npm:xtend@4.0.1",
        "builtin-status-codes": "npm:builtin-status-codes@3.0.0",
        "readable-stream": "npm:readable-stream@2.3.3"
      }
    },
    "npm:crypto-browserify@3.11.1": {
      "map": {
        "inherits": "npm:inherits@2.0.1",
        "create-hmac": "npm:create-hmac@1.1.6",
        "create-ecdh": "npm:create-ecdh@4.0.0",
        "browserify-cipher": "npm:browserify-cipher@1.0.0",
        "diffie-hellman": "npm:diffie-hellman@5.0.2",
        "pbkdf2": "npm:pbkdf2@3.0.14",
        "browserify-sign": "npm:browserify-sign@4.0.4",
        "public-encrypt": "npm:public-encrypt@4.0.0",
        "create-hash": "npm:create-hash@1.1.3",
        "randombytes": "npm:randombytes@2.0.5"
      }
    },
    "npm:form-data@2.1.4": {
      "map": {
        "combined-stream": "npm:combined-stream@1.0.5",
        "asynckit": "npm:asynckit@0.4.0",
        "mime-types": "npm:mime-types@2.1.17"
      }
    },
    "npm:combined-stream@1.0.5": {
      "map": {
        "delayed-stream": "npm:delayed-stream@1.0.0"
      }
    },
    "npm:har-validator@4.2.1": {
      "map": {
        "ajv": "npm:ajv@4.11.8",
        "har-schema": "npm:har-schema@1.0.5"
      }
    },
    "npm:jspm-nodelibs-zlib@0.2.3": {
      "map": {
        "browserify-zlib": "npm:browserify-zlib@0.1.4"
      }
    },
    "npm:http-signature@1.1.1": {
      "map": {
        "assert-plus": "npm:assert-plus@0.2.0",
        "sshpk": "npm:sshpk@1.13.1",
        "jsprim": "npm:jsprim@1.4.1"
      }
    },
    "npm:hawk@3.1.3": {
      "map": {
        "boom": "npm:boom@2.10.1",
        "hoek": "npm:hoek@2.16.3",
        "cryptiles": "npm:cryptiles@2.0.5",
        "sntp": "npm:sntp@1.0.9"
      }
    },
    "npm:tough-cookie@2.3.2": {
      "map": {
        "punycode": "npm:punycode@1.4.1"
      }
    },
    "npm:jspm-nodelibs-string_decoder@0.2.1": {
      "map": {
        "string_decoder": "npm:string_decoder@0.10.31"
      }
    },
    "npm:sshpk@1.13.1": {
      "map": {
        "assert-plus": "npm:assert-plus@1.0.0",
        "dashdash": "npm:dashdash@1.14.1",
        "asn1": "npm:asn1@0.2.3",
        "getpass": "npm:getpass@0.1.7"
      }
    },
    "npm:jsprim@1.4.1": {
      "map": {
        "assert-plus": "npm:assert-plus@1.0.0",
        "extsprintf": "npm:extsprintf@1.3.0",
        "json-schema": "npm:json-schema@0.2.3",
        "verror": "npm:verror@1.10.0"
      }
    },
    "npm:ajv@4.11.8": {
      "map": {
        "json-stable-stringify": "npm:json-stable-stringify@1.0.1",
        "co": "npm:co@4.6.0"
      }
    },
    "npm:create-hmac@1.1.6": {
      "map": {
        "create-hash": "npm:create-hash@1.1.3",
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "inherits": "npm:inherits@2.0.1",
        "ripemd160": "npm:ripemd160@2.0.1",
        "sha.js": "npm:sha.js@2.4.8",
        "cipher-base": "npm:cipher-base@1.0.4"
      }
    },
    "npm:browserify-sign@4.0.4": {
      "map": {
        "create-hash": "npm:create-hash@1.1.3",
        "create-hmac": "npm:create-hmac@1.1.6",
        "inherits": "npm:inherits@2.0.1",
        "parse-asn1": "npm:parse-asn1@5.1.0",
        "elliptic": "npm:elliptic@6.4.0",
        "bn.js": "npm:bn.js@4.11.8",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:diffie-hellman@5.0.2": {
      "map": {
        "randombytes": "npm:randombytes@2.0.5",
        "bn.js": "npm:bn.js@4.11.8",
        "miller-rabin": "npm:miller-rabin@4.0.0"
      }
    },
    "npm:public-encrypt@4.0.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.3",
        "randombytes": "npm:randombytes@2.0.5",
        "parse-asn1": "npm:parse-asn1@5.1.0",
        "bn.js": "npm:bn.js@4.11.8",
        "browserify-rsa": "npm:browserify-rsa@4.0.1"
      }
    },
    "npm:randombytes@2.0.5": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1"
      }
    },
    "npm:create-hash@1.1.3": {
      "map": {
        "inherits": "npm:inherits@2.0.1",
        "ripemd160": "npm:ripemd160@2.0.1",
        "sha.js": "npm:sha.js@2.4.8",
        "cipher-base": "npm:cipher-base@1.0.4"
      }
    },
    "npm:boom@2.10.1": {
      "map": {
        "hoek": "npm:hoek@2.16.3"
      }
    },
    "npm:cryptiles@2.0.5": {
      "map": {
        "boom": "npm:boom@2.10.1"
      }
    },
    "npm:sntp@1.0.9": {
      "map": {
        "hoek": "npm:hoek@2.16.3"
      }
    },
    "npm:jspm-nodelibs-buffer@0.2.3": {
      "map": {
        "buffer": "npm:buffer@5.0.7"
      }
    },
    "npm:browserify-zlib@0.1.4": {
      "map": {
        "readable-stream": "npm:readable-stream@2.3.3",
        "pako": "npm:pako@0.2.9"
      }
    },
    "npm:pbkdf2@3.0.14": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "create-hash": "npm:create-hash@1.1.3",
        "create-hmac": "npm:create-hmac@1.1.6",
        "ripemd160": "npm:ripemd160@2.0.1",
        "sha.js": "npm:sha.js@2.4.8"
      }
    },
    "npm:create-ecdh@4.0.0": {
      "map": {
        "elliptic": "npm:elliptic@6.4.0",
        "bn.js": "npm:bn.js@4.11.8"
      }
    },
    "npm:browserify-cipher@1.0.0": {
      "map": {
        "browserify-aes": "npm:browserify-aes@1.0.8",
        "browserify-des": "npm:browserify-des@1.0.0",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.3"
      }
    },
    "npm:jspm-nodelibs-url@0.2.1": {
      "map": {
        "url": "npm:url@0.11.0"
      }
    },
    "npm:mime-types@2.1.17": {
      "map": {
        "mime-db": "npm:mime-db@1.30.0"
      }
    },
    "npm:readable-stream@2.3.3": {
      "map": {
        "string_decoder": "npm:string_decoder@1.0.3",
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "core-util-is": "npm:core-util-is@1.0.2",
        "isarray": "npm:isarray@1.0.0",
        "process-nextick-args": "npm:process-nextick-args@1.0.7",
        "util-deprecate": "npm:util-deprecate@1.0.2",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:parse-asn1@5.1.0": {
      "map": {
        "create-hash": "npm:create-hash@1.1.3",
        "pbkdf2": "npm:pbkdf2@3.0.14",
        "browserify-aes": "npm:browserify-aes@1.0.8",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.3",
        "asn1.js": "npm:asn1.js@4.9.1"
      }
    },
    "npm:elliptic@6.4.0": {
      "map": {
        "bn.js": "npm:bn.js@4.11.8",
        "inherits": "npm:inherits@2.0.1",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "minimalistic-crypto-utils": "npm:minimalistic-crypto-utils@1.0.1",
        "hmac-drbg": "npm:hmac-drbg@1.0.1",
        "hash.js": "npm:hash.js@1.1.3",
        "brorand": "npm:brorand@1.1.0"
      }
    },
    "npm:ripemd160@2.0.1": {
      "map": {
        "inherits": "npm:inherits@2.0.1",
        "hash-base": "npm:hash-base@2.0.2"
      }
    },
    "npm:browserify-aes@1.0.8": {
      "map": {
        "create-hash": "npm:create-hash@1.1.3",
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "cipher-base": "npm:cipher-base@1.0.4",
        "evp_bytestokey": "npm:evp_bytestokey@1.0.3",
        "inherits": "npm:inherits@2.0.1",
        "buffer-xor": "npm:buffer-xor@1.0.3"
      }
    },
    "npm:cipher-base@1.0.4": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "inherits": "npm:inherits@2.0.1"
      }
    },
    "npm:browserify-rsa@4.0.1": {
      "map": {
        "bn.js": "npm:bn.js@4.11.8",
        "randombytes": "npm:randombytes@2.0.5"
      }
    },
    "npm:evp_bytestokey@1.0.3": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1",
        "md5.js": "npm:md5.js@1.3.4"
      }
    },
    "npm:miller-rabin@4.0.0": {
      "map": {
        "bn.js": "npm:bn.js@4.11.8",
        "brorand": "npm:brorand@1.1.0"
      }
    },
    "npm:browserify-des@1.0.0": {
      "map": {
        "cipher-base": "npm:cipher-base@1.0.4",
        "inherits": "npm:inherits@2.0.1",
        "des.js": "npm:des.js@1.0.0"
      }
    },
    "npm:sha.js@2.4.8": {
      "map": {
        "inherits": "npm:inherits@2.0.1"
      }
    },
    "npm:json-stable-stringify@1.0.1": {
      "map": {
        "jsonify": "npm:jsonify@0.0.0"
      }
    },
    "npm:url@0.11.0": {
      "map": {
        "punycode": "npm:punycode@1.3.2",
        "querystring": "npm:querystring@0.2.0"
      }
    },
    "npm:dashdash@1.14.1": {
      "map": {
        "assert-plus": "npm:assert-plus@1.0.0"
      }
    },
    "npm:getpass@0.1.7": {
      "map": {
        "assert-plus": "npm:assert-plus@1.0.0"
      }
    },
    "npm:ecc-jsbn@0.1.1": {
      "map": {
        "jsbn": "npm:jsbn@0.1.1"
      }
    },
    "npm:verror@1.10.0": {
      "map": {
        "assert-plus": "npm:assert-plus@1.0.0",
        "extsprintf": "npm:extsprintf@1.3.0",
        "core-util-is": "npm:core-util-is@1.0.2"
      }
    },
    "npm:bcrypt-pbkdf@1.0.1": {
      "map": {
        "tweetnacl": "npm:tweetnacl@0.14.5"
      }
    },
    "npm:jspm-nodelibs-stream@0.2.1": {
      "map": {
        "stream-browserify": "npm:stream-browserify@2.0.1"
      }
    },
    "npm:string_decoder@1.0.3": {
      "map": {
        "safe-buffer": "npm:safe-buffer@5.1.1"
      }
    },
    "npm:md5.js@1.3.4": {
      "map": {
        "hash-base": "npm:hash-base@3.0.4",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:asn1.js@4.9.1": {
      "map": {
        "bn.js": "npm:bn.js@4.11.8",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:hmac-drbg@1.0.1": {
      "map": {
        "hash.js": "npm:hash.js@1.1.3",
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "minimalistic-crypto-utils": "npm:minimalistic-crypto-utils@1.0.1"
      }
    },
    "npm:hash.js@1.1.3": {
      "map": {
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:hash-base@2.0.2": {
      "map": {
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:des.js@1.0.0": {
      "map": {
        "minimalistic-assert": "npm:minimalistic-assert@1.0.0",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:stream-browserify@2.0.1": {
      "map": {
        "readable-stream": "npm:readable-stream@2.3.3",
        "inherits": "npm:inherits@2.0.3"
      }
    },
    "npm:hash-base@3.0.4": {
      "map": {
        "inherits": "npm:inherits@2.0.3",
        "safe-buffer": "npm:safe-buffer@5.1.1"
      }
    },
    "npm:jspm-nodelibs-os@0.2.2": {
      "map": {
        "os-browserify": "npm:os-browserify@0.3.0"
      }
    }
  }
});
