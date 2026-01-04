/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import com.mamba.bytecodeexplorer.file.type.RealFile;
import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.lang.classfile.instruction.ArrayStoreInstruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.instruction.ThrowInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author joemw
 */
public class ClassAnalysis {
    private final ClassModel cm;
        
    public ClassAnalysis(Path path) throws IOException{
        cm = ClassFile.of().parse(path);
    }
    
    public final ClassModel classModel(){
        return cm;
    }
    
    public final String name(){
        return cm.thisClass().asInternalName();
    }
    
    public List<String> getMethodSources(){
        var methods = cm.methods();
        var methodSources = new ArrayList<String>();
        for(MethodModel mm : methods){   
            methodSources.add(new MethodModelInfo(mm).methodSource());
        }
        
        return methodSources;
    }
    
    String displayName(MethodModel m) {
        if (!isConstructor(m)) {
            return m.methodName().stringValue();
        }

        var className = cm.thisClass()
                                 .name()
                                 .stringValue(); // simple or fq, your choice

        var params = new MethodModelInfo(m).parameters();

        return className + "(" + params + ")";
    }
    
    private boolean isConstructor(MethodModel m) {
        return m.methodName().stringValue().equals("<init>");
    }

    public List<String> getMethodNames(){
        var methods = cm.methods();
        var methodNames = new ArrayList<String>();
        for(MethodModel mm : methods){   
            methodNames.add(displayName(mm));
        }
        
        return methodNames;
    }
    
    public String bytecodeString(){
        var methods = cm.methods();
        var builder = new StringBuilder();
        
        for(MethodModel mm : methods){            
            builder.append(new MethodModelInfo(mm).methodSource()).append("\n\n");
            builder.append(methodInstructions(mm)).append("\n");
        }
       
        return builder.toString().trim();
    }
    
