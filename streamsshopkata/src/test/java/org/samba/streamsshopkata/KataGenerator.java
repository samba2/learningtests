package org.samba.streamsshopkata;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KataGenerator {

    private static final String targetClass = "KataTest";
    private static final String sourceFile = "KataSolutionTest.java";
    private static final String basePath = "streamsshopkata/src/test/java/org/samba/streamsshopkata/";

    private static final List<String> importsBlackList = List.of(
            "com.google.common.collect",
            "java.util.stream");

    public static void main(String[] args) throws IOException {
        var cu = StaticJavaParser.parse(Path.of(basePath, sourceFile).toFile());
        LexicalPreservingPrinter.setup(cu);

        ModifierVisitor<?> solutionCleanerVisitor = new SolutionCleanerVisitor();
        solutionCleanerVisitor.visit(cu, null);

        // fix lexical printer keeping "IGNORE" comments
        var out = Arrays.stream(LexicalPreservingPrinter.print(cu).split("\n"))
                .filter(s -> !s.contains("IGNORE"))
                .collect(Collectors.joining("\n"));

        var outputPath = Path.of(basePath, targetClass + ".java");
        Files.writeString(outputPath, out);
        System.out.println("\nPrepared Kata in " + outputPath);
    }

    private static class SolutionCleanerVisitor extends ModifierVisitor<Void> {

        @Override
        public Node visit(ImportDeclaration id, Void arg) {
            super.visit(id, arg);
            if (isInImportsBlackList(id.getNameAsString())) {
                return null;
            } else {
                return id;
            }
        }

        private boolean isInImportsBlackList(String importDeclaration) {
            for (String pattern : importsBlackList) {
                if (importDeclaration.contains(pattern)) return true;
            }
            return false;
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration cd, Void arg) {
            super.visit(cd, arg);
            cd.setName(targetClass);
            return cd;
        }

        @Override
        public MethodDeclaration visit(MethodDeclaration md, Void arg) {
            super.visit(md, arg);
            if (isSolutionContainingMethod(md)) {
                // removed IGNORE methods
                if (isIgnored(md)) return null;

                replaceImplementation(md);
                md.removeComment();
            }
            return md;
        }

        // look for "private static" methods
        private boolean isSolutionContainingMethod(MethodDeclaration md) {
            return md.getModifiers().contains(Modifier.privateModifier()) &&
                    md.getModifiers().contains(Modifier.staticModifier());
        }

        private boolean isIgnored(MethodDeclaration md) {
            var comment = md.getComment();
            return comment.isPresent() && comment.get().getContent().contains("IGNORE");
        }

        private void replaceImplementation(MethodDeclaration md) {
            BlockStmt cb = new BlockStmt();
            cb.addStatement("throw new UnsupportedOperationException(\"IMPLEMENT ME\");");
            md.setBody(cb);
        }
    }
}
