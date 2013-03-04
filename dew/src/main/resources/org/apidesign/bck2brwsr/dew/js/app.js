// 'use strict';

// Declare app level module which depends on filters, and services
angular.module('bck2brwsr', []).
  directive('uiCodemirror', ['$timeout', function($timeout) {
        'use strict';

        var events = ["cursorActivity", "viewportChange", "gutterClick", "focus", "blur", "scroll", "update"];
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, elm, attrs, ngModel) {
                var options, opts, onChange, deferCodeMirror, codeMirror, timeoutId, val;

                if (elm[0].type !== 'textarea') {
                    throw new Error('uiCodemirror3 can only be applied to a textarea element');
                }

                options = /* uiConfig.codemirror  || */ {};
                opts = angular.extend({}, options, scope.$eval(attrs.uiCodemirror));

                onChange = function(instance, changeObj) {                    
                    val = instance.getValue();
                    $timeout.cancel(timeoutId);
                    timeoutId = $timeout(function() {
                        ngModel.$setViewValue(val);                        
                      }, 500);                    
                };
                
                deferCodeMirror = function() {
                    codeMirror = CodeMirror.fromTextArea(elm[0], opts);
                    elm[0].codeMirror = codeMirror;
                    // codeMirror.on("change", onChange(opts.onChange));
                    codeMirror.on("change", onChange);

                    for (var i = 0, n = events.length, aEvent; i < n; ++i) {
                        aEvent = opts["on" + events[i].charAt(0).toUpperCase() + events[i].slice(1)];
                        if (aEvent === void 0)
                            continue;
                        if (typeof aEvent !== "function")
                            continue;
                                                
                        var bound = _.bind( aEvent, scope );
                        
                        codeMirror.on(events[i], bound);
                    }

                    // CodeMirror expects a string, so make sure it gets one.
                    // This does not change the model.
                    ngModel.$formatters.push(function(value) {
                        if (angular.isUndefined(value) || value === null) {
                            return '';
                        }
                        else if (angular.isObject(value) || angular.isArray(value)) {
                            throw new Error('ui-codemirror cannot use an object or an array as a model');
                        }
                        return value;
                    });

                    // Override the ngModelController $render method, which is what gets called when the model is updated.
                    // This takes care of the synchronizing the codeMirror element with the underlying model, in the case that it is changed by something else.
                    ngModel.$render = function() {
                        codeMirror.setValue(ngModel.$viewValue);
                    };

                };

                $timeout(deferCodeMirror);

            }
        };
}]);

function DevCtrl( $scope, $http ) {
    var templateHtml = 
"<html><body>\n" +
"  <input data-bind=\"value: value, valueUpdate: 'afterkeydown'\" \n" +
"     value=\"0\" type=\"number\">\n" +
"  </input>\n" +
"  * <span data-bind=\"text: value\">0</span> \n" +
"  = <span data-bind=\"text: powerValue\">0</span>\n" +
"  <br/>\n" +
"  <button id='dupl'>Duplicate!</button>\n" +
"  <button id=\"clear\">Clear!</button>" +
" <hr/>\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n" +
" <script src=\"/bck2brwsr.js\"></script>\n" +
" <script type=\"text/javascript\">\n" +
"   function ldCls(res) {\n" +
"     var request = new XMLHttpRequest();\n" +
"     request.open('GET', '/classes/' + res, false);\n" +
"     request.send();\n" +
"     var arr = eval('(' + request.responseText + ')');\n" +
"     return arr;\n" +
"   }\n" +
"   var vm = bck2brwsr(ldCls);\n" +
"   vm.loadClass('${fqn}');\n" +
" </script>\n" +
"</body></html>";
    var templateJava = 
"package bck2brwsr.demo;\n" +
"import org.apidesign.bck2brwsr.htmlpage.api.*;\n" +
"import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;\n" +
"\n" +
"@Page(xhtml=\"index.html\", className=\"Index\", properties={\n" +
"  @Property(name=\"value\", type=int.class)\n" +
"})\n" +
"class YourFirstHTML5PageInRealLanguage {\n" +
"  static { new Index().applyBindings(); }\n" +
"  @On(event=CLICK, id=\"dupl\") static void duplicateValue(Index m) {\n" +
"    m.setValue(m.getValue() * 2);\n" +
"  }\n" +
"  @On(event=CLICK, id=\"clear\") static void zeroTheValue(Index m) {\n" +
"     m.setValue(0);;\n" +
"  }\n" +
"  @ComputedProperty static int powerValue(int value) {\n" +
"    return value * value;\n" +
"  }\n" +
"}";

    
    $scope.makeMarker = function( editor, line ) {
        var marker = document.createElement("div");
        marker.innerHTML = " ";
        marker.className = "issue";
        
        var info = editor.lineInfo(line);
        editor.setGutterMarker(line, "issues", info.markers ? null : marker);
        
        return marker;
    };
    
    
    // Returns a function, that, as long as it continues to be invoked, will not
    // be triggered. The function will be called after it stops being called for
    // N milliseconds. If `immediate` is passed, trigger the function on the
    // leading edge, instead of the trailing.
    $scope.debounce = function(func, wait, immediate) {
      var timeout, result;
      return function() {
        var context = this, args = arguments;
        var later = function() {
          timeout = null;
          if (!immediate) result = func.apply(context, args);
        };
        var callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) result = func.apply(context, args);
        return result;
      };
    };
    
    $scope.reload = function() {
        $scope.errors = null;
        var frame = document.getElementById("result");        
        frame.src = "result.html";
        frame.contentDocument.location.reload(true);
        frame.contentWindow.location.reload();
        document.getElementById("editorJava").codeMirror.clearGutter("issues");   
    };
    
    $scope.fail = function( data ) {
        $scope.errors = eval( data );
        var editor = document.getElementById("editorJava").codeMirror;   
        editor.clearGutter( "issues" );
        
        for( var i = 0; i < $scope.errors.length; i ++ ) {
            $scope.makeMarker( editor, $scope.errors[i].line - 1 );
        }
        
    };
    
    $scope.post = function() {
        return $http({url: ".",
            method: "POST",
            //headers: this.headers,
            data: { html : $scope.html, java : $scope.java} 
        }).success( $scope.reload ).error( $scope.fail );
    };
    
    $scope.errorClass = function( kind ) {
        switch( kind ) {
            case "ERROR" :
                return "error";
            default :         
                return "warning";   
        }
    };
    
    $scope.gotoError = function( line, col ) {
        var editor = document.getElementById("editorJava").codeMirror;   
        editor.setCursor({ line: line - 1, ch : col - 1 });
        editor.focus();
    };
    
    $scope.tab = "html";
    $scope.html= templateHtml;  
    $scope.java = templateJava;  
    
    $scope.$watch( "html", $scope.debounce( $scope.post, 2000 ) );
    $scope.$watch( "java", $scope.debounce( $scope.post, 2000 ) );
    $scope.post();
    
}
