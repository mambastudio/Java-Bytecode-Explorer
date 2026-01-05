/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import com.mamba.bytecodeexplorer.classanalysis.FieldInfo;
import com.mamba.bytecodeexplorer.classanalysis.MethodModelInfo;
import java.io.IOException;
import static java.lang.IO.println;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.lang.classfile.instruction.ArrayStoreInstruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.ConstantInstruction.ArgumentConstantInstruction;
import java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction;
import java.lang.classfile.instruction.ConstantInstruction.LoadConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author user
 */
public class TestClassFile {
    
    
    void main() throws IOException{
        ClassModel cm = ClassFile.of().parse(Path.of("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx\\target\\classes\\com\\mamba\\mambaui", "MambauiTheme.class"));
       
        List<MethodModel> methods = cm.methods();
        
        for(MethodModel mm : methods){            
            IO.println(new MethodModelInfo(mm).methodSource());
            IO.println(this.methodInstructions(mm).indent(5));
        }
    }
        
    public MethodModel defaultConstructor(ClassModel cm){
        for(MethodModel mm : cm.methods())
            if(mm.methodName().equalsString("<init>"))
                return mm;
        throw new UnsupportedOperationException(String.format("no default constructor in %s", cm.thisClass().name()));
    }
    
    public void printAllMethods(ClassModel cm){
        for (MethodModel mm : cm.methods())
            println(mm.methodName());
    }
    
    public void printMethod(MethodModel mm){
        if(mm.code().get() instanceof CodeModel cModel){
            for(CodeElement cElement : cModel){
                println(cElement.getClass().getSimpleName()+ " -> " +cElement);
            }
        }
    }
    
    public void printInstructionMethod(MethodModel mm){
        if(mm.code().get() instanceof CodeModel cModel){
            for(CodeElement cElement : cModel){
                if(cElement instanceof Instruction inst){
                    String opcodeStr = inst.opcode().toString().toLowerCase();
                    switch(inst){
                        case InvokeInstruction is -> println(formatOpcodeLine(opcodeStr, friendly(is)));
                        case LoadInstruction li -> println(formatOpcodeLine(opcodeStr, "slot " +li.slot()));
                        case ConstantInstruction ci -> {
                            switch(ci){ //exhaustive
                                case LoadConstantInstruction lci -> println(formatOpcodeLine(opcodeStr, lci.constantValue().toString()));   
                                case IntrinsicConstantInstruction _ -> println(opcodeStr);
                                case ArgumentConstantInstruction acc -> println(formatOpcodeLine(opcodeStr, acc.constantValue().toString()));
                            }
                        }  
                        case NewReferenceArrayInstruction nra -> println(formatOpcodeLine(opcodeStr, friendly(nra)));
                        case StackInstruction _ -> println(opcodeStr);
                        case FieldInstruction fi -> println(formatOpcodeLine(opcodeStr, friendly(fi)));
                        case ArrayStoreInstruction _-> println(opcodeStr);
                        case ArrayLoadInstruction _-> println(opcodeStr);
                        case ReturnInstruction _ -> println(opcodeStr);
                        default -> println(inst);
                    }
                }
            }
        }
    }
    
    public String methodInstructions(MethodModel mm){
        StringBuilder builder = new StringBuilder();
        
        if(mm.code().get() instanceof CodeModel cModel){
            for(CodeElement cElement : cModel){
                if(cElement instanceof Instruction inst){
                    String opcodeStr = inst.opcode().toString().toLowerCase();
                    switch(inst){
                        case InvokeInstruction is -> builder.append(friendly(is)).append("\n");
                        case LoadInstruction li -> builder.append(formatOpcodeLine(opcodeStr, "slot " +li.slot())).append("\n");
                        case StoreInstruction si -> builder.append(formatOpcodeLine(opcodeStr, "slot " + si.slot())).append("\n");
                        case IncrementInstruction ii -> builder.append(formatOpcodeLine(opcodeStr, "slot " + ii.slot())).append("\n");
                        case ConstantInstruction ci -> {
                            switch(ci){ //exhaustive
                                case LoadConstantInstruction lci -> builder.append(formatOpcodeLine(opcodeStr, lci.constantValue().toString())).append("\n");   
                                case IntrinsicConstantInstruction ici -> builder.append(formatOpcodeLine(opcodeStr, ici.constantValue().toString())).append("\n");
                                case ArgumentConstantInstruction acc -> builder.append(formatOpcodeLine(opcodeStr, acc.constantValue().toString())).append("\n");
                            }
                        }  
                        case NewReferenceArrayInstruction nra -> builder.append(formatOpcodeLine(opcodeStr, friendly(nra))).append("\n");
                        case StackInstruction _ -> builder.append(opcodeStr).append("\n");
                        case FieldInstruction fi -> builder.append(formatOpcodeLine(opcodeStr, friendly(fi))).append("\n");
                        case ArrayStoreInstruction asi-> builder.append(formatOpcodeLine(opcodeStr, asi.typeKind().name().toLowerCase())).append("\n");
                        case ArrayLoadInstruction ali-> builder.append(formatOpcodeLine(opcodeStr, ali.typeKind().name().toLowerCase())).append("\n");
                        case ReturnInstruction ri -> builder.append(formatOpcodeLine(opcodeStr, ri.typeKind().name().toLowerCase())).append("\n");
                        case BranchInstruction b -> builder.append(formatOpcodeLine(opcodeStr, b.target().toString())).append("\n"); //Need proper display in future
                        case OperatorInstruction oi -> builder.append(formatOpcodeLine(opcodeStr, oi.typeKind().name().toLowerCase())).append("\n");

                        default -> builder.append(inst).append(inst.getClass().getName()).append("\n");
                    }
                }
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
    
    private String formatOpcodeLine(String opcode, String info) {
        return String.format("%-15s -> %s", opcode, info);
    } 

    private String friendly(InvokeInstruction ii) {
        String opcode = ii.opcode().name().toLowerCase();
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
                "%-15s -> %s.%s(%s) : %s",
                opcode, owner, name, params, ret);
    }  
    
    private String friendly(NewReferenceArrayInstruction nra) {
        String component =
            nra.componentType()
               .name()
               .stringValue()
               .replace('/', '.');

        return String.format("%s[]", component);
    }
}
