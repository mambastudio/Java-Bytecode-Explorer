/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import static com.mamba.bytecodeexplorer.classanalysis.UtilBytecode.methodModifiers;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attributes;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author joemw
 */
public record MethodModelInfo(MethodModel mm) {

    public MethodModelInfo {
        Objects.requireNonNull(mm);
    }
    
    public String methodName(){
        return mm.methodName().stringValue();
    }

    public String methodSource() {
        AccessFlags flags = mm.flags();

        String modifiers = String.join(" ", methodModifiers(flags));
        String returnType = mm.methodTypeSymbol().returnType().displayName();
        String name = mm.methodName().stringValue();
        String params = parameters();

        String throwsClause = mm.findAttribute(Attributes.exceptions())
            .map(attr -> attr.exceptions().stream()
                    .map(e -> e.name())
                    .collect(Collectors.joining(", ", " throws ", "")))
            .orElse("");


        return (modifiers + " " +
                returnType + " " +
                name + "(" + params + ")" +
                throwsClause + ";").trim();
    }
    
    public String parameters() {
        MethodTypeDesc mt = mm.methodTypeSymbol();
        boolean varargs = mm.flags().has(AccessFlag.VARARGS);

        var paramTypes = mt.parameterList();

        var paramNames = mm.findAttribute(Attributes.methodParameters())
                .map(attr -> attr.parameters().stream()
                        .map(p -> p.name()
                                .map(Utf8Entry::stringValue)
                                .orElse(""))
                        .toList())
                .orElse(List.of());

        List<String> result = new ArrayList<>();

        for (int i = 0; i < paramTypes.size(); i++) {
            ClassDesc t = paramTypes.get(i);
            

            String name = i < paramNames.size() && !paramNames.get(i).isEmpty() ? paramNames.get(i) : "arg" + i;
            boolean isLast = i == paramTypes.size() - 1;

            switch(varargs && isLast && t.isArray()) {
                case true -> result.add(t.componentType().displayName() + "... " + name);
                case false -> result.add(t.displayName() + " " + name);
            }
        }

        return String.join(", ", result);
    }
}