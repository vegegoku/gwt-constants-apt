package com.progressoft.brix.domino;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MessegerUtil {

    private Messager messager;

    public MessegerUtil(Messager messager) {
        this.messager = messager;
    }

    public void handleError(Exception e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        messager.printMessage(Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString());
    }

}
