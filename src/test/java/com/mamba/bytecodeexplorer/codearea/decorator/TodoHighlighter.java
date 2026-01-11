/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.codearea.decorator;

import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

/**
 *
 * @author joemw
 */
public class TodoHighlighter implements SyntaxDecorator {

    private static final StyleAttributeMap RED =
        StyleAttributeMap.builder().setTextColor(Color.RED).build();

    private static final StyleAttributeMap BLACK =
        StyleAttributeMap.builder().setTextColor(Color.BLACK).build();
    
    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int line) {
        String text = model.getPlainText(line);
        RichParagraph.Builder b = RichParagraph.builder();

        int i = 0;
        while (i < text.length()) {
            int idx = text.indexOf("TODO", i);

            if (idx < 0) {
                b.addSegment(text, i, text.length(), BLACK);
                break;
            }

            if (idx > i) {
                b.addSegment(text, i, idx, BLACK);
            }

            b.addSegment(text, idx, idx + 4, RED);
            i = idx + 4;
        }
        return b.build();
    }

    @Override
    public void handleChange(
            CodeTextModel model, 
            TextPos start, 
            TextPos end, 
            int charsTop, 
            int linesAdded, 
            int charsBottom) {
        // force refresh by invalidating decorator
        
    }
    
}
