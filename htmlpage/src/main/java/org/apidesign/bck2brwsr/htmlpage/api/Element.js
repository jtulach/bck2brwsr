

function org_apidesign_bck2brwsr_htmlpage_api_Element_setAttribute_Lorg_apidesign_bck2brwsr_htmlpage_api_ElementLjava_lang_StringLjava_lang_Object(self, property, value) {
    document.getElementById(self.id)[property] = value;
}

function org_apidesign_bck2brwsr_htmlpage_api_Element_addOnClick_Ljava_lang_Runnable(self, run) {
    document.getElementById(self.id).onClick = function() { run.runV(); };
}

