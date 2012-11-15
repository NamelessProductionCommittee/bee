/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package bee.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kiss.Extensible;
import kiss.I;
import kiss.model.ClassUtil;
import bee.Bee;
import bee.Platform;
import bee.UserInterface;
import bee.api.Command;
import bee.api.Task;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;

/**
 * @version 2012/11/09 13:17:38
 */
public class Javadoc extends Task {

    /** The output root directory for javadoc. */
    protected Path output;

    /** The output root directory for javadoc. */
    protected Class<? extends CustomJavadoc> doclet;

    /**
     * <p>
     * Generate javadoc with the specified doclet.
     * </p>
     */
    @Command("Generate javadoc.")
    public void javadoc() {
        // specify output directory
        if (output == null) {
            output = project.getOutput().resolve("api");
        }

        if (Files.exists(output) && !Files.isDirectory(output)) {
            throw new IllegalArgumentException("Javadoc output path is not directory. " + output.toAbsolutePath());
        }

        // specify doclet
        Class docletClass;

        if (doclet == null) {
            CustomJavadoc custom = ui.ask("Multiple doclets are found.", I.find(CustomJavadoc.class));

            if (custom == null) {
                docletClass = Standard.class;
            } else {
                docletClass = custom.getClass();
            }
        } else {
            docletClass = doclet;
        }

        List<String> options = new CopyOnWriteArrayList();

        if (docletClass == Standard.class) {
            options.add("-d");
            options.add(output.toAbsolutePath().toString());
        }
        options.add("-encoding");
        options.add("UTF-8");

        for (Path sources : project.getSources()) {
            for (Path path : I.walk(sources, "**.java")) {
                options.add(path.toString());
            }
        }

        // setup
        CustomJavadoc.outputDirectory = output;

        PrintWriter writer = new PrintWriter(I.make(UIWriter.class));
        int result = Main.execute("", writer, writer, writer, docletClass.getName(), docletClass.getClassLoader(), options.toArray(new String[options.size()]));

        if (result == 1) {
            // success

        } else {
            // fail
            throw new Error("Javadoc command is failed.");
        }
    }

    /**
     * @version 2012/11/09 14:37:40
     */
    private static class UIWriter extends Writer {

        private UserInterface ui;

        /**
         * @param ui
         */
        private UIWriter(UserInterface ui) {
            this.ui = ui;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String message = String.valueOf(cbuf, off, len).trim();

            if (message.endsWith(Platform.EOL)) {
                message = message.substring(0, message.length() - Platform.EOL.length());
            }

            if (message.length() != 0) {
                ui.talk(message);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
        }
    }

    /**
     * <p>
     * Create your custom javadoc view.
     * </p>
     * 
     * @version 2012/11/09 15:51:00
     */
    public static class CustomJavadoc extends Doclet implements Extensible {

        /** The user interface to log. */
        protected static final UserInterface ui = I.make(UserInterface.class);

        /** The root directory for Javadoc. */
        protected static Path outputDirectory;

        /**
         * <p>
         * Start entry point.
         * </p>
         * 
         * @param root
         * @return
         */
        public static boolean start(RootDoc root) {
            ui.talk("@@@@@@@@@@@@@@@@@@");
            ui.talk(outputDirectory);

            // build index.html
            Path path = ClassUtil.getArchive(Bee.class).resolve("bee/task/javadoc/in");
            System.out.println(path);
            System.out.println(Files.exists(path));

            I.copy(ClassUtil.getArchive(Bee.class).resolve("bee/task/javadoc"), outputDirectory, "**");

            for (ClassDoc classDoc : root.classes()) {
                System.out.println(classDoc.qualifiedName());
            }
            return false;
        }
    }

    /**
     * @version 2012/11/09 14:14:03
     */
    public static class CustomDoc extends Doclet {

        /**
         * @param root
         * @return
         */
        public static boolean start(RootDoc root) {
            for (ClassDoc classDoc : root.classes()) {
                System.out.format("Class: %s\r\n", classDoc.name() + "  " + classDoc.qualifiedName());
                for (MethodDoc methodDoc : classDoc.methods()) {

                    // ソース位置
                    SourcePosition position = methodDoc.position();
                    int line = position.line();
                    String path;

                    try {
                        path = position.file().getCanonicalPath();
                    } catch (IOException e) {
                        throw I.quiet(e);
                    }

                    // 修飾子
                    String modifiersName = methodDoc.modifiers();

                    // 戻り値
                    Type returnType = methodDoc.returnType();
                    String returnName = returnType.typeName();
                    if (returnType.dimension() != null) {
                        returnName += returnType.dimension();
                    }

                    // メソッド名
                    String methodName = methodDoc.name();

                    // パラメータ
                    String paramName = "";
                    for (Parameter parameter : methodDoc.parameters()) {
                        paramName += "".equals(paramName) ? parameter.toString() : ", " + parameter.toString();
                    }

                    System.out.format("\t[%s:%03d]", path, line);
                    System.out.format("%s %s %s(%s)\r\n", modifiersName, returnName, methodName, paramName);
                }
            }
            return true;
        }
    }
}