    public String methodInstructions(MethodModel mm){
        StringBuilder builder = new StringBuilder(); 
        
        Optional<CodeAttribute> codeAttrOpt = mm.findAttribute(Attributes.code());

        if (codeAttrOpt.isEmpty())
            return "";

        CodeAttribute code = codeAttrOpt.get();
        CodeModel cModel = code; // safe, CodeAttribute extends CodeModel
                
        int offset = 0;
        
        for(CodeElement cElement : cModel){
            if(cElement instanceof Instruction inst){
                String opcodeStr = inst.opcode().toString().toLowerCase();
                switch(inst){
                    case InvokeInstruction is -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(is))).append("\n");
                    case LoadInstruction li -> builder.append(formatOpcodeLine(offset, opcodeStr, "slot " +li.slot())).append("\n");
                    case StoreInstruction si -> builder.append(formatOpcodeLine(offset, opcodeStr, "slot " + si.slot())).append("\n");
                    case IncrementInstruction ii -> builder.append(formatOpcodeLine(offset, opcodeStr, "slot " + ii.slot())).append("\n");
                    case ConstantInstruction ci -> {
                        switch(ci){ //exhaustive
                            case ConstantInstruction.LoadConstantInstruction lci -> builder.append(formatOpcodeLine(offset, opcodeStr, lci.constantValue().toString())).append("\n");   
                            case ConstantInstruction.IntrinsicConstantInstruction ici -> builder.append(formatOpcodeLine(offset, opcodeStr, ici.constantValue().toString())).append("\n");
                            case ConstantInstruction.ArgumentConstantInstruction acc -> builder.append(formatOpcodeLine(offset, opcodeStr, acc.constantValue().toString())).append("\n");
                        }
                    }  
                    case TypeCheckInstruction tci -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(tci))).append("\n");
                    case InvokeDynamicInstruction idi -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(idi))).append("\n");
                    case ThrowInstruction _ -> builder.append(formatOpcodeLine(offset, opcodeStr, "athrow")).append("\n");
                    case NewReferenceArrayInstruction nra -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(nra))).append("\n");
                    case StackInstruction _ -> builder.append(formatOpcodeLine(offset, opcodeStr, "")).append("\n");
                    case TableSwitchInstruction tsi -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(code, tsi))).append("\n");
                    case NewObjectInstruction noi -> builder.append(formatOpcodeLine(offset, opcodeStr, noi.className().asInternalName())).append("\n");
                    case FieldInstruction fi -> builder.append(formatOpcodeLine(offset, opcodeStr, friendly(fi))).append("\n");
                    case ArrayStoreInstruction asi-> builder.append(formatOpcodeLine(offset, opcodeStr, asi.typeKind().name().toLowerCase())).append("\n");
                    case ArrayLoadInstruction ali-> builder.append(formatOpcodeLine(offset, opcodeStr, ali.typeKind().name().toLowerCase())).append("\n");
                    case ReturnInstruction ri -> builder.append(formatOpcodeLine(offset, opcodeStr, ri.typeKind().name().toLowerCase())).append("\n");
                    case BranchInstruction b -> builder.append(formatOpcodeLine(offset, opcodeStr, labelName(code, b.target()))).append("\n"); //Need proper display in future
                    case OperatorInstruction oi -> builder.append(formatOpcodeLine(offset, opcodeStr, oi.typeKind().name().toLowerCase())).append("\n");

                    default -> builder.append(inst).append(inst.getClass().getName()).append("\n");
                }
                
                offset += inst.sizeInBytes();
            }
        }
        
        return builder.toString();
    }
    
    private String friendly(FieldInstruction fis){        
        return switch(fis.opcode().toString().toLowerCase()){
            case "putstatic", "getstatic", "putfield", "getfield" -> new FieldInfo(fis).toString();  
            default -> fis.toString();
        };
    }
        
    private String formatOpcodeLine(int offset, String opcode, String info) {
        String prefix = String.format("%4d  %-15s -> ", offset, opcode);

        if (info == null || info.isEmpty())
            return String.format("%4d  %-15s", offset, opcode);

        String[] lines = info.split("\\R");

        if (lines.length == 1)
            return prefix + lines[0];

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(lines[0]).append('\n');

        int operandIndentWidth = prefix.indexOf("->") + 6;
        String indent = " ".repeat(operandIndentWidth);
        for (int i = 1; i < lines.length; i++) {
            sb.append(indent).append(lines[i]).append('\n');
        }

        sb.setLength(sb.length() - 1); // trim last newline
        return sb.toString();
    }

    
    private String friendly(NewReferenceArrayInstruction nra) {
        String component =
            nra.componentType()
               .name()
               .stringValue()
               .replace('/', '.');

        return String.format("%s[]", component);
    }

    private String friendly(InvokeInstruction ii) {        
        var ref = ii.method(); 
        
        String owner =
            ref.owner().name().stringValue().replace('/', '.');

        String name =
            ref.name().stringValue();

        MethodTypeDesc mtd =
            MethodTypeDesc.ofDescriptor(ref.type().stringValue());

        String params = mtd.parameterList().stream()
            .map(ClassDesc::displayName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        String ret = mtd.returnType().displayName();

        return String.format(
                "%s.%s(%s) : %s",
                owner, name, params, ret);

    }    
    
    private String friendly(CodeAttribute code, TableSwitchInstruction ts) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("[%d..%d]%n",
            ts.lowValue(), ts.highValue()
        ));

        for (SwitchCase sc : ts.cases()) {
            sb.append(String.format(
                "%d       : goto %s%n",
                sc.caseValue(),
                labelName(code, sc.target())
            ));
        }

        sb.append(String.format(
            "default : goto %s",
            labelName(code, ts.defaultTarget())
        ));

        return sb.toString();
    }
    
    private String labelName(CodeAttribute code, Label label) {
        int bci = code.labelToBci(label);
        return bci >= 0 ? "L" + bci : "<unbound>";
    }  
    
    private String friendly(InvokeDynamicInstruction ii) {        
        String name = ii.name().stringValue();

        MethodTypeDesc mtd = ii.typeSymbol();

        String params = mtd.parameterList().stream()
                .map(ClassDesc::displayName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String ret = mtd.returnType().displayName();

        return String.format(
            "%s(%s) : %s",
           name, params, ret
        );
    }
    
    private String friendly(TypeCheckInstruction tci) {        
        // Convert ClassEntry → ClassDesc → display name
        String type = tci.type()
                .asSymbol()
                .displayName();

        return String.format(
            "%s",
            type
        );
    }
}
