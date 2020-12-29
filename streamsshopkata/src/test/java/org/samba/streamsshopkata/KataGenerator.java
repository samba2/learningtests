package org.samba.streamsshopkata;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KataGenerator {
    public static void main(String[] args) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(new File("streamsshopkata/src/test/java/org/samba/streamsshopkata/KataSolutionTest.java"));
        LexicalPreservingPrinter.setup(cu);

        ModifierVisitor<?> solutionCleanerVisitor = new SolutionCleanerVisitor();
        solutionCleanerVisitor.visit(cu, null);

        // fix lexicical printer keeping "IGNORE" comments
        String out = LexicalPreservingPrinter.print(cu);
        String filtered = Arrays.stream(out.split("\n"))
                .filter(s -> !s.contains("IGNORE"))
                .collect(Collectors.joining("\n"));

        Files.writeString(Path.of("streamsshopkata/src/test/java/org/samba/streamsshopkata/KataTest.java"), filtered);
    }

    private static class SolutionCleanerVisitor extends ModifierVisitor<Void> {

        @Override
        public Node visit(ImportDeclaration id, Void arg) {
            var IMPORTS_TO_BE_REMOVED = List.of(
                    "com.google.common.collect",
                    "java.util.stream");
            super.visit(id, arg);

            for (String pattern : IMPORTS_TO_BE_REMOVED) {
                if (id.getNameAsString().contains(pattern)) return null;
            }

            return id;
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration cd, Void arg) {
            super.visit(cd, arg);
            cd.setName("KataTest");
            return cd;
        }

        @Override
        public MethodDeclaration visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            // look for "private static" methods
            if (md.getModifiers().contains(Modifier.privateModifier()) &&
                    md.getModifiers().contains(Modifier.staticModifier())) {

                // remove ignored methods
                var comment = md.getComment();
                if (comment.isPresent() && comment.get().getContent().contains("IGNORE")) {
                    return null;
                }

                // replace implementation
                BlockStmt cb = new BlockStmt();
                cb.addStatement("throw new UnsupportedOperationException(\"IMPLEMENT ME\");");
                md.setBody(cb);

                md.removeComment();
            }
            return md;
        }
    }
}
