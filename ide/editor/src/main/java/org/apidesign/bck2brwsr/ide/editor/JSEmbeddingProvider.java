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
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class JSEmbeddingProvider extends JavaParserResultTask<Parser.Result> {

    private static final int PRIORITY = 1000;
    private static final String JS_ANNOTATION = "org.apidesign.bck2brwsr.core.JavaScriptBody";  //NOI18N
    private static final String BODY = "body";                            //NOI18N
    private static final String JAVA_MIME_TYPE = "text/x-java";           //NOI18N
    private static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; //NOI18N
    private final AtomicBoolean canceled = new AtomicBoolean();

    private JSEmbeddingProvider() {
        super(JavaSource.Phase.ELEMENTS_RESOLVED);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void cancel() {
        canceled.set(true);
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void run(Parser.Result t, SchedulerEvent se) {
        canceled.set(false);
        final CompilationInfo ci = CompilationInfo.get(t);
        final CompilationUnitTree cu = ci.getCompilationUnit();
        final Trees trees = ci.getTrees();
        final SourcePositions sp = trees.getSourcePositions();
        final Finder f = new Finder(trees);
        final List<LiteralTree> result = new ArrayList<LiteralTree>();
        f.scan(cu, result);
        if (!result.isEmpty()) {
            try {
                final TokenHierarchy<Document> tk = TokenHierarchy.get(ci.getDocument());
                final Language<?> java = Language.find(JAVA_MIME_TYPE);
                final Language<?> javaScript = Language.find(JAVASCRIPT_MIME_TYPE);
                if (java != null && javaScript != null) {
                    final TokenSequence<?> seq = tk.tokenSequence(java);
                    if (seq != null) {
                        for (LiteralTree lt : result) {
                            final int start = (int) sp.getStartPosition(cu, lt);
                            final int end = (int) sp.getEndPosition(cu, lt);
                            seq.move(start);
                            while (seq.moveNext() && seq.offset() < end) {
                                seq.createEmbedding(javaScript, 1, 1, true);
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }




    private static final class Finder extends TreePathScanner<Void, List<? super LiteralTree>> {

        private final Trees trees;
        private CompilationUnitTree cu;
        private boolean inEmbedding;

        Finder(final Trees trees) {
            this.trees = trees;
        }

        @Override
        public Void visitCompilationUnit(
                final CompilationUnitTree unit,
                final List p) {
            this.cu = unit;
            return super.visitCompilationUnit(unit, p);
        }



        @Override
        public Void visitMethod(
                final MethodTree m,
                final List<? super LiteralTree> p) {
            for (AnnotationTree a : m.getModifiers().getAnnotations()) {
                final TypeElement ae = (TypeElement) trees.getElement(TreePath.getPath(cu, a.getAnnotationType()));
                if (ae != null && JS_ANNOTATION.contentEquals(ae.getQualifiedName())) {
                    final List<? extends ExpressionTree> args =  a.getArguments();
                    for (ExpressionTree kvp : args) {
                        if (kvp instanceof AssignmentTree) {
                            final AssignmentTree assignemt = (AssignmentTree) kvp;
                            if (BODY.equals(assignemt.getVariable().toString())) {
                                inEmbedding = true;
                                try {
                                    scan(assignemt.getExpression(), p);
                                } finally {
                                    inEmbedding = false;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitLiteral(LiteralTree node, List<? super LiteralTree> p) {
            if (inEmbedding) {
                p.add(node);
            }
            return super.visitLiteral(node, p);
        }

    }

    @MimeRegistration(
            service = TaskFactory.class,
            mimeType = JAVA_MIME_TYPE)
    public static final class Factory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snpsht) {
            return Collections.singleton(new JSEmbeddingProvider());
        }
    }

}
