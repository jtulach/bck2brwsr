/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.bck2brwsr.ide.editor;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.BLOCK_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.JAVADOC_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.LINE_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_JSNI2JavaScriptBody", description = "#DESC_JSNI2JavaScriptBody", category = "general")
@Messages({
    "DN_JSNI2JavaScriptBody=JSNI to @JavaScriptBody",
    "DESC_JSNI2JavaScriptBody=JSNI to @JavaScriptBody"
})
public class JSNI2JavaScriptBody {

    @TriggerTreeKind(Kind.METHOD)
    @Messages("ERR_JSNI2JavaScriptBody=Can convert JSNI to @JavaScriptBody")
    public static ErrorDescription computeWarning(final HintContext ctx) {
        Token<JavaTokenId> token = findBlockToken(ctx.getInfo(), ctx.getPath(), ctx);

        if (token == null) {
            return null;
        }

        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_JSNI2JavaScriptBody(), fix);
    }

    private static Token<JavaTokenId> findBlockToken(CompilationInfo info, TreePath path, HintContext ctx) {
        int end = (int) info.getTrees().getSourcePositions().getEndPosition(path.getCompilationUnit(), path.getLeaf());
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        if (ts == null) return null;

        ts.move(end);

        if ((ctx != null && ctx.isCanceled()) || !ts.movePrevious() || ts.token().id() != JavaTokenId.SEMICOLON) return null;

        OUTER: while (ts.movePrevious()) {
            if (ctx != null && ctx.isCanceled()) return null;

            switch (ts.token().id()) {
                case WHITESPACE: break;
                case LINE_COMMENT: break;
                case JAVADOC_COMMENT: break;
                case BLOCK_COMMENT:
                    final CharSequence tok = ts.token().text();
                    final int l = tok.length(); 
                    if (l > 4 
                        && tok.subSequence(0, 4).toString().equals("/*-{") // NOI18N
                        && tok.subSequence(l - 4, l).toString().equals("}-*/") // NOI18N
                    ) {
                        return ts.offsetToken();
                    }
                    break;
                default:
                    break OUTER;
            }
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_JSNI2JavaScriptBody=Convert JSNI to @JavaScriptBody")
        protected String getText() {
            return Bundle.FIX_JSNI2JavaScriptBody();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            Token<JavaTokenId> jsniComment = findBlockToken(ctx.getWorkingCopy(), ctx.getPath(), null);

            if (jsniComment == null) {
                //XXX: warn?
                return ;
            }
            
            JsniCommentTokenizer tok = new JsniCommentTokenizer();
            ManglingSink ms = new ManglingSink();
            final CharSequence cmnt = jsniComment.text();
            tok.process(cmnt.subSequence(4, cmnt.length() - 4), ms);

            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            MethodTree mt = (MethodTree) ctx.getPath().getLeaf();
            List<LiteralTree> params = new ArrayList<LiteralTree>();

            for (VariableTree p : mt.getParameters()) {
                params.add(make.Literal(p.getName().toString()));
            }

            AnnotationTree jsBody = make.Annotation(make.QualIdent("org.apidesign.bck2brwsr.core.JavaScriptBody"),
                Arrays.<ExpressionTree>asList(
                    make.Assignment(make.Identifier("args"), make.NewArray(null, Collections.<ExpressionTree>emptyList(), params)),
                    make.Assignment(make.Identifier("body"), make.Literal(ms.out.toString()))
                )
            );


            ctx.getWorkingCopy().rewrite(mt.getModifiers(), make.addModifiersAnnotation(mt.getModifiers(), jsBody));
        }
    }
}
