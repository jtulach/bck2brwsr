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
    var templateHtml = "<html><body>\n"
        + " <button id='btn'>Hello!</button>\n"
        + " <hr/>\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + "\n"
        + " <script src=\"/bck2brwsr.js\"></script>\n"
        + " <script type=\"text/javascript\">\n"
        + "   function ldCls(res) {\n"
        + "     var request = new XMLHttpRequest();\n"
        + "     request.open('GET', '/classes/' + res, false);\n"
        + "     request.send();\n"
        + "     var arr = eval('(' + request.responseText + ')');\n"
        + "     return arr;\n"
        + "   }\n"
        + "   var vm = new bck2brwsr(ldCls);\n"
        + "   vm.loadClass('bck2brwsr.demo.Index');\n"
        + " </script>\n"
        + "</body></html>\n";
    var templateJava = "package bck2brwsr.demo;\n"
        + "import org.apidesign.bck2brwsr.htmlpage.api.*;\n"
        + "import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;\n"
        + "@Page(xhtml=\"index.html\", className=\"Index\")\n"
        + "class YourFirstHTML5PageInRealLanguage {\n"
        + "   @On(event=CLICK, id=\"btn\") static void clcs() {\n"
        + "     Element.alert(\"Hello World!\");\n"
        + "     Index.BTN.setDisabled(true);\n"
        + "   }\n"
        + "}\n";

    
    $scope.reload= function() {
        var frame = document.getElementById("result");        
        frame.src = "result.html";
        frame.contentDocument.location.reload(true);
        frame.contentWindow.location.reload();
    };
    
    $scope.post = function(html, java) {
        return $http({url: ".",
            method: "POST",
            //headers: this.headers,
            data: { html : $scope.html, java : $scope.java} 
        }).success( $scope.reload );
    };
    
    $scope.tab = "html";
    $scope.html= templateHtml;  
    $scope.java = templateJava;  
    
    $scope.tabActive = function( tab ) {
        return tab === $scope.tab ? "active" : "";
    };
    
    // $scope.$watch( "html", htmlChange );
    $scope.post();
    
}
