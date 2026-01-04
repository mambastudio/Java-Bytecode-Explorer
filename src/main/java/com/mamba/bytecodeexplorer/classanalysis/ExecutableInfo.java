/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import static com.mamba.bytecodeexplorer.classanalysis.UtilBytecode.toBinaryClassName;
import static com.mamba.bytecodeexplorer.classanalysis.UtilBytecode.toSimpleName;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author joemw
 */
public sealed interface ExecutableInfo {    
    public interface HasParameters{
        List<ExecutableInfo.ParameterInfo> parameters();
    }
    
    public record ConstructorInfo(String className, MethodTypeDesc descriptor, AccessFlags flags, List<ParameterInfo> parameters, boolean isVarArgs) implements ExecutableInfo, HasParameters{}    
    public record StaticInitializerInfo(String name, MethodTypeDesc descriptor, AccessFlags flags) implements ExecutableInfo {}
    public record MethodInfo(String name, MethodTypeDesc descriptor, AccessFlags flags, List<ParameterInfo> parameters, boolean isVarArgs) implements ExecutableInfo, HasParameters{
        public MethodInfo {
            if ("<init>".equals(name) || "<clinit>".equals(name)) {
                throw new IllegalArgumentException(
                    name+ " must be modeled as ConstructorInfo"
                );
            }
        }
    }            

    public record ParameterInfo(ClassDesc type, String name, boolean varargs){}
    
    static List<ExecutableInfo> of(ClassModel cm) {
        var ownerSimpleName =  simpleClassName(cm.thisClass().name().toString(), true);
        var result = new ArrayList<ExecutableInfo>();

        for (MethodModel mm : cm.methods()) {
            var params = parameters(mm);
            var varArgs = mm.flags().has(AccessFlag.VARARGS);
            var descriptor = mm.methodTypeSymbol();
            var flags = mm.flags();

            if ("<init>".equals(mm.methodName().stringValue())) {
                result.add(new ConstructorInfo(
                        ownerSimpleName,
                        descriptor,
                        flags,
                        params,
                        varArgs
                ));
            } 
            else if("<clinit>".equals((mm.methodName()).stringValue())){
                result.add(new StaticInitializerInfo(
                        "static initializer", 
                        descriptor, 
                        flags));
            }
            else {
                result.add(new MethodInfo(
                        mm.methodName().stringValue(),
                        descriptor,
                        flags,
                        params,
                        varArgs
                ));
            }
        }

        return List.copyOf(result);
    }
    
    public static List<ParameterInfo> parameters(MethodModel mm) {
        var mt = mm.methodTypeSymbol();
        var isVarArgsMethod = mm.flags().has(AccessFlag.VARARGS);
        
        var paramTypes = mt.parameterList();

        // MethodParameters attribute (optional)
        var paramNames = mm.findAttribute(Attributes.methodParameters())
                .map(attr -> attr.parameters().stream()
                        .map(p -> p.name()
                                .map(Utf8Entry::stringValue)
                                .orElse(""))
                        .toList())
                .orElse(List.of());

        var result = new ArrayList<ParameterInfo>(paramTypes.size());

        for (int i = 0; i < paramTypes.size(); i++) {
            var type = paramTypes.get(i);
            var name =
                    (i < paramNames.size() && !paramNames.get(i).isBlank())
                            ? paramNames.get(i)
                            : "arg" + i;

            var isLast = (i == paramTypes.size() - 1);
            var isVarArgParam = isVarArgsMethod && isLast && type.isArray();

            var effectiveType =
                    isVarArgParam ? type.componentType() : type;

            result.add(new ParameterInfo(
                    effectiveType,
                    name,
                    isVarArgParam
            ));
        }

        return result;
    }
    
    static String simpleClassName(String binaryName, boolean withDollar) {
        int slash = binaryName.lastIndexOf('/');
        String simple = (slash >= 0)
                ? binaryName.substring(slash + 1)
                : binaryName;

        int dollar = simple.lastIndexOf('$');
        return (dollar >= 0 && withDollar)
                ? simple.substring(dollar + 1)
                : simple;
    }
    
    default String formatExecutable() {
        return switch (this) {
            case MethodInfo m -> m.name() + "(" + formatParamTypes() + ")";
            case ConstructorInfo c -> c.className() + "(" + formatParamTypes() + ")";
            case StaticInitializerInfo _ -> "static initializer";
        };
    }

    default String formatParamTypes() {
        if (!(this instanceof HasParameters hp))
            return "";

        return hp.parameters().stream()
            .map(p -> {
                String name = p.type().displayName();
                name = toBinaryClassName(name);
                name = toSimpleName(name);
                return p.varargs() ? name + "..." : name;
            })
            .collect(Collectors.joining(", "));
    }


}
