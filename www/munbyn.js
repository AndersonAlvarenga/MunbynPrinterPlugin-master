var exec = require('cordova/exec');

var BTPrinter = {
   write: function(fnSuccess, fnError, deviceName, message){
      exec(fnSuccess, fnError, "MunbynWrapper", "write", [deviceName,message]);
   },
   list: function(fnSuccess, fnError){
      exec(fnSuccess, fnError, 'MunbynWrapper', 'list', []);
   },
   seachPermissionConnect: function(fnSuccess, fnError) {
       exec(fnSuccess, fnError, 'MunbynWrapper', 'seachPermissionConnect', [])

   },
   solicitaPermissaoConnect: function(fnSuccess, fnError) {
        exec(fnSuccess, fnError, 'MunbynWrapper', 'solicitaPermissaoConnect', [])
    }
};

module.exports = BTPrinter